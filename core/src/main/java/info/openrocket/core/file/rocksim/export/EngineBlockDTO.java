package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.rocketcomponent.EngineBlock;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Models a Rocksim XML Element for an EngineBlock. EngineBlocks in Rocksim are
 * treated as rings with a special
 * usage code.
 */
@JacksonXmlRootElement(localName = "Ring")
public class EngineBlockDTO extends CenteringRingDTO {

    /**
     * Copy constructor.
     *
     * @param theOREngineBlock
     */
    public EngineBlockDTO(EngineBlock theOREngineBlock) {
        super(theOREngineBlock);
        setUsageCode(UsageCode.EngineBlock);
    }
}
