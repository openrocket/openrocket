package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 */
@XmlRootElement(name = "Ring")
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

    @XmlElement(name = "OD")
    private double od = 0d;
    @XmlElement(name = "ID")
    private double id = 0d;
    @XmlElement(name = "UsageCode")
    private int usageCode = UsageCode.CenteringRing.ordinal;
    @XmlElement(name = "AutoSize")
    private int autoSize = 0;

    public CenteringRingDTO() {

    }
    public CenteringRingDTO(RadiusRingComponent cr) {
        super(cr);
        setId(cr.getInnerRadius()* RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(cr.getOuterRadius()* RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
    }

    public CenteringRingDTO(ThicknessRingComponent trc) {
        super(trc);
        setId(trc.getInnerRadius()* RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(trc.getOuterRadius()* RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
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
