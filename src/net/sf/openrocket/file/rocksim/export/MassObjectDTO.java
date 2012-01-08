package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.rocketcomponent.MassObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "MassObject")
@XmlAccessorType(XmlAccessType.FIELD)
public class MassObjectDTO extends BasePartDTO{

    @XmlElement(name = "TypeCode")
    private int typeCode = 0;

    public MassObjectDTO() {
    }

    public MassObjectDTO(MassObject ec) {
        super(ec);
    }
}
