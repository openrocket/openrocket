package net.sf.openrocket.preset.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.unit.UnitGroup;

public class MaterialHolder {

	private final Map<String,Material.Bulk> bulkMaterials = new HashMap<String,Material.Bulk>();
	
	private final Map<String,Material.Surface> surfaceMaterials = new HashMap<String,Material.Surface>();
	
	private final Map<String, Material.Line> lineMaterials = new HashMap<String,Material.Line>();
	
	public void put( Material material ) {
		switch ( material.getType() ) {
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
	
	public Material.Bulk getBulkMaterial( Material.Bulk material ) {
		Material.Bulk m = bulkMaterials.get( material.getName() );
		return (m==null) ? material : m;
	}
	
	public Material.Surface getSurfaceMaterial( Material.Surface material, Double thickness ) {
		Material.Surface m = surfaceMaterials.get(material.getName() );
		if ( m != null ) {
			return m;
		}
		// Try to see if we can convert a bulk material.
		if ( thickness == null ) {
			// if we have no thickness, there is nothing we can do
			return material;
		}
		String thicknessName = UnitGroup.UNITS_LENGTH.getUnit("mm").toString(thickness);
		String convertedMaterialName = material.getName() + "(" + thicknessName + ")";
		m = surfaceMaterials.get(convertedMaterialName);
		if ( m != null ) {
			return m;
		}
		Material.Bulk bulk = bulkMaterials.get(material.getName() );
		
		if ( bulk == null ) {
			return material;
		}
		
		// Ok, now we have a thickness and a bulk material of the correct name,
		// we can make our own surface material.
		
		Material.Surface surface = new Material.Surface( convertedMaterialName, bulk.getDensity() * thickness , true);
		
		this.put(surface);

		return surface;
		
	}
	
	public Material.Line getLineMaterial( Material.Line material ) {
		Material.Line m = lineMaterials.get( material.getName() );
		return (m==null) ? material : m;
	}
	
	public int size() {
		return bulkMaterials.size() + surfaceMaterials.size() + lineMaterials.size();
	}

	public Collection<Material> values() {
		
		HashSet<Material> allMats = new HashSet<Material>();
		allMats.addAll( bulkMaterials.values() );
		allMats.addAll( surfaceMaterials.values() );
		allMats.addAll( lineMaterials.values() );
		
		return allMats;
		
	}
}
