package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the XML element for a RockSim pod.
 */
@XmlRootElement(name = RockSimCommonConstants.EXTERNAL_POD)
@XmlAccessorType(XmlAccessType.FIELD)
public class PodSetDTO extends BasePartDTO implements AttachableParts {
    @XmlElement(name = RockSimCommonConstants.AUTO_CALC_RADIAL_DISTANCE)
    private int autoCalcRadialDistance = 0;
    @XmlElement(name = RockSimCommonConstants.AUTO_CALC_RADIAL_ANGLE)
    private int autoCalcRadialAngle = 0;
    @XmlElementWrapper(name = RockSimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.NOSE_CONE, type = NoseConeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.TRANSITION, type = TransitionDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.EXTERNAL_POD, type = PodSetDTO.class)})
    List<BasePartDTO> attachedParts = new ArrayList<BasePartDTO>();

    /**
     * Default constructor.
     */
    public PodSetDTO() {
    }

    /**
     * Copy constructor.  Fully populates this instance with values taken from the OR PodSet.
     *
     * @param theORPodSet
     */
    public PodSetDTO(PodSet theORPodSet) {
        super(theORPodSet);
        // OR should always override the radial angle and distance
        setAutoCalcRadialDistance(false);
        setAutoCalcRadialAngle(false);
        setRadialAngle(theORPodSet.getAngleOffset());
        setRadialLoc(theORPodSet.getRadiusMethod().getRadius(
                theORPodSet.getParent(), theORPodSet,
                theORPodSet.getRadiusOffset()) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setXb(theORPodSet.getAxialOffset(AxialMethod.TOP) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        for (RocketComponent child : theORPodSet.getChildren()) {
            if (child instanceof PodSet) {
                addAttachedPart(new PodSetDTO((PodSet) child));
            } else if (child instanceof BodyTube) {
                addAttachedPart(new BodyTubeDTO((BodyTube) child));
            } else if (child instanceof NoseCone) {
                if (((NoseCone) child).isFlipped()) {
                    addAttachedPart(new TransitionDTO((NoseCone) child));
                } else {
                    addAttachedPart(new NoseConeDTO((NoseCone) child));
                }
            } else if (child instanceof Transition) {
                addAttachedPart(new TransitionDTO((Transition) child));
            }
        }
    }

    public int getAutoCalcRadialDistance() {
        return autoCalcRadialDistance;
    }

    public void setAutoCalcRadialDistance(boolean motorMount) {
        if (motorMount) {
            this.autoCalcRadialDistance = 1;
        } else {
            this.autoCalcRadialDistance = 0;
        }
    }

    public int getAutoCalcRadialAngle() {
        return autoCalcRadialAngle;
    }

    public void setAutoCalcRadialAngle(boolean motorMount) {
        if (motorMount) {
            this.autoCalcRadialAngle = 1;
        } else {
            this.autoCalcRadialAngle = 0;
        }
    }

    @Override
    public void addAttachedPart(BasePartDTO part) {
        if (!attachedParts.contains(part)) {
            attachedParts.add(part);
        }
    }

    @Override
    public void removeAttachedPart(BasePartDTO part) {
        attachedParts.remove(part);
    }
}
