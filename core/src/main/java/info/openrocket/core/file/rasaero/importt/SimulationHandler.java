package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.Coordinate;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Handles RASAero simulation elements.
 * We will only import the motor information from it.
 */
public class SimulationHandler extends AbstractElementHandler {
    private final DocumentLoadingContext context;
    private final Rocket rocket;
    private final SimulationOptions launchSiteSettings;
    private final int simulationNr;

    // Motor information
    private ThrustCurveMotor sustainerEngine;
    private Double sustainerIgnitionDelay;
    private Double sustainerLaunchWt;
    private Double sustainerCG;
    private ThrustCurveMotor booster1Engine;
    private Double booster1IgnitionDelay;
    private Double booster1SeparationDelay;
    private Double booster1LaunchWt;
    private Double booster1CG;
    private Boolean includeBooster1;
    private ThrustCurveMotor booster2Engine;
    private Double booster2SeparationDelay;
    private Double booster2LaunchWt;
    private Double booster2CG;
    private Boolean includeBooster2;

    public SimulationHandler(DocumentLoadingContext context, Rocket rocket, SimulationOptions launchSiteSettings,
            int simulationNr) {
        this.context = context;
        this.rocket = rocket;
        this.launchSiteSettings = launchSiteSettings;
        this.simulationNr = simulationNr;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.SUSTAINER_ENGINE.equals(element)
                || RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY.equals(element)
                || RASAeroCommonConstants.SUSTAINER_LAUNCH_WT.equals(element)
                || RASAeroCommonConstants.SUSTAINER_CG.equals(element)
                || RASAeroCommonConstants.BOOSTER1_ENGINE.equals(element)
                || RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY.equals(element)
                || RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY.equals(element)
                || RASAeroCommonConstants.BOOSTER1_LAUNCH_WT.equals(element)
                || RASAeroCommonConstants.BOOSTER1_CG.equals(element)
                || RASAeroCommonConstants.INCLUDE_BOOSTER1.equals(element)
                || RASAeroCommonConstants.BOOSTER2_ENGINE.equals(element)
                || RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY.equals(element)
                || RASAeroCommonConstants.BOOSTER2_LAUNCH_WT.equals(element)
                || RASAeroCommonConstants.BOOSTER2_CG.equals(element)
                || RASAeroCommonConstants.INCLUDE_BOOSTER2.equals(element)) {
            return PlainTextHandler.INSTANCE;
        }
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.SUSTAINER_ENGINE.equals(element)) {
            sustainerEngine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
        } else if (RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY.equals(element)) {
            sustainerIgnitionDelay = Double.parseDouble(content);
        } else if (RASAeroCommonConstants.SUSTAINER_LAUNCH_WT.equals(element)) {
            sustainerLaunchWt = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT;
        } else if (RASAeroCommonConstants.SUSTAINER_CG.equals(element)) {
            sustainerCG = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        } else if (RASAeroCommonConstants.BOOSTER1_ENGINE.equals(element)) {
            booster1Engine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
        } else if (RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY.equals(element)) {
            booster1IgnitionDelay = Double.parseDouble(content);
        } else if (RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY.equals(element)) {
            booster1SeparationDelay = Double.parseDouble(content);
        } else if (RASAeroCommonConstants.BOOSTER1_LAUNCH_WT.equals(element)) {
            booster1LaunchWt = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT;
        } else if (RASAeroCommonConstants.BOOSTER1_CG.equals(element)) {
            booster1CG = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        } else if (RASAeroCommonConstants.INCLUDE_BOOSTER1.equals(element)) {
            includeBooster1 = Boolean.parseBoolean(content);
        } else if (RASAeroCommonConstants.BOOSTER2_ENGINE.equals(element)) {
            booster2Engine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
        } else if (RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY.equals(element)) {
            booster2SeparationDelay = Double.parseDouble(content);
        } else if (RASAeroCommonConstants.BOOSTER2_LAUNCH_WT.equals(element)) {
            booster2LaunchWt = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT;
        } else if (RASAeroCommonConstants.BOOSTER2_CG.equals(element)) {
            booster2CG = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        } else if (RASAeroCommonConstants.INCLUDE_BOOSTER2.equals(element)) {
            includeBooster2 = Boolean.parseBoolean(content);
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        FlightConfigurationId fcid = new FlightConfigurationId();
        rocket.createFlightConfiguration(fcid);
        // Select if this is the first config
        if (rocket.getSelectedConfiguration().getId().equals(FlightConfigurationId.DEFAULT_VALUE_FCID)) {
            rocket.setSelectedConfiguration(fcid);
        }

        // Add motors to the rocket
        MotorMount sustainerMount = addMotorToStage(0, sustainerEngine, sustainerIgnitionDelay, fcid, true, warnings);
        MotorMount booster1Mount = addMotorToStage(1, booster1Engine, booster1IgnitionDelay, fcid, includeBooster1,
                warnings);
        MotorMount booster2Mount = addMotorToStage(2, booster2Engine, 0.0, fcid, includeBooster2, warnings);

        // Set separation settings
        setSeparationDelay(0, 0.0, fcid);
        if (includeBooster1) {
            setSeparationDelay(1, booster1SeparationDelay, fcid);
        }
        if (includeBooster2) {
            setSeparationDelay(2, booster2SeparationDelay, fcid);
        }

        // Add a new simulation
        Simulation sim = new Simulation(rocket);
        sim.setFlightConfigurationId(fcid);
        sim.setName("Simulation " + simulationNr);
        sim.copySimulationOptionsFrom(launchSiteSettings);
        context.getOpenRocketDocument().addSimulation(sim);

        // Set the weight and CG overrides
        applyMassOverrides(warnings);
        applyCGOverrides(sustainerMount, booster1Mount, booster2Mount, fcid);
    }

    /**
     * Add a new motor to a stage
     * 
     * @param stageNr          number of the stage to add the motor to
     * @param motor            motor to add
     * @param ignitionDelay    ignition delay of the motor
     * @param id               flight config id to alter
     * @param enableMotorMount whether the motor mount should be enabled or disabled
     * @param warnings         The warning set
     */
    private MotorMount addMotorToStage(final int stageNr, final Motor motor, final Double ignitionDelay,
            final FlightConfigurationId id, boolean enableMotorMount,
            final WarningSet warnings) {
        if (motor == null || rocket.getStage(stageNr) == null) {
            return null;
        }
        MotorMount mount = getMotorMountForStage(stageNr);
        if (mount == null) {
            warnings.add("No motor mount found for stage " + stageNr + ". Ignoring motor.");
            return null;
        }
        MotorConfiguration motorConfig = new MotorConfiguration(mount, id);
        motorConfig.setMotor(motor);

        // RASAero requires apogee deployment, so the sustainer motor should always be
        // plugged
        if (stageNr == 0) {
            motorConfig.setEjectionDelay(Motor.PLUGGED_DELAY);
        }

        double delay = ignitionDelay != null ? ignitionDelay : 0.0;
        motorConfig.setIgnitionDelay(delay);
        if (stageNr < rocket.getStageCount() - 1) { // Use burnout non-last if multi-staged rocket
            motorConfig.setIgnitionEvent(IgnitionEvent.BURNOUT);
        } else {
            motorConfig.setIgnitionEvent(IgnitionEvent.AUTOMATIC);
        }
        mount.setMotorConfig(motorConfig, id);
        mount.setMotorMount(enableMotorMount);

        return mount;
    }

    private void setSeparationDelay(final int stageNr, Double separationDelay,
            final FlightConfigurationId id) {
        if (rocket.getStage(stageNr) == null) {
            return;
        }
        StageSeparationConfiguration config = rocket.getStage(stageNr).getSeparationConfigurations().get(id);
        if (separationDelay != null) {
            config.setSeparationDelay(separationDelay);
        }
        config.setSeparationEvent(StageSeparationConfiguration.SeparationEvent.BURNOUT);
    }

    /**
     * Returns the furthest back motor mount in the stage.
     * 
     * @param stageNr stage number
     * @return furthest back motor mount of the stage
     */
    private MotorMount getMotorMountForStage(int stageNr) {
        AxialStage stage = (AxialStage) rocket.getChild(stageNr);
        for (int i = stage.getChildCount() - 1; i > 0; i--) {
            RocketComponent component = stage.getChild(i);
            if (component instanceof MotorMount) {
                return (MotorMount) component;
            }
        }
        return null;
    }

    private void applyMassOverrides(WarningSet warnings) {
        // Don't do anything if the mass has already been overridden by a previous
        // simulation
        if (rocket.getStage(0).isMassOverridden()) {
            return;
        }

        applySustainerMassOverride();
        applyBooster1MassOverride(warnings);
        applyBooster2MassOverride(warnings);
    }

    /**
     * Applies the mass from the RASAero simulation to the sustainer as an override,
     * and returns the final sustainer mass.
     * Note: the sustainer motor mass is subtracted from the RASAero mass to get the
     * final mass.
     * 
     * @return the final sustainer mass
     */
    private double applySustainerMassOverride() {
        if (sustainerLaunchWt == null || sustainerLaunchWt == 0) {
            return 0;
        }

        // Get the sustainer motor weight
        double sustainerMotorWt = 0;
        if (sustainerEngine != null) {
            sustainerMotorWt = sustainerEngine.getLaunchMass();
        }

        double sustainerWt = sustainerLaunchWt - sustainerMotorWt;
        AxialStage sustainer = rocket.getStage(0);
        sustainer.setMassOverridden(true);
        sustainer.setSubcomponentsOverriddenMass(true);
        sustainer.setOverrideMass(sustainerWt);

        return sustainerWt;
    }

    /**
     * Applies the mass from the RASAero simulation to booster1 as an override, and
     * returns the final booster1 mass.
     * Note: the sustainer mass and booster1 motor mass is subtracted from the
     * RASAero mass to get the final mass.
     * 
     * @param warnings list to add import warnings to
     * @return the final booster1 mass
     */
    private double applyBooster1MassOverride(WarningSet warnings) {
        if (!includeBooster1 || booster1LaunchWt == null || booster1LaunchWt == 0 || sustainerLaunchWt == null) {
            return 0;
        }

        // Calculate the final booster mass
        final double boosterWt;
        if (sustainerLaunchWt > booster1LaunchWt) {
            warnings.add("Sustainer wt is greater than total loaded wt of booster 1. Setting mass to 0.");
            boosterWt = 0;
        } else {
            // Get the booster motor weight
            double boosterMotorWt = 0;
            if (booster1Engine != null) {
                boosterMotorWt = booster1Engine.getLaunchMass();
            }

            boosterWt = booster1LaunchWt - boosterMotorWt - sustainerLaunchWt;
        }

        AxialStage booster = rocket.getStage(1);
        booster.setMassOverridden(true);
        booster.setSubcomponentsOverriddenMass(true);
        booster.setOverrideMass(boosterWt);

        return boosterWt;
    }

    /**
     * Applies the mass from the RASAero simulation to booster2 as an override, and
     * returns the final booster2 mass.
     * Note: the sustainer mass, booster1 mass, and booster2 motor mass is
     * subtracted from the RASAero mass to get the final mass.
     * 
     * @param warnings list to add import warnings to
     * @return the final booster2 mass
     */
    private double applyBooster2MassOverride(WarningSet warnings) {
        if (!includeBooster2 || booster2LaunchWt == null || booster2LaunchWt == 0 || booster1LaunchWt == null) {
            return 0;
        }

        // Calculate the final booster mass
        final double boosterWt;
        if (booster1LaunchWt > booster2LaunchWt) {
            warnings.add("Booster1 wt is greater than total loaded wt of booster 2. Setting mass to 0.");
            boosterWt = 0;
        } else {
            // Get the booster motor weight
            double boosterMotorWt = 0;
            if (booster2Engine != null) {
                boosterMotorWt = booster2Engine.getLaunchMass();
            }

            boosterWt = booster2LaunchWt - boosterMotorWt - booster1LaunchWt;
        }

        AxialStage booster = rocket.getStage(2);
        booster.setMassOverridden(true);
        booster.setSubcomponentsOverriddenMass(true);
        booster.setOverrideMass(boosterWt);

        return boosterWt;
    }

    /**
     * Applies the CG extracted from the simulation to the sustainer, booster1, and
     * booster2.
     * 
     * @param sustainerMount the sustainer motor mount
     * @param booster1Mount  the booster1 motor mount
     * @param booster2Mount  the booster2 motor mount
     * @param fcid           the flight configuration ID of this simulation
     */
    private void applyCGOverrides(MotorMount sustainerMount, MotorMount booster1Mount, MotorMount booster2Mount,
            FlightConfigurationId fcid) {
        // Don't do anything if the CG has already been overridden by a previous
        // simulation
        if (rocket.getStage(0).isCGOverridden()) {
            return;
        }

        applySustainerCGOverride(sustainerMount, fcid);
        applyBooster1CGOverride(booster1Mount, fcid);
        applyBooster2CGOverride(booster2Mount, fcid);
    }

    /**
     * Applies the CG from the RASAero simulation to the sustainer as an override.
     * This CG value can be applied directly.
     * 
     * @param sustainerMount the sustainer motor mount
     * @param fcid           the flight configuration ID of this simulation
     * @return the CG of the sustainer
     */
    private Double applySustainerCGOverride(MotorMount sustainerMount, FlightConfigurationId fcid) {
        if (sustainerCG == null) {
            return null;
        }

        AxialStage sustainer = rocket.getStage(0);

        /*
         * sustainerCG is the combined CG of the sustainer and its motor (if present),
         * so we need to calculate the CG of the sustainer without the motor CG
         */
        Double CG = getStageCGWithoutMotorCG(sustainer, sustainerCG, sustainerMount, sustainerEngine, fcid);

        sustainer.setCGOverridden(true);
        sustainer.setSubcomponentsOverriddenCG(true);
        sustainer.setOverrideCGX(CG);

        return CG;
    }

    /**
     * Applies the CG from the RASAero simulation to booster1 as an override.
     * The CG value returned by RASAero is the aggregate CG of the sustainer and
     * booster1.
     * To calculate the CG of booster1, we use the following formula:
     * booster1 CG = [booster1 mass x (distance between CG of sustainer and combined
     * CG)] / (sustainer mass)
     * 
     * @param booster1Mount the booster1 motor mount
     * @param fcid          the flight configuration ID of this simulation
     * @return the CG of booster1
     */
    private Double applyBooster1CGOverride(MotorMount booster1Mount, FlightConfigurationId fcid) {
        if (!includeBooster1 || booster1CG == null || sustainerCG == null || booster1LaunchWt == null
                || sustainerLaunchWt == null) {
            return null;
        }

        AxialStage booster = rocket.getStage(1);

        // Do a back-transform of the combined CG of the sustainer and booster1 to get
        // the CG of booster1
        Double CG = getCGFromCombinedCG(sustainerLaunchWt, booster1LaunchWt - sustainerLaunchWt, sustainerCG,
                booster1CG);

        /*
         * the above CG is the combined CG of booster1 and its motor (if present),
         * so we need to calculate the CG of booster1 without the motor CG
         */
        CG = getStageCGWithoutMotorCG(booster, CG, booster1Mount, booster1Engine, fcid);

        // ! this CG is relative to the front of the sustainer, we need it referenced to
        // the front of the booster
        CG = CG - booster.getPosition().x;

        booster.setCGOverridden(true);
        booster.setSubcomponentsOverriddenCG(true);
        booster.setOverrideCGX(CG);

        return CG;
    }

    /**
     * Applies the CG from the RASAero simulation to booster2 as an override.
     * The CG value returned by RASAero is the aggregate CG of the sustainer,
     * booster1, and booster2.
     * To calculate the CG of booster2, we use the following formula:
     * booster2 CG = [booster2 mass x (distance between CG of sustainer and combined
     * CG)] / (sustainer mass + booster1 mass)
     * 
     * @param booster2Mount the booster1 motor mount
     * @param fcid          the flight configuration ID of this simulation
     * @return the CG of booster2
     */
    private Double applyBooster2CGOverride(MotorMount booster2Mount, FlightConfigurationId fcid) {
        if (!includeBooster2 || booster2CG == null || booster1CG == null || sustainerCG == null ||
                booster2LaunchWt == null || booster1LaunchWt == null || sustainerLaunchWt == null) {
            return null;
        }

        AxialStage booster = rocket.getStage(2);

        // Do a back-transform of the combined CG of the sustainer, booster1, and
        // booster2 to get the CG of booster2
        Double CG = getCGFromCombinedCG(booster1LaunchWt, booster2LaunchWt - booster1LaunchWt, booster1CG, booster2CG);

        /*
         * the above CG is the combined CG of booster2 and its motor (if present),
         * so we need to calculate the CG of booster2 without the motor CG
         */
        CG = getStageCGWithoutMotorCG(booster, CG, booster2Mount, booster2Engine, fcid);

        // ! this CG is relative to the front of the sustainer, we need it referenced to
        // the front of the booster
        CG = CG - booster.getPosition().x;

        booster.setCGOverridden(true);
        booster.setSubcomponentsOverriddenCG(true);
        booster.setOverrideCGX(CG);

        return CG;
    }

    /**
     * Return the CG of object B from the CG of object A, the combined CG of A and
     * B, and the masses of A and B.
     * 
     * @param objAMass   the mass of object A (known CG)
     * @param objBMass   the mass of object B (unknown CG)
     * @param objACG     the CG of object A
     * @param combinedCG the combined CG of A and B
     * @return the CG of object B
     */
    private Double getCGFromCombinedCG(Double objAMass, Double objBMass, Double objACG, Double combinedCG) {
        if (combinedCG == null || objACG == null || objAMass == null || objBMass == null) {
            return combinedCG;
        }
        return combinedCG * (1 + objAMass / objBMass) - objACG * (objAMass / objBMass);
    }

    /**
     * Extracts the CG of a stage from a combined CG of the stage and stage motor.
     * 
     * @param stage      the stage
     * @param combinedCG the combiend CG of the stage and its motor
     * @param mount      the motor mount of the stage that holds the motor
     * @param motor      the motor
     * @param fcid       the flight configuration ID of this simulation
     * @return the CG of the stage without the motor CG
     */
    private Double getStageCGWithoutMotorCG(AxialStage stage, Double combinedCG, MotorMount mount,
            ThrustCurveMotor motor,
            FlightConfigurationId fcid) {
        if (motor == null || mount == null || stage == null || combinedCG == null) {
            return combinedCG;
        }
        double stageMass = stage.getMass(); // Should be overridden by now, so don't use getSectionMass()
        double motorMass = motor.getLaunchMass();

        Coordinate[] CGPoints = motor.getCGPoints();
        if (CGPoints != null && CGPoints.length > 1) {
            double motorPositionXRel = mount.getMotorPosition(fcid).x; // Motor position relative to the mount
            double mountLocationX = mount.getLocations()[0].x;
            double motorLocationX = mountLocationX + motorPositionXRel; // Front location of the motor
            double motorCG = motorLocationX + CGPoints[0].x;

            return getCGFromCombinedCG(motorMass, stageMass, motorCG, combinedCG);
        }

        return combinedCG;
    }
}
