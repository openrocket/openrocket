package info.openrocket.core.file.rocksim.export;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.LaunchLug;

/**
 * This class models an XML element for a Rocksim LaunchLug.
 */
@XmlRootElement(name = RockSimCommonConstants.LAUNCH_LUG)
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchLugDTO extends BasePartDTO {

    @XmlElement(name = RockSimCommonConstants.OD)
    private double od = 0.0d;
    @XmlElement(name = RockSimCommonConstants.ID)
    private double id = 0.0d;

    /**
     * Default constructor.
     */
    public LaunchLugDTO() {
    }

    /**
     * Copy constructor. Fully populates this instance with values taken from the OR
     * LaunchLug.
     *
     * @param theORLaunchLug
     */
    public LaunchLugDTO(LaunchLug theORLaunchLug) {
        super(theORLaunchLug);
        setId(theORLaunchLug.getInnerRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORLaunchLug.getOuterRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRadialAngle(theORLaunchLug.getAngleOffset());
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
