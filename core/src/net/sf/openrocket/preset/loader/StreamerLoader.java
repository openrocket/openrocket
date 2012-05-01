package net.sf.openrocket.preset.loader;

import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class StreamerLoader extends BaseComponentLoader {

	Map<String,Material> materialMap;

	public StreamerLoader(Map<String, Material> materials) {
		super(materials);
		this.materialMap = materials;
		fileColumns.add(new SurfaceMaterialColumnParser(materials,"Material",ComponentPreset.MATERIAL));
		fileColumns.add(new DoubleUnitColumnParser("Length","Units",ComponentPreset.LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Width","Units",ComponentPreset.WIDTH));
		fileColumns.add(new DoubleUnitColumnParser("Thickness","Units",ComponentPreset.THICKNESS));
	}


	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.STREAMER;
	}


	@Override
	protected RocksimComponentFileType getFileType() {
		return RocksimComponentFileType.STREAMER;
	}

}
