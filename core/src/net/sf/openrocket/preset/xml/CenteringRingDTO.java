
package net.sf.openrocket.preset.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;

/**
 * Centering Ring preset XML handler.
 */
@XmlRootElement(name = "CenteringRing")
@XmlAccessorType(XmlAccessType.FIELD)
public class CenteringRingDTO extends BodyTubeDTO {

    /**
     * Default constructor.
     */
    public CenteringRingDTO() {
    }

    /**
     * Most-useful constructor that maps a TubeCoupler preset to a TubeCouplerDTO.
     *
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected tube coupler keys are not in the preset
     */
    public CenteringRingDTO(ComponentPreset thePreset) {
        super(thePreset);
    }

    @Override
    public ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return super.asComponentPreset(ComponentPreset.Type.CENTERING_RING, materials);
    }
}
