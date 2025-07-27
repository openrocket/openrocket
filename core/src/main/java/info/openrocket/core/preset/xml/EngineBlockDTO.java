
package info.openrocket.core.preset.xml;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Engine block preset XML handler.
 */
@XmlRootElement(name = "EngineBlock")
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineBlockDTO extends BodyTubeDTO {

    /**
     * Default constructor.
     */
    public EngineBlockDTO() {
    }

    /**
     * Most-useful constructor that maps a EngineBlock preset to a EngineBlockDTO.
     *
     * @param thePreset the preset
     *
     * @throws info.openrocket.core.util.BugException thrown if the expected engine
     *                                                block keys are not in the
     *                                                preset
     */
    public EngineBlockDTO(ComponentPreset thePreset) {
        super(thePreset);
    }

    @Override
    public ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
            throws InvalidComponentPresetException {
        return super.asComponentPreset(legacy, ComponentPreset.Type.ENGINE_BLOCK, materials);
    }
}
