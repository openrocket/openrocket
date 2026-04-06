package info.openrocket.core.preset.xml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;

/**
 * A NoseCone preset XML handler.
 */
@JacksonXmlRootElement(localName = "NoseCone")
public class NoseConeDTO extends BaseComponentDTO {

    @JacksonXmlProperty(localName = "Shape")
    private ShapeDTO shape;
    @JacksonXmlProperty(localName = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @JacksonXmlProperty(localName = "ShoulderDiameter")
    private AnnotatedLengthDTO shoulderDiameter;
    @JacksonXmlProperty(localName = "ShoulderLength")
    private AnnotatedLengthDTO shoulderLength;
    @JacksonXmlProperty(localName = "Length")
    private AnnotatedLengthDTO length;

    @JacksonXmlProperty(localName = "Thickness")
    private AnnotatedLengthDTO thickness;

    @JacksonXmlProperty(localName = "InsideDiameter")
    private AnnotatedLengthDTO insideDiameter;

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
     * @throws info.openrocket.core.util.BugException thrown if the expected body
     *                                                tube keys are not in the
     *                                                preset
     */
    public NoseConeDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setShape(ShapeDTO.asDTO(thePreset.get(ComponentPreset.SHAPE)));
        setOutsideDiameter(thePreset.get(ComponentPreset.AFT_OUTER_DIAMETER));
        if (thePreset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
            setShoulderDiameter(thePreset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
        }
        if (thePreset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
            setShoulderLength(thePreset.get(ComponentPreset.AFT_SHOULDER_LENGTH));
        }
        setLength(thePreset.get(ComponentPreset.LENGTH));
        if (thePreset.has(ComponentPreset.THICKNESS)) {
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

    @JsonIgnore
    public void setOutsideDiameter(final double theOutsideDiameter) {
        outsideDiameter = new AnnotatedLengthDTO(theOutsideDiameter);
    }

    public double getShoulderDiameter() {
        return shoulderDiameter.getValue();
    }

    public void setShoulderDiameter(final AnnotatedLengthDTO theShoulderDiameter) {
        shoulderDiameter = theShoulderDiameter;
    }

    @JsonIgnore
    public void setShoulderDiameter(final double theShoulderDiameter) {
        shoulderDiameter = new AnnotatedLengthDTO(theShoulderDiameter);
    }

    public double getShoulderLength() {
        return shoulderLength.getValue();
    }

    public void setShoulderLength(final AnnotatedLengthDTO theShoulderLength) {
        shoulderLength = theShoulderLength;
    }

    @JsonIgnore
    public void setShoulderLength(final double theShoulderLength) {
        shoulderLength = new AnnotatedLengthDTO(theShoulderLength);
    }

    public double getLength() {
        return length.getValue();
    }

    public void setLength(final AnnotatedLengthDTO theLength) {
        length = theLength;
    }

    @JsonIgnore
    public void setLength(final double theLength) {
        length = new AnnotatedLengthDTO(theLength);
    }

    public double getThickness() {
        return thickness.getValue();
    }

    public void setThickness(AnnotatedLengthDTO thickness) {
        this.thickness = thickness;
    }

    @JsonIgnore
    public void setThickness(double thickness) {
        this.thickness = new AnnotatedLengthDTO(thickness);
    }

    public double getInsideDiameter() {
        return insideDiameter == null ? 0.0 : insideDiameter.getValue();
    }

    public void setInsideDiameter(AnnotatedLengthDTO insideDiameter) {
        this.insideDiameter = insideDiameter;
    }

    @JsonIgnore
    public void setInsideDiameter(double insideDiameter) {
        this.insideDiameter = new AnnotatedLengthDTO(insideDiameter);
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        props.put(ComponentPreset.LEGACY, legacy);
        addProps(props, materials);
        props.put(ComponentPreset.SHAPE, shape.getORShape());
        props.put(ComponentPreset.AFT_OUTER_DIAMETER, this.getOutsideDiameter());
        if (shoulderLength != null) {
            props.put(ComponentPreset.AFT_SHOULDER_LENGTH, this.getShoulderLength());
        }
        if (shoulderDiameter != null) {
            props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, this.getShoulderDiameter());
        }
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
        if (thickness != null) {
            props.put(ComponentPreset.THICKNESS, this.getThickness());
        }

        return ComponentPresetFactory.create(props);
    }

}
