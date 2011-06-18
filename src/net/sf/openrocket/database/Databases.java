package net.sf.openrocket.database;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.MaterialStorage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Prefs;


/**
 * A class that contains single instances of {@link Database} for specific purposes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Databases {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	
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
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Acrylic"), 1190, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Balsa"), 170, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Birch"), 670, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Cardboard"), 680, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Carbonfiber"), 1780, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Cork"), 240, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.DepronXPS"), 40, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Fiberglass"), 1850, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Kraftphenolic"), 950, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Maple"), 755, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Paperoffice"), 820, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Pine"), 530, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Plywoodbirch"), 630, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.PolycarbonateLexan"), 1200, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Polystyrene"), 1050, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.PVC"), 1390, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Spruce"), 450, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.StyrofoamgenericEPS"), 20, false));
		//		BULK_MATERIAL.add(new Material.Bulk("Styrofoam (Blue foam, XPS)", 32, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.StyrofoamBluefoamXPS"), 32, false));
		BULK_MATERIAL.add(new Material.Bulk(trans.get("Databases.materials.Quantumtubing"), 1050, false));
		
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Ripstopnylon"), 0.067, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Mylar"), 0.021, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Polyethylenethin"), 0.015, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Polyethyleneheavy"), 0.040, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Silk"), 0.060, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Paperoffice"), 0.080, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Cellophane"), 0.018, false));
		SURFACE_MATERIAL.add(new Material.Surface(trans.get("Databases.materials.Crepepaper"), 0.025, false));
		
		//// Thread (heavy-duty)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Threadheavy-duty"), 0.0003, false));
		//// Elastic cord (round 2mm, 1/16 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Elasticcordround2mm"), 0.0018, false));
		//// Elastic cord (flat  6mm, 1/4 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Elasticcordflat6mm"), 0.0043, false));
		//// Elastic cord (flat 12mm, 1/2 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Elasticcordflat12mm"), 0.008, false));
		//// Elastic cord (flat 19mm, 3/4 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Elasticcordflat19mm"), 0.0012, false));
		//// Elastic cord (flat 25mm, 1 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Elasticcordflat25mm"), 0.0016, false));
		//// Braided nylon (2 mm, 1/16 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Braidednylon2mm"), 0.001, false));
		//// Braided nylon (3 mm, 1/8 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Braidednylon3mm"), 0.0035, false));
		//// Tubular nylon (11 mm, 7/16 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Tubularnylon11mm"), 0.013, false));
		//// Tubular nylon (14 mm, 9/16 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Tubularnylon14mm"), 0.016, false));
		//// Tubular nylon (25 mm, 1 in)
		LINE_MATERIAL.add(new Material.Line(trans.get("Databases.materials.Tubularnylon25mm"), 0.029, false));
		

		// Add user-defined materials
		for (Material m : Prefs.getUserMaterials()) {
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
	 * Find a material from the database or return a new material if the specified
	 * material with the specified density is not found.
	 * 
	 * @param type			the material type.
	 * @param name			the material name.
	 * @param density		the density of the material.
	 * @param userDefined	whether a newly created material should be user-defined.
	 * @return				the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String name, double density,
			boolean userDefined) {
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
			if (m.getName().equalsIgnoreCase(name) && MathUtil.equals(m.getDensity(), density)) {
				return m;
			}
		}
		return Material.newMaterial(type, name, density, userDefined);
	}
	

}
