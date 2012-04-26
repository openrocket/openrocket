package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class TubeCouplerLoader extends BodyTubeLoader {

	public TubeCouplerLoader(Map<String, Material> materials) {
		super(materials);
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.TUBE_COUPLER;
	}

	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.TUBE_COUPLER;
	}

}
