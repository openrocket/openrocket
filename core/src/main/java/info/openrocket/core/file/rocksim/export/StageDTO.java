package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.ArrayList;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

/**
 * Placeholder for a Rocksim Stage.
 */
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = StageDTODeserializer.class)
public class StageDTO {

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(contentUsing = PartListSerializer.class)
    @com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper(useWrapping = false)
    private final List<BasePartDTO> externalPart = new ArrayList<>();

    /**
     * Default constructor.
     */
    public StageDTO() {
    }

    /**
     * Copy constructor.
     *
     * @param theORStage  the OR stage
     * @param design      the encompassing container DTO
     * @param stageNumber the stage number (3 is always at the top, even if it's the
     *                    only one)
     */
    public StageDTO(AxialStage theORStage, RocketDesignDTO design, int stageNumber) {

        if (stageNumber == 3) {
            if (theORStage.isMassOverridden()) {
                design.setStage3Mass(theORStage.getMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage3CG(theORStage.getOverrideCGX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        if (stageNumber == 2) {
            if (theORStage.isMassOverridden()) {
                design.setStage2Mass(theORStage.getMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage2CGAlone(
                        theORStage.getOverrideCGX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        if (stageNumber == 1) {
            if (theORStage.isMassOverridden()) {
                design.setStage1Mass(theORStage.getMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage1CGAlone(
                        theORStage.getOverrideCGX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        List<RocketComponent> children = theORStage.getChildren();
		for (RocketComponent rocketComponents : children) {
			if (rocketComponents instanceof NoseCone) {
				addExternalPart(toNoseConeDTO((NoseCone) rocketComponents));
			} else if (rocketComponents instanceof BodyTube) {
				addExternalPart(toBodyTubeDTO((BodyTube) rocketComponents));
			} else if (rocketComponents instanceof Transition) {
				addExternalPart(toTransitionDTO((Transition) rocketComponents));
			}
		}
    }

    public List<BasePartDTO> getExternalPart() {
        return externalPart;
    }

    public void addExternalPart(BasePartDTO theExternalPartDTO) {
        externalPart.add(theExternalPartDTO);
    }

    private AbstractTransitionDTO toNoseConeDTO(NoseCone nc) {
        if (nc.isFlipped()) {
            return new TransitionDTO(nc);
        } else {
            return new NoseConeDTO(nc);
        }
    }

    private BodyTubeDTO toBodyTubeDTO(BodyTube bt) {
        return new BodyTubeDTO(bt);
    }

    private TransitionDTO toTransitionDTO(Transition tran) {
        return new TransitionDTO(tran);
    }
}
