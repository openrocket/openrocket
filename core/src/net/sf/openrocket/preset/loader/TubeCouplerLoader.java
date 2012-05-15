package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

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
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.TUBE_COUPLER;
	}

}
