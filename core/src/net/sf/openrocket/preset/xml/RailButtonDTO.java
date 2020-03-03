
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
@XmlRootElement(name = "RailButton")
@XmlAccessorType(XmlAccessType.FIELD)
public class RailButtonDTO extends BaseComponentDTO {

    @XmlElement(name = "InsideDiameter")
    private AnnotatedLengthDTO insideDiameter;
    @XmlElement(name = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @XmlElement(name = "Height")
    private AnnotatedLengthDTO height;
    @XmlElement(name = "StandoffHeight")
    private AnnotatedLengthDTO standoffHeight;
    @XmlElement(name = "FlangeHeight")
    private AnnotatedLengthDTO flangeHeight;

    /**
     * Default constructor.
     */
    public RailButtonDTO() {
    }

    /**
     * Most-useful constructor that maps a RailButton preset to a RailButtonDTO.
     *
     * @param preset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
     */
    public RailButtonDTO(final ComponentPreset preset) {
        super(preset);
        setInsideDiameter(preset.get(ComponentPreset.INNER_DIAMETER));
        setOutsideDiameter(preset.get(ComponentPreset.OUTER_DIAMETER));
        setHeight(preset.get(ComponentPreset.HEIGHT));
        setStandoffHeight(preset.get(ComponentPreset.STANDOFF_HEIGHT));
        setFlangeHeight(preset.get(ComponentPreset.FLANGE_HEIGHT));
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

    public double getHeight() {
        return height.getValue();
    }

    public void setHeight(final AnnotatedLengthDTO theHeight) {
        height = theHeight;
    }

    public void setHeight(final double theHeight) {
        height = new AnnotatedLengthDTO(theHeight);
    }
    
    public double getStandoffHeight() {
    	return standoffHeight.getValue();
    }
    
    public void setStandoffHeight(final AnnotatedLengthDTO theStandoffHeight) {
    	standoffHeight = theStandoffHeight;
    }
    
    public void setStandoffHeight(final double theStandoffHeight) {
    	standoffHeight = new AnnotatedLengthDTO(theStandoffHeight);
    }
    
    public double getFlangeHeight() {
    	return flangeHeight.getValue();
    }
    
    public void setFlangeHeight(final AnnotatedLengthDTO theFlangeHeight) {
    	flangeHeight = theFlangeHeight;
    }
    
    public void setFlangeHeight(final double theFlangeHeight) {
    	flangeHeight = new AnnotatedLengthDTO(theFlangeHeight);
    }

    @Override
    public ComponentPreset asComponentPreset(java.util.List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return asComponentPreset(ComponentPreset.Type.RAIL_BUTTON, materials);
    }

    public ComponentPreset asComponentPreset(ComponentPreset.Type type, List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.INNER_DIAMETER, this.getInsideDiameter());
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.HEIGHT, this.getHeight());
        props.put(ComponentPreset.STANDOFF_HEIGHT, this.getStandoffHeight());
        props.put(ComponentPreset.FLANGE_HEIGHT, this.getFlangeHeight());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
