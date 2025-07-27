
package info.openrocket.core.preset.xml;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Body tube preset XML handler.
 */
@XmlRootElement(name = "BodyTube")
@XmlAccessorType(XmlAccessType.FIELD)
public class BodyTubeDTO extends BaseComponentDTO {

    @XmlElement(name = "InsideDiameter")
    private AnnotatedLengthDTO insideDiameter;
    @XmlElement(name = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @XmlElement(name = "Length")
    private AnnotatedLengthDTO length;

    /**
     * Default constructor.
     */
    public BodyTubeDTO() {
    }

    /**
     * Most-useful constructor that maps a BodyTube preset to a BodyTubeDTO.
     *
     * @param preset the preset
     *
     * @throws info.openrocket.core.util.BugException thrown if the expected body
     *                                                tube keys are not in the
     *                                                preset
     */
    public BodyTubeDTO(final ComponentPreset preset) {
        super(preset);
        setInsideDiameter(preset.get(ComponentPreset.INNER_DIAMETER));
        setOutsideDiameter(preset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(preset.get(ComponentPreset.LENGTH));
    }

    public double getInsideDiameter() {
        return insideDiameter.getValue();
    }

    public void setInsideDiameter(final AnnotatedLengthDTO theLength) {
        insideDiameter = theLength;
    }

    public void setInsideDiameter(final double theId) {
        insideDiameter = new AnnotatedLengthDTO(theId);
    }

    public double getOutsideDiameter() {
        return outsideDiameter.getValue();
    }

    public void setOutsideDiameter(final AnnotatedLengthDTO theOd) {
        outsideDiameter = theOd;
    }

    public void setOutsideDiameter(final double theOd) {
        outsideDiameter = new AnnotatedLengthDTO(theOd);
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

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, java.util.List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        return asComponentPreset(legacy, ComponentPreset.Type.BODY_TUBE, materials);
    }

    public ComponentPreset asComponentPreset(Boolean legacy, ComponentPreset.Type type, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        props.put(ComponentPreset.LEGACY, legacy);
        addProps(props, materials);
        props.put(ComponentPreset.INNER_DIAMETER, this.getInsideDiameter());
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
