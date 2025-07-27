package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;

import java.io.File;

public class LaunchLugLoader extends BaseComponentLoader {

	public LaunchLugLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("ID", "Units", ComponentPreset.INNER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("OD", "Units", ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH));

	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.LAUNCH_LUG;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.LAUNCH_LUG;
	}

}
