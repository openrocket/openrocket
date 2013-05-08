package net.sf.openrocket.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.material.MaterialStorage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;


/**
 * A class that contains single instances of {@link Database} for specific purposes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Databases {
	private static final Logger log = LoggerFactory.getLogger(Databases.class);
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
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Acrylic", 1190));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Aluminum", 2700));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Balsa", 170));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Basswood", 500));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Birch", 670));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Brass", 8600));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Cardboard", 680));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Carbon fiber", 1780));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Cork", 240));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Depron (XPS)", 40));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Fiberglass", 1850));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Kraft phenolic", 950));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Maple", 755));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Paper (office)", 820));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Pine", 530));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Plywood (birch)", 630));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Polycarbonate (Lexan)", 1200));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Polystyrene", 1050));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "PVC", 1390));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Spruce", 450));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Steel", 7850));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Styrofoam (generic EPS)", 20));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Styrofoam \"Blue foam\" (XPS)", 32));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Titanium", 4500));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Quantum tubing", 1050));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Blue tube", 1300));
		
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Ripstop nylon", 0.067));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Mylar", 0.021));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Polyethylene (thin)", 0.015));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Polyethylene (heavy)", 0.040));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Silk", 0.060));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Paper (office)", 0.080));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Cellophane", 0.018));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Cr\u00eape paper", 0.025));
		
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Thread (heavy-duty)", 0.0003));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)", 0.0018));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 6 mm, 1/4 in)", 0.0043));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 12 mm, 1/2 in)", 0.008));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 19 mm, 3/4 in)", 0.0012));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 25 mm, 1 in)", 0.0016));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Braided nylon (2 mm, 1/16 in)", 0.001));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Braided nylon (3 mm, 1/8 in)", 0.0035));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (11 mm, 7/16 in)", 0.013));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (14 mm, 9/16 in)", 0.016));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (25 mm, 1 in)", 0.029));
		
		
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
	
	
	private static Material newMaterial(Type type, String baseName, double density) {
		String name = trans.get("material", baseName);
		return Material.newMaterial(type, name, density, false);
	}
	
	
	
	
	/*
	 * Used just for ensuring initialization of the class.
	 */
	public static void fakeMethod() {
		
	}
	
	/**
	 * Find a material from the database with the specified type and name.  Returns
	 * <code>null</code> if the specified material could not be found.
	 * <p>
	 * This method will attempt to localize the material name to the current locale, or use
	 * the provided name if unable to do so.
	 * 
	 * @param type		the material type.
	 * @param baseName	the material base name in the database.
	 * @return			the material, or <code>null</code> if not found.
	 */
	public static Material findMaterial(Material.Type type, String baseName) {
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
		
		String name = trans.get("material", baseName);
		
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
	 * <p>
	 * This method will attempt to localize the material name to the current locale, or use
	 * the provided name if unable to do so.
	 * 
	 * @param type			the material type.
	 * @param baseName			the base name of the material.
	 * @param density		the density of the material.
	 * @return				the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String baseName, double density) {
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
		
		String name = trans.get("material", baseName);
		
		for (Material m : db) {
			if (m.getName().equalsIgnoreCase(name) && MathUtil.equals(m.getDensity(), density)) {
				return m;
			}
		}
		return Material.newMaterial(type, name, density, true);
	}
	
}
