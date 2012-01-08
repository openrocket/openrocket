package net.sf.openrocket.file.rocksim.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RocksimDesignDTO {

    @XmlElement(name = "RocketDesign")
    private RocketDesignDTO design;

    public RocksimDesignDTO() {
    }

    public RocketDesignDTO getDesign() {
        return design;
    }

    public void setDesign(RocketDesignDTO theDesign) {
        design = theDesign;
    }
}
