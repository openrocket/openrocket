package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.rocketcomponent.NoseCone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class models a RockSim XML Element for a nose cone.
 */
@XmlRootElement(name = RockSimCommonConstants.NOSE_CONE)
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends AbstractTransitionDTO {


    @XmlElement(name = RockSimCommonConstants.BASE_DIA)
    private double baseDia = 0d;
    @XmlElement(name = RockSimCommonConstants.SHOULDER_LEN)
    private double shoulderLen = 0d;
    @XmlElement(name = RockSimCommonConstants.SHOULDER_OD)
    private double shoulderOD = 0d;

    /**
     * Default constructor.
     */
    public NoseConeDTO() {
    }

    /**
     * Full copy constructor.  Fully populates this instance with values taken from the OR NoseCone instance.
     *
     * @param nc  the OR nose cone
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
