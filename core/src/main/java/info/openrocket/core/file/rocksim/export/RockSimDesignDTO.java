package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * High-level placeholder element for Rocksim.
 */
public class RockSimDesignDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.ROCKET_DESIGN)
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
