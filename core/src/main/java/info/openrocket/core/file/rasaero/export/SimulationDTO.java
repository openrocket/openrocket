package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.CustomBooleanAdapter;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
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
    private Double sustainerLaunchWt = 0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerNozzleDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerCG = 0d;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_IGNITION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerIgnitionDelay = 0d;

    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_ENGINE)
    private String booster1Engine;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_LAUNCH_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1LaunchWt = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_SEPARATION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1SeparationDelay = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_IGNITION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1IgnitionDelay = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1CG = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1NozzleDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.INCLUDE_BOOSTER1)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean includeBooster1 = false;

    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_ENGINE)
    private String booster2Engine;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_LAUNCH_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2LaunchWt = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_SEPARATION_DELAY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2Delay = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_CG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2CG = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_NOZZLE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2NozzleDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.INCLUDE_BOOSTER2)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean includeBooster2 = false;

    @XmlElement(name = RASAeroCommonConstants.FLIGHT_TIME)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double flightTime = 0d;
    @XmlElement(name = RASAeroCommonConstants.TIME_TO_APOGEE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double timetoApogee = 0d;
    @XmlElement(name = RASAeroCommonConstants.MAX_ALTITUDE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double maxAltitude = 0d;
    @XmlElement(name = RASAeroCommonConstants.MAX_VELOCITY)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double maxVelocity = 0d;
    @XmlElement(name = RASAeroCommonConstants.OPTIMUM_WT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double optimumWt = 0d;
    @XmlElement(name = RASAeroCommonConstants.OPTIMUM_MAX_ALT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double optimumMaxAlt = 0d;

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
            List<ThrustCurveMotor> motors,
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

        // Get sustainer motor mass
        MotorMount sustainerMount = mounts.get((AxialStage) rocket.getChild(0));
        Motor sustainerMotor = null;
        double sustainerMotorMass = 0;
        if (sustainerMount != null) {
            MotorConfiguration sustainerConfig = sustainerMount.getMotorConfig(fcid);
            sustainerMotor = sustainerConfig.getMotor();
            sustainerMotorMass = sustainerMotor != null ? sustainerMotor.getLaunchMass() : 0;
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
            StageSeparationConfiguration separationConfig = null;
            double motorMass = 0;
            if (motorConfig != null) {
                motor = motorConfig.getMotor();
                motorMass = motor != null ? motor.getLaunchMass() : 0;
                separationConfig = stage.getSeparationConfigurations().get(fcid);
            }
            int stageNr = rocket.getChildPosition(stage);

            // Add friendly reminder to user
            if (motor == null) {
                warnings.add(String.format(trans.get("RASAeroExport.warning13"), stage.getName()));
            }

            // Add the simulation info for each stage
            FlightConfiguration CGCalcConfig = new FlightConfiguration(rocket);
            RigidBody calc;
            double ignitionDelay, totalCG, separationDelay;
            switch (stageNr) {
                // Sustainer
                case 0:
                    setSustainerEngine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, warnings));

                    // Calculate mass & CG of sustainer
                    CGCalcConfig.setOnlyStage(0);
                    calc = MassCalculator.calculateStructure(CGCalcConfig);

                    // Set mass
                    double sustainerMass = calc.getMass() + motorMass;
                    setSustainerLaunchWt(sustainerMass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // Set CG
                    double sustainerCG = calc.getCM().x; // = sutainer CG with no motors
                    sustainerCG = addMotorCGToStageCG(sustainerCG, calc.getMass(), mount, motor, fcid);
                    setSustainerCG(sustainerCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    // Set ignition delay
                    ignitionDelay = motorConfig != null ? motorConfig.getIgnitionDelay() : 0;
                    setSustainerIgnitionDelay(ignitionDelay);

                    break;
                // Booster 1
                case 1:
                    setBooster1Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, warnings));

                    // Calculate mass & CG of sustainer + booster 1 combined
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    calc = MassCalculator.calculateStructure(CGCalcConfig);

                    // Set mass
                    double booster1Mass = calc.getMass() + motorMass + sustainerMotorMass;
                    setBooster1LaunchWt(booster1Mass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // Set CG
                    totalCG = calc.getCM().x; // = sustainer + booster 1 CG with no sustainer & booster & motors
                    totalCG = addMotorCGToStageCG(totalCG, calc.getMass(), sustainerMount, sustainerMotor, fcid);
                    totalCG = addMotorCGToStageCG(totalCG, calc.getMass() + sustainerMotorMass, mount, motor, fcid);
                    setBooster1CG(totalCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    // Set ignition delay
                    ignitionDelay = motorConfig != null ? motorConfig.getIgnitionDelay() : 0;
                    setBooster1IgnitionDelay(ignitionDelay);

                    // Set separation delay
                    separationDelay = separationConfig != null ? separationConfig.getSeparationDelay() : 0;
                    setBooster1SeparationDelay(separationDelay); // TODO: this could be handled a bit better (look at
                                                                 // separation delay, upper stage ignition event etc.)

                    setIncludeBooster1(mount != null && mount.isMotorMount());

                    break;
                // Booster 2
                case 2:
                    setBooster2Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, warnings));

                    // Calculate mass & CG of sustainer + booster 1 + booster 2 combined
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    calc = MassCalculator.calculateStructure(CGCalcConfig);

                    // Get booster1 motor mass
                    double booster1MotorMass = 0;
                    MotorMount booster1Mount = mounts.get((AxialStage) rocket.getChild(1));
                    Motor booster1Motor = null;
                    if (booster1Mount != null) {
                        MotorConfiguration booster1Config = booster1Mount.getMotorConfig(fcid);
                        booster1Motor = booster1Config.getMotor();
                        booster1MotorMass = booster1Motor != null ? booster1Motor.getLaunchMass() : 0;
                    }

                    // Set mass
                    double booster2Mass = calc.getMass() + motorMass + sustainerMotorMass + booster1MotorMass;
                    setBooster2LaunchWt(booster2Mass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // Set CG
                    totalCG = calc.getCM().x; // CG of sustainer + booster 1 + booster 2 combined, with no sustainer,
                                              // booster1 and booster2 motors!
                    totalCG = addMotorCGToStageCG(totalCG, calc.getMass(), sustainerMount, sustainerMotor, fcid);
                    totalCG = addMotorCGToStageCG(totalCG, calc.getMass() + sustainerMotorMass, booster1Mount,
                            booster1Motor, fcid);
                    totalCG = addMotorCGToStageCG(totalCG, calc.getMass() + sustainerMotorMass + booster1MotorMass,
                            mount, motor, fcid);
                    setBooster2CG(totalCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    // Set separation delay
                    separationDelay = separationConfig != null ? separationConfig.getSeparationDelay() : 0;
                    setBooster2Delay(separationDelay); // TODO: this could be handled a bit better (look at separation
                                                       // delay, upper stage ignition event etc.)

                    setIncludeBooster2(mount != null && mount.isMotorMount());

                    break;
                // Invalid
                default:
                    errors.add(String.format(trans.get("RASAeroExport.error25"), stageNr, simulationName));
            }
        }
    }

    /**
     * Combines the stage CG with the CG of the motor in that stage.
     * 
     * @param stageCG The CG of the stage
     * @param mount   The motor mount of the stage
     * @param motor   The motor in the stage
     * @return The combined CG
     */
    private double addMotorCGToStageCG(double stageCG, double stageMass, MotorMount mount, Motor motor,
            FlightConfigurationId fcid) {
        if (mount == null || !(motor instanceof ThrustCurveMotor)) {
            return stageCG;
        }

        // Calculate the motor CG
        double motorPositionXRel = mount.getMotorPosition(fcid).x; // Motor position relative to the mount
        double mountLocationX = mount.getLocations()[0].x;
        double motorLocationX = mountLocationX + motorPositionXRel;
        double motorCG = ((ThrustCurveMotor) motor).getCGPoints()[0].x + motorLocationX;

        double motorMass = motor.getLaunchMass();

        return (stageCG * stageMass + motorCG * motorMass) / (stageMass + motorMass);
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
