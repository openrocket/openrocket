
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
     * @param thePreset  the preset
     *
     * @throws net.sf.openrocket.util.BugException thrown if the expected engine block keys are not in the preset
     */
    public EngineBlockDTO(ComponentPreset thePreset) {
        super(thePreset);
    }

    @Override
    public ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException {
        return super.asComponentPreset(ComponentPreset.Type.ENGINE_BLOCK, materials);
    }
}
