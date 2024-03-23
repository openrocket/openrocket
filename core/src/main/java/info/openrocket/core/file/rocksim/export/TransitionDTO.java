package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.Transition;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class models a transition XML element in Rocksim file format.
 */
@XmlRootElement(name = RockSimCommonConstants.TRANSITION)
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends AbstractTransitionDTO {

    @XmlElement(name = RockSimCommonConstants.FRONT_SHOULDER_LEN)
    private double frontShoulderLen = 0d;
    @XmlElement(name = RockSimCommonConstants.REAR_SHOULDER_LEN)
    private double rearShoulderLen = 0d;
    @XmlElement(name = RockSimCommonConstants.FRONT_SHOULDER_DIA)
    private double frontShoulderDia = 0d;
    @XmlElement(name = RockSimCommonConstants.REAR_SHOULDER_DIA)
    private double rearShoulderDia = 0d;
    @XmlElement(name = RockSimCommonConstants.FRONT_DIA)
    private double frontDia = 0d;
    @XmlElement(name = RockSimCommonConstants.REAR_DIA)
    private double rearDia = 0d;

    /**
     * Default constructor.
     */
    public TransitionDTO() {
    }

    /**
     * Copy constructor. This TransitionDTO instance will be as equivalent as
     * possible to the OR <code>tran</code>
     * once the constructor returns. No further modification (invoking setters) is
     * necessary.
     *
     * @param theORTransition the OR transition
     */
    public TransitionDTO(Transition theORTransition) {
        super(theORTransition);
        setFrontDia(theORTransition.getForeRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearDia(theORTransition.getAftRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderDia(
                theORTransition.getForeShoulderRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderLen(
                theORTransition.getForeShoulderLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setRearShoulderDia(
                theORTransition.getAftShoulderRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearShoulderLen(
                theORTransition.getAftShoulderLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

    }

    public double getFrontShoulderLen() {
        return frontShoulderLen;
    }

    public void setFrontShoulderLen(double theFrontShoulderLen) {
        frontShoulderLen = theFrontShoulderLen;
    }

    public double getRearShoulderLen() {
        return rearShoulderLen;
    }

    public void setRearShoulderLen(double theRearShoulderLen) {
        rearShoulderLen = theRearShoulderLen;
    }

    public double getFrontShoulderDia() {
        return frontShoulderDia;
    }

    public void setFrontShoulderDia(double theFrontShoulderDia) {
        frontShoulderDia = theFrontShoulderDia;
    }

    public double getRearShoulderDia() {
        return rearShoulderDia;
    }

    public void setRearShoulderDia(double theRearShoulderDia) {
        rearShoulderDia = theRearShoulderDia;
    }

    public double getFrontDia() {
        return frontDia;
    }

    public void setFrontDia(double theFrontDia) {
        frontDia = theFrontDia;
    }

    public double getRearDia() {
        return rearDia;
    }

    public void setRearDia(double theRearDia) {
        rearDia = theRearDia;
    }
}
