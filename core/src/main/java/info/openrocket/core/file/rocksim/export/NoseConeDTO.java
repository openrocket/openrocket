package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.NoseCone;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This class models a RockSim XML Element for a nose cone.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.NOSE_CONE)
public class NoseConeDTO extends AbstractTransitionDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.BASE_DIA)
    private double baseDia = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.SHOULDER_LEN)
    private double shoulderLen = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.SHOULDER_OD)
    private double shoulderOD = 0.0d;

    /**
     * Default constructor.
     */
    public NoseConeDTO() {
    }

    /**
     * Full copy constructor. Fully populates this instance with values taken from
     * the OR NoseCone instance.
     *
     * @param nc the OR nose cone
     */
    public NoseConeDTO(NoseCone nc) {
        super(nc);
        setBaseDia(nc.getAftRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setShoulderLen(nc.getAftShoulderLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setShoulderOD(nc.getAftShoulderRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
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
