package info.openrocket.core.file.threemf.export;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.openrocket.OpenRocketSaverTest;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeMFExporterFactoryTest {
    private static final String MODEL_NAMESPACE =
            "http://schemas.microsoft.com/3dmanufacturing/core/2015/02";

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Module dbOverrides = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ComponentPresetDao.class).toProvider(new OpenRocketSaverTest.EmptyComponentDbProvider());
                bind(MotorDatabase.class).toProvider(new OpenRocketSaverTest.MotorDbProvider());
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }
        };
        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(dbOverrides), pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void exportsNoseAndThreeFinInstancesAsFourNamedObjects(@TempDir Path directory) throws Exception {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage stage = rocket.getStage(0);
        NoseCone nose = new NoseCone();
        nose.setName("Printable nose");
        nose.setLength(0.08);
        nose.setBaseRadius(0.025);
        nose.setThickness(0.002);
        stage.addChild(nose);

        BodyTube body = new BodyTube();
        body.setLength(0.2);
        body.setOuterRadius(0.025);
        body.setThickness(0.002);
        stage.addChild(body);
        TrapezoidFinSet fins = new TrapezoidFinSet();
        fins.setName("Printable fin");
        fins.setFinCount(3);
        fins.setRootChord(0.06);
        fins.setTipChord(0.03);
        fins.setHeight(0.04);
        fins.setThickness(0.003);
        body.addChild(fins);

        Path output = directory.resolve("parts.3mf");
        WarningSet warnings = new WarningSet();
        ThreeMFExportOptions options = new ThreeMFExportOptions();
        options.setExportChildren(false);
        ThreeMFExporterFactory exporter = new ThreeMFExporterFactory(
                List.of(nose, fins), rocket.getSelectedConfiguration(), output.toFile(), options, warnings);

        assertTrue(exporter.doExport());
        assertTrue(Files.isRegularFile(output));
        assertEquals(0, warnings.size());

        try (ZipFile zip = new ZipFile(output.toFile())) {
            assertNotNull(zip.getEntry("[Content_Types].xml"));
            assertNotNull(zip.getEntry("_rels/.rels"));
            assertNotNull(zip.getEntry("3D/3dmodel.model"));
            Document model = parse(zip, "3D/3dmodel.model");
            assertEquals("millimeter", model.getDocumentElement().getAttribute("unit"));
            NodeList objects = model.getElementsByTagNameNS(MODEL_NAMESPACE, "object");
            NodeList items = model.getElementsByTagNameNS(MODEL_NAMESPACE, "item");
            assertEquals(4, objects.getLength());
            assertEquals(4, items.getLength());
            Set<String> names = IntStream.range(0, objects.getLength())
                    .mapToObj(index -> ((Element) objects.item(index)).getAttribute("name"))
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(Set.of("Printable nose", "Printable fin (1/3)",
                    "Printable fin (2/3)", "Printable fin (3/3)"), names);
            double[] finSize = getObjectSize(model, "Printable fin (1/3)");
            assertTrue(finSize[2] < finSize[0] && finSize[2] < finSize[1]);
            assertAllVerticesOnOrAboveBuildPlate(model);
            assertAllTriangleIndicesValid(model);
        }
    }

    @Test
    void zeroThicknessPartsDoNotOverwriteExistingFile(@TempDir Path directory) throws Exception {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        BodyTube body = new BodyTube();
        body.setOuterRadius(0.02);
        body.setThickness(0);
        body.setLength(0.1);
        rocket.getStage(0).addChild(body);
        Path output = directory.resolve("existing.3mf");
        byte[] original = "keep me".getBytes(StandardCharsets.UTF_8);
        Files.write(output, original);

        WarningSet warnings = new WarningSet();
        ThreeMFExporterFactory exporter = new ThreeMFExporterFactory(
                List.of(body), rocket.getSelectedConfiguration(), output.toFile(),
                new ThreeMFExportOptions(), warnings);

        assertFalse(exporter.doExport());
        assertArrayEquals(original, Files.readAllBytes(output));
        assertTrue(warnings.size() >= 1);
    }

    @Test
    void exportsOfficialThreeDPrintableExample() throws Exception {
        Path example = Path.of("src/main/resources/datafiles/examples/3D printable nose cone and fins.ork");
        Rocket rocket = new GeneralRocketLoader(example.toFile()).load().getRocket();
        Path output = Path.of("build/3mf-integration/official-example.3mf");
        Files.createDirectories(output.getParent());
        WarningSet warnings = new WarningSet();
        ThreeMFExporterFactory exporter = new ThreeMFExporterFactory(
                List.of(rocket), rocket.getSelectedConfiguration(), output.toFile(),
                new ThreeMFExportOptions(), warnings);

        assertTrue(exporter.doExport());
        assertTrue(Files.size(output) > 0);
        try (ZipFile zip = new ZipFile(output.toFile())) {
            Document model = parse(zip, "3D/3dmodel.model");
            assertTrue(model.getElementsByTagNameNS(MODEL_NAMESPACE, "object").getLength() >= 4);
        }
    }

    @Test
    void orientsCommonPartTypesByComponentSemantics(@TempDir Path directory) throws Exception {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage stage = rocket.getStage(0);
        NoseCone nose = new NoseCone();
        nose.setName("orientation nose");
        nose.setLength(0.08);
        nose.setBaseRadius(0.02);
        nose.setThickness(0.002);
        stage.addChild(nose);

        BodyTube tube = new BodyTube();
        tube.setName("orientation tube");
        tube.setLength(0.12);
        tube.setOuterRadius(0.02);
        tube.setThickness(0.002);
        stage.addChild(tube);

        CenteringRing ring = new CenteringRing();
        ring.setName("orientation ring");
        ring.setLength(0.004);
        ring.setOuterRadius(0.018);
        ring.setInnerRadiusAutomatic(false);
        ring.setInnerRadius(0.01);
        tube.addChild(ring);

        LaunchLug lug = new LaunchLug();
        lug.setName("orientation lug");
        lug.setLength(0.05);
        lug.setOuterRadius(0.004);
        lug.setThickness(0.001);
        tube.addChild(lug);

        RailButton button = new RailButton();
        button.setName("orientation rail button");
        button.setOuterDiameter(0.014);
        button.setInnerDiameter(0.010);
        button.setTotalHeight(0.006);
        tube.addChild(button);

        Path output = directory.resolve("orientations.3mf");
        WarningSet warnings = new WarningSet();
        ThreeMFExporterFactory exporter = new ThreeMFExporterFactory(
                List.of(nose, tube), rocket.getSelectedConfiguration(), output.toFile(),
                new ThreeMFExportOptions(), warnings);
        assertTrue(exporter.doExport());

        try (ZipFile zip = new ZipFile(output.toFile())) {
            Document model = parse(zip, "3D/3dmodel.model");
            double[] noseSize = getObjectSize(model, "orientation nose");
            double[] tubeSize = getObjectSize(model, "orientation tube");
            double[] ringSize = getObjectSize(model, "orientation ring");
            double[] lugSize = getObjectSize(model, "orientation lug");
            double[] buttonSize = getObjectSize(model, "orientation rail button");
            assertTrue(noseSize[2] > noseSize[0] && noseSize[2] > noseSize[1]);
            assertTrue(tubeSize[2] > tubeSize[0] && tubeSize[2] > tubeSize[1]);
            assertTrue(ringSize[2] < ringSize[0] && ringSize[2] < ringSize[1]);
            assertTrue(lugSize[2] > lugSize[0] && lugSize[2] > lugSize[1]);
            assertTrue(buttonSize[2] < Math.max(buttonSize[0], buttonSize[1]));
        }
    }

    @Test
    void deduplicatesParentChildSelectionsAndExcludesInactiveStages(@TempDir Path directory) throws Exception {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage activeStage = rocket.getStage(0);
        BodyTube activeTube = new BodyTube();
        activeTube.setName("active tube");
        activeTube.setLength(0.1);
        activeTube.setOuterRadius(0.02);
        activeTube.setThickness(0.002);
        activeStage.addChild(activeTube);
        LaunchLug lugs = new LaunchLug();
        lugs.setName("paired lug");
        lugs.setLength(0.03);
        lugs.setOuterRadius(0.004);
        lugs.setThickness(0.001);
        lugs.setInstanceCount(2);
        activeTube.addChild(lugs);

        AxialStage inactiveStage = new AxialStage();
        rocket.addChild(inactiveStage);
        BodyTube inactiveTube = new BodyTube();
        inactiveTube.setName("inactive tube");
        inactiveTube.setLength(0.1);
        inactiveTube.setOuterRadius(0.02);
        inactiveTube.setThickness(0.002);
        inactiveStage.addChild(inactiveTube);
        rocket.getSelectedConfiguration()._setStageActive(inactiveStage.getStageNumber(), false);

        Path output = directory.resolve("selection.3mf");
        ThreeMFExporterFactory exporter = new ThreeMFExporterFactory(
                List.of(rocket, activeTube, lugs), rocket.getSelectedConfiguration(), output.toFile(),
                new ThreeMFExportOptions(), new WarningSet());
        assertTrue(exporter.doExport());

        try (ZipFile zip = new ZipFile(output.toFile())) {
            Document model = parse(zip, "3D/3dmodel.model");
            NodeList objects = model.getElementsByTagNameNS(MODEL_NAMESPACE, "object");
            assertEquals(3, objects.getLength());
            Set<String> names = IntStream.range(0, objects.getLength())
                    .mapToObj(index -> ((Element) objects.item(index)).getAttribute("name"))
                    .collect(java.util.stream.Collectors.toSet());
            assertFalse(names.contains("inactive tube"));
            assertEquals(Set.of("active tube", "paired lug (1/2)", "paired lug (2/2)"), names);
        }
    }

    private static Document parse(ZipFile zip, String entryName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try (InputStream input = zip.getInputStream(zip.getEntry(entryName))) {
            return factory.newDocumentBuilder().parse(input);
        }
    }

    private static void assertAllVerticesOnOrAboveBuildPlate(Document model) {
        NodeList vertices = model.getElementsByTagNameNS(MODEL_NAMESPACE, "vertex");
        for (int index = 0; index < vertices.getLength(); index++) {
            Element vertex = (Element) vertices.item(index);
            assertTrue(Double.parseDouble(vertex.getAttribute("z")) >= -1.0e-6);
        }
        NodeList meshes = model.getElementsByTagNameNS(MODEL_NAMESPACE, "mesh");
        for (int index = 0; index < meshes.getLength(); index++) {
            Element mesh = (Element) meshes.item(index);
            NodeList meshVertices = mesh.getElementsByTagNameNS(MODEL_NAMESPACE, "vertex");
            double minZ = Double.POSITIVE_INFINITY;
            for (int vertexIndex = 0; vertexIndex < meshVertices.getLength(); vertexIndex++) {
                minZ = Math.min(minZ, Double.parseDouble(
                        ((Element) meshVertices.item(vertexIndex)).getAttribute("z")));
            }
            assertEquals(0, minZ, 1.0e-6);
        }
    }

    private static void assertAllTriangleIndicesValid(Document model) {
        NodeList meshes = model.getElementsByTagNameNS(MODEL_NAMESPACE, "mesh");
        for (int index = 0; index < meshes.getLength(); index++) {
            Element mesh = (Element) meshes.item(index);
            int vertexCount = mesh.getElementsByTagNameNS(MODEL_NAMESPACE, "vertex").getLength();
            NodeList triangles = mesh.getElementsByTagNameNS(MODEL_NAMESPACE, "triangle");
            for (int triangleIndex = 0; triangleIndex < triangles.getLength(); triangleIndex++) {
                Element triangle = (Element) triangles.item(triangleIndex);
                assertIndex(triangle.getAttribute("v1"), vertexCount);
                assertIndex(triangle.getAttribute("v2"), vertexCount);
                assertIndex(triangle.getAttribute("v3"), vertexCount);
            }
        }
    }

    private static void assertIndex(String value, int vertexCount) {
        int index = Integer.parseInt(value);
        assertTrue(index >= 0 && index < vertexCount);
    }

    private static double[] getObjectSize(Document model, String name) {
        NodeList objects = model.getElementsByTagNameNS(MODEL_NAMESPACE, "object");
        for (int index = 0; index < objects.getLength(); index++) {
            Element object = (Element) objects.item(index);
            if (!name.equals(object.getAttribute("name"))) {
                continue;
            }
            double[] min = new double[] {
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
            };
            double[] max = new double[] {
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY
            };
            NodeList vertices = object.getElementsByTagNameNS(MODEL_NAMESPACE, "vertex");
            for (int vertexIndex = 0; vertexIndex < vertices.getLength(); vertexIndex++) {
                Element vertex = (Element) vertices.item(vertexIndex);
                double[] coordinate = new double[] {
                        Double.parseDouble(vertex.getAttribute("x")),
                        Double.parseDouble(vertex.getAttribute("y")),
                        Double.parseDouble(vertex.getAttribute("z"))
                };
                for (int axis = 0; axis < 3; axis++) {
                    min[axis] = Math.min(min[axis], coordinate[axis]);
                    max[axis] = Math.max(max[axis], coordinate[axis]);
                }
            }
            return new double[] {max[0] - min[0], max[1] - min[1], max[2] - min[2]};
        }
        throw new AssertionError("Missing 3MF object " + name);
    }
}
