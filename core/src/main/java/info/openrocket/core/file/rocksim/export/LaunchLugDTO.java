package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.LaunchLug;

/**
 * This class models an XML element for a Rocksim LaunchLug.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.LAUNCH_LUG)
public class LaunchLugDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.OD)
    private double od = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.ID)
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
