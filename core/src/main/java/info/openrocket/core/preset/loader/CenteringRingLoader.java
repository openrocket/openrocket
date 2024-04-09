package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;

import java.io.File;

public class CenteringRingLoader extends BodyTubeLoader {

	public CenteringRingLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.CENTERING_RING;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.CENTERING_RING;
	}

}
