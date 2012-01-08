package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.rocketcomponent.Bulkhead;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "Ring")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkheadDTO extends CenteringRingDTO {
    public BulkheadDTO(Bulkhead bh) {
        super(bh);
        setUsageCode(CenteringRingDTO.UsageCode.Bulkhead);
    }
}
