package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.RadiusRingComponent;
import info.openrocket.core.rocketcomponent.ThicknessRingComponent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Centering ring conversion from OR to RockSim.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.RING)
public class CenteringRingDTO extends BasePartDTO {

    protected enum UsageCode {
        // UsageCode
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

    @JacksonXmlProperty(localName = RockSimCommonConstants.OD)
    private double od = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.ID)
    private double id = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.USAGE_CODE)
    private int usageCode = UsageCode.CenteringRing.ordinal;
    @JacksonXmlProperty(localName = RockSimCommonConstants.AUTO_SIZE)
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
        setId(theORRadiusRing.getInnerRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORRadiusRing.getOuterRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
    }

    /**
     * Copy constructor.
     *
     * @param theORThicknessRing
     */
    public CenteringRingDTO(ThicknessRingComponent theORThicknessRing) {
        super(theORThicknessRing);
        setId(theORThicknessRing.getInnerRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORThicknessRing.getOuterRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
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
