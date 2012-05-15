package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

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
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.CENTERING_RING;
	}

}
