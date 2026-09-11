package info.openrocket.core.file.rocksim.export;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Container for the simulations exported at the root of a RockSim document.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationResultsListDTO {

	@XmlElement(name = RockSimCommonConstants.SIMULATION_RESULTS)
	private final List<SimulationResultsDTO> simulations = new ArrayList<>();

	/**
	 * Constructor required by JAXB.
	 */
	public SimulationResultsListDTO() {
	}

	/**
	 * Convert every OpenRocket simulation in document order.
	 *
	 * @param document the source OpenRocket document
	 * @param context  the component-to-mount-serial mapping for this export
	 */
	public SimulationResultsListDTO(OpenRocketDocument document, RockSimExportContext context) {
		for (Simulation simulation : document.getSimulations()) {
			simulations.add(new SimulationResultsDTO(simulation, context));
		}
	}

	public List<SimulationResultsDTO> getSimulations() {
		return simulations;
	}
}
