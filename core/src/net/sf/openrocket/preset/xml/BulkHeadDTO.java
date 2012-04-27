
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
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected bulk head keys are not in the preset
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
    public ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException {
        TypedPropertyMap props = new TypedPropertyMap();
        addProps(props, materials);
        props.put(ComponentPreset.OUTER_DIAMETER, this.getOutsideDiameter());
        props.put(ComponentPreset.LENGTH, this.getLength());
        props.put(ComponentPreset.TYPE, ComponentPreset.Type.BULK_HEAD);

        return ComponentPresetFactory.create(props);
    }

}
