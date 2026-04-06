package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * The top level Rocksim document.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.ROCK_SIM_DOCUMENT)
public class RockSimDocumentDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.FILE_VERSION)
    private final String version = "4";

    @JacksonXmlProperty(localName = RockSimCommonConstants.DESIGN_INFORMATION)
    private RockSimDesignDTO design;

    /**
     * Constructor.
     */
    public RockSimDocumentDTO() {
    }

    /**
     * Get the subordinate design DTO.
     *
     * @return the RockSimDesignDTO
     */
    public RockSimDesignDTO getDesign() {
        return design;
    }

    /**
     * Setter.
     *
     * @param theDesign
     */
    public void setDesign(RockSimDesignDTO theDesign) {
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
