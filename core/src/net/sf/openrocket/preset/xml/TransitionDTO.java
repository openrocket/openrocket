
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Transition preset XML handler.
 */
@XmlRootElement(name = "Transition")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends BaseComponentDTO {

    @XmlElement(name = "Shape")
    private ShapeDTO shape;

    @XmlElement(name = "ForeOutsideDiameter")
    private double foreOutsideDiameter;
    @XmlElement(name = "ForeShoulderDiameter")
    private double foreShoulderDiameter;
    @XmlElement(name = "ForeShoulderLength")
    private double foreShoulderLength;

    @XmlElement(name = "AftOutsideDiameter")
    private double aftOutsideDiameter;
    @XmlElement(name = "AftShoulderDiameter")
    private double aftShoulderDiameter;
    @XmlElement(name = "AftShoulderLength")
    private double aftShoulderLength;

    @XmlElement(name = "Length")
    private double length;


    /**
     * Default constructor.
     */
    public TransitionDTO() {
    }

    /**
     * Most-useful constructor that maps a Transition preset to a TransitionDTO.
     *
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected transition keys are not in the preset
     */
    public TransitionDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setShape(ShapeDTO.asDTO(thePreset.get(ComponentPreset.SHAPE)));
        setForeOutsideDiameter(thePreset.get(ComponentPreset.FORE_OUTER_DIAMETER));
        setForeShoulderDiameter(thePreset.get(ComponentPreset.FORE_SHOULDER_DIAMETER));
        setForeShoulderLength(thePreset.get(ComponentPreset.FORE_SHOULDER_LENGTH));
        setAftOutsideDiameter(thePreset.get(ComponentPreset.AFT_OUTER_DIAMETER));
        setAftShoulderDiameter(thePreset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
        setAftShoulderLength(thePreset.get(ComponentPreset.AFT_SHOULDER_LENGTH));
        setLength(thePreset.get(ComponentPreset.LENGTH));
    }

    public ShapeDTO getShape() {
        return shape;
    }

    public void setShape(final ShapeDTO theShape) {
        shape = theShape;
    }

    public double getForeOutsideDiameter() {
        return foreOutsideDiameter;
    }

    public void setForeOutsideDiameter(final double theForeOutsideDiameter) {
        foreOutsideDiameter = theForeOutsideDiameter;
    }

    public double getForeShoulderDiameter() {
        return foreShoulderDiameter;
    }

    public void setForeShoulderDiameter(final double theForeShoulderDiameter) {
        foreShoulderDiameter = theForeShoulderDiameter;
    }

    public double getForeShoulderLength() {
        return foreShoulderLength;
    }

    public void setForeShoulderLength(final double theForeShoulderLength) {
        foreShoulderLength = theForeShoulderLength;
    }

    public double getAftOutsideDiameter() {
        return aftOutsideDiameter;
    }

    public void setAftOutsideDiameter(final double theAftOutsideDiameter) {
        aftOutsideDiameter = theAftOutsideDiameter;
    }

    public double getAftShoulderDiameter() {
        return aftShoulderDiameter;
    }

    public void setAftShoulderDiameter(final double theAftShoulderDiameter) {
        aftShoulderDiameter = theAftShoulderDiameter;
    }

    public double getAftShoulderLength() {
        return aftShoulderLength;
    }

    public void setAftShoulderLength(final double theAftShoulderLength) {
        aftShoulderLength = theAftShoulderLength;
    }

    public double getLength() {
        return length;
    }

    public void setLength(final double theLength) {
        length = theLength;
    }
}
