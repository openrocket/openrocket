package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.preset.TypedPropertyMap;

public class BulkHeadLoader extends BaseComponentLoader {

	public BulkHeadLoader(Map<String, Material> materials) {
		super(materials);
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
