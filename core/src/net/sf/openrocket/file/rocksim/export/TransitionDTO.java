package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.Transition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RocksimCommonConstants.TRANSITION)
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends AbstractTransitionDTO {


    @XmlElement(name = RocksimCommonConstants.FRONT_SHOULDER_LEN)
    private double frontShoulderLen = 0d;
    @XmlElement(name = RocksimCommonConstants.REAR_SHOULDER_LEN)
    private double rearShoulderLen = 0d;
    @XmlElement(name = RocksimCommonConstants.FRONT_SHOULDER_DIA)
    private double frontShoulderDia = 0d;
    @XmlElement(name = RocksimCommonConstants.REAR_SHOULDER_DIA)
    private double rearShoulderDia = 0d;
    @XmlElement(name = RocksimCommonConstants.FRONT_DIA)
    private double frontDia = 0d;
    @XmlElement(name = RocksimCommonConstants.REAR_DIA)
    private double rearDia = 0d;

    public TransitionDTO() {
    }

    public TransitionDTO(Transition tran) {
        super(tran);
        setFrontDia(tran.getForeRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearDia(tran.getAftRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderDia(tran.getForeShoulderRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderLen(tran.getForeShoulderLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setRearShoulderDia(tran.getAftShoulderRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearShoulderLen(tran.getAftShoulderLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);


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
