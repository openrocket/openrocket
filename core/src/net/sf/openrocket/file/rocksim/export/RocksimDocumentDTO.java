package net.sf.openrocket.file.rocksim.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RockSimDocument")
@XmlAccessorType(XmlAccessType.FIELD)
public class RocksimDocumentDTO {
    
    @XmlElement(name = "FileVersion")
    private final String version = "4";

    @XmlElement(name = "DesignInformation")
    private RocksimDesignDTO design;

    public RocksimDocumentDTO() {
    }

    public RocksimDesignDTO getDesign() {
        return design;
    }

    public void setDesign(RocksimDesignDTO theDesign) {
        this.design = theDesign;
    }

    public String getVersion() {
        return version;
    }
}
