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
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.importt.RASAeroLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.TestRockets;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
     * A clustered motor mount must contribute the launch mass of every motor in the cluster, not just one.
     */
    @Test
    public void testClusteredMotorLaunchWeightAndCG() {
        Rocket rocket = TestRockets.makeEstesAlphaIII();
        AxialStage sustainer = (AxialStage) rocket.getChild(0);
        InnerTube mount = null;
        for (RocketComponent component : rocket) {
            if (component instanceof InnerTube) {
                mount = (InnerTube) component;
            }
        }
        mount.setClusterConfiguration(ClusterConfiguration.CONFIGURATIONS[5]);    // 4-ring
        assertEquals(4, mount.getMotorCount());

        OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
        Simulation simulation = new Simulation(document, rocket);
        simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
        Map<AxialStage, MotorMount> mounts = new HashMap<>();
        mounts.put(sustainer, mount);

        SimulationDTO dto = new SimulationDTO(rocket, simulation, mounts, new ArrayList<>(), new WarningSet(),
                new ErrorSet());

        // Expected: the structure plus four motors at their launch mass and CG
        RigidBody structure = MassCalculator.calculateStructure(new FlightConfiguration(rocket));
        ThrustCurveMotor motor = (ThrustCurveMotor) mount.getMotorConfig(TestRockets.TEST_FCID_0).getMotor();
        double motorMass = motor.getLaunchMass();
        double motorCG = mount.getComponentLocations()[0].getX()
                + mount.getMotorPosition(TestRockets.TEST_FCID_0).getX() + motor.getCGPoints()[0].getX();
        double expectedMass = structure.getMass() + 4 * motorMass;
        double expectedCG = (structure.getMass() * structure.getCM().getX() + 4 * motorMass * motorCG) / expectedMass;

        assertEquals(expectedMass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT, dto.getSustainerLaunchWt(),
                1e-6);
        assertEquals(expectedCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, dto.getSustainerCG(), 1e-6);
    }

    /**
     * RASAero aborts the simulation when an included booster has no motor, and flies an included booster even when
     * the OpenRocket flight configuration has that stage disabled. The booster must only be included when it is
     * active in the simulation's configuration and has a motor.
     */
    @Test
    public void testIncludeBoosterFollowsFlightConfiguration() {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        AxialStage booster = (AxialStage) rocket.getChild(1);
        InnerTube boosterMount = (InnerTube) SimulationListDTO.findMotorMount(booster);

        // Booster active with a motor
        FlightConfigurationId withBooster = rocket.getSelectedConfiguration().getId();
        assertTrue(exportSimulation(document, withBooster).getIncludeBooster1());

        // Booster disabled in the flight configuration
        FlightConfigurationId boosterDisabled = new FlightConfigurationId();
        rocket.createFlightConfiguration(boosterDisabled);
        configureMotor((InnerTube) SimulationListDTO.findMotorMount((AxialStage) rocket.getChild(0)), boosterDisabled,
                "H128W", 0);
        configureMotor(boosterMount, boosterDisabled, "I357T", 0);
        rocket.getFlightConfiguration(boosterDisabled).setAllStages();
        rocket.getFlightConfiguration(boosterDisabled)._setStageActive(booster.getStageNumber(), false);
        assertFalse(exportSimulation(document, boosterDisabled).getIncludeBooster1());

        // Booster without a motor in this flight configuration
        FlightConfigurationId noBoosterMotor = new FlightConfigurationId();
        rocket.createFlightConfiguration(noBoosterMotor);
        configureMotor((InnerTube) SimulationListDTO.findMotorMount((AxialStage) rocket.getChild(0)), noBoosterMotor,
                "H128W", 0);
        rocket.getFlightConfiguration(noBoosterMotor).setAllStages();
        assertFalse(exportSimulation(document, noBoosterMotor).getIncludeBooster1());
    }

    private SimulationDTO exportSimulation(OpenRocketDocument document, FlightConfigurationId configurationId) {
        Rocket rocket = document.getRocket();
        Simulation simulation = new Simulation(document, rocket);
        simulation.setFlightConfigurationId(configurationId);
        Map<AxialStage, MotorMount> mounts = new HashMap<>();
        for (RocketComponent child : rocket.getChildren()) {
            mounts.put((AxialStage) child, SimulationListDTO.findMotorMount((AxialStage) child));
        }
        return new SimulationDTO(rocket, simulation, mounts, new ArrayList<>(), new WarningSet(), new ErrorSet());
    }

    /**
     * A two-stage rocket whose first flight configuration has an H128W sustainer and an I357T booster.
     */
    private OpenRocketDocument makeTwoStageDocument() {
        OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
        Rocket rocket = document.getRocket();
        AxialStage sustainer = rocket.getStage(0);
        NoseCone noseCone = new NoseCone(Transition.Shape.OGIVE, 0.2, 0.025);
        sustainer.addChild(noseCone);
        BodyTube sustainerBody = new BodyTube(0.5, 0.025, 0.001);
        sustainer.addChild(sustainerBody);
        InnerTube sustainerMount = makeMotorMount();
        sustainerBody.addChild(sustainerMount);

        AxialStage booster = new AxialStage();
        rocket.addChild(booster);
        BodyTube boosterBody = new BodyTube(0.4, 0.025, 0.001);
        booster.addChild(boosterBody);
        InnerTube boosterMount = makeMotorMount();
        boosterBody.addChild(boosterMount);

        FlightConfigurationId configurationId = new FlightConfigurationId();
        rocket.createFlightConfiguration(configurationId);
        configureMotor(sustainerMount, configurationId, "H128W", 0);
        configureMotor(boosterMount, configurationId, "I357T", 0);
        rocket.getFlightConfiguration(configurationId).setAllStages();
        rocket.setSelectedConfiguration(configurationId);
        return document;
    }

    private InnerTube makeMotorMount() {
        InnerTube mount = new InnerTube();
        mount.setLength(0.15);
        mount.setOuterRadius(0.015);
        mount.setThickness(0.001);
        mount.setMotorMount(true);
        return mount;
    }

    private MotorConfiguration configureMotor(InnerTube mount, FlightConfigurationId configurationId,
            String designation, double ejectionDelay) {
        MotorConfiguration configuration = new MotorConfiguration(mount, configurationId);
        configuration.setMotor(makeMotor(designation));
        configuration.setEjectionDelay(ejectionDelay);
        mount.setMotorConfig(configuration, configurationId);
        return configuration;
    }

    private ThrustCurveMotor makeMotor(String designation) {
        return new ThrustCurveMotor.Builder()
                .setManufacturer(Manufacturer.getManufacturer("AeroTech"))
                .setDesignation(designation)
                .setCode(designation)
                .setCommonName(designation)
                .setMotorType(Motor.Type.RELOAD)
                .setStandardDelays(new double[] { 0.0, 6.0 })
                .setDiameter(0.029)
                .setLength(0.12)
                .setTimePoints(new double[] { 0.0, 0.5, 1.0 })
                .setThrustPoints(new double[] { 0.0, 200.0, 0.0 })
                .setCGPoints(new CoordinateIF[] {
                        new Coordinate(0.06, 0.0, 0.0, 0.2),
                        new Coordinate(0.06, 0.0, 0.0, 0.15),
                        new Coordinate(0.06, 0.0, 0.0, 0.1) })
                .setDigest("AeroTech-" + designation)
                .build();
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
