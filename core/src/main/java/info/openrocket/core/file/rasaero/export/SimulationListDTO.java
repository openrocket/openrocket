package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader.RASAeroMotor;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.SIMULATION_LIST)
public class SimulationListDTO {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = RASAeroCommonConstants.SIMULATION)
    private final List<SimulationDTO> simulations = new LinkedList<>();

    /**
     * We need a default, no-args constructor.
     */
    public SimulationListDTO() {
    }

    public SimulationListDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        Map<AxialStage, MotorMount> mounts = new HashMap<>();
        Rocket rocket = document.getRocket();

        // RASAero supports one motor per stage, so retain the first populated mount
        // found in each core stage.
        for (RocketComponent child : rocket.getChildren()) {
            AxialStage stage = (AxialStage) child;
            mounts.put(stage, findMotorMount(stage));
        }

        // Load all RASAero motors
        List<RASAeroMotor> motors = RASAeroMotorsLoader.loadAllRASAeroMotors(warnings);

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

    /**
     * Finds the first populated motor mount anywhere within a core stage.
     * Structural inner tubes may contain another inner tube that acts as the actual
     * motor mount, so checking only direct body-tube children is insufficient.
     *
     * @param stage the core stage whose component subtree is searched
     * @return the first populated mount in the stage, or {@code null} when absent
     */
    static MotorMount findMotorMount(AxialStage stage) {
        for (RocketComponent component : stage.getAllChildren()) {
            if (component instanceof MotorMount) {
                MotorMount mount = (MotorMount) component;
                // Exclude mounts belonging to nested parallel stages.
                if (component.getStage() == stage && mount.hasMotor()) {
                    return mount;
                }
            }
        }
        return null;
    }

    public List<SimulationDTO> getSimulations() {
        return simulations;
    }

    public void addSimulation(SimulationDTO simulation) {
        simulations.add(simulation);
    }

}
