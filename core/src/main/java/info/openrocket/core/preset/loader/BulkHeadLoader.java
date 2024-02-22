package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.preset.TypedPropertyMap;

import java.io.File;

public class BulkHeadLoader extends BaseComponentLoader {

	public BulkHeadLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		fileColumns.add(new DoubleUnitColumnParser("OD", "Units", ComponentPreset.OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH));

	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.BULK_HEAD;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.BULKHEAD;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {
		props.put(ComponentPreset.FILLED, true);
		super.postProcess(props);
	}

}
