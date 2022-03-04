package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The top level Rocksim document.
 */
@XmlRootElement(name = RocksimCommonConstants.ROCK_SIM_DOCUMENT)
@XmlAccessorType(XmlAccessType.FIELD)
public class RocksimDocumentDTO {

    @XmlElement(name = RocksimCommonConstants.FILE_VERSION)
    private final String version = "4";

    @XmlElement(name = RocksimCommonConstants.DESIGN_INFORMATION)
    private RocksimDesignDTO design;

    /**
     * Constructor.
     */
    public RocksimDocumentDTO() {
    }

    /**
     * Get the subordinate design DTO.
     *
     * @return  the RocksimDesignDTO
     */
    public RocksimDesignDTO getDesign() {
        return design;
    }

    /**
     * Setter.
     *
     * @param theDesign
     */
    public void setDesign(RocksimDesignDTO theDesign) {
        this.design = theDesign;
    }

    /**
     * Getter.
     *
     * @return
     */
    public String getVersion() {
        return version;
    }
}
