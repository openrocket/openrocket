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
 * A NoseCone preset XML handler.
 */
@XmlRootElement(name = "NoseCone")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends BaseComponentDTO {

    @XmlElement(name = "Shape")
    private ShapeDTO shape;
    @XmlElement(name = "OutsideDiameter")
    private double outsideDiameter;
    @XmlElement(name = "ShoulderDiameter")
    private double shoulderDiameter;
    @XmlElement(name = "Length")
    private double length;

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
        setShoulderDiameter(thePreset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
        setLength(thePreset.get(ComponentPreset.LENGTH));
    }

    public ShapeDTO getShape() {
        return shape;
    }

    public void setShape(final ShapeDTO theShape) {
        shape = theShape;
    }

    public double getOutsideDiameter() {
        return outsideDiameter;
    }

    public void setOutsideDiameter(final double theOutsideDiameter) {
        outsideDiameter = theOutsideDiameter;
    }

    public double getShoulderDiameter() {
        return shoulderDiameter;
    }

    public void setShoulderDiameter(final double theShoulderDiameter) {
        shoulderDiameter = theShoulderDiameter;
    }

    public double getLength() {
        return length;
    }

    public void setLength(final double theLength) {
        length = theLength;
    }

    public ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.SHAPE, shape.getORShape());
        props.put(ComponentPreset.AFT_OUTER_DIAMETER, this.getShoulderDiameter());
        props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);

        return ComponentPresetFactory.create(props);
    }

}
