package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

import java.io.File;

public class RailButtonLoader extends BaseComponentLoader {

	public RailButtonLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("ID","Units",ComponentPreset.INNER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("OD","Units",ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Height","Units",ComponentPreset.HEIGHT));
		fileColumns.add(new DoubleUnitColumnParser("Flange Height", "Units", ComponentPreset.FLANGE_HEIGHT));
		fileColumns.add(new DoubleUnitColumnParser("Standoff Height", "Units", ComponentPreset.STANDOFF_HEIGHT));

	}


	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.RAIL_BUTTON;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.LAUNCH_LUG;
	}

}
