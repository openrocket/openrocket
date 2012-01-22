package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Centering ring conversion from OR to Rocksim.
 */
@XmlRootElement(name = RocksimCommonConstants.RING)
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

    @XmlElement(name = RocksimCommonConstants.OD)
    private double od = 0d;
    @XmlElement(name = RocksimCommonConstants.ID)
    private double id = 0d;
    @XmlElement(name = RocksimCommonConstants.USAGE_CODE)
    private int usageCode = UsageCode.CenteringRing.ordinal;
    @XmlElement(name = RocksimCommonConstants.AUTO_SIZE)
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
        setId(theORRadiusRing.getInnerRadius()* RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORRadiusRing.getOuterRadius()* RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
    }

    /**
     * Copy constructor.
     *
     * @param theORThicknessRing
     */
    public CenteringRingDTO(ThicknessRingComponent theORThicknessRing) {
        super(theORThicknessRing);
        setId(theORThicknessRing.getInnerRadius()* RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORThicknessRing.getOuterRadius()* RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
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
