package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

import java.io.File;

public class TransitionLoader extends NoseConeLoader {

	public TransitionLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("Front Insert Len","Units",ComponentPreset.FORE_SHOULDER_LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Front Insert OD","Units",ComponentPreset.FORE_SHOULDER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Front OD","Units",ComponentPreset.FORE_OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Rear Insert Len","Units",ComponentPreset.AFT_SHOULDER_LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Rear Insert OD","Units",ComponentPreset.AFT_SHOULDER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Rear OD","Units",ComponentPreset.AFT_OUTER_DIAMETER));
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.TRANSITION;
	}

	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.TRANSITION;
	}

}
