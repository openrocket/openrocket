package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;

import java.io.File;

public class RailButtonLoader extends BaseComponentLoader {

	public RailButtonLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("ID", "Units", ComponentPreset.INNER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("OD", "Units", ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Height", "Units", ComponentPreset.HEIGHT));
		fileColumns.add(new DoubleUnitColumnParser("Flange Height", "Units", ComponentPreset.FLANGE_HEIGHT));
		fileColumns.add(new DoubleUnitColumnParser("Standoff Height", "Units", ComponentPreset.BASE_HEIGHT));

	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.RAIL_BUTTON;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.LAUNCH_LUG;
	}

}
