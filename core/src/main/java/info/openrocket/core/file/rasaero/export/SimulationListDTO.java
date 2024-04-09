package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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

            // If at this point, we still don't have a mount, there is probably a mount
            // without a motor.
            // In that case, add a null mount, so that mass/CG export happens.
            if (!mounts.containsKey(stage)) {
                mounts.put(stage, null);
            }
        }

        // Load all RASAero motors
        List<ThrustCurveMotor> motors = RASAeroMotorsLoader.loadAllRASAeroMotors(warnings);

        // Add all the simulations
        for (Simulation simulation : document.getSimulations()) {
            addSimulation(new SimulationDTO(rocket, simulation, mounts, motors, warnings, errors));
        }

        // If there are no simulations, add a default simulation (to have the mass/CG
        // export)
        if (document.getSimulations().size() == 0) {
            addSimulation(new SimulationDTO(rocket, null, mounts, motors, warnings, errors));
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
