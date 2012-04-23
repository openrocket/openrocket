
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Base class for the external representation of all component presets.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseComponentDTO {

    @XmlElement(name = "Manufacturer")
    private String manufacturer;
    @XmlElement(name = "PartNumber")
    private String partNo;
    @XmlElement(name = "Description")
    private String description;
    @XmlElement(name = "Material")
    private String material;
    @XmlElement(name = "Mass")
    private double mass;

    /**
     * Default constructor.
     */
    protected BaseComponentDTO() {
    }

    /**
     * Constructor.
     *
     * @param preset  the preset to use to pull data values out of
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
     */
    protected BaseComponentDTO(final ComponentPreset preset) {
        setManufacturer(preset.getManufacturer().getSimpleName());
        setPartNo(preset.getPartNo());
        setDescription(preset.get(ComponentPreset.DESCRIPTION));
        setMaterial(preset.get(ComponentPreset.MATERIAL).getName());
        if (preset.has(ComponentPreset.MASS)) {
            setMass(preset.get(ComponentPreset.MASS));
        }
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String theManufacturer) {
        manufacturer = theManufacturer;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(final String thePartNo) {
        partNo = thePartNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String theDescription) {
        description = theDescription;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(final String theMaterial) {
        material = theMaterial;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(final double theMass) {
        mass = theMass;
    }
}
