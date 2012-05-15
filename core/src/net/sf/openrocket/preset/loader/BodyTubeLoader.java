package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

import java.io.File;

public class BodyTubeLoader extends BaseComponentLoader {

	public BodyTubeLoader(MaterialHolder materials, File theBasePathToLoadFrom) {
		super(materials, theBasePathToLoadFrom);
		fileColumns.add(new DoubleUnitColumnParser("ID","Units",ComponentPreset.INNER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("OD","Units",ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length","Units",ComponentPreset.LENGTH));

	}


	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.BODY_TUBE;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.BODY_TUBE;
	}

}
