
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.material.Material;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML handler for materials.
 */
@XmlRootElement(name = "Material")
@XmlAccessorType(XmlAccessType.FIELD)
public class MaterialDTO {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Density")
    private double density;
    @XmlElement(name = "Type")
    private MaterialTypeDTO type;
    @XmlAttribute(name = "UnitsOfMeasure")
    private String uom;

    /**
     * Default constructor.
     */
    public MaterialDTO() {
    }

    public MaterialDTO(final Material theMaterial) {
        this(theMaterial.getName(), theMaterial.getDensity(), MaterialTypeDTO.asDTO(theMaterial.getType()),
                theMaterial.getType().getUnitGroup().getDefaultUnit().toString());
    }

    public MaterialDTO(final String theName, final double theDensity, final MaterialTypeDTO theType, final String theUom) {
        name = theName;
        density = theDensity;
        type = theType;
        uom = theUom;
    }

    public String getName() {
        return name;
    }

    public void setName(final String theName) {
        name = theName;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(final double theDensity) {
        density = theDensity;
    }

    public MaterialTypeDTO getType() {
        return type;
    }

    public void setType(final MaterialTypeDTO theType) {
        type = theType;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(final String theUom) {
        uom = theUom;
    }
}
