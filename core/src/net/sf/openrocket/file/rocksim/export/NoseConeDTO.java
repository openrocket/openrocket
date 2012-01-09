package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.NoseCone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "NoseCone")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends AbstractTransitionDTO {


    @XmlElement(name = "BaseDia")
    private double baseDia = 0d;
    @XmlElement(name = "ShoulderLen")
    private double shoulderLen = 0d;
    @XmlElement(name = "ShoulderOD")
    private double shoulderOD = 0d;

    public NoseConeDTO() {
    }

    public NoseConeDTO(NoseCone nc) {
        super(nc);
        setBaseDia(nc.getAftRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setShoulderLen(nc.getAftShoulderLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setShoulderOD(nc.getAftShoulderRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
    }

    public double getBaseDia() {
        return baseDia;
    }

    public void setBaseDia(double theBaseDia) {
        baseDia = theBaseDia;
    }

    public double getShoulderLen() {
        return shoulderLen;
    }

    public void setShoulderLen(double theShoulderLen) {
        shoulderLen = theShoulderLen;
    }

    public double getShoulderOD() {
        return shoulderOD;
    }

    public void setShoulderOD(double theShoulderOD) {
        shoulderOD = theShoulderOD;
    }
}
