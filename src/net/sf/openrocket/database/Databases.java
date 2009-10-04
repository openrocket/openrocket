package net.sf.openrocket.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.openrocket.file.GeneralMotorLoader;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.MaterialStorage;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Prefs;


/**
 * A class that contains single instances of {@link Database} for specific purposes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Databases {

	/* Static implementations of specific databases: */
	/**
	 * The motor database.
	 */
	public static final Database<Motor> MOTOR = new Database<Motor>(new GeneralMotorLoader());
	
	
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
	
	

	// TODO: HIGH: loading the thrust curves and other databases
	static {
		
		try {
			MOTOR.loadJarDirectory("datafiles/thrustcurves/", ".*\\.[eE][nN][gG]$");
		} catch (IOException e) {
			System.out.println("Could not read thrust curves from JAR: "+e.getMessage());
			
			// Try to find directory as a system resource
			File dir;
			URL url = ClassLoader.getSystemResource("datafiles/thrustcurves/");
			
			try {
				dir = JarUtil.urlToFile(url);
			} catch (Exception e1) {
				dir = new File("datafiles/thrustcurves/");
			}
				
			try {
				MOTOR.loadDirectory(dir, ".*\\.[eE][nN][gG]$");
			} catch (IOException e1) {
				System.out.println("Could not read thrust curves from directory either.");
				throw new RuntimeException(e1);
			}
		}
	}
	
	static {
		
		// Add default materials
		BULK_MATERIAL.add(new Material.Bulk("Acrylic",		1190, false));
		BULK_MATERIAL.add(new Material.Bulk("Balsa",		 170, false));
		BULK_MATERIAL.add(new Material.Bulk("Birch",		 670, false));
		BULK_MATERIAL.add(new Material.Bulk("Cardboard",	 680, false));
		BULK_MATERIAL.add(new Material.Bulk("Carbon fiber",	1780, false));
		BULK_MATERIAL.add(new Material.Bulk("Cork",			 240, false));
		BULK_MATERIAL.add(new Material.Bulk("Depron",		  40, false));
		BULK_MATERIAL.add(new Material.Bulk("Fiberglass",	1850, false));
		BULK_MATERIAL.add(new Material.Bulk("Kraft phenolic",950, false));
		BULK_MATERIAL.add(new Material.Bulk("Maple",		 755, false));
		BULK_MATERIAL.add(new Material.Bulk("Paper (office)",820, false));
		BULK_MATERIAL.add(new Material.Bulk("Pine",			 530, false));
		BULK_MATERIAL.add(new Material.Bulk("Plywood (birch)",630, false));
		BULK_MATERIAL.add(new Material.Bulk("Polycarbonate (Lexan)",1200, false));
		BULK_MATERIAL.add(new Material.Bulk("Polystyrene",  1050, false));
		BULK_MATERIAL.add(new Material.Bulk("PVC",			1390, false));
		BULK_MATERIAL.add(new Material.Bulk("Spruce",		 450, false));
		// TODO: CRITICAL: Add styrofoam
		BULK_MATERIAL.add(new Material.Bulk("Quantum tubing",1050, false));
		
		SURFACE_MATERIAL.add(new Material.Surface("Ripstop nylon",			0.067, false));
		SURFACE_MATERIAL.add(new Material.Surface("Mylar", 					0.021, false));
		SURFACE_MATERIAL.add(new Material.Surface("Polyethylene (thin)",	0.015, false));
		SURFACE_MATERIAL.add(new Material.Surface("Polyethylene (heavy)", 	0.040, false));
		SURFACE_MATERIAL.add(new Material.Surface("Silk", 					0.060, false));
		SURFACE_MATERIAL.add(new Material.Surface("Paper (office)",			0.080, false));
		SURFACE_MATERIAL.add(new Material.Surface("Cellophane", 			0.018, false));
		SURFACE_MATERIAL.add(new Material.Surface("Cr\u00eape paper", 		0.025, false));
		
		LINE_MATERIAL.add(new Material.Line("Thread (heavy-duty)", 				0.0003, false));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (round 2mm, 1/16 in)",0.0018, false));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat  6mm, 1/4 in)",	0.0043, false));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 12mm, 1/2 in)",	0.008, false));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 19mm, 3/4 in)",	0.0012, false));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 25mm, 1 in)",	0.0016, false));
		LINE_MATERIAL.add(new Material.Line("Braided nylon (2 mm, 1/16 in)", 	0.001, false));
		LINE_MATERIAL.add(new Material.Line("Braided nylon (3 mm, 1/8 in)", 	0.0035, false));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (11 mm, 7/16 in)",	0.013, false));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (14 mm, 9/16 in)",	0.016, false));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (25 mm, 1 in)",		0.029, false));
		
		
		// Add user-defined materials
		for (Material m: Prefs.getUserMaterials()) {
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
				System.err.println("ERROR: Unknown material type " + m);
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
			throw new IllegalArgumentException("Illegal material type: "+type);
		}
		
		for (Material m: db) {
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
			throw new IllegalArgumentException("Illegal material type: "+type);
		}

		for (Material m: db) {
			if (m.getName().equalsIgnoreCase(name) && MathUtil.equals(m.getDensity(), density)) {
				return m;
			}
		}
		return Material.newMaterial(type, name, density, userDefined);
	}	
	
	

	/**
	 * Return all motor in the database matching a search criteria.  Any search criteria that
	 * is null or NaN is ignored.
	 * 
	 * @param type			the motor type, or null.
	 * @param manufacturer	the manufacturer, or null.
	 * @param designation	the designation, or null.
	 * @param diameter		the diameter, or NaN.
	 * @param length		the length, or NaN.
	 * @return				an array of all the matching motors.
	 */
	public static Motor[] findMotors(Motor.Type type, String manufacturer, String designation, double diameter, double length) {
		ArrayList<Motor> results = new ArrayList<Motor>();
		
		for (Motor m: MOTOR) {
			boolean match = true;
			if (type != null  &&  type != m.getMotorType())
				match = false;
			else if (manufacturer != null  &&  !m.getManufacturer().matches(manufacturer))
				match = false;
			else if (designation != null  &&  !designation.equalsIgnoreCase(m.getDesignation()))
				match = false;
			else if (!Double.isNaN(diameter)  &&  (Math.abs(diameter - m.getDiameter()) > 0.0015))
				match = false;
			else if (!Double.isNaN(length) && (Math.abs(length - m.getLength()) > 0.0015))
				match = false;
			
			if (match)
				results.add(m);
		}
		
		return results.toArray(new Motor[0]);
	}

}
