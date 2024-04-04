package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.position.AnglePositionable;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
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
    @XmlElement(name = RockSimCommonConstants.DETACHABLE)
    private int detachable = 0;       // This pod can be ejected during simulations (0 = false, 1 = true)
    @XmlElement(name = RockSimCommonConstants.REMOVED)
    private int ejected = 0;          // Mark this pod as ejected (0 = false, 1 = true)
    @XmlElementWrapper(name = RockSimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.NOSE_CONE, type = NoseConeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.TRANSITION, type = TransitionDTO.class)})
    List<BasePartDTO> attachedParts = new ArrayList<>();

    /**
     * Default constructor.
     */
    protected PodSetDTO() {
    }

    /**
     * Generate a set of PodSetDTOs from the given OR PodSet.
     * RockSim only allows single-instance PodSets, so we need to generate a set of them.
     * @param theORPodSet the OR PodSet
     * @return the set of PodSetDTOs
     */
    public static PodSetDTO[] generatePodSetDTOs(ComponentAssembly theORPodSet) {
        PodSetDTO[] set = new PodSetDTO[theORPodSet.getInstanceCount()];
        int i = 0;
        for (RocketComponent podInstance : theORPodSet.splitInstances()) {
            set[i] = new PodSetDTO((PodSet) podInstance);
            i++;
        }
        return set;
    }

    /**
     * Copy constructor.  Fully populates this instance with values taken from the OR PodSet.
     * This constructor should not be called directly.  Instead, use {@link #generatePodSetDTOs}.
     * @param theORPodSet the single-instance OR PodSet
     */
    protected PodSetDTO(ComponentAssembly theORPodSet) {
        super(theORPodSet);
        // OR should always override the radial angle and distance
        setAutoCalcRadialDistance(false);
        setAutoCalcRadialAngle(false);
        setDetachable(0);
        setEjected(0);
        final double angleOffset = theORPodSet.getAngleOffset();
        setRadialAngle(angleOffset);
        setRadialLoc(theORPodSet.getRadiusMethod().getRadius(
                theORPodSet.getParent(), theORPodSet,
                theORPodSet.getRadiusOffset()) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setXb(theORPodSet.getAxialOffset(AxialMethod.TOP) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        // Children of a PodSet in RockSim do not use angles relative to their PodSet parent, but instead use absolute angle.
        // Therefore, we must apply those angles to the children of the PodSet.
        addAngleOffsetToChildren(theORPodSet, angleOffset);

        for (RocketComponent child : theORPodSet.getChildren()) {
            if (child instanceof BodyTube) {
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

    private void addAngleOffsetToChildren(RocketComponent component, double angleOffset) {
        for (RocketComponent child : component.getChildren()) {
            if (child instanceof AnglePositionable anglePositionable) {
                anglePositionable.setAngleOffset(anglePositionable.getAngleOffset() + angleOffset);
            }
            // No need to add an offset to the children of a component assembly. When the component assembly is
            // converted to a PodSetDTO, its angle offset will be applied to the children (and that angle offset already
            // includes the angle offset of the parent).
            if (!(child instanceof ComponentAssembly)) {
                addAngleOffsetToChildren(child, angleOffset);
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


    public int getDetachable() {
        return detachable;
    }

    public void setDetachable(int detachable) {
        this.detachable = detachable;
    }

    public int getEjected() {
        return ejected;
    }

    public void setEjected(int ejected) {
        this.ejected = ejected;
    }
}
