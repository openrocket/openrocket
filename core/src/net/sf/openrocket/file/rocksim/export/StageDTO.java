package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import java.util.List;

/**
 * Placeholder for a Rocksim Stage.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StageDTO {

    @XmlElementRefs({
            @XmlElementRef(name = RocksimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.NOSE_CONE, type = NoseConeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.TRANSITION, type = TransitionDTO.class)
    })
    private List<BasePartDTO> externalPart = new ArrayList<BasePartDTO>();

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
     * @param stageNumber the stage number (3 is always at the top, even if it's the only one)
     */
    public StageDTO(Stage theORStage, RocketDesignDTO design, int stageNumber) {

        if (stageNumber == 3) {
            if (theORStage.isMassOverridden()) {
                design.setStage3Mass(theORStage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage3CG(theORStage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }
        
        if (stageNumber == 2) {
            if (theORStage.isMassOverridden()) {
                design.setStage2Mass(theORStage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage2CGAlone(theORStage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        if (stageNumber == 1) {
            if (theORStage.isMassOverridden()) {
                design.setStage1Mass(theORStage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (theORStage.isCGOverridden()) {
                design.setStage1CGAlone(theORStage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        List<RocketComponent> children = theORStage.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
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

    private NoseConeDTO toNoseConeDTO(NoseCone nc) {
        return new NoseConeDTO(nc);
    }

    private BodyTubeDTO toBodyTubeDTO(BodyTube bt) {
        return new BodyTubeDTO(bt);
    }

    private TransitionDTO toTransitionDTO(Transition tran) {
        return new TransitionDTO(tran);
    }
}
