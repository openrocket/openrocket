package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class ParachuteLoader extends BaseComponentLoader {

	Map<String,Material> materialMap;
	
	public ParachuteLoader(Map<String, Material> materials) {
		super(materials);
		this.materialMap = materials;
		fileColumns.add(new IntegerColumnParser("n sides", ComponentPreset.SIDES));
		fileColumns.add(new DoubleUnitColumnParser("OD","Units",ComponentPreset.DIAMETER));
		fileColumns.add(new IntegerColumnParser("Shroud Count", ComponentPreset.LINE_COUNT));
		fileColumns.add(new DoubleUnitColumnParser("Shroud Len", "Units", ComponentPreset.LINE_LENGTH));
		fileColumns.add(new LineMaterialColumnParser(materials,"Shroud Material",ComponentPreset.LINE_MATERIAL));
		fileColumns.add(new DoubleUnitColumnParser("Chute Thickness", "Units", ComponentPreset.THICKNESS));
		fileColumns.add( new SurfaceMaterialColumnParser(materials,"Chute Material", ComponentPreset.MATERIAL));
	}

	
	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.PARACHUTE;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.PARACHUTE;
	}

}
