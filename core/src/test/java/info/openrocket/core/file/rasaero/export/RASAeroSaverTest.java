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
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.models.wind.WindModelType;
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

            assertEquals(4, warnings.size(), " incorrect amount of RASAero export warnings"); // incl. mixed surface finishes
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

            assertEquals(3, warnings.size(), " incorrect amount of RASAero export warnings"); // incl. mixed surface finishes
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
     * RASAero gives a body tube the aft diameter of the previous component, so a diameter step must be an export
     * error instead of silently changing the tube's diameter.
     */
    @Test
    public void testBodyTubeDiameterStepIsAnError() {
        OpenRocketDocument document = loadRocket("01.One-stage.ork");
        ErrorSet errors = new ErrorSet();
        new RASAeroSaver().marshalToRASAero(document, new WarningSet(), errors);
        assertEquals(0, errors.size());

        NoseCone noseCone = (NoseCone) document.getRocket().getStage(0).getChild(0);
        noseCone.setAftRadiusAutomatic(false);
        noseCone.setAftRadius(noseCone.getAftRadius() - 0.001);
        new RASAeroSaver().marshalToRASAero(document, new WarningSet(), errors);
        assertTrue(errors.size() > 0);
    }

    /**
     * RASAero has one surface finish for the whole rocket, so the finish of most of the rocket's surface must be
     * exported, mapped to the nearest RASAero finish, with a warning when the rocket has several finishes.
     */
    @Test
    public void testExportsPredominantSurfaceFinish() {
        OpenRocketDocument document = loadRocket("01.One-stage.ork");
        for (RocketComponent component : document.getRocket()) {
            if (component instanceof ExternalComponent) {
                ((ExternalComponent) component).setFinish(ExternalComponent.Finish.POLISHED);
            }
        }
        WarningSet warnings = new WarningSet();
        String result = new RASAeroSaver().marshalToRASAero(document, warnings, new ErrorSet());
        assertTrue(result.contains("<Surface>Polished</Surface>"));
        assertFalse(warnings.stream().anyMatch(w -> w.toString().contains("RASAeroExport.warningB1")));

        NoseCone noseCone = (NoseCone) document.getRocket().getStage(0).getChild(0);
        noseCone.setFinish(ExternalComponent.Finish.ROUGH);
        warnings = new WarningSet();
        result = new RASAeroSaver().marshalToRASAero(document, warnings, new ErrorSet());
        assertTrue(result.contains("<Surface>Polished</Surface>"));
        assertTrue(warnings.stream().anyMatch(w -> w.toString().contains("RASAeroExport.warningB1")));
    }

    /**
     * A non-trapezoidal fin set is exported as the trapezoid with the same root chord, span, area and area centroid.
     * For an elliptical fin, that is the symmetric trapezoid with a tip chord of (pi/2 - 1) times the root chord.
     */
    @Test
    public void testExportsEllipticalFinsAsEquivalentTrapezoid() throws Exception {
        BodyTube bodyTube = makeFinTestRocket();
        EllipticalFinSet fins = new EllipticalFinSet();
        fins.setFinCount(3);
        fins.setLength(0.1);
        fins.setHeight(0.05);
        bodyTube.addChild(fins);

        WarningSet warnings = new WarningSet();
        FinDTO fin = new FinDTO(fins, warnings, new ErrorSet());

        double rootChord = 0.1;
        double tipChord = (Math.PI / 2 - 1) * rootChord;
        double toInches = RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        assertEquals(rootChord * toInches, fin.getChord(), 1e-6);
        assertEquals(0.05 * toInches, fin.getSpan(), 1e-6);
        assertEquals(tipChord * toInches, fin.getTipChord(), 0.01 * tipChord * toInches);
        assertEquals((rootChord - tipChord) / 2 * toInches, fin.getSweepDistance(), 0.01 * rootChord * toInches);
        assertTrue(warnings.stream().anyMatch(w -> w.toString().contains("RASAeroExport.warningB3")));
    }

    /**
     * RASAero has one fin set per body tube, so a second fin set must not silently replace the first.
     */
    @Test
    public void testKeepsTheFirstOfSeveralFinSets() throws Exception {
        BodyTube bodyTube = makeFinTestRocket();
        bodyTube.addChild(new TrapezoidFinSet(3, 0.1, 0.05, 0.02, 0.05));
        bodyTube.addChild(new TrapezoidFinSet(4, 0.08, 0.04, 0.01, 0.04));

        WarningSet warnings = new WarningSet();
        BodyTubeDTO dto = new BodyTubeDTO(bodyTube, warnings, new ErrorSet());

        assertEquals(3, dto.getFin().getCount());
        assertTrue(warnings.stream().anyMatch(w -> w.toString().contains("RASAeroExport.warningB4")));
    }

    /**
     * The importer represents RASAero fin cans and boattails as inline pods, so exporting an imported design must
     * write them back as fin cans and boattails.
     */
    @Test
    public void testFinCanAndBoattailRoundTrip() throws Exception {
        OpenRocketDocument document = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(document);
        context.setMotorFinder(new DatabaseMotorFinder());
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            new RASAeroLoader().loadFromStream(context, new BufferedInputStream(stream), null);
        }

        ErrorSet errors = new ErrorSet();
        String result = new RASAeroSaver().marshalToRASAero(document, new WarningSet(), errors);

        assertEquals(0, errors.size(), errors.toString());
        assertTrue(result.contains("<FinCan>"), result);
        assertTrue(result.contains("<InsideDiameter>3</InsideDiameter>"), result);
        assertTrue(result.contains("<ShoulderLength>0.25</ShoulderLength>"), result);
        assertTrue(result.contains("<BoatTail>"), result);
        assertTrue(result.contains("<RearDiameter>2.75</RearDiameter>"), result);

        document = OpenRocketDocumentFactory.createEmptyRocket();
        context = new DocumentLoadingContext();
        context.setOpenRocketDocument(document);
        context.setMotorFinder(new DatabaseMotorFinder());
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Show-off.CDX1")) {
            assertNotNull(stream, "Could not open Show-off.CDX1");
            new RASAeroLoader().loadFromStream(context, new BufferedInputStream(stream), null);
        }
        errors = new ErrorSet();
        result = new RASAeroSaver().marshalToRASAero(document, new WarningSet(), errors);
        assertEquals(0, errors.size(), errors.toString());
        assertTrue(result.contains("<FinCan>"), result);
        assertTrue(result.contains("<BoatTail>"), result);
    }

    private BodyTube makeFinTestRocket() {
        Rocket rocket = new Rocket();
        AxialStage stage = new AxialStage();
        rocket.addChild(stage);
        stage.addChild(new NoseCone());
        BodyTube bodyTube = new BodyTube();
        bodyTube.setLength(0.5);
        stage.addChild(bodyTube);
        return bodyTube;
    }

    /**
     * RASAero writes and reads the rocket's CP element, not a CD element.
     */
    @Test
    public void testExportsCPElement() {
        OpenRocketDocument document = loadRocket("01.One-stage.ork");
        String result = new RASAeroSaver().marshalToRASAero(document, new WarningSet(), new ErrorSet());
        assertTrue(result.contains("<CP>0</CP>"));
        assertFalse(result.contains("<CD>"));
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

    /**
     * RASAero's booster separation delay is the coast time after booster burnout, and the sustainer ignition delay is
     * the coast time after separation. OpenRocket's default staging separates the booster and ignites the sustainer
     * at the booster's ejection charge, i.e. the booster motor's ejection delay after burnout.
     */
    @Test
    public void testStagingAtBoosterEjectionCharge() {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        FlightConfigurationId configurationId = rocket.getSelectedConfiguration().getId();
        AxialStage booster = (AxialStage) rocket.getChild(1);
        SimulationListDTO.findMotorMount(booster).getMotorConfig(configurationId).setEjectionDelay(4);

        SimulationDTO simulation = exportSimulation(document, configurationId);
        assertEquals(4, simulation.getBooster1SeparationDelay(), 1e-9);
        assertEquals(0, simulation.getSustainerIgnitionDelay(), 1e-9);
    }

    /**
     * A booster that separates 0.5 s after burnout, with a sustainer that ignites 2 s after booster burnout, coasts
     * for 1.5 s after separation in RASAero's timeline.
     */
    @Test
    public void testStagingAfterBoosterBurnout() {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        FlightConfigurationId configurationId = rocket.getSelectedConfiguration().getId();
        AxialStage sustainer = (AxialStage) rocket.getChild(0);
        AxialStage booster = (AxialStage) rocket.getChild(1);
        StageSeparationConfiguration separation = new StageSeparationConfiguration();
        separation.setSeparationEvent(StageSeparationConfiguration.SeparationEvent.BURNOUT);
        separation.setSeparationDelay(0.5);
        booster.getSeparationConfigurations().set(configurationId, separation);
        MotorConfiguration sustainerMotor = SimulationListDTO.findMotorMount(sustainer).getMotorConfig(configurationId);
        sustainerMotor.setIgnitionEvent(IgnitionEvent.BURNOUT);
        sustainerMotor.setIgnitionDelay(2);

        SimulationDTO simulation = exportSimulation(document, configurationId);
        assertEquals(0.5, simulation.getBooster1SeparationDelay(), 1e-9);
        assertEquals(1.5, simulation.getSustainerIgnitionDelay(), 1e-9);
    }

    /**
     * An air-started sustainer cannot be represented in RASAero, which ignites the sustainer after separation.
     */
    @Test
    public void testStagingWarnsForAirStart() {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        FlightConfigurationId configurationId = rocket.getSelectedConfiguration().getId();
        AxialStage sustainer = (AxialStage) rocket.getChild(0);
        SimulationListDTO.findMotorMount(sustainer).getMotorConfig(configurationId)
                .setIgnitionEvent(IgnitionEvent.LAUNCH);

        WarningSet warnings = new WarningSet();
        exportSimulation(document, configurationId, warnings);
        assertTrue(warnings.toString().contains("RASAeroExport.warning16"));
    }

    /**
     * RASAero flies one motor per stage and has no parallel boosters, so clustered motors and motors in parallel
     * stages must be reported.
     */
    @Test
    public void testWarnsForMotorsRASAeroDoesNotFly() {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        FlightConfigurationId configurationId = rocket.getSelectedConfiguration().getId();
        AxialStage sustainer = (AxialStage) rocket.getChild(0);
        WarningSet warnings = new WarningSet();
        exportSimulation(document, configurationId, warnings);
        assertFalse(warnings.toString().contains("RASAeroExport.warning20"));

        ((InnerTube) SimulationListDTO.findMotorMount(sustainer))
                .setClusterConfiguration(ClusterConfiguration.CONFIGURATIONS[1]);
        warnings = new WarningSet();
        exportSimulation(document, configurationId, warnings);
        assertTrue(warnings.toString().contains("RASAeroExport.warning20"));

        ParallelStage sideBoosters = new ParallelStage(2);
        ((BodyTube) sustainer.getChild(1)).addChild(sideBoosters);
        BodyTube podBody = new BodyTube(0.2, 0.012, 0.001);
        sideBoosters.addChild(podBody);
        InnerTube podMount = makeMotorMount();
        podMount.setOuterRadius(0.01);
        podBody.addChild(podMount);
        configureMotor(podMount, configurationId, "G80T", 0);
        rocket.getFlightConfiguration(configurationId).setAllStages();
        warnings = new WarningSet();
        exportSimulation(document, configurationId, warnings);
        assertTrue(warnings.toString().contains("RASAeroExport.warning19"));
    }

    /**
     * RASAero uses the nozzle exit diameter for its power-on drag and its altitude thrust correction. A cluster is
     * exported as a single nozzle with the same exit area.
     */
    @Test
    public void testNozzleExitDiameterExport() throws Exception {
        OpenRocketDocument document = makeTwoStageDocument();
        Rocket rocket = document.getRocket();
        FlightConfigurationId configurationId = rocket.getSelectedConfiguration().getId();
        MotorMount sustainerMount = SimulationListDTO.findMotorMount((AxialStage) rocket.getChild(0));
        InnerTube boosterMount = (InnerTube) SimulationListDTO.findMotorMount((AxialStage) rocket.getChild(1));
        boosterMount.setClusterConfiguration(ClusterConfiguration.CONFIGURATIONS[1]);
        sustainerMount.getMotorConfig(configurationId).setNozzleExitDiameter(0.02);
        boosterMount.getMotorConfig(configurationId).setNozzleExitDiameter(0.015);

        SimulationDTO simulation = exportSimulation(document, configurationId);
        assertEquals(0.02 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH,
                simulation.getSustainerNozzleDiameter(), 1e-9);
        assertEquals(0.015 * Math.sqrt(2) * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH,
                simulation.getBooster1NozzleDiameter(), 1e-9);

        Simulation documentSimulation = new Simulation(document, rocket);
        documentSimulation.setFlightConfigurationId(configurationId);
        document.addSimulation(documentSimulation);
        ErrorSet errors = new ErrorSet();
        String exported = new RASAeroSaver().marshalToRASAero(document, new WarningSet(), errors);
        assertEquals(0, errors.size(), errors.toString());
        // RASAero reads the booster nozzle exit diameter from NozzleDiameter, but writes it as NozzleExitDiameter
        assertTrue(exported.contains("<NozzleDiameter>"));
        assertTrue(exported.contains("<NozzleExitDiameter>"));
    }

    /**
     * The launch site is exported from the simulation options, also for simulations that have not been run. RASAero's
     * barometric pressure is reduced to sea level, and a positive wind speed is a headwind.
     */
    @Test
    public void testLaunchSiteExport() {
        OpenRocketDocument document = makeTwoStageDocument();
        Simulation simulation = new Simulation(document, document.getRocket());
        document.addSimulation(simulation);
        SimulationOptions options = simulation.getOptions();
        options.setLaunchAltitude(1000);
        options.setISAAtmosphere(false);
        options.setLaunchTemperature(300);
        options.setLaunchPressure(88000);
        options.setWindModelType(WindModelType.AVERAGE);
        options.getAverageWindModel().setAverage(5);
        options.setLaunchIntoWind(true);

        LaunchSiteDTO launchSite = new LaunchSiteDTO(document, new WarningSet(), new ErrorSet());
        double altitude = 1000 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE;
        assertEquals(altitude, launchSite.getAltitude(), 1e-6);
        assertEquals(88000, RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LAUNCH_PRESSURE(launchSite.getPressure(),
                altitude), 1);
        // The barometric pressure reduced to sea level is higher than the launch site pressure
        assertTrue(launchSite.getPressure() > 88000 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_PRESSURE);
        assertEquals(5 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED, launchSite.getWindSpeed(), 1e-6);

        // With the standard atmosphere, the barometric pressure is left empty and the ISA temperature is exported
        options.setISAAtmosphere(true);
        options.setLaunchIntoWind(false);
        options.setLaunchRodDirection(options.getAverageWindModel().getDirection() + Math.PI);
        launchSite = new LaunchSiteDTO(document, new WarningSet(), new ErrorSet());
        assertEquals(0, launchSite.getPressure(), 1e-9);
        assertEquals(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(288.15 - 0.0065 * 1000),
                launchSite.getTemperature(), 0.1);
        assertEquals(-5 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED, launchSite.getWindSpeed(), 1e-6);
    }

    /**
     * RASAero has a single launch site and a single constant wind, so differing launch sites and multi-level wind
     * must be reported.
     */
    @Test
    public void testLaunchSiteWarnings() {
        OpenRocketDocument document = makeTwoStageDocument();
        Simulation first = new Simulation(document, document.getRocket());
        Simulation second = new Simulation(document, document.getRocket());
        document.addSimulation(first);
        document.addSimulation(second);
        second.getOptions().setLaunchAltitude(first.getOptions().getLaunchAltitude() + 500);
        first.getOptions().setWindModelType(WindModelType.MULTI_LEVEL);
        first.getOptions().getMultiLevelWindModel().addWindLevel(100, 4, 0, 0.1);

        WarningSet warnings = new WarningSet();
        new LaunchSiteDTO(document, warnings, new ErrorSet());
        assertTrue(warnings.toString().contains("RASAeroExport.warning21"));
        assertTrue(warnings.toString().contains("RASAeroExport.warning22"));
    }

    private SimulationDTO exportSimulation(OpenRocketDocument document, FlightConfigurationId configurationId) {
        return exportSimulation(document, configurationId, new WarningSet());
    }

    private SimulationDTO exportSimulation(OpenRocketDocument document, FlightConfigurationId configurationId,
            WarningSet warnings) {
        Rocket rocket = document.getRocket();
        Simulation simulation = new Simulation(document, rocket);
        simulation.setFlightConfigurationId(configurationId);
        Map<AxialStage, MotorMount> mounts = new HashMap<>();
        for (RocketComponent child : rocket.getChildren()) {
            mounts.put((AxialStage) child, SimulationListDTO.findMotorMount((AxialStage) child));
        }
        return new SimulationDTO(rocket, simulation, mounts, new ArrayList<>(), warnings, new ErrorSet());
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
        boosterBody.addChild(new TrapezoidFinSet(3, 0.1, 0.05, 0.03, 0.06));
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
