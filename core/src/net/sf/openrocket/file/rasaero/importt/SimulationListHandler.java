package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.motor.IgnitionEvent;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.simulation.SimulationOptions;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for simulation importing from a RASAero file.
 * A SimulationList is a collection of RASAero simulations.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SimulationListHandler extends AbstractElementHandler {
    private final DocumentLoadingContext context;
    private final Rocket rocket;
    private final SimulationOptions launchSiteSettings;
    private int nrOfSimulations = 0;


    public SimulationListHandler(DocumentLoadingContext context, Rocket rocket, SimulationOptions launchSiteSettings) {
        this.context = context;
        this.rocket = rocket;
        this.launchSiteSettings = launchSiteSettings;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (RASAeroCommonConstants.SIMULATION.equals(element)) {
            nrOfSimulations++;
            return new SimulationHandler(context, rocket, launchSiteSettings, nrOfSimulations);
        }
        return null;
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        RASAeroMotorsLoader.clearAllMotors();
    }

    /**
     * Handles RASAero simulation elements.
     * We will only import the motor information from it.
     */
    private static class SimulationHandler extends AbstractElementHandler {
        private final DocumentLoadingContext context;
        private final Rocket rocket;
        private final SimulationOptions launchSiteSettings;
        private final int simulationNr;

        // Motor information
        private ThrustCurveMotor sustainerEngine;
        private Double sustainerIgnitionDelay;
        private ThrustCurveMotor booster1Engine;
        private Double booster1IgnitionDelay;
        private Double booster1SeparationDelay;
        private Boolean includeBooster1;
        private ThrustCurveMotor booster2Engine;
        private Double booster2SeparationDelay;
        private Boolean includeBooster2;

        public SimulationHandler(DocumentLoadingContext context, Rocket rocket, SimulationOptions launchSiteSettings, int simulationNr) {
            this.context = context;
            this.rocket = rocket;
            this.launchSiteSettings = launchSiteSettings;
            this.simulationNr = simulationNr;
        }

        @Override
        public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
            if (RASAeroCommonConstants.SUSTAINER_ENGINE.equals(element) || RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY.equals(element)
                    || RASAeroCommonConstants.BOOSTER1_ENGINE.equals(element) || RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY.equals(element)
                    || RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY.equals(element) || RASAeroCommonConstants.INCLUDE_BOOSTER1.equals(element)
                    || RASAeroCommonConstants.BOOSTER2_ENGINE.equals(element) || RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY.equals(element)
                    || RASAeroCommonConstants.INCLUDE_BOOSTER2.equals(element)) {
                return PlainTextHandler.INSTANCE;
            }
            return null;
        }

        @Override
        public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
            if (RASAeroCommonConstants.SUSTAINER_ENGINE.equals(element)) {
                sustainerEngine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
            } else if (RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY.equals(element)) {
                sustainerIgnitionDelay = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.BOOSTER1_ENGINE.equals(element)) {
                booster1Engine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
            } else if (RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY.equals(element)) {
                booster1IgnitionDelay = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY.equals(element)) {
                booster1SeparationDelay = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.INCLUDE_BOOSTER1.equals(element)) {
                includeBooster1 = Boolean.parseBoolean(content);
            } else if (RASAeroCommonConstants.BOOSTER2_ENGINE.equals(element)) {
                booster2Engine = RASAeroMotorsLoader.getMotorFromRASAero(content, warnings);
            } else if (RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY.equals(element)) {
                booster2SeparationDelay = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.INCLUDE_BOOSTER2.equals(element)) {
                includeBooster2 = Boolean.parseBoolean(content);
            }
        }

        @Override
        public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
            FlightConfigurationId id = new FlightConfigurationId();
            rocket.createFlightConfiguration(id);
            // Select if this is the first config
            if (rocket.getSelectedConfiguration().getId().equals(FlightConfigurationId.DEFAULT_VALUE_FCID)) {
                rocket.setSelectedConfiguration(id);
            }

            // Add motors to the rocket
            addMotorToStage(0, sustainerEngine, sustainerIgnitionDelay, id, warnings);
            if (includeBooster1) {
                addMotorToStage(1, booster1Engine, booster1IgnitionDelay, id, warnings);
            }
            if (includeBooster2) {
                addMotorToStage(2, booster2Engine, 0.0, id, warnings);
            }

            // Set separation settings
            setSeparationDelay(0, 0.0, id);
            if (includeBooster1) {
                setSeparationDelay(1, booster1SeparationDelay, id);
            }
            if (includeBooster2) {
                setSeparationDelay(2, booster2SeparationDelay, id);
            }

            // Add a new simulation
            Simulation sim = new Simulation(rocket);
            sim.setFlightConfigurationId(id);
            sim.setName("Simulation " + simulationNr);
            sim.copySimulationOptionsFrom(launchSiteSettings);
            context.getOpenRocketDocument().addSimulation(sim);
        }

        private void addMotorToStage(final int stageNr, final Motor motor, final Double ignitionDelay,
                                     final FlightConfigurationId id, final WarningSet warnings) {
            if (motor == null || rocket.getStage(stageNr) == null) {
                return;
            }
            MotorMount mount = getMotorMountForStage(stageNr);
            if (mount == null) {
                warnings.add("No motor mount found for stage " + stageNr + ".  Ignoring motor.");
                return;
            }
            MotorConfiguration motorConfig = new MotorConfiguration(mount, id);
            motorConfig.setMotor(motor);
            double delay = ignitionDelay != null ? ignitionDelay : 0.0;
            motorConfig.setIgnitionDelay(delay);
            if (stageNr < rocket.getStageCount() - 1) {       // Use burnout non-last if multi-staged rocket
                motorConfig.setIgnitionEvent(IgnitionEvent.BURNOUT);
            } else {
                motorConfig.setIgnitionEvent(IgnitionEvent.AUTOMATIC);
            }
            mount.setMotorConfig(motorConfig, id);
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
         * @param stage stage number
         * @return furthest back motor mount of the stage
         */
        private MotorMount getMotorMountForStage(int stage) {
            MotorMount mount = null;
            for (RocketComponent component : rocket.getStage(stage)) {
                if (component instanceof MotorMount) {
                    mount = (MotorMount) component;
                }
            }
            if (mount != null) {
                mount.setMotorMount(true);
            }
            return mount;
        }
    }
}
