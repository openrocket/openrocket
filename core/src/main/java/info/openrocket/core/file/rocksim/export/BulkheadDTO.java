package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.Bulkhead;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Conversion of an OR Bulkhead to an RockSim Bulkhead. Bulkheads are
 * represented as Rings in RockSim.
 */
@XmlRootElement(name = RockSimCommonConstants.RING)
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkheadDTO extends CenteringRingDTO {

    /**
     * Constructor.
     *
     * @param theORBulkhead the OR bulkhead
     */
    public BulkheadDTO(Bulkhead theORBulkhead) {
        super(theORBulkhead);
        setUsageCode(CenteringRingDTO.UsageCode.Bulkhead);
    }
}
