package info.openrocket.core.file.rocksim.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import info.openrocket.core.util.MemoryManagement;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.RocketSaver;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;

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
			log.error("Could not marshall a design to RockSim format. " + e.getMessage());
		}
		
		return null;
	}
	
	@Override
	public void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options, WarningSet warnings, ErrorSet errors) throws IOException {
		log.info("Saving .rkt file");

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, StandardCharsets.UTF_8));
		writer.write(marshalToRockSim(doc));
		writer.flush();
	}

	@Override
	public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
		return marshalToRockSim(doc).length();
	}

	/**
	 * Root conversion method. It iterates over all subcomponents.
	 *
	 * @param doc the OR design
	 * @return a corresponding Rocksim representation
	 */
	private RockSimDocumentDTO toRockSimDocumentDTO(OpenRocketDocument doc) {
		RockSimDocumentDTO rsd = new RockSimDocumentDTO();

		rsd.setDesign(toRockSimDesignDTO(doc.getRocket()));

		return rsd;
	}

	private RockSimDesignDTO toRockSimDesignDTO(Rocket rocket) {
		RockSimDesignDTO result = new RockSimDesignDTO();
		result.setDesign(toRocketDesignDTO(rocket));
		return result;
	}

	private RocketDesignDTO toRocketDesignDTO(Rocket rocket) {
		rocket = rocket.copyWithOriginalID();		// Make sure we don't change the original design.
		RocketDesignDTO result = new RocketDesignDTO();

		final FlightConfiguration configuration = rocket.getEmptyConfiguration();
		final RigidBody spentData = MassCalculator.calculateStructure(configuration);
		final double cg = spentData.cm.x * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;

		int stageCount = rocket.getChildCount();
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
			result.setStage3(toStageDTO(rocket.getChild(0).getStage(), result, 3));
		}
		if (stageCount > 1) {
			result.setStage2(toStageDTO(rocket.getChild(1).getStage(), result, 2));
		}
		if (stageCount > 2) {
			result.setStage1(toStageDTO(rocket.getChild(2).getStage(), result, 1));
		}
		// Set the last serial number element and reset it.
		result.setLastSerialNumber(BasePartDTO.getCurrentSerialNumber());
		BasePartDTO.resetCurrentSerialNumber();

		// Clean up
		MemoryManagement.collectable(rocket);

		return result;
	}

	private StageDTO toStageDTO(AxialStage stage, RocketDesignDTO designDTO, int stageNumber) {
		return new StageDTO(stage, designDTO, stageNumber);
	}

}
