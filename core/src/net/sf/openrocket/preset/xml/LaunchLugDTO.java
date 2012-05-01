
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
 * Body tube preset XML handler.
 */
@XmlRootElement(name = "LaunchLug")
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchLugDTO extends BaseComponentDTO {

    @XmlElement(name = "InsideDiameter")
    private AnnotatedLengthDTO insideDiameter;
    @XmlElement(name = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @XmlElement(name = "Length")
    private AnnotatedLengthDTO length;

    /**
     * Default constructor.
     */
    public LaunchLugDTO() {
    }

    /**
     * Most-useful constructor that maps a LaunchLug preset to a LaunchLugDTO.
     *
     * @param preset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
     */
    public LaunchLugDTO(final ComponentPreset preset) {
        super(preset);
        setInsideDiameter(preset.get(ComponentPreset.INNER_DIAMETER));
        setOutsideDiameter(preset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(preset.get(ComponentPreset.LENGTH));
    }

    public double getInsideDiameter() {
        return insideDiameter.getValue();
    }

    public void setInsideDiameter( final AnnotatedLengthDTO theLength ) {
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
    public ComponentPreset asComponentPreset(java.util.List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return asComponentPreset(ComponentPreset.Type.LAUNCH_LUG, materials);
    }

    public ComponentPreset asComponentPreset(ComponentPreset.Type type, List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.INNER_DIAMETER, this.getInsideDiameter());
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
