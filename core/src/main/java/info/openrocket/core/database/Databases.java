package info.openrocket.core.database;

import info.openrocket.core.material.MaterialGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.material.Material.Type;
import info.openrocket.core.material.MaterialStorage;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

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
	public static final Database<Material> BULK_MATERIAL = new Database<>();
	/**
	 * A database of surface materials (with surface densities).
	 */
	public static final Database<Material> SURFACE_MATERIAL = new Database<>();
	/**
	 * A database of linear material (with length densities).
	 */
	public static final Database<Material> LINE_MATERIAL = new Database<>();
	
	
	
	static {
		
		// Add default materials
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Acrylic", 1190, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Aluminum", 2700, MaterialGroup.METALS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Balsa", 170, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Basswood", 500, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Birch", 670, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Brass", 8600, MaterialGroup.METALS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Cardboard", 680, MaterialGroup.PAPER));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Carbon fiber", 1780, MaterialGroup.COMPOSITES));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Cork", 240, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Delrin", 1420, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Depron (XPS)", 40, MaterialGroup.FOAMS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Fiberglass", 1850, MaterialGroup.COMPOSITES));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Kraft phenolic", 950, MaterialGroup.COMPOSITES));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Maple", 755, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Nylon", 1150, MaterialGroup.FIBERS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Paper (office)", 820, MaterialGroup.PAPER));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Pine", 530, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Plywood (birch)", 630, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Polycarbonate (Lexan)", 1200, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Polystyrene", 1050, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "PVC", 1390, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Spruce", 450, MaterialGroup.WOODS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Steel", 7850, MaterialGroup.METALS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Styrofoam (generic EPS)", 20, MaterialGroup.FOAMS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Styrofoam \"Blue foam\" (XPS)", 32, MaterialGroup.FOAMS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Titanium", 4500, MaterialGroup.METALS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Quantum tubing", 1050, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "Blue tube", 1300, MaterialGroup.COMPOSITES));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "PLA - 100% infill", 1250, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "PETG - 100% infill", 1250, MaterialGroup.PLASTICS));
		BULK_MATERIAL.add(newMaterial(Material.Type.BULK, "ABS - 100% infill", 1050, MaterialGroup.PLASTICS));

		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Ripstop nylon", 0.067, MaterialGroup.FABRICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Mylar", 0.021, MaterialGroup.PLASTICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Polyethylene (thin)", 0.015, MaterialGroup.PLASTICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Polyethylene (heavy)", 0.040, MaterialGroup.PLASTICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Silk", 0.060, MaterialGroup.FABRICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Paper (office)", 0.080, MaterialGroup.PAPER));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Cellophane", 0.018, MaterialGroup.PLASTICS));
		SURFACE_MATERIAL.add(newMaterial(Material.Type.SURFACE, "Cr\u00eape paper", 0.025, MaterialGroup.PAPER));
		
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Thread (heavy-duty)", 0.0003, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)", 0.0018, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 6 mm, 1/4 in)", 0.0043, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 12 mm, 1/2 in)", 0.008, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 19 mm, 3/4 in)", 0.0012, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic cord (flat 25 mm, 1 in)", 0.0016, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Braided nylon (2 mm, 1/16 in)", 0.001, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Braided nylon (3 mm, 1/8 in)", 0.0035, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (11 mm, 7/16 in)", 0.013, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (14 mm, 9/16 in)", 0.016, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Tubular nylon (25 mm, 1 in)", 0.029, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar thread 138  (0.4 mm, 1/64 in)", 0.00014808, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar thread 207  (0.5 mm, 1/64 in)", 0.00023622, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar thread 346  (0.7 mm, 1/32 in)", 0.00047243, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar thread 415  (0.8 mm, 1/32 in)", 0.00055117, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar thread 800 (1.1 mm, 3/64 in)", 0.00099211, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (3.2 mm, 1/8 in)", 0.00967306, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (4.8 mm, 3/16 in)", 0.01785797, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (6.4 mm, 1/4 in)", 0.02976328, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (7.9 mm, 5/16 in)", 0.04464491, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (10 mm, 3/8 in)", 0.05952655, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (11 mm, 7/16 in)", 0.07440819, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (13 mm, 1/2 in)", 0.11607678, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (14 mm, 9/16 in)", 0.20834293, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (16 mm, 5/8 in)", 0.28721562, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (19 mm, 3/4 in)", 0.3497185, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Kevlar 12-strand (25 mm, 1 in)", 0.45686629, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Nylon flat webbing md. (10 mm, 3/8 in)", 0.00951444, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Nylon flat webbing md. (13 mm, 1/2  in)", 0.01334208, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Nylon flat webbing md. (16 mm, 5/8 in)", 0.01618548, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Nylon flat webbing lg. (14 mm, 9/16 in)", 0.02723097, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Nylon flat webbing lg. (25 mm, 1 in)", 0.03969816, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Paraline small IIIA (6.4 mm, 1.4 in)", 0.00371829, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic rubber band (flat 3.2 mm, 1/8 in)", 0.00297638, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic rubber band (flat 6.4 mm, 1/4 in)", 0.00613107, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (flat 3.2 mm, 1/8 in)", 0.00106, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (flat 4 mm, 5/32 in)", 0.002, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (flat 6.4 mm, 1/4 in)", 0.00254, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (round 2 mm, 1/16 in)", 0.0035, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (round 2.5 mm, 3/32 in)", 0.0038, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (flat 10 mm, 3/8 in)", 0.00381, MaterialGroup.THREADS_LINES));
		LINE_MATERIAL.add(newMaterial(Material.Type.LINE, "Elastic braided cord (flat 13 mm, 1/2 in)", 0.00551172, MaterialGroup.THREADS_LINES));
		
		
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
	
	/**
	 * builds a new material based on the parameters given
	 * @param type		The type of material
	 * @param baseName	the name of material
	 * @param density	density
	 * @return	a new object with the material data
	 */
	private static Material newMaterial(Type type, String baseName, double density, MaterialGroup group) {
		String name = trans.get("material", baseName);
		return Material.newMaterial(type, name, density, group, false);
	}

	private static Material newMaterial(Type type, String baseName, double density) {
		return newMaterial(type, baseName, density, null);
	}
	
	
	
	
	/*
	 * Used just for ensuring initialization of the class.
	 */
	public static void fakeMethod() {
		
	}

	/**
	 * Find a material from the database or return a new user defined material if the specified
	 * material with the specified density is not found.
	 * <p>
	 * This method will attempt to localize the material name to the current locale, or use
	 * the provided name if unable to do so.
	 *
	 * @param type			the material type.
	 * @param baseName		the base name of the material.
	 * @param density		the density of the material.
	 * @param group			the material group.
	 * @return				the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String baseName, double density, MaterialGroup group) {
		Database<Material> db = getDatabase(type);
		String name = trans.get("material", baseName);

		for (Material m : db) {
			// Material group comparison is omitted to keep compatibility with files pre OR 24.12
			if (m.getName().equalsIgnoreCase(name) && MathUtil.equals(m.getDensity(), density)) {
				return m;
			}
		}
		return Material.newMaterial(type, name, density, group, true, true);
	}

	/**
	 * Find a material from the database or return a new user defined material if the specified
	 * material with the specified density is not found.
	 * <p>
	 * This method will attempt to localize the material name to the current locale, or use
	 * the provided name if unable to do so.
	 *
	 * @param type			the material type.
	 * @param baseName		the base name of the material.
	 * @param density		the density of the material.
	 * @return				the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String baseName, double density) {
		return findMaterial(type, baseName, density, null);
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
		Database<Material> db = getDatabase(type);
		String name = trans.get("material", baseName);
		
		for (Material m : db) {
			if (m.getName().equalsIgnoreCase(name)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * gets the specific database with the given type
	 * @param 	type	the desired type
	 * @return	the database of the type given
	 */
	public static Database<Material> getDatabase(Material.Type type) {
		return switch (type) {
			case BULK -> BULK_MATERIAL;
			case SURFACE -> SURFACE_MATERIAL;
			case LINE -> LINE_MATERIAL;
			default -> throw new IllegalArgumentException("Illegal material type: " + type);
		};
	}

	public static void addMaterial(Material material) {
		Database<Material> db = getDatabase(material.getType());
		db.add(material);
	}

	public static void removeMaterial(Material material) {
		Database<Material> db = getDatabase(material.getType());
		db.remove(material);
	}
	
}
