
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
 * Bulkhead preset XML handler.
 */
@JacksonXmlRootElement(localName = "BulkHead")
public class BulkHeadDTO extends BaseComponentDTO {

    @JacksonXmlProperty(localName = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @JacksonXmlProperty(localName = "Length")
    private AnnotatedLengthDTO length;

    public BulkHeadDTO() {
    }

    /**
     * Most-useful constructor that maps a BulkHead preset to a BulkHeadDTO.
     *
     * @param thePreset the preset
     *
     * @throws info.openrocket.core.util.BugException thrown if the expected
     *                                                bulkhead keys are not in the
     *                                                preset
     */
    public BulkHeadDTO(final ComponentPreset thePreset) {
        super(thePreset);
        setOutsideDiameter(thePreset.get(ComponentPreset.OUTER_DIAMETER));
        setLength(thePreset.get(ComponentPreset.LENGTH));
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
    public ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        props.put(ComponentPreset.LEGACY, legacy);
        addProps(props, materials);
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.BULK_HEAD);

        return ComponentPresetFactory.create(props);
    }

}
