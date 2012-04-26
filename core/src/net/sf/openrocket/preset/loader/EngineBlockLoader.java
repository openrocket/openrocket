package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class EngineBlockLoader extends BodyTubeLoader {

	public EngineBlockLoader(Map<String, Material> materials) {
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
