
package info.openrocket.core.preset.xml;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.rocketcomponent.ExternalComponent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 * Body tube preset XML handler.
 */
@JacksonXmlRootElement(localName = "RailButton")
public class RailButtonDTO extends BaseComponentDTO {

    @JacksonXmlProperty(localName = "InnerDiameter")
    private AnnotatedLengthDTO innerDiameter;
    @JacksonXmlProperty(localName = "OuterDiameter")
    private AnnotatedLengthDTO outerDiameter;
    @JacksonXmlProperty(localName = "Height")
    private AnnotatedLengthDTO height;
    @JacksonXmlProperty(localName = "BaseHeight")
    private AnnotatedLengthDTO baseHeight;
    @JacksonXmlProperty(localName = "FlangeHeight")
    private AnnotatedLengthDTO flangeHeight;
    @JacksonXmlProperty(localName = "ScrewHeight")
    private AnnotatedLengthDTO screwHeight;
    @JacksonXmlProperty(localName = "ScrewMass")
    private AnnotatedMassDTO screwMass;
    @JacksonXmlProperty(localName = "NutMass")
    private AnnotatedMassDTO nutMass;
    @JacksonXmlProperty(localName = "DragCoefficient")
    private String dragCoefficient;
    @JacksonXmlProperty(localName = "Finish")
    private String finish;

    /**
     * Default constructor.
     */
    public RailButtonDTO() {
    }

    /**
     * Most-useful constructor that maps a RailButton preset to a RailButtonDTO.
     *
     * @param preset the preset
     *
     * @throws info.openrocket.core.util.BugException thrown if the expected body
     *                                                tube keys are not in the
     *                                                preset
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

    public void setInnerDiameter(final AnnotatedLengthDTO theLength) {
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

    @JsonIgnore
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

    @JsonIgnore
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

    public String getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(String dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, java.util.List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        return asComponentPreset(legacy, ComponentPreset.Type.RAIL_BUTTON, materials);
    }

    public ComponentPreset asComponentPreset(Boolean legacy, ComponentPreset.Type type, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
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
        if (dragCoefficient != null && !dragCoefficient.trim().isEmpty()) {
            try {
                props.put(ComponentPreset.CD, Double.parseDouble(dragCoefficient.trim()));
            } catch (NumberFormatException ignored) {
                // skip invalid CD value
            }
        }
        if (finish != null && !finish.trim().isEmpty()) {
            try {
                props.put(ComponentPreset.FINISH, ExternalComponent.Finish.valueOf(finish.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // skip unknown finish value
            }
        }
        props.put(ComponentPreset.TYPE, type);

        return ComponentPresetFactory.create(props);
    }
}
