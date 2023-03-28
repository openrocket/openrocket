package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.file.rasaero.importt.RASAeroMotorsLoader;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = RASAeroCommonConstants.SIMULATION_LIST)
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationListDTO {
    @XmlElement(name = RASAeroCommonConstants.SIMULATION)
    private final List<SimulationDTO> simulations = new LinkedList<>();

    /**
     * We need a default, no-args constructor.
     */
    public SimulationListDTO() {
    }

    public SimulationListDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        Map<AxialStage, MotorMount> mounts = new HashMap<>();
        Rocket rocket = document.getRocket();

        // Fetch all the motor mounts from the design
        for (RocketComponent child : rocket.getChildren()) {
            AxialStage stage = (AxialStage) child;
            if (mounts.containsKey(stage)) {
                continue;
            }
            for (RocketComponent stageChild : stage.getChildren()) {
                if (stageChild instanceof BodyTube) {
                    // First check if the body tube itself has a motor
                    if (((BodyTube) stageChild).hasMotor()) {
                        mounts.put(stage, (BodyTube) stageChild);
                        break;
                    }
                    // Then check if it has an inner tube with a motor
                    else {
                        boolean addedMount = false;
                        for (RocketComponent tubeChild : stageChild.getChildren()) {
                            if (tubeChild instanceof MotorMount && ((MotorMount) tubeChild).hasMotor()) {
                                mounts.put(stage, (MotorMount) tubeChild);
                                addedMount = true;
                                break;
                            }
                        }
                        if (addedMount) {
                            break;
                        }
                    }
                }
            }
        }

        // Load all RASAero motors
        List<ThrustCurveMotor> motors = RASAeroMotorsLoader.loadAllRASAeroMotors(warnings);

        for (Simulation simulation : document.getSimulations()) {
            addSimulation(new SimulationDTO(rocket, simulation, mounts, motors, warnings, errors));
        }

        motors.clear();
    }

    public List<SimulationDTO> getSimulations() {
        return simulations;
    }

    public void addSimulation(SimulationDTO simulation) {
        simulations.add(simulation);
    }

}
