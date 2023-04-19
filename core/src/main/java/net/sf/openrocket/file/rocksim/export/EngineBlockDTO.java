package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.rocketcomponent.EngineBlock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models a Rocksim XML Element for an EngineBlock.  EngineBlocks in Rocksim are treated as rings with a special
 * usage code.
 */
@XmlRootElement(name = "Ring")
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineBlockDTO extends CenteringRingDTO{

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
