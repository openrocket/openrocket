package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Centering ring conversion from OR to RockSim.
 */
@XmlRootElement(name = RockSimCommonConstants.RING)
@XmlAccessorType(XmlAccessType.FIELD)
public class CenteringRingDTO extends BasePartDTO {

    @XmlTransient
    protected enum UsageCode {
        //UsageCode
        CenteringRing(0),
        Bulkhead(1),
        EngineBlock(2),
        Sleeve(3),
        TubeCoupler(4);

        int ordinal;

        UsageCode(int x) {
            ordinal = x;
        }
    }

    @XmlElement(name = RockSimCommonConstants.OD)
    private double od = 0d;
    @XmlElement(name = RockSimCommonConstants.ID)
    private double id = 0d;
    @XmlElement(name = RockSimCommonConstants.USAGE_CODE)
    private int usageCode = UsageCode.CenteringRing.ordinal;
    @XmlElement(name = RockSimCommonConstants.AUTO_SIZE)
    private int autoSize = 0;

    /**
     * Default Constructor.
     */
    public CenteringRingDTO() {
    }

    /**
     * Copy constructor.
     *
     * @param theORRadiusRing
     */
    public CenteringRingDTO(RadiusRingComponent theORRadiusRing) {
        super(theORRadiusRing);
        setId(theORRadiusRing.getInnerRadius()* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORRadiusRing.getOuterRadius()* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
    }

    /**
     * Copy constructor.
     *
     * @param theORThicknessRing
     */
    public CenteringRingDTO(ThicknessRingComponent theORThicknessRing) {
        super(theORThicknessRing);
        setId(theORThicknessRing.getInnerRadius()* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORThicknessRing.getOuterRadius()* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
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

    public int getUsageCode() {
        return usageCode;
    }

    public void setUsageCode(int theUsageCode) {
        usageCode = theUsageCode;
    }

    public void setUsageCode(UsageCode theUsageCode) {
        usageCode = theUsageCode.ordinal;
    }

    public int getAutoSize() {
        return autoSize;
    }

    public void setAutoSize(int theAutoSize) {
        autoSize = theAutoSize;
    }

}
