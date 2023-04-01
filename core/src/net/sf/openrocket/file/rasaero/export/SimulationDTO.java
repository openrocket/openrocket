package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.rasaero.CustomBooleanAdapter;
import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.RigidBody;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

    /**
     * We need a default, no-args constructor.
     */
    public SimulationDTO() {
    }

    /**
     * RASAero Simulation object.
     * @param rocket the rocket
     * @param simulation the simulation to convert
     * @param mounts a map of stages and their corresponding motor mount (only 1 mount per stage allowed)
     * @param motors a list of RASAero motors
     * @param warnings a list to add export warnings to
     * @param errors a list to add export errors to
     */
    public SimulationDTO(Rocket rocket, Simulation simulation, Map<AxialStage, MotorMount> mounts, List<ThrustCurveMotor> motors,
                         WarningSet warnings, ErrorSet errors) {
        FlightConfigurationId fcid = simulation.getFlightConfigurationId();
        if (fcid == null) {
            warnings.add(String.format("Empty simulation '%s', ignoring.", simulation.getName()));
            return;
        }

        if (mounts.isEmpty()) {
            warnings.add(String.format("No motors found in simulation '%s', ignoring.", simulation.getName()));
            return;
        }

        // Get sustainer motor mass
        MotorMount sustainerMount = mounts.get((AxialStage) rocket.getChild(0));
        MotorConfiguration sustainerConfig = sustainerMount.getMotorConfig(fcid);
        Motor sustainerMotor = sustainerConfig.getMotor();
        double sustainerMotorMass = sustainerMotor != null ? sustainerMotor.getLaunchMass() : 0;

        for (Map.Entry<AxialStage, MotorMount> mountSet : mounts.entrySet()) {
            AxialStage stage = mountSet.getKey();
            MotorMount mount = mountSet.getValue();
            if (mount == null || stage == null) {
                continue;
            }

            // Get the motor info for this stage
            MotorConfiguration motorConfig = mount.getMotorConfig(fcid);
            Motor motor = motorConfig.getMotor();
            double motorMass = motor != null ? motor.getLaunchMass() : 0;
            StageSeparationConfiguration separationConfig = stage.getSeparationConfigurations().get(fcid);
            int stageNr = rocket.getChildPosition(stage);

            FlightConfiguration CGCalcConfig = new FlightConfiguration(rocket);
            switch (stageNr) {
                // Sustainer
                case 0:
                    setSustainerEngine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, motorConfig, warnings));
                    double sustainerMass = stage.getSectionMass() + motorMass;
                    setSustainerLaunchWt(sustainerMass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // Calculate CG of sustainer
                    CGCalcConfig.setOnlyStage(0);
                    double sustainerCG = MassCalculator.calculateStructure(CGCalcConfig).getCM().x;
                    setSustainerCG(sustainerCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    setSustainerIgnitionDelay(motorConfig.getIgnitionDelay());
                    break;
                // Booster 1
                case 1:
                    setBooster1Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, motorConfig, warnings));

                    // Aggregate CG of sustainer and booster 1
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    RigidBody calc = MassCalculator.calculateStructure(CGCalcConfig);

                    // Aggregate mass of sustainer and booster 1
                    double booster1Mass = calc.getMass() + motorMass + sustainerMotorMass;
                    setBooster1LaunchWt(booster1Mass * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // CG
                    double totalCG = calc.getCM().x;
                    setBooster1CG(totalCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    setBooster1IgnitionDelay(motorConfig.getIgnitionDelay());
                    setBooster1SeparationDelay(separationConfig.getSeparationDelay());      // TODO: this could be handled a bit better (look at separation delay, upper stage ignition event etc.)
                    setIncludeBooster1(mount.isMotorMount());
                    break;
                // Booster 2
                case 2:
                    setBooster2Engine(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, motor, motorConfig, warnings));

                    // Calculate the aggregated CG of the sustainer, booster and booster 2
                    CGCalcConfig.setOnlyStage(0);
                    for (int i = 1; i <= stage.getStageNumber(); i++) {
                        CGCalcConfig._setStageActive(i, true);
                    }
                    calc = MassCalculator.calculateStructure(CGCalcConfig);

                    // Get booster1 motor mass
                    MotorMount booster1Mount = mounts.get((AxialStage) rocket.getChild(1));
                    MotorConfiguration booster1Config = booster1Mount.getMotorConfig(fcid);
                    Motor booster1Motor = booster1Config.getMotor();
                    double booster1MotorMass = booster1Motor != null ? booster1Motor.getLaunchMass() : 0;

                    // Aggregate mass of sustainer, booster 1 and booster 2
                    double booster2Mass = calc.getMass() + motorMass + sustainerMotorMass + booster1MotorMass;
                    setBooster2LaunchWt(booster2Mass  * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_WEIGHT);

                    // CG
                    totalCG = calc.getCM().x;
                    setBooster2CG(totalCG * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                    setBooster2Delay(separationConfig.getSeparationDelay());      // TODO: this could be handled a bit better (look at separation delay, upper stage ignition event etc.)
                    setIncludeBooster2(mount.isMotorMount());
                    break;
                // Invalid
                default:
                    errors.add(String.format("Invalid stage number '%d' for simulation '%s'",
                            stageNr, simulation.getName()));
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
