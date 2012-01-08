package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.LaunchLug;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "LaunchLug")
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchLugDTO extends BasePartDTO {

    @XmlElement(name = "OD")
    private double od = 0d;
    @XmlElement(name = "ID")
    private double id = 0d;

    public LaunchLugDTO() {
    }

    public LaunchLugDTO(LaunchLug ec) {
        super(ec);
        setId(ec.getInnerRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(ec.getOuterRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRadialAngle(ec.getRadialDirection());
    }

    public double getOd() {
        return od;
    }

    public void setOd(double theOd) {
        od = theOd;
    }

    public double getId() {
        return id;
    }

    public void setId(double theId) {
        id = theId;
    }
}
