
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
 * Bulkhead preset XML handler.
 */
@XmlRootElement(name = "BulkHead")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkHeadDTO extends BaseComponentDTO {

    @XmlElement(name = "OutsideDiameter")
    private AnnotatedLengthDTO outsideDiameter;
    @XmlElement(name = "Length")
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

    public void setOutsideDiameter(final double theOutsideDiameter) {
        outsideDiameter = new AnnotatedLengthDTO(theOutsideDiameter);
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
