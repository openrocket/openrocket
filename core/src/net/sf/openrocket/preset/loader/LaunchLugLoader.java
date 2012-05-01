package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class LaunchLugLoader extends BaseComponentLoader {

	public LaunchLugLoader(Map<String, Material> materials) {
		super(materials);
		fileColumns.add(new DoubleUnitColumnParser("ID","Units",ComponentPreset.INNER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("OD","Units",ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length","Units",ComponentPreset.LENGTH));

	}

	
	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.LAUNCH_LUG;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.LAUNCH_LUG;
	}

}
