package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
     * @return  the RockSimDesignDTO
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
