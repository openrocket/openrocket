package info.openrocket.core.preset.loader;

import java.io.File;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.BugException;

public class MaterialLoader extends RockSimComponentFileLoader {

	private final MaterialHolder materialMap = new MaterialHolder();

	private final static TypedKey<String> MATERIALNAME = new TypedKey<>("MaterialName", String.class);
	private final static TypedKey<String> UNITS = new TypedKey<>("Units", String.class);
	private final static TypedKey<Double> DENSITY = new TypedKey<>("Density", Double.class);

	public MaterialLoader(File theBasePathToLoadFrom) {
		super(theBasePathToLoadFrom);
		fileColumns.add(new StringColumnParser("Material Name", MATERIALNAME));
		fileColumns.add(new StringColumnParser("Units", UNITS));
		fileColumns.add(new DoubleColumnParser("Density", DENSITY));
	}

	@Override
	protected RockSimComponentFileType getFileType() {
		return RockSimComponentFileType.MATERIAL;
	}

	public MaterialHolder getMaterialMap() {
		return materialMap;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {
		String name = props.get(MATERIALNAME);
		String unit = props.get(UNITS);
		double density = props.get(DENSITY);

		String cleanedMaterialName = stripAll(name, '"').trim();

		if ("g/cm".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.LINE, cleanedMaterialName, 0.1d * density));
		} else if ("g/cm2".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.SURFACE, cleanedMaterialName, 10.0d * density));
		} else if ("g/cm3".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.BULK, cleanedMaterialName, 1000.0d * density));
		} else if ("kg/m3".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.BULK, cleanedMaterialName, density));
		} else if ("lb/ft3".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.BULK, cleanedMaterialName, 16.0184634d * density));
		} else if ("oz/in".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.LINE, cleanedMaterialName, 1.11612296d * density));
		} else if ("oz/in2".equals(unit)) {
			materialMap.put(Databases.findMaterial(Material.Type.SURFACE, cleanedMaterialName, 43.94184876d * density));
		} else {
			throw new BugException("Unknown unit in Materials file: " + unit);
		}
	}

}