package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.Bulkhead;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Conversion of an OR Bulkhead to an RockSim Bulkhead. Bulkheads are
 * represented as Rings in RockSim.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.RING)
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
