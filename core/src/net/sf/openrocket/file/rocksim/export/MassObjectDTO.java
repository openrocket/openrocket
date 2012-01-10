package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.MassObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that models a Rocksim MassObject.
 */
@XmlRootElement(name = RocksimCommonConstants.MASS_OBJECT)
@XmlAccessorType(XmlAccessType.FIELD)
public class MassObjectDTO extends BasePartDTO{

    @XmlElement(name = RocksimCommonConstants.TYPE_CODE)
    private int typeCode = 0;

    /**
     * Default constructor.
     */
    public MassObjectDTO() {
    }

    /**
     * Typed constructor.
     *
     * @param mo  OR MassObject
     */
    public MassObjectDTO(MassObject mo) {
        super(mo);
    }
}
