
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Transition preset XML handler.
 */
@XmlRootElement(name = "Transition")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends BaseComponentDTO {

    @XmlElement(name = "Shape")
    private ShapeDTO shape;

    @XmlElement(name = "ForeOutsideDiameter")
    private AnnotatedLengthDTO foreOutsideDiameter;
    @XmlElement(name = "ForeShoulderDiameter")
    private AnnotatedLengthDTO foreShoulderDiameter;
    @XmlElement(name = "ForeShoulderLength")
    private AnnotatedLengthDTO foreShoulderLength;

    @XmlElement(name = "AftOutsideDiameter")
    private AnnotatedLengthDTO aftOutsideDiameter;
    @XmlElement(name = "AftShoulderDiameter")
    private AnnotatedLengthDTO aftShoulderDiameter;
    @XmlElement(name = "AftShoulderLength")
    private AnnotatedLengthDTO aftShoulderLength;

    @XmlElement(name = "Length")
    private AnnotatedLengthDTO length;
    
    @XmlElement(name = "Thickness")
    private AnnotatedLengthDTO thickness;


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
        if ( thePreset.has(ComponentPreset.THICKNESS)) {
        	setThickness(thePreset.get(ComponentPreset.THICKNESS));
        }
    }

    public ShapeDTO getShape() {
        return shape;
    }

    public void setShape(final ShapeDTO theShape) {
        shape = theShape;
    }

    public double getForeOutsideDiameter() {
        return foreOutsideDiameter.getValue();
    }

    public void setForeOutsideDiameter(final AnnotatedLengthDTO theForeOutsideDiameter) {
        foreOutsideDiameter = theForeOutsideDiameter;
    }

    public void setForeOutsideDiameter(final double theForeOutsideDiameter) {
        foreOutsideDiameter = new AnnotatedLengthDTO(theForeOutsideDiameter);
    }

    public double getForeShoulderDiameter() {
        return foreShoulderDiameter.getValue();
    }

    public void setForeShoulderDiameter(final AnnotatedLengthDTO theForeShoulderDiameter) {
        foreShoulderDiameter = theForeShoulderDiameter;
    }

    public void setForeShoulderDiameter(final double theForeShoulderDiameter) {
        foreShoulderDiameter = new AnnotatedLengthDTO(theForeShoulderDiameter);
    }

    public double getForeShoulderLength() {
        return foreShoulderLength.getValue();
    }

    public void setForeShoulderLength(final AnnotatedLengthDTO theForeShoulderLength) {
        foreShoulderLength = theForeShoulderLength;
    }

    public void setForeShoulderLength(final double theForeShoulderLength) {
        foreShoulderLength = new AnnotatedLengthDTO(theForeShoulderLength);
    }

    public double getAftOutsideDiameter() {
        return aftOutsideDiameter.getValue();
    }

    public void setAftOutsideDiameter(final AnnotatedLengthDTO theAftOutsideDiameter) {
        aftOutsideDiameter = theAftOutsideDiameter;
    }

    public void setAftOutsideDiameter(final double theAftOutsideDiameter) {
        aftOutsideDiameter = new AnnotatedLengthDTO(theAftOutsideDiameter);
    }

    public double getAftShoulderDiameter() {
        return aftShoulderDiameter.getValue();
    }

    public void setAftShoulderDiameter(final AnnotatedLengthDTO theAftShoulderDiameter) {
        aftShoulderDiameter = theAftShoulderDiameter;
    }

    public void setAftShoulderDiameter(final double theAftShoulderDiameter) {
        aftShoulderDiameter = new AnnotatedLengthDTO(theAftShoulderDiameter);
    }

    public double getAftShoulderLength() {
        return aftShoulderLength.getValue();
    }

    public void setAftShoulderLength(final AnnotatedLengthDTO theAftShoulderLength) {
        aftShoulderLength = theAftShoulderLength;
    }

    public void setAftShoulderLength(final double theAftShoulderLength) {
        aftShoulderLength = new AnnotatedLengthDTO(theAftShoulderLength);
    }

    public double getLength() {
        return length.getValue();
    }

    public void setLength(final AnnotatedLengthDTO theLength) {
        length = theLength;
    }

    public void setLength(final double theLength) {
        length = new AnnotatedLengthDTO(theLength);
    }

    public double getThickness() {
		return thickness.getValue();
	}

	public void setThickness(AnnotatedLengthDTO thickness) {
		this.thickness = thickness;
	}

	public void setThickness(double thickness) {
		this.thickness = new AnnotatedLengthDTO(thickness);
	}

	@Override
	public ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.SHAPE, shape.getORShape());
        props.put(ComponentPreset.FORE_OUTER_DIAMETER, this.getForeOutsideDiameter());
        props.put(ComponentPreset.FORE_SHOULDER_DIAMETER, this.getForeShoulderDiameter());
        props.put(ComponentPreset.FORE_SHOULDER_LENGTH, this.getForeShoulderLength());
        props.put(ComponentPreset.AFT_OUTER_DIAMETER, this.getAftOutsideDiameter());
        props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, this.getAftShoulderDiameter());
        props.put(ComponentPreset.AFT_SHOULDER_LENGTH, this.getAftShoulderLength());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
        if ( thickness != null ) {
        	props.put(ComponentPreset.THICKNESS, this.getThickness());
        }

        return ComponentPresetFactory.create(props);
    }
}
