package net.sf.openrocket.database;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.MaterialStorage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;


/**
 * A class that contains single instances of {@link Database} for specific purposes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Databases {
	private static final LogHelper log = Application.getLogger();
	
	/* Static implementations of specific databases: */
	
	/**
	 * A database of bulk materials (with bulk densities).
	 */
	public static final Database<Material> BULK_MATERIAL = new Database<Material>();
	/**
	 * A database of surface materials (with surface densities).
	 */
	public static final Database<Material> SURFACE_MATERIAL = new Database<Material>();
	/**
	 * A database of linear material (with length densities).
	 */
	public static final Database<Material> LINE_MATERIAL = new Database<Material>();
	
	
	
	static {
		
		// Add default materials
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Acrylic", 1190));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Aluminum", 2700));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Balsa", 170));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Basswood", 500));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Birch", 670));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Brass", 8600));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Cardboard", 680));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Carbonfiber", 1780));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Cork", 240));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"DepronXPS", 40));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Fiberglass", 1850));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Kraftphenolic", 950));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Maple", 755));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Paperoffice", 820));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Pine", 530));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Plywoodbirch", 630));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"PolycarbonateLexan", 1200));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Polystyrene", 1050));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"PVC", 1390));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Spruce", 450));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Steel", 7850));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"StyrofoamgenericEPS", 20));
		//		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Styrofoam (Blue foam, XPS)", 32));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"StyrofoamBluefoamXPS", 32));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Titanium", 4500));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"Quantumtubing", 1050));
		BULK_MATERIAL.add(Material.newSystemMaterial(Material.Type.BULK,"BlueTube", 1300));
		
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Ripstopnylon", 0.067));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Mylar", 0.021));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Polyethylenethin", 0.015));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Polyethyleneheavy", 0.040));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Silk", 0.060));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Paperoffice", 0.080));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Cellophane", 0.018));
		SURFACE_MATERIAL.add(Material.newSystemMaterial(Material.Type.SURFACE,"Crepepaper", 0.025));
		
		//// Thread (heavy-duty)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Threadheavy-duty", 0.0003));
		//// Elastic cord (round 2mm, 1/16 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Elasticcordround2mm", 0.0018));
		//// Elastic cord (flat  6mm, 1/4 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Elasticcordflat6mm", 0.0043));
		//// Elastic cord (flat 12mm, 1/2 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Elasticcordflat12mm", 0.008));
		//// Elastic cord (flat 19mm, 3/4 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Elasticcordflat19mm", 0.0012));
		//// Elastic cord (flat 25mm, 1 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Elasticcordflat25mm", 0.0016));
		//// Braided nylon (2 mm, 1/16 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Braidednylon2mm", 0.001));
		//// Braided nylon (3 mm, 1/8 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Braidednylon3mm", 0.0035));
		//// Tubular nylon (11 mm, 7/16 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Tubularnylon11mm", 0.013));
		//// Tubular nylon (14 mm, 9/16 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Tubularnylon14mm", 0.016));
		//// Tubular nylon (25 mm, 1 in)
		LINE_MATERIAL.add(Material.newSystemMaterial(Material.Type.LINE,"Tubularnylon25mm", 0.029));
		
		
		// Add user-defined materials
		for (Material m : Application.getPreferences().getUserMaterials()) {
			switch (m.getType()) {
			case LINE:
				LINE_MATERIAL.add(m);
				break;
			
			case SURFACE:
				SURFACE_MATERIAL.add(m);
				break;
			
			case BULK:
				BULK_MATERIAL.add(m);
				break;
			
			default:
				log.warn("ERROR: Unknown material type " + m);
			}
		}
		
		// Add database storage listener
		MaterialStorage listener = new MaterialStorage();
		LINE_MATERIAL.addDatabaseListener(listener);
		SURFACE_MATERIAL.addDatabaseListener(listener);
		BULK_MATERIAL.addDatabaseListener(listener);
	}
	
	
	/*
	 * Used just for ensuring initialization of the class.
	 */
	public static void fakeMethod() {
		
	}
	
	
	/**
	 * Find a material from the database with the specified type and name.  Returns
	 * <code>null</code> if the specified material could not be found.
	 * 
	 * @param type	the material type.
	 * @param name	the material name in the database.
	 * @return		the material, or <code>null</code> if not found.
	 */
	public static Material findMaterial(Material.Type type, String name) {
		Database<Material> db;
		switch (type) {
		case BULK:
			db = BULK_MATERIAL;
			break;
		case SURFACE:
			db = SURFACE_MATERIAL;
			break;
		case LINE:
			db = LINE_MATERIAL;
			break;
		default:
			throw new IllegalArgumentException("Illegal material type: " + type);
		}
		
		for (Material m : db) {
			if (m.getName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}
	
	
	/**
	 * Find a material from the database or return a new user defined material if the specified
	 * material with the specified density is not found.
	 * 
	 * @param type			the material type.
	 * @param name			the material name.
	 * @param density		the density of the material.
	 * @return				the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String key, String name, double density) {
		Database<Material> db;
		switch (type) {
		case BULK:
			db = BULK_MATERIAL;
			break;
		case SURFACE:
			db = SURFACE_MATERIAL;
			break;
		case LINE:
			db = LINE_MATERIAL;
			break;
		default:
			throw new IllegalArgumentException("Illegal material type: " + type);
		}
		
		Material bestMatch = null;
		
		// Alter the search mechanism to handle older specifications.
		// If a key is specified, then we match on key, if one is found.
		// Otherwise we return the material which matches on name.
		// this requires us to loop through the entire db at least once to look for the key.
		for (Material m : db) {
			// perfect match based on key.
			if ( key != null && m.getKey().equals(key) && MathUtil.equals(m.getDensity(), density) ) {
				return m;
			}
			if (m.getName().equalsIgnoreCase(name) && MathUtil.equals(m.getDensity(), density)) {
				bestMatch = m;
			}
		}
		if ( bestMatch != null ) {
			return bestMatch;
		}
		return Material.newUserMaterial(type, name, density);
	}
	
	
}
