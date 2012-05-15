package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.preset.TypedPropertyMap;

import java.io.File;

public class BulkHeadLoader extends BaseComponentLoader {

	public BulkHeadLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("OD","Units",ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length","Units",ComponentPreset.LENGTH));

	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.BULK_HEAD;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.BULKHEAD;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {
		props.put(ComponentPreset.FILLED, true);
		super.postProcess(props);
	}


}
