
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
 * Streamer preset XML handler.
 */
@XmlRootElement(name = "Streamer")
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamerDTO extends BaseComponentDTO {

    @XmlElement(name = "Length")
    private AnnotatedLengthDTO length;
    @XmlElement(name = "Width")
    private AnnotatedLengthDTO width;
    @XmlElement(name = "Thickness")
    private AnnotatedLengthDTO thickness;

    /**
     * Default constructor.
     */
    public StreamerDTO() {
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
    public StreamerDTO(final ComponentPreset preset) {
        super(preset);
        setWidth(preset.get(ComponentPreset.WIDTH));
        setThickness(preset.get(ComponentPreset.THICKNESS));
        setLength(preset.get(ComponentPreset.LENGTH));
    }

    public double getWidth() {
        return width.getValue();
    }

    public void setWidth(final AnnotatedLengthDTO theWidth) {
        width = theWidth;
    }

    public void setWidth(final double theId) {
        width = new AnnotatedLengthDTO(theId);
    }

    public double getThickness() {
        return thickness.getValue();
    }

    public void setThickness(final AnnotatedLengthDTO theThickness) {
        thickness = theThickness;
    }

    public void setThickness(final double theThickness) {
        thickness = new AnnotatedLengthDTO(theThickness);
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
        return asComponentPreset(legacy, ComponentPreset.Type.STREAMER, materials);
    }

    public ComponentPreset asComponentPreset(Boolean legacy, ComponentPreset.Type type, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.LEGACY, legacy);
        // TODO - seems some vendors use a bulk material for the sheet along with a
        // Thickness.
        // need to fix the MATERIAL packed into the componentpreset.
        props.put(ComponentPreset.WIDTH, this.getWidth());
        props.put(ComponentPreset.THICKNESS, this.getThickness());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
