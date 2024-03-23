
package info.openrocket.core.preset.xml;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Tube coupler preset XML handler.
 */
@XmlRootElement(name = "TubeCoupler")
@XmlAccessorType(XmlAccessType.FIELD)
public class TubeCouplerDTO extends BodyTubeDTO {

    /**
     * Default constructor.
     */
    public TubeCouplerDTO() {
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
    public TubeCouplerDTO(ComponentPreset thePreset) {
        super(thePreset);
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        return super.asComponentPreset(legacy, ComponentPreset.Type.TUBE_COUPLER, materials);
    }
}
