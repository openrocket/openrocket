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
import info.openrocket.core.rocketcomponent.FinSet;
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
		return marshalToRockSim(doc, new WarningSet());
	}

	/**
	 * This method marshals an OpenRocketDocument (OR design) to Rocksim-compliant XML.
	 *
	 * @param doc      the OR design
	 * @param warnings the warning set to which features that RockSim cannot represent are added
	 * @return Rocksim-compliant XML
	 */
	public String marshalToRockSim(OpenRocketDocument doc, WarningSet warnings) {
		
		try {
			JAXBContext binder = JAXBContext.newInstance(RockSimDocumentDTO.class);
			Marshaller marshaller = binder.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			
			marshaller.marshal(toRockSimDocumentDTO(doc, warnings), sw);
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
		String payload = marshalToRockSim(doc, warnings);
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
	 * @param doc      the OR design
	 * @param warnings the warning set to which features that RockSim cannot represent are added
	 * @return a corresponding Rocksim representation
	 */
	private RockSimDocumentDTO toRockSimDocumentDTO(OpenRocketDocument doc, WarningSet warnings) {
		Rocket exportRocket = doc.getRocket().copyWithOriginalID();
		// Component serials are references shared by the design, motors, and
		// deployment events.  Serialize their allocation so separate exports cannot
		// interleave the static sequence used by the existing DTO hierarchy.
		synchronized (BasePartDTO.class) {
			BasePartDTO.resetCurrentSerialNumber();
			try {
				RockSimDocumentDTO result = new RockSimDocumentDTO();
				RockSimExportContext context = new RockSimExportContext(warnings);
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

		int stageCount = Math.min(axialStages.size(), 3);
		for (int i = 3; i < axialStages.size(); i++) {
			RockSimExportContext.warn(context, "RockSimExport.warning.stage", axialStages.get(i).getName());
		}
		if (stageCount == 3) {
			result.setStage321CG(cg);
		} else if (stageCount == 2) {
			result.setStage32CG(cg);
		} else {
			result.setStage3CG(cg);
		}

		result.setName(rocket.getName());
		result.setComments(rocket.getComment());
		result.setDesigner(rocket.getDesigner());
		result.setRevisions(rocket.getRevision());
		// RockSim's fixed Cd is the Cd of the whole design, so only a single overridden stage can be exported as such
		if (stageCount == 1 && axialStages.get(0).isCDOverridden()
				&& axialStages.get(0).isSubcomponentsOverriddenCD()) {
			result.setFixedCD(axialStages.get(0).getOverrideCD());
		}
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
		setStageOverrides(rocket, axialStages, result);
		if (!supportsBarrowmanEquations(rocket)) {
			result.setCPMethod(RocketDesignDTO.CP_METHOD_ROCKSIM);
		}
		// Record the final component serial number in the RockSim design.
		result.setLastSerialNumber(BasePartDTO.getCurrentSerialNumber());

		return result;
	}

	/**
	 * RockSim's stage overrides are all-or-nothing: when UseKnownMass is set, RockSim flies each stack of stages with
	 * the sum of the stage masses and the CG of that stack (sustainer alone, sustainer and first booster, all
	 * stages), measured from the nose tip. So when any stage mass or CG is overridden, the mass of every stage and
	 * the CG of every stack are exported.
	 */
	private void setStageOverrides(Rocket rocket, List<AxialStage> axialStages, RocketDesignDTO design) {
		int stageCount = Math.min(axialStages.size(), 3);
		boolean overridden = false;
		for (int i = 0; i < stageCount; i++) {
			overridden |= axialStages.get(i).isMassOverridden() || axialStages.get(i).isCGOverridden();
		}
		if (!overridden) {
			return;
		}

		design.setUseKnownMass(1);
		FlightConfiguration stack = new FlightConfiguration(rocket);
		stack.clearAllStages();
		for (int i = 0; i < stageCount; i++) {
			int stageNumber = axialStages.get(i).getStageNumber();

			FlightConfiguration single = new FlightConfiguration(rocket);
			single.clearAllStages();
			single._setStageActive(stageNumber, true);
			double mass = MassCalculator.calculateStructure(single).getMass()
					* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;

			stack._setStageActive(stageNumber, true);
			double cg = MassCalculator.calculateStructure(stack).getCM().getX()
					* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;

			switch (i) {
				case 0:
					design.setStage3Mass(mass);
					design.setStage3CG(cg);
					break;
				case 1:
					design.setStage2Mass(mass);
					design.setStage32CG(cg);
					break;
				default:
					design.setStage1Mass(mass);
					design.setStage321CG(cg);
					break;
			}
		}
	}

	/**
	 * RockSim only supports the Barrowman equations for fin sets with 3, 4 or 6 fins
	 * (CRocket::SupportsBarrowmanEquations in RockSim 11); other designs need RockSim's own stability equations.
	 */
	private static boolean supportsBarrowmanEquations(Rocket rocket) {
		for (RocketComponent component : rocket) {
			if (component instanceof FinSet) {
				int finCount = ((FinSet) component).getFinCount();
				if (finCount != 3 && finCount != 4 && finCount != 6) {
					return false;
				}
			}
		}
		return true;
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
