package info.openrocket.core.file.wavefrontobj.export;

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
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OBJExporterFactoryTest {
    private static final File TMP_DIR = new File("./tmp/");

    @BeforeAll
    public static void setup() {
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

        if (!(TMP_DIR.exists() && TMP_DIR.isDirectory())) {
            boolean success = TMP_DIR.mkdirs();
            if (!success) {
                fail("Unable to create core/tmp dir needed for tests.");
            }
        }
    }

    @Test
    public void testExport() throws IOException {
        // Rocket generation
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage sustainer = rocket.getStage(0);

        NoseCone noseCone = new NoseCone();
        noseCone.setBaseRadius(0.05);
        noseCone.setLength(0.1);
        noseCone.setShoulderLength(0.01);
        noseCone.setShoulderRadius(0.03);
        noseCone.setShoulderThickness(0.002);
        noseCone.setShoulderCapped(false);
        sustainer.addChild(noseCone);

        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.05);
        bodyTube.setThickness(0.005);
        bodyTube.setLength(0.3);
        sustainer.addChild(bodyTube);

        LaunchLug launchLug = new LaunchLug();
        launchLug.setLength(0.05);
        launchLug.setOuterRadius(0.02);
        launchLug.setThickness(0.005);
        launchLug.setInstanceCount(2);
        launchLug.setInstanceSeparation(0.1);
        bodyTube.addChild(launchLug);

        TrapezoidFinSet finSet = new TrapezoidFinSet();
        finSet.setRootChord(0.05);
        finSet.setThickness(0.005);
        finSet.setTabLength(0.03);
        finSet.setTabHeight(0.01);
        finSet.setTabOffset(-0.0075);
        finSet.setCantAngle(Math.toRadians(10));
        bodyTube.addChild(finSet);

        TubeFinSet tubeFinSet = new TubeFinSet();
        tubeFinSet.setFinCount(4);
        tubeFinSet.setOuterRadius(0.01);
        tubeFinSet.setLength(0.05);
        tubeFinSet.setBaseRotation(Math.PI / 8);
        tubeFinSet.setAxialOffset(-0.1);
        bodyTube.addChild(tubeFinSet);

        Transition transition = new Transition();
        transition.setLength(0.15);
        transition.setForeRadius(0.05);
        transition.setAftRadius(0.025);
        transition.setThickness(0.003);
        transition.setShapeType(Transition.Shape.PARABOLIC);
        transition.setShapeParameter(0.7);
        sustainer.addChild(transition);

        Parachute parachute = new Parachute();
        parachute.setRadiusAutomatic(false);
        parachute.setRadius(0.05);
        parachute.setLength(0.075);
        parachute.setRadialPosition(0.02);
        parachute.setRadialDirection(Math.PI / 3);
        bodyTube.addChild(parachute);

        RailButton railButton = new RailButton();
        railButton.setScrewHeight(0.0025);
        railButton.setAngleOffset(Math.toRadians(67));
        bodyTube.addChild(railButton);

        CenteringRing centeringRing = new CenteringRing();
        centeringRing.setOuterRadius(0.04);
        centeringRing.setInnerRadiusAutomatic(false);
        centeringRing.setInnerRadius(0.03);
        centeringRing.setLength(0.1);
        bodyTube.addChild(centeringRing);

        InnerTube innerTube = new InnerTube();
        bodyTube.addChild(innerTube);


        // ------------------------------


        // Create a list of components to export
        List<RocketComponent> components = List.of(rocket);

        // Create a temp file for storing the exported OBJ
        Path tempFile = Files.createTempFile("testExport", ".obj");

        // Do the exporting
        OBJExportOptions options = new OBJExportOptions(rocket);
        options.setScaling(30);
        options.setExportChildren(true);
        options.setRemoveOffset(true);
        WarningSet warnings = new WarningSet();
        OBJExporterFactory exporterFactory = new OBJExporterFactory(components, rocket.getSelectedConfiguration(), tempFile.toFile(), options, warnings);
        exporterFactory.doExport();
        //// Just hope for no exceptions :)
        assertEquals(warnings.size(), 0);


        // Test with other parameters
        noseCone.setShoulderCapped(false);
        railButton.setScrewHeight(0);
        bodyTube.setFilled(true);

        options.setTriangulate(true);
        options.setTriangulationMethod(ObjUtils.TriangulationMethod.DELAUNAY);
        options.setRemoveOffset(false);
        options.setExportAppearance(true);
        options.setScaling(1000);
        options.setLOD(ObjUtils.LevelOfDetail.LOW_QUALITY);

        exporterFactory = new OBJExporterFactory(components, rocket.getSelectedConfiguration(), tempFile.toFile(), options, warnings);
        exporterFactory.doExport();
        //// Just hope for no exceptions :)
        assertEquals(warnings.size(), 0);

        // Test zero-thickness nose cone
        noseCone.setThickness(0);

        exporterFactory = new OBJExporterFactory(components, rocket.getSelectedConfiguration(), tempFile.toFile(), options, warnings);
        exporterFactory.doExport();
        //// Just hope for no exceptions :)
        assertEquals(warnings.size(), 1);

        // Test simple triangulation
        options.setTriangulationMethod(ObjUtils.TriangulationMethod.SIMPLE);

        exporterFactory = new OBJExporterFactory(components, rocket.getSelectedConfiguration(), tempFile.toFile(), options, warnings);
        exporterFactory.doExport();
        //// Just hope for no exceptions :)
        assertEquals(warnings.size(), 1);

        // Clean up
        Files.delete(tempFile);
    }
}
