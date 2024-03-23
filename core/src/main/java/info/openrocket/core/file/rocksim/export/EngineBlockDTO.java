package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.rocketcomponent.EngineBlock;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Models a Rocksim XML Element for an EngineBlock. EngineBlocks in Rocksim are
 * treated as rings with a special
 * usage code.
 */
@XmlRootElement(name = "Ring")
@XmlAccessorType(XmlAccessType.FIELD)
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
