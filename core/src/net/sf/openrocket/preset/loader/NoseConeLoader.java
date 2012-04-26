package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.preset.TypedPropertyMap;

public class NoseConeLoader extends BaseComponentLoader {

	public NoseConeLoader(Map<String, Material> materials) {
		super(materials);
		fileColumns.add(new DoubleUnitColumnParser("Outer Dia","Units",ComponentPreset.AFT_OUTER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Length","Units",ComponentPreset.LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Insert Length","Units",ComponentPreset.AFT_SHOULDER_LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Insert OD","Units",ComponentPreset.AFT_SHOULDER_DIAMETER));
		fileColumns.add(new DoubleUnitColumnParser("Thickness","Units",ComponentPreset.THICKNESS));
		fileColumns.add(new ShapeColumnParser() );
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.NOSE_CONE;
	}

	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.NOSE_CONE;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {

		if ( props.containsKey( ComponentPreset.THICKNESS )) {
			double thickness = props.get(ComponentPreset.THICKNESS);
			if ( thickness == 0d ) {
				props.remove( ComponentPreset.THICKNESS );
				props.put(ComponentPreset.FILLED, true);
			}
		}
		super.postProcess(props);
	}

}
