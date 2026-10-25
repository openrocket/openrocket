package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.CustomBooleanAdapter;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader.RASAeroMotor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.startup.Application;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = RASAeroCommonConstants.SIMULATION)
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationDTO {
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_ENGINE)
    private String sustainerEngine;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_LAUNCH_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerLaunchWt = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerNozzleDiameter = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerCG = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerIgnitionDelay = 0.0d;

    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_ENGINE)
    private String booster1Engine;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_LAUNCH_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1LaunchWt = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1SeparationDelay = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1IgnitionDelay = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1CG = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1NozzleDiameter = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.INCLUDE_BOOSTER1)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean includeBooster1 = false;

    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_ENGINE)
    private String booster2Engine;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_LAUNCH_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2LaunchWt = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2Delay = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2CG = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2NozzleDiameter = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.INCLUDE_BOOSTER2)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean includeBooster2 = false;

    @XmlElement(name = RASAeroCommonConstants.FLIGHT_TIME)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double flightTime = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.TIME_TO_APOGEE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double timetoApogee = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.MAX_ALTITUDE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double maxAltitude = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.MAX_VELOCITY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double maxVelocity = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.OPTIMUM_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double optimumWt = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.OPTIMUM_MAX_ALT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double optimumMaxAlt = 0.0d;

    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default, no-args constructor.
     */
    public SimulationDTO() {
    }

    /**
     * RASAero Simulation object.
     * 
     * @param rocket     the rocket
     * @param simulation the simulation to convert
     * @param mounts     a map of stages and their corresponding motor mount (only 1
     *                   mount per stage allowed)
     *                   if a motor mount is null, it means that stage does not have
     *                   any motors, but mass/CG export should still take place
     * @param motors     a list of RASAero motors
     * @param warnings   a list to add export warnings to
     * @param errors     a list to add export errors to
     */
    public SimulationDTO(Rocket rocket, Simulation simulation, Map<AxialStage, MotorMount> mounts,
            List<RASAeroMotor> motors,
            WarningSet warnings, ErrorSet errors) {
        String simulationName = simulation != null ? simulation.getName() : "DEFAULT";
        FlightConfigurationId fcid = simulation != null ? simulation.getFlightConfigurationId() : null;

        if (simulation != null && fcid == null) {
            warnings.add(String.format(trans.get("RASAeroExport.warning11"), simulationName));
            return;
        }

        if (mounts.isEmpty()) {
            warnings.add(String.format(trans.get("RASAeroExport.warning12"), simulationName));
            return;
        }

        for (Map.Entry<AxialStage, MotorMount> mountSet : mounts.entrySet()) {
            AxialStage stage = mountSet.getKey();
            MotorMount mount = mountSet.getValue();
            if (stage == null) {
                continue;
            }

            // Get the motor info for this stage
            MotorConfiguration motorConfig = mount != null ? mount.getMotorConfig(fcid) : null;
            Motor motor = null;
            if (motorConfig != null) {
                motor = motorConfig.getMotor();
            }
            int stageNr = rocket.getChildPosition(stage);
            // RASAero aborts the simulation when an included booster has no motor
            boolean includeStage = motor != null
                    && (fcid == null || rocket.getFlightConfiguration(fcid).isStageActive(stage.getStageNumber()));

            // Add friendly reminder to user
            if (motor == null) {
                warnings.add(String.format(trans.get("RASAeroExport.warning13"), stage.getName()));
            }

            // Add the simulation info for each stage. The launch mass & CG of each stage stack is calculated using
            // the simulation's flight configuration, so that all motors (clusters and multiple mounts) are included.
            FlightConfiguration CGCalcConfig = fcid != null ? new FlightConfiguration(rocket, fcid) :
                    new FlightConfiguration(rocket);
            RigidBody calc;
            double ignitionDelay;
            switch (stageNr) {
                // Sustainer
                case 0:
                    setSustainerEngine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor,
                            motorConfig != null ? motorConfig.getEjectionDelay() : Double.NaN, warnings));

                    // Calculate mass & CG of sustainer
                    CGCalcConfig.setOnlyStage(0);
                    calc = MassCalculator.calculateLaunch(CGCalcConfig);

                    setSustainerLaunchWt(calc.getMass() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);
                    setSustainerCG(calc.getCM().getX() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                    setSustainerNozzleDiameter(getNozzleExitDiameter(mount, fcid));

                    // Set ignition delay
                    ignitionDelay = motorConfig != null ? motorConfig.getIgnitionDelay() : 0;
                    setSustainerIgnitionDelay(ignitionDelay);

                    break;
                // Booster 1
                case 1:
                    setBooster1Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor,
                            motorConfig != null ? motorConfig.getEjectionDelay() : Double.NaN, warnings));

                    // Calculate mass & CG of sustainer + booster 1 combined
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    calc = MassCalculator.calculateLaunch(CGCalcConfig);

                    setBooster1LaunchWt(calc.getMass() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);
                    setBooster1CG(calc.getCM().getX() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                    setBooster1NozzleDiameter(getNozzleExitDiameter(mount, fcid));

                    // Set ignition delay
                    ignitionDelay = motorConfig != null ? motorConfig.getIgnitionDelay() : 0;
                    setBooster1IgnitionDelay(ignitionDelay);

                    setIncludeBooster1(includeStage);

                    break;
                // Booster 2
                case 2:
                    setBooster2Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor,
                            motorConfig != null ? motorConfig.getEjectionDelay() : Double.NaN, warnings));

                    // Calculate mass & CG of sustainer + booster 1 + booster 2 combined
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    calc = MassCalculator.calculateLaunch(CGCalcConfig);

                    setBooster2LaunchWt(calc.getMass() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);
                    setBooster2CG(calc.getCM().getX() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                    setBooster2NozzleDiameter(getNozzleExitDiameter(mount, fcid));

                    setIncludeBooster2(includeStage);

                    break;
                // Invalid
                default:
                    errors.add(String.format(trans.get("RASAeroExport.error25"), stageNr, simulationName));
            }
        }

        setStagingDelays(rocket, fcid, mounts, warnings);
        if (fcid != null) {
            warnUnsupportedMotors(rocket, fcid, mounts, warnings);
        }
    }

    /**
     * RASAero flies a single motor per stage and has no parallel boosters, so warn about the motors it does not fly.
     */
    private void warnUnsupportedMotors(Rocket rocket, FlightConfigurationId fcid, Map<AxialStage, MotorMount> mounts,
            WarningSet warnings) {
        for (MotorConfiguration motorConfig : rocket.getFlightConfiguration(fcid).getActiveMotors()) {
            MotorMount mount = motorConfig.getMount();
            AxialStage stage = mount.getStage();
            if (stage.getParent() != rocket) {
                warnings.add(String.format(trans.get("RASAeroExport.warning19"), stage.getName()));
            } else if (mounts.get(stage) != mount || mount.getMotorCount() > 1) {
                warnings.add(String.format(trans.get("RASAeroExport.warning20"), stage.getName()));
            }
        }
    }

    /**
     * Returns the nozzle exit diameter of the motors in a mount, in RASAero units. RASAero flies a single motor per
     * stage, so a cluster gets the diameter of a single nozzle with the same total exit area.
     *
     * @param mount the motor mount, or null
     * @param fcid  the flight configuration
     * @return the nozzle exit diameter, or 0 when it is unknown
     */
    static double getNozzleExitDiameter(MotorMount mount, FlightConfigurationId fcid) {
        if (mount == null) {
            return 0;
        }
        MotorConfiguration motorConfig = mount.getMotorConfig(fcid);
        return motorConfig.getNozzleExitDiameter() * Math.sqrt(mount.getMotorCount())
                * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
    }

    /**
     * RASAero's staging timeline is relative to the burnout of each booster: the booster and the stage above it
     * coast together for the booster's separation delay, after which the upper stage coasts for its ignition delay
     * before its motor ignites. The OpenRocket separation and ignition events are converted to that timeline.
     */
    private void setStagingDelays(Rocket rocket, FlightConfigurationId fcid, Map<AxialStage, MotorMount> mounts,
            WarningSet warnings) {
        for (int lower = 1; lower <= Math.min(2, rocket.getChildCount() - 1); lower++) {
            if (!(lower == 1 ? includeBooster1 : includeBooster2)) {
                continue;
            }
            AxialStage lowerStage = (AxialStage) rocket.getChild(lower);
            AxialStage upperStage = (AxialStage) rocket.getChild(lower - 1);
            // An included booster always has a motor
            double lowerEjectionDelay = mounts.get(lowerStage).getMotorConfig(fcid).getEjectionDelay();
            MotorMount upperMount = mounts.get(upperStage);
            MotorConfiguration upperMotor = upperMount != null ? upperMount.getMotorConfig(fcid) : null;
            boolean upperHasMotor = upperMotor != null && upperMotor.getMotor() != null;

            // Times after the burnout of the lower stage
            double ignition = 0;
            if (upperHasMotor) {
                IgnitionEvent ignitionEvent = upperMotor.getIgnitionEvent();
                if ((ignitionEvent == IgnitionEvent.AUTOMATIC || ignitionEvent == IgnitionEvent.EJECTION_CHARGE)
                        && lowerEjectionDelay != Motor.PLUGGED_DELAY) {
                    ignition = lowerEjectionDelay;
                } else if (ignitionEvent != IgnitionEvent.BURNOUT) {
                    warnings.add(String.format(trans.get("RASAeroExport.warning16"), upperStage.getName(),
                            ignitionEvent.toString()));
                }
                ignition += upperMotor.getIgnitionDelay();
            }

            StageSeparationConfiguration separationConfig = lowerStage.getSeparationConfigurations().get(fcid);
            SeparationEvent separationEvent = separationConfig.getSeparationEvent();
            double separation = 0;
            if (separationEvent == SeparationEvent.EJECTION && lowerEjectionDelay != Motor.PLUGGED_DELAY) {
                separation = lowerEjectionDelay;
            } else if (separationEvent == SeparationEvent.UPPER_IGNITION && upperHasMotor) {
                separation = ignition;
            } else if (separationEvent != SeparationEvent.BURNOUT) {
                warnings.add(String.format(trans.get("RASAeroExport.warning17"), lowerStage.getName(),
                        separationEvent.toString()));
            }
            separation += separationConfig.getSeparationDelay();

            // RASAero cannot ignite the upper stage before the lower stage separates
            double ignitionAfterSeparation = ignition - separation;
            if (ignitionAfterSeparation < 0) {
                warnings.add(String.format(trans.get("RASAeroExport.warning18"), upperStage.getName()));
                ignitionAfterSeparation = 0;
            }

            if (lower == 1) {
                setBooster1SeparationDelay(separation);
                setSustainerIgnitionDelay(ignitionAfterSeparation);
            } else {
                setBooster2Delay(separation);
                setBooster1IgnitionDelay(ignitionAfterSeparation);
            }
        }
    }

    public String getSustainerEngine() {
        return sustainerEngine;
    }

    public void setSustainerEngine(String sustainerEngine) {
        this.sustainerEngine = sustainerEngine;
    }

    public Double getSustainerLaunchWt() {
        return sustainerLaunchWt;
    }

    public void setSustainerLaunchWt(Double sustainerLaunchWt) {
        this.sustainerLaunchWt = sustainerLaunchWt;
    }

    public Double getSustainerNozzleDiameter() {
        return sustainerNozzleDiameter;
    }

    public void setSustainerNozzleDiameter(Double sustainerNozzleDiameter) {
        this.sustainerNozzleDiameter = sustainerNozzleDiameter;
    }

    public Double getSustainerCG() {
        return sustainerCG;
    }

    public void setSustainerCG(Double sustainerCG) {
        this.sustainerCG = sustainerCG;
    }

    public Double getSustainerIgnitionDelay() {
        return sustainerIgnitionDelay;
    }

    public void setSustainerIgnitionDelay(Double sustainerIgnitionDelay) {
        this.sustainerIgnitionDelay = sustainerIgnitionDelay;
    }

    public String getBooster1Engine() {
        return booster1Engine;
    }

    public void setBooster1Engine(String booster1Engine) {
        this.booster1Engine = booster1Engine;
    }

    public Double getBooster1LaunchWt() {
        return booster1LaunchWt;
    }

    public void setBooster1LaunchWt(Double booster1LaunchWt) {
        this.booster1LaunchWt = booster1LaunchWt;
    }

    public Double getBooster1SeparationDelay() {
        return booster1SeparationDelay;
    }

    public void setBooster1SeparationDelay(Double booster1SeparationDelay) {
        this.booster1SeparationDelay = booster1SeparationDelay;
    }

    public Double getBooster1IgnitionDelay() {
        return booster1IgnitionDelay;
    }

    public void setBooster1IgnitionDelay(Double booster1IgnitionDelay) {
        this.booster1IgnitionDelay = booster1IgnitionDelay;
    }

    public Double getBooster1CG() {
        return booster1CG;
    }

    public void setBooster1CG(Double booster1CG) {
        this.booster1CG = booster1CG;
    }

    public Double getBooster1NozzleDiameter() {
        return booster1NozzleDiameter;
    }

    public void setBooster1NozzleDiameter(Double booster1NozzleDiameter) {
        this.booster1NozzleDiameter = booster1NozzleDiameter;
    }

    public Boolean getIncludeBooster1() {
        return includeBooster1;
    }

    public void setIncludeBooster1(Boolean includeBooster1) {
        this.includeBooster1 = includeBooster1;
    }

    public String getBooster2Engine() {
        return booster2Engine;
    }

    public void setBooster2Engine(String booster2Engine) {
        this.booster2Engine = booster2Engine;
    }

    public Double getBooster2LaunchWt() {
        return booster2LaunchWt;
    }

    public void setBooster2LaunchWt(Double booster2LaunchWt) {
        this.booster2LaunchWt = booster2LaunchWt;
    }

    public Double getBooster2Delay() {
        return booster2Delay;
    }

    public void setBooster2Delay(Double booster2Delay) {
        this.booster2Delay = booster2Delay;
    }

    public Double getBooster2CG() {
        return booster2CG;
    }

    public void setBooster2CG(Double booster2CG) {
        this.booster2CG = booster2CG;
    }

    public Double getBooster2NozzleDiameter() {
        return booster2NozzleDiameter;
    }

    public void setBooster2NozzleDiameter(Double booster2NozzleDiameter) {
        this.booster2NozzleDiameter = booster2NozzleDiameter;
    }

    public Boolean getIncludeBooster2() {
        return includeBooster2;
    }

    public void setIncludeBooster2(Boolean includeBooster2) {
        this.includeBooster2 = includeBooster2;
    }

    public Double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(Double flightTime) {
        this.flightTime = flightTime;
    }

    public Double getTimetoApogee() {
        return timetoApogee;
    }

    public void setTimetoApogee(Double timetoApogee) {
        this.timetoApogee = timetoApogee;
    }

    public Double getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(Double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public Double getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(Double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public Double getOptimumWt() {
        return optimumWt;
    }

    public void setOptimumWt(Double optimumWt) {
        this.optimumWt = optimumWt;
    }

    public Double getOptimumMaxAlt() {
        return optimumMaxAlt;
    }

    public void setOptimumMaxAlt(Double optimumMaxAlt) {
        this.optimumMaxAlt = optimumMaxAlt;
    }
}
