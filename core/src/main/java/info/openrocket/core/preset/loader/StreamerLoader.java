package info.openrocket.core.preset.loader;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.preset.TypedPropertyMap;

import java.io.File;

public class StreamerLoader extends BaseComponentLoader {

	private final MaterialHolder materials;

	public StreamerLoader(MaterialHolder theMaterials, File theBasePath) {
		super(theMaterials, theBasePath);
		materials = theMaterials;

		// The base component loader adds a bulk material loader, which is incompatible
		// with the surface loader
		// for a streamer. Remove the file column parser here so we can set your own in
		// the code that follows.
		for (int i = 0; i < fileColumns.size(); i++) {
			RockSimComponentFileColumnParser rocksimComponentFileColumnParser = fileColumns.get(i);
			if (rocksimComponentFileColumnParser instanceof MaterialColumnParser) {
				fileColumns.remove(rocksimComponentFileColumnParser);
			}
		}

		fileColumns.add(new SurfaceMaterialColumnParser(materials, "Material", ComponentPreset.MATERIAL));
		fileColumns.add(new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH));
		fileColumns.add(new DoubleUnitColumnParser("Width", "Units", ComponentPreset.WIDTH));
		fileColumns.add(new DoubleUnitColumnParser("Thickness", "Units", ComponentPreset.THICKNESS));
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.STREAMER;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.STREAMER;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {
		super.postProcess(props);

		// Fix the material since some files use bulk materials for streamers.
		Double thickness = props.get(ComponentPreset.THICKNESS);
		Material.Surface myMaterial = (Material.Surface) props.get(ComponentPreset.MATERIAL);

		Material.Surface m = materials.getSurfaceMaterial(myMaterial, thickness);

		props.put(ComponentPreset.MATERIAL, m != null ? m : myMaterial);
	}

}
