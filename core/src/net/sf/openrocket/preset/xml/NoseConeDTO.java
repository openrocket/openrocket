package net.sf.openrocket.preset.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;

/**
 * A NoseCone preset XML handler.
 */
@XmlRootElement(name = "NoseCone")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends BaseComponentDTO {

    @XmlElement(name = "Shape")
    private ShapeDTO shape;
    @XmlElement(name = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @XmlElement(name = "ShoulderDiameter")
    private AnnotatedLengthDTO shoulderDiameter;
    @XmlElement(name = "ShoulderLength")
    private AnnotatedLengthDTO shoulderLength;
    @XmlElement(name = "Length")
    private AnnotatedLengthDTO length;

    @XmlElement(name = "Thickness")
    private AnnotatedLengthDTO thickness;
    
    /**
     * Default constructor.
     */
    public NoseConeDTO() {
    }

    /**
     * Constructor that
     *
     * @param thePreset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
     */
    public NoseConeDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setShape(ShapeDTO.asDTO(thePreset.get(ComponentPreset.SHAPE)));
        setOutsideDiameter(thePreset.get(ComponentPreset.AFT_OUTER_DIAMETER));
        if ( thePreset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
        	setShoulderDiameter(thePreset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
        }
        if ( thePreset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
        	setShoulderLength(thePreset.get(ComponentPreset.AFT_SHOULDER_LENGTH));
        }
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

    public double getOutsideDiameter() {
        return outsideDiameter.getValue();
    }

    public void setOutsideDiameter(final AnnotatedLengthDTO theOutsideDiameter) {
        outsideDiameter = theOutsideDiameter;
    }

    public void setOutsideDiameter(final double theOutsideDiameter) {
        outsideDiameter = new AnnotatedLengthDTO(theOutsideDiameter);
    }

    public double getShoulderDiameter() {
        return shoulderDiameter.getValue();
    }

    public void setShoulderDiameter(final AnnotatedLengthDTO theShoulderDiameter) {
        shoulderDiameter = theShoulderDiameter;
    }

    public void setShoulderDiameter(final double theShoulderDiameter) {
        shoulderDiameter = new AnnotatedLengthDTO(theShoulderDiameter);
    }

    public double getShoulderLength() {
        return shoulderLength.getValue();
    }

    public void setShoulderLength(final AnnotatedLengthDTO theShoulderLength) {
    	shoulderLength = theShoulderLength;
    }

    public void setShoulderLength(final double theShoulderLength) {
    	shoulderLength = new AnnotatedLengthDTO(theShoulderLength);
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
        props.put(ComponentPreset.AFT_OUTER_DIAMETER, this.getOutsideDiameter());
        if ( shoulderLength != null ) {
        	props.put(ComponentPreset.AFT_SHOULDER_LENGTH, this.getShoulderLength());
        }
        if ( shoulderDiameter != null ) {
        	props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, this.getShoulderDiameter());
        }
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
        if ( thickness != null ) {
        	props.put(ComponentPreset.THICKNESS, this.getThickness());
        }

        return ComponentPresetFactory.create(props);
    }

}
