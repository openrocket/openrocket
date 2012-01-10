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

    public StageDTO(Stage stage, RocketDesignDTO design, int stageNumber) {

        if (stageNumber == 3) {
            if (stage.isMassOverridden()) {
                design.setStage3Mass(stage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (stage.isCGOverridden()) {
                design.setStage3CG(stage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }
        
        if (stageNumber == 2) {
            if (stage.isMassOverridden()) {
                design.setStage2Mass(stage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (stage.isCGOverridden()) {
                design.setStage2CGAlone(stage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        if (stageNumber == 1) {
            if (stage.isMassOverridden()) {
                design.setStage1Mass(stage.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
                design.setUseKnownMass(1);
            }
            if (stage.isCGOverridden()) {
                design.setStage1CGAlone(stage.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        List<RocketComponent> children = stage.getChildren();
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
