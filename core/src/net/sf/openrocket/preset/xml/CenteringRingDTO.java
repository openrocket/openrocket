
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Centering ring preset XML handler.
 */
@XmlRootElement(name = "CenteringRing")
@XmlAccessorType(XmlAccessType.FIELD)
public class CenteringRingDTO extends BaseComponentDTO {

    @XmlElement(name = "InsideDiameter")
    private double insideDiameter;
    @XmlElement(name = "OutsideDiameter")
    private double outsideDiameter;
    @XmlElement(name = "Length")
    private double length;

    /**
     * Default constructor.
     */
    public CenteringRingDTO() {
    }

    /**
     * Most-useful constructor that maps a CenteringRing preset to a CenteringRingDTO.
     *
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected centering ring keys are not in the preset
     */
    public CenteringRingDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setInsideDiameter(thePreset.get(ComponentPreset.INNER_DIAMETER));
        setOutsideDiameter(thePreset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(thePreset.get(ComponentPreset.LENGTH));
    }

    public double getInsideDiameter() {
        return insideDiameter;
    }

    public void setInsideDiameter(final double theInsideDiameter) {
        insideDiameter = theInsideDiameter;
    }

    public double getOutsideDiameter() {
        return outsideDiameter;
    }

    public void setOutsideDiameter(final double theOutsideDiameter) {
        outsideDiameter = theOutsideDiameter;
    }

    public double getLength() {
        return length;
    }

    public void setLength(final double theLength) {
        length = theLength;
    }
}
