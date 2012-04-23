
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Body tube preset XML handler.
 */
@XmlRootElement(name = "BodyTube")
@XmlAccessorType(XmlAccessType.FIELD)
public class BodyTubeDTO extends BaseComponentDTO {

    @XmlElement(name = "InsideDiameter")
    private double insideDiameter;
    @XmlElement(name = "OutsideDiameter")
    private double outsideDiameter;
    @XmlElement(name = "Length")
    private double length;

    /**
     * Default constructor.
     */
    public BodyTubeDTO() {
    }

    /**
     * Most-useful constructor that maps a BodyTube preset to a BodyTubeDTO.
     *
     * @param preset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
     */
    public BodyTubeDTO(final ComponentPreset preset) {
        super(preset);
        setInsideDiameter(preset.get(ComponentPreset.INNER_DIAMETER));
        setOutsideDiameter(preset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(preset.get(ComponentPreset.LENGTH));
    }

    public double getInsideDiameter() {
        return insideDiameter;
    }

    public void setInsideDiameter(final double theId) {
        insideDiameter = theId;
    }

    public double getOutsideDiameter() {
        return outsideDiameter;
    }

    public void setOutsideDiameter(final double theOd) {
        outsideDiameter = theOd;
    }

    public double getLength() {
        return length;
    }

    public void setLength(final double theLength) {
        length = theLength;
    }
}
