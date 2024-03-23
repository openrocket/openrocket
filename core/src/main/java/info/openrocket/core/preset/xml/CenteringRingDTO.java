
package info.openrocket.core.preset.xml;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;

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
     * @param thePreset the preset
     *
     * @throws info.openrocket.core.util.BugException thrown if the expected tube
     *                                                coupler keys are not in the
     *                                                preset
     */
    public CenteringRingDTO(ComponentPreset thePreset) {
        super(thePreset);
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        return super.asComponentPreset(legacy, ComponentPreset.Type.CENTERING_RING, materials);
    }
}
