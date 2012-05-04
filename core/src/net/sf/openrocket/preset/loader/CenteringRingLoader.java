package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class CenteringRingLoader extends BodyTubeLoader {

	public CenteringRingLoader(MaterialHolder materials) {
		super(materials);
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
