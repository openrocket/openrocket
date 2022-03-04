package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * High-level placeholder element for Rocksim.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RocksimDesignDTO {

    @XmlElement(name = RocksimCommonConstants.ROCKET_DESIGN)
    private RocketDesignDTO design;

    /**
     * Constructor.
     */
    public RocksimDesignDTO() {
    }

    /**
     * Get the DTO.
     *
     * @return the DTO
     */
    public RocketDesignDTO getDesign() {
        return design;
    }

    /**
     * Set the DTO.
     *
     * @param theDesign  the DTO
     */
    public void setDesign(RocketDesignDTO theDesign) {
        design = theDesign;
    }
}
