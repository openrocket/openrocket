
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bulkhead preset XML handler.
 */
@XmlRootElement(name = "BulkHead")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkHeadDTO extends BaseComponentDTO {

    @XmlElement(name = "OutsideDiameter")
    private double outsideDiameter;
    @XmlElement(name = "Length")
    private double length;

    public BulkHeadDTO() {
    }

    /**
     * Most-useful constructor that maps a BulkHead preset to a BulkHeadDTO.
     *
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected bulk head keys are not in the preset
     */
    public BulkHeadDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setOutsideDiameter(thePreset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(thePreset.get(ComponentPreset.LENGTH));
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
