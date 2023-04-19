
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

    @XmlElement(name = "InnerDiameter")
    private AnnotatedLengthDTO innerDiameter;
    @XmlElement(name = "OuterDiameter")
    private AnnotatedLengthDTO outerDiameter;
    @XmlElement(name = "Height")
    private AnnotatedLengthDTO height;
    @XmlElement(name = "BaseHeight")
    private AnnotatedLengthDTO baseHeight;
    @XmlElement(name = "FlangeHeight")
    private AnnotatedLengthDTO flangeHeight;
    @XmlElement(name = "ScrewHeight")
    private AnnotatedLengthDTO screwHeight;
    @XmlElement(name = "ScrewMass")
    private AnnotatedMassDTO screwMass;
    @XmlElement(name = "NutMass")
    private AnnotatedMassDTO nutMass;

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
        setBaseHeight(preset.get(ComponentPreset.BASE_HEIGHT));
        setFlangeHeight(preset.get(ComponentPreset.FLANGE_HEIGHT));
        setScrewHeight(preset.get(ComponentPreset.SCREW_HEIGHT));
        setScrewMass(preset.get(ComponentPreset.SCREW_MASS));
        setNutMass(preset.get(ComponentPreset.NUT_MASS));
    }

    public double getInnerDiameter() {
        return innerDiameter.getValue();
    }

    public void setInnerDiameter(final AnnotatedLengthDTO theLength ) {
    	innerDiameter = theLength;
    }
    
    public void setInsideDiameter(final double theId) {
        innerDiameter = new AnnotatedLengthDTO(theId);
    }

    public double getOuterDiameter() {
        return outerDiameter.getValue();
    }

    public void setOuterDiameter(final AnnotatedLengthDTO theOd) {
        outerDiameter = theOd;
    }

    public void setOutsideDiameter(final double theOd) {
        outerDiameter = new AnnotatedLengthDTO(theOd);
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
    
    public double getBaseHeight() {
    	return baseHeight.getValue();
    }
    
    public void setBaseHeight(final double theBaseHeight) {
    	baseHeight = new AnnotatedLengthDTO(theBaseHeight);
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

    public double getScrewHeight() {
        return screwHeight.getValue();
    }

    public void setScrewHeight(final double screwHeight) {
        this.screwHeight = new AnnotatedLengthDTO(screwHeight);
    }

    public double getScrewMass() {
        return screwMass.getValue();
    }

    public void setScrewMass(double screwMass) {
        this.screwMass = new AnnotatedMassDTO(screwMass);
    }

    public double getNutMass() {
        return nutMass.getValue();
    }

    public void setNutMass(double nutMass) {
        this.nutMass = new AnnotatedMassDTO(nutMass);
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, java.util.List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return asComponentPreset(legacy, ComponentPreset.Type.RAIL_BUTTON, materials);
    }

    public ComponentPreset asComponentPreset(Boolean legacy, ComponentPreset.Type type, List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
		props.put(ComponentPreset.LEGACY, legacy);
        addProps(props, materials);
        props.put(ComponentPreset.INNER_DIAMETER, this.getInnerDiameter());
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOuterDiameter());
        props.put(ComponentPreset.HEIGHT, this.getHeight());
        props.put(ComponentPreset.BASE_HEIGHT, this.getBaseHeight());
        props.put(ComponentPreset.FLANGE_HEIGHT, this.getFlangeHeight());
        props.put(ComponentPreset.SCREW_HEIGHT, this.getScrewHeight());
        props.put(ComponentPreset.SCREW_MASS, this.getScrewMass());
        props.put(ComponentPreset.NUT_MASS, this.getNutMass());
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
