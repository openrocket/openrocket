package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.Bulkhead;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Conversion of an OR Bulkhead to an Rocksim Bulkhead.  Bulkheads are represented as Rings in Rocksim.
 */
@XmlRootElement(name = RocksimCommonConstants.RING)
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkheadDTO extends CenteringRingDTO {

    /**
     * Constructor.
     *
     * @param bh bulkhead
     */
    public BulkheadDTO(Bulkhead bh) {
        super(bh);
        setUsageCode(CenteringRingDTO.UsageCode.Bulkhead);
    }
}
