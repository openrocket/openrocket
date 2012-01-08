package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.Transition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "Transition")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends AbstractTransitionDTO {


    @XmlElement(name = "FrontShoulderLen")
    private double frontShoulderLen = 0d;
    @XmlElement(name = "RearShoulderLen")
    private double rearShoulderLen = 0d;
    @XmlElement(name = "FrontShoulderDia")
    private double frontShoulderDia = 0d;
    @XmlElement(name = "RearShoulderDia")
    private double rearShoulderDia = 0d;
    @XmlElement(name = "FrontDia")
    private double frontDia = 0d;
    @XmlElement(name = "RearDia")
    private double rearDia = 0d;

    public TransitionDTO() {
    }

    public TransitionDTO(Transition tran) {
        super(tran);
        setFrontDia(tran.getForeRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearDia(tran.getAftRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderDia(tran.getForeShoulderRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setFrontShoulderLen(tran.getForeShoulderLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setRearShoulderDia(tran.getAftShoulderRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRearShoulderLen(tran.getAftShoulderLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);


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
