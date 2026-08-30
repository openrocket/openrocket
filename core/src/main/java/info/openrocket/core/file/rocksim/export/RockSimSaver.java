package info.openrocket.core.file.rocksim.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.RocketSaver;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MemoryManagement;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for converting an OpenRocket design to a Rocksim design.
 */
public class RockSimSaver extends RocketSaver {
	
	/**
	 * The logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(RockSimSaver.class);
	
	/**
	 * This method marshals an OpenRocketDocument (OR design) to Rocksim-compliant XML.
	 *
	 * @param doc the OR design
	 * @return Rocksim-compliant XML
	 */
	public String marshalToRockSim(OpenRocketDocument doc) {
		
		try {
			JAXBContext binder = JAXBContext.newInstance(RockSimDocumentDTO.class);
			Marshaller marshaller = binder.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			
			marshaller.marshal(toRockSimDocumentDTO(doc), sw);
			return sw.toString();
		} catch (Exception e) {
			log.error("Could not marshal a design to RockSim format.", e);
		}
		
		return null;
	}
	
	@Override
	public void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options, WarningSet warnings, ErrorSet errors) throws IOException {
		log.info("Saving .rkt file");

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, StandardCharsets.UTF_8));
		String payload = marshalToRockSim(doc);
		if (payload == null) {
			// Surface the export failure to callers instead of failing later on a null write.
			String message = "Could not export RockSim file.";
			if (errors != null) {
				errors.add(message);
			}
			throw new BugException(message);
		}
		writer.write(payload);
		writer.flush();
	}

	@Override
	public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
		String payload = marshalToRockSim(doc);
		if (payload == null) {
			return 0;
		}
		return payload.length();
	}

	/**
	 * Root conversion method. It iterates over all subcomponents.
	 *
	 * @param doc the OR design
	 * @return a corresponding Rocksim representation
	 */
	private RockSimDocumentDTO toRockSimDocumentDTO(OpenRocketDocument doc) {
		Rocket exportRocket = doc.getRocket().copyWithOriginalID();
		// Component serials are references shared by the design, motors, and
		// deployment events.  Serialize their allocation so separate exports cannot
		// interleave the static sequence used by the existing DTO hierarchy.
		synchronized (BasePartDTO.class) {
			BasePartDTO.resetCurrentSerialNumber();
			try {
				RockSimDocumentDTO result = new RockSimDocumentDTO();
				RockSimExportContext context = new RockSimExportContext();
				result.setDesign(toRockSimDesignDTO(exportRocket, context));
				result.setSimulationResultsList(new SimulationResultsListDTO(doc, context));
				return result;
			} finally {
				BasePartDTO.resetCurrentSerialNumber();
				MemoryManagement.collectable(exportRocket);
			}
		}
	}

	private RockSimDesignDTO toRockSimDesignDTO(Rocket rocket, RockSimExportContext context) {
		RockSimDesignDTO result = new RockSimDesignDTO();
		result.setDesign(toRocketDesignDTO(rocket, context));
		return result;
	}

	private RocketDesignDTO toRocketDesignDTO(Rocket rocket, RockSimExportContext context) {
		RocketDesignDTO result = new RocketDesignDTO();
		List<AxialStage> axialStages = getAxialStages(rocket);

		final FlightConfiguration configuration = rocket.getEmptyConfiguration();
		final RigidBody spentData = MassCalculator.calculateStructure(configuration);
		final double cg = spentData.cm.getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;

		int stageCount = axialStages.size();
		if (stageCount == 3) {
			result.setStage321CG(cg);
		} else if (stageCount == 2) {
			result.setStage32CG(cg);
		} else {
			result.setStage3CG(cg);
		}

		result.setName(rocket.getName());
		result.setStageCount(stageCount);
		if (stageCount > 0) {
			result.setStage3(toStageDTO(axialStages.get(0), result, 3, context));
		}
		if (stageCount > 1) {
			result.setStage2(toStageDTO(axialStages.get(1), result, 2, context));
		}
		if (stageCount > 2) {
			result.setStage1(toStageDTO(axialStages.get(2), result, 1, context));
		}
		// Record the final component serial number in the RockSim design.
		result.setLastSerialNumber(BasePartDTO.getCurrentSerialNumber());

		return result;
	}

	private List<AxialStage> getAxialStages(Rocket rocket) {
		List<AxialStage> stages = new ArrayList<>();
		for (RocketComponent child : rocket.getChildren()) {
			if (child instanceof AxialStage) {
				stages.add((AxialStage) child);
			}
		}
		return stages;
	}

	private StageDTO toStageDTO(AxialStage stage, RocketDesignDTO designDTO, int stageNumber,
			RockSimExportContext context) {
		return new StageDTO(stage, designDTO, stageNumber, context);
	}

}
