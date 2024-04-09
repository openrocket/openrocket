package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;

import java.io.File;

public class TubeCouplerLoader extends BodyTubeLoader {

	public TubeCouplerLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.TUBE_COUPLER;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.TUBE_COUPLER;
	}

}
