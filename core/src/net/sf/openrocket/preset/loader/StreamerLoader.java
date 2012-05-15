package net.sf.openrocket.preset.loader;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.preset.TypedPropertyMap;

import java.io.File;

public class StreamerLoader extends BaseComponentLoader {

	private final MaterialHolder materials;

	public StreamerLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		this.materials = materials;
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


	@Override
	protected void postProcess(TypedPropertyMap props) {
		super.postProcess(props);

		// Fix the material since some files use bulk materials for streamers.
		Double thickness = props.get( ComponentPreset.THICKNESS );
		Material.Surface material = (Material.Surface) props.get( ComponentPreset.MATERIAL );

		material = materials.getSurfaceMaterial(material, thickness);

		props.put(ComponentPreset.MATERIAL, material);
	}

}
