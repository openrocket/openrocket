package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.LaunchLug;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class models an XML element for a Rocksim LaunchLug.
 */
@XmlRootElement(name = RocksimCommonConstants.LAUNCH_LUG)
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchLugDTO extends BasePartDTO {

    @XmlElement(name = RocksimCommonConstants.OD)
    private double od = 0d;
    @XmlElement(name = RocksimCommonConstants.ID)
    private double id = 0d;

    /**
     * Default constructor.
     */
    public LaunchLugDTO() {
    }

    /**
     * Copy constructor.  Fully populates this instance with values taken from the OR LaunchLug.
     *
     * @param theORLaunchLug
     */
    public LaunchLugDTO(LaunchLug theORLaunchLug) {
        super(theORLaunchLug);
        setId(theORLaunchLug.getInnerRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORLaunchLug.getOuterRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRadialAngle(theORLaunchLug.getRadialDirection());
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
