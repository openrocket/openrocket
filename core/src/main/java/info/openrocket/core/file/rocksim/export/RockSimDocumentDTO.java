package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The top level Rocksim document.
 */
@XmlRootElement(name = RockSimCommonConstants.ROCK_SIM_DOCUMENT)
@XmlAccessorType(XmlAccessType.FIELD)
public class RockSimDocumentDTO {

    @XmlElement(name = RockSimCommonConstants.FILE_VERSION)
    private final String version = "4";

    @XmlElement(name = RockSimCommonConstants.DESIGN_INFORMATION)
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
