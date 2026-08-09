package info.openrocket.core.file.rasaero.export;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.rasaero.importt.RASAeroLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.database.Databases;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RASAeroSaverTest {
    // TODO: export a complex design
    // TODO: check recovery
    // TODO: check sims (including weights and CG)

    @BeforeAll
    public static void setup() {
        Module applicationModule = new ServicesForTesting();

        // Pre-initialize Databases.trans with a real translator before DebugTranslator takes over.
        // Uses applicationModule with RocketDescriptor overridden to a no-op stub so that
        // Set<RocketSubstitutor> (from PluginModule) is not required — avoiding Multibinder conflicts.
        Module noOpDescriptor = new AbstractModule() {
            @Override
            protected void configure() {
                bind(RocketDescriptor.class).toInstance(new RocketDescriptor() {
                    public String format(Rocket rocket, FlightConfigurationId fcid) { return ""; }
                    public String format(String name, Rocket rocket, FlightConfigurationId fcid) { return ""; }
                });
            }
        };
        Application.setInjector(Guice.createInjector(Modules.override(applicationModule).with(noOpDescriptor)));
        Databases.fakeMethod();

        Module pluginModule = new PluginModule();

        Module debugTranslator = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }
        };

        Module dbOverrides = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ComponentPresetDao.class).toProvider(new EmptyComponentDbProvider());
                bind(MotorDatabase.class).toProvider(new MotorDbProvider());
            }
        };

        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator),
                pluginModule, dbOverrides);
        Application.setInjector(injector);
    }

    @Test
    public void testSingleStage() {
        OpenRocketDocument originalDocument = loadRocket("01.One-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(3, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("01.One-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 01.One-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> originalChildren = originalDocument.getRocket().getAllChildren();
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(originalChildren.size(), importedChildren.size(), " Number of total children doesn't match");

            // TODO: check all components
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTwoStage() {
        OpenRocketDocument originalDocument = loadRocket("02.Two-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(2, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("02.Two-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 02.Two-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(18, importedChildren.size(), " Number of total children doesn't match");
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testThreeStage() {
        OpenRocketDocument originalDocument = loadRocket("03.Three-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(2, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("03.Three-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 03.Three-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(21, importedChildren.size(), " Number of total children doesn't match");
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Regression test: the RASAero exporter must preserve the original document order of the
     * external components instead of grouping them by type. A design whose components interleave
     * by type (NoseCone → BodyTube → Transition → BodyTube) must serialize in that exact order,
     * otherwise the importer -- which constructs components sequentially -- would rebuild a
     * different geometry.
     */
    @Test
    public void testComponentOrderPreserved() {
        OpenRocketDocument originalDocument = OpenRocketDocumentFactory.createEmptyRocket();
        Rocket rocket = originalDocument.getRocket();
        AxialStage stage = new AxialStage();
        rocket.addChild(stage);

        NoseCone nose = new NoseCone();
        nose.setShapeType(Transition.Shape.OGIVE);
        nose.setLength(0.1);
        nose.setBaseRadius(0.025);
        stage.addChild(nose);

        BodyTube tube1 = new BodyTube();
        tube1.setOuterRadius(0.025);
        tube1.setLength(0.2);
        stage.addChild(tube1);

        Transition transition = new Transition();
        transition.setShapeType(Transition.Shape.CONICAL);
        transition.setForeRadius(0.025);
        transition.setAftRadius(0.02);
        transition.setLength(0.05);
        stage.addChild(transition);

        BodyTube tube2 = new BodyTube();
        tube2.setOuterRadius(0.02);
        tube2.setLength(0.15);
        stage.addChild(tube2);

        try {
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);
            assertEquals(0, errors.size(), " unexpected RASAero export errors: " + errors);

            // The Transition element must be serialized between the two BodyTube elements, not after
            // both of them (which is what type-grouped serialization would produce).
            int noseIdx = result.indexOf("<NoseCone>");
            int firstTube = result.indexOf("<BodyTube>");
            int transitionIdx = result.indexOf("<Transition>");
            int lastTube = result.lastIndexOf("<BodyTube>");
            assertTrue(noseIdx >= 0 && firstTube >= 0 && transitionIdx >= 0 && lastTube > firstTube,
                    " expected a NoseCone, two BodyTubes and a Transition in the output");
            assertTrue(noseIdx < firstTube, " NoseCone should be serialized first");
            assertTrue(firstTube < transitionIdx, " first BodyTube should precede the Transition");
            assertTrue(transitionIdx < lastTube,
                    " Transition should precede the second BodyTube; component order was not preserved");

            // Round-trip: re-import and confirm the component order survived.
            Path output = Files.createTempFile("component-order", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);

            AxialStage importedStage = importedDocument.getRocket().getStage(0);
            assertEquals(4, importedStage.getChildCount(), " imported stage should have 4 components");
            assertTrue(importedStage.getChild(0) instanceof NoseCone, " child 0 should be a NoseCone");
            assertTrue(importedStage.getChild(1) instanceof BodyTube, " child 1 should be a BodyTube");
            assertTrue(importedStage.getChild(2) instanceof Transition
                    && !(importedStage.getChild(2) instanceof NoseCone), " child 2 should be a Transition");
            assertTrue(importedStage.getChild(3) instanceof BodyTube, " child 3 should be a BodyTube");
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OpenRocketDocument loadRocket(String fileName) {
        GeneralRocketLoader loader = new GeneralRocketLoader(new File(fileName));
        InputStream is = this.getClass().getResourceAsStream("/file/rasaero/export/" + fileName);
        String failMsg = String.format("Problem in unit test, cannot find %s", fileName);
        assertNotNull(is, failMsg);

        OpenRocketDocument rocketDoc = null;
        try {
            rocketDoc = loader.load(is, fileName);
        } catch (RocketLoadException e) {
            fail("RocketLoadException while loading file " + fileName + " : " + e.getMessage());
        }

        try {
            is.close();
        } catch (IOException e) {
            fail("Unable to close input stream for file " + fileName + ": " + e.getMessage());
        }

        return rocketDoc;
    }

    private static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {

        final ComponentPresetDao db = new ComponentPresetDatabase();

        @Override
        public ComponentPresetDao get() {
            return db;
        }
    }

    private static class MotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {

        final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();

        public MotorDbProvider() {
        }

        @Override
        public ThrustCurveMotorSetDatabase get() {
            return db;
        }
    }
}
