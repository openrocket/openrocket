
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
     * @param preset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
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

    public void setWidth( final AnnotatedLengthDTO theWidth ) {
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
    public ComponentPreset asComponentPreset(java.util.List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return asComponentPreset(ComponentPreset.Type.STREAMER, materials);
    }

    public ComponentPreset asComponentPreset(ComponentPreset.Type type, List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        // TODO - seems some vendors use a bulk material for the sheet along with a Thickness.
        // need to fix the MATERIAL packed into the componentpreset.
        props.put(ComponentPreset.WIDTH, this.getWidth());
        props.put(ComponentPreset.THICKNESS, this.getThickness());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
