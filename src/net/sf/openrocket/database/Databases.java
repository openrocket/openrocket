package net.sf.openrocket.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.openrocket.file.MotorLoader;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.MathUtil;


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
	public static final Database<Motor> MOTOR = new Database<Motor>(new MotorLoader());
	
	
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
	
	// TODO: HIGH: Move materials into data files
	static {
		
		BULK_MATERIAL.add(new Material.Bulk("Acrylic",		1190));
		BULK_MATERIAL.add(new Material.Bulk("Balsa",		 170));
		BULK_MATERIAL.add(new Material.Bulk("Birch",		 670));
		BULK_MATERIAL.add(new Material.Bulk("Cardboard",	 680));
		BULK_MATERIAL.add(new Material.Bulk("Carbon fiber",	1780));
		BULK_MATERIAL.add(new Material.Bulk("Cork",			 240));
		BULK_MATERIAL.add(new Material.Bulk("Fiberglass",	1850));
		BULK_MATERIAL.add(new Material.Bulk("Kraft phenolic",950));
		BULK_MATERIAL.add(new Material.Bulk("Maple",		 755));
		BULK_MATERIAL.add(new Material.Bulk("Paper (office)",820));
		BULK_MATERIAL.add(new Material.Bulk("Pine",			 530));
		BULK_MATERIAL.add(new Material.Bulk("Plywood (birch)",630));
		BULK_MATERIAL.add(new Material.Bulk("Polycarbonate (Lexan)",1200));
		BULK_MATERIAL.add(new Material.Bulk("Polystyrene",  1050));
		BULK_MATERIAL.add(new Material.Bulk("PVC",			1390));
		BULK_MATERIAL.add(new Material.Bulk("Spruce",		 450));
		BULK_MATERIAL.add(new Material.Bulk("Quantum tubing",1050));
		
		SURFACE_MATERIAL.add(new Material.Surface("Ripstop nylon",			0.067));
		SURFACE_MATERIAL.add(new Material.Surface("Mylar", 					0.021));
		SURFACE_MATERIAL.add(new Material.Surface("Polyethylene (thin)",	0.015));
		SURFACE_MATERIAL.add(new Material.Surface("Polyethylene (heavy)", 	0.040));
		SURFACE_MATERIAL.add(new Material.Surface("Silk", 					0.060));
		SURFACE_MATERIAL.add(new Material.Surface("Paper (office)",			0.080));
		SURFACE_MATERIAL.add(new Material.Surface("Cellophane", 			0.018));
		SURFACE_MATERIAL.add(new Material.Surface("Cr\u00eape paper", 		0.025));
		
		LINE_MATERIAL.add(new Material.Line("Thread (heavy-duty)", 				0.0003));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (round 2mm, 1/16 in)",0.0018));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat  6mm, 1/4 in)",	0.0043));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 12mm, 1/2 in)",	0.008));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 19mm, 3/4 in)",	0.0012));
		LINE_MATERIAL.add(new Material.Line("Elastic cord (flat 25mm, 1 in)",	0.0016));
		LINE_MATERIAL.add(new Material.Line("Braided nylon (2 mm, 1/16 in)", 	0.001));
		LINE_MATERIAL.add(new Material.Line("Braided nylon (3 mm, 1/8 in)", 	0.0035));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (11 mm, 7/16 in)",	0.013));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (14 mm, 9/16 in)",	0.016));
		LINE_MATERIAL.add(new Material.Line("Tubular nylon (25 mm, 1 in)",		0.029));
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
	 * @param type		the material type.
	 * @param name		the material name.
	 * @param density	the density of the material.
	 * @return			the material object from the database or a new material.
	 */
	public static Material findMaterial(Material.Type type, String name, double density) {
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
		return Material.newMaterial(type, name, density);
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
			else if (manufacturer != null  &&  !manufacturer.equalsIgnoreCase(m.getManufacturer()))
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
