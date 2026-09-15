
package info.openrocket.core.preset.xml;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 * Body tube preset XML handler.
 */
@JacksonXmlRootElement(localName = "BodyTube")
public class BodyTubeDTO extends BaseComponentDTO {

    @JacksonXmlProperty(localName = "InsideDiameter")
    private AnnotatedLengthDTO insideDiameter;
    @JacksonXmlProperty(localName = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @JacksonXmlProperty(localName = "Length")
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

    @JsonIgnore
    public void setInsideDiameter(final double theId) {
        insideDiameter = new AnnotatedLengthDTO(theId);
    }

    public double getOutsideDiameter() {
        return outsideDiameter.getValue();
    }

    public void setOutsideDiameter(final AnnotatedLengthDTO theOd) {
        outsideDiameter = theOd;
    }

    @JsonIgnore
    public void setOutsideDiameter(final double theOd) {
        outsideDiameter = new AnnotatedLengthDTO(theOd);
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
