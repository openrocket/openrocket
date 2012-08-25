package net.sf.openrocket.preset.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.unit.UnitGroup;

public class MaterialHolder {
	
	private final Map<String, Material.Bulk> bulkMaterials = new HashMap<String, Material.Bulk>();
	
	private final Map<String, Material.Surface> surfaceMaterials = new HashMap<String, Material.Surface>();
	
	private final Map<String, Material.Line> lineMaterials = new HashMap<String, Material.Line>();
	
	public MaterialHolder() {
	}
	
	public MaterialHolder(List<Material> materials) {
		if (materials == null) {
			return;
		}
		for (Material material : materials) {
			this.put(material);
		}
	}
	
	public void put(Material material) {
		switch (material.getType()) {
		case BULK:
			bulkMaterials.put(material.getName(), (Material.Bulk) material);
			break;
		case SURFACE:
			surfaceMaterials.put(material.getName(), (Material.Surface) material);
			break;
		case LINE:
			lineMaterials.put(material.getName(), (Material.Line) material);
			break;
		}
	}
	
	public Material getMaterial(Material material) {
		switch (material.getType()) {
		case BULK:
			return getBulkMaterial((Material.Bulk) material);
		case SURFACE:
			return getSurfaceMaterial((Material.Surface) material, null);
		case LINE:
			return getLineMaterial((Material.Line) material);
		default:
			return null;
		}
	}
	
	public Material.Bulk getBulkMaterial(Material.Bulk material) {
		Material.Bulk m = bulkMaterials.get(material.getName());
		return m;
	}
	
	public Material.Surface getSurfaceMaterial(Material.Surface material, Double thickness) {
		Material.Surface m = surfaceMaterials.get(material.getName());
		if (m != null) {
			return m;
		}
		// Try to see if we can convert a bulk material.
		if (thickness == null) {
			// if we have no thickness, there is nothing we can do
			return null;
		}
		String thicknessName = UnitGroup.UNITS_LENGTH.getUnit("mm").toString(thickness);
		String convertedMaterialName = material.getName() + "(" + thicknessName + ")";
		m = surfaceMaterials.get(convertedMaterialName);
		if (m != null) {
			return m;
		}
		Material.Bulk bulk = bulkMaterials.get(material.getName());
		
		if (bulk == null) {
			return null;
		}
		
		// Ok, now we have a thickness and a bulk material of the correct name,
		// we can make our own surface material.
		
		Material.Surface surface = (Material.Surface) Databases.findMaterial(Material.Type.SURFACE, convertedMaterialName, bulk.getDensity() * thickness);
		
		this.put(surface);
		
		return surface;
		
	}
	
	public Material.Line getLineMaterial(Material.Line material) {
		Material.Line m = lineMaterials.get(material.getName());
		return m;
	}
	
	public int size() {
		return bulkMaterials.size() + surfaceMaterials.size() + lineMaterials.size();
	}
	
	public Collection<Material> values() {
		
		HashSet<Material> allMats = new HashSet<Material>();
		allMats.addAll(bulkMaterials.values());
		allMats.addAll(surfaceMaterials.values());
		allMats.addAll(lineMaterials.values());
		
		return allMats;
		
	}
	
	public Database<Material> asDatabase(Material.Type theType) {
		Database<Material> result = new Database<Material>();
		switch (theType) {
		case LINE:
			result.addAll(lineMaterials.values());
			break;
		case SURFACE:
			result.addAll(surfaceMaterials.values());
			break;
		case BULK:
		default:
			result.addAll(bulkMaterials.values());
		}
		return result;
	}
}
