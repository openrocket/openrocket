package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * High-level placeholder element for Rocksim.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RockSimDesignDTO {

    @XmlElement(name = RockSimCommonConstants.ROCKET_DESIGN)
    private RocketDesignDTO design;

    /**
     * Constructor.
     */
    public RockSimDesignDTO() {
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
     * @param theDesign the DTO
     */
    public void setDesign(RocketDesignDTO theDesign) {
        design = theDesign;
    }
}
