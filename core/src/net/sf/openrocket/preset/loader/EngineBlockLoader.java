package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class EngineBlockLoader extends BodyTubeLoader {

	public EngineBlockLoader(MaterialHolder materials) {
		super(materials);
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.ENGINE_BLOCK;
	}

	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.ENGINE_BLOCK;
	}

}
