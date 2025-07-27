package info.openrocket.core.preset.loader;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.preset.TypedPropertyMap;

import java.io.File;

public class ParachuteLoader extends BaseComponentLoader {

	private final MaterialHolder materials;

	public ParachuteLoader(MaterialHolder materials, File theBasePath) {
		super(materials, theBasePath);
		this.materials = materials;
		fileColumns.add(new IntegerColumnParser("n sides", ComponentPreset.SIDES));
		fileColumns.add(new DoubleUnitColumnParser("OD", "Units", ComponentPreset.DIAMETER));
		fileColumns.add(new IntegerColumnParser("Shroud Count", ComponentPreset.LINE_COUNT));
		fileColumns.add(new DoubleUnitColumnParser("Shroud Len", "Units", ComponentPreset.LINE_LENGTH));
		fileColumns.add(new LineMaterialColumnParser(materials, "Shroud Material", ComponentPreset.LINE_MATERIAL));
		fileColumns.add(new DoubleUnitColumnParser("Chute Thickness", "Units", ComponentPreset.THICKNESS));
		fileColumns.add(new SurfaceMaterialColumnParser(materials, "Chute Material", ComponentPreset.MATERIAL));
	}

	@Override
	protected Type getComponentPresetType() {
		return ComponentPreset.Type.PARACHUTE;
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.PARACHUTE;
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
