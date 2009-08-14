package net.sf.openrocket.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.util.MathUtil;


public abstract class MotorLoader implements Loader<Motor> {
	
	
	/** 
	 * Manufacturer codes to expand.  These are general translations that are used
	 * both in RASP and RockSim engine files.
	 */
	private static final Map<String,String> MANUFACTURER_CODES =
		new HashMap<String,String>();
	static {
		
		/*
		 * TODO: CRITICAL: Should names have Inc. LLC. etc?
		 */
		
		// AeroTech has many name combinations...
		for (String s: new String[] { "A", "AT", "AERO", "AEROT", "AEROTECH" }) {
			MANUFACTURER_CODES.put(s, "AeroTech");
			MANUFACTURER_CODES.put(s+"-RMS", "AeroTech");
			MANUFACTURER_CODES.put(s+"/RMS", "AeroTech");
			MANUFACTURER_CODES.put(s+"-RCS", "AeroTech");
			MANUFACTURER_CODES.put(s+"/RCS", "AeroTech");
			MANUFACTURER_CODES.put("RCS-" + s, "AeroTech");
			MANUFACTURER_CODES.put("RCS/" + s, "AeroTech");
			MANUFACTURER_CODES.put(s+"-APOGEE", "AeroTech");
			MANUFACTURER_CODES.put(s+"/APOGEE", "AeroTech");
		}
		MANUFACTURER_CODES.put("ISP", "AeroTech");
		
		MANUFACTURER_CODES.put("AHR", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRID", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRIDS", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRID ROCKETRY", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRIDS ROCKETRY", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRID ROCKETRY LLC", "Alpha Hybrid Rocketry LLC");
		MANUFACTURER_CODES.put("ALPHA HYBRID ROCKETRY, LLC", "Alpha Hybrid Rocketry LLC");

		MANUFACTURER_CODES.put("AMW", "Animal Motor Works");
		MANUFACTURER_CODES.put("AW", "Animal Motor Works");
		MANUFACTURER_CODES.put("ANIMAL", "Animal Motor Works");
		MANUFACTURER_CODES.put("ANIMAL MOTOR WORKS", "Animal Motor Works");
		
		MANUFACTURER_CODES.put("AP", "Apogee");
		MANUFACTURER_CODES.put("APOG", "Apogee");
		MANUFACTURER_CODES.put("APOGEE", "Apogee");
		MANUFACTURER_CODES.put("P", "Apogee");
		
		MANUFACTURER_CODES.put("CES", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CESARONI", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CESARONI TECHNOLOGY", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CESARONI TECHNOLOGY INC", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CESARONI TECHNOLOGY INC.", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CESARONI TECHNOLOGY INCORPORATED", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CTI", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CS", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("CSR", "Cesaroni Technology Inc.");
		MANUFACTURER_CODES.put("PRO38", "Cesaroni Technology Inc.");
		
		MANUFACTURER_CODES.put("CR", "Contrail Rockets");
		MANUFACTURER_CODES.put("CONTR", "Contrail Rockets");
		MANUFACTURER_CODES.put("CONTRAIL", "Contrail Rockets");
		MANUFACTURER_CODES.put("CONTRAIL ROCKET", "Contrail Rockets");
		MANUFACTURER_CODES.put("CONTRAIL ROCKETS", "Contrail Rockets");
		
		MANUFACTURER_CODES.put("E", "Estes");
		MANUFACTURER_CODES.put("ES", "Estes");
		MANUFACTURER_CODES.put("ESTES", "Estes");
		
		MANUFACTURER_CODES.put("EM", "Ellis Mountain");
		MANUFACTURER_CODES.put("ELLIS", "Ellis Mountain");
		MANUFACTURER_CODES.put("ELLIS MOUNTAIN", "Ellis Mountain");
		MANUFACTURER_CODES.put("ELLIS MOUNTAIN ROCKET", "Ellis Mountain");
		MANUFACTURER_CODES.put("ELLIS MOUNTAIN ROCKETS", "Ellis Mountain");
		
		MANUFACTURER_CODES.put("GR", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA ROCKET", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA ROCKETS", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA MOTOR", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA MOTORS", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA ROCKET MOTOR", "Gorilla Rocket Motors");
		MANUFACTURER_CODES.put("GORILLA ROCKET MOTORS", "Gorilla Rocket Motors");
		
		MANUFACTURER_CODES.put("H", "HyperTEK");
		MANUFACTURER_CODES.put("HT", "HyperTEK");
		MANUFACTURER_CODES.put("HYPER", "HyperTEK");
		MANUFACTURER_CODES.put("HYPERTEK", "HyperTEK");
		
		MANUFACTURER_CODES.put("K", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KBA", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("K/AT", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("K-AT", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOS", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON/AT", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON-AT", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON/AEROTECH", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON-AEROTECH", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON-BY-AEROTECH", "Kosdon by AeroTech");
		MANUFACTURER_CODES.put("KOSDON BY AEROTECH", "Kosdon by AeroTech");
		
		MANUFACTURER_CODES.put("LOKI", "Loki Research");
		MANUFACTURER_CODES.put("LOKI RESEARCH", "Loki Research");
		MANUFACTURER_CODES.put("LR", "Loki Research");
		
		MANUFACTURER_CODES.put("PM", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PML", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES LTD", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES, LTD", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES LTD.", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES, LTD.", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES LIMITED", "Public Missiles, Ltd.");
		MANUFACTURER_CODES.put("PUBLIC MISSILES, LIMITED", "Public Missiles, Ltd.");
		
		MANUFACTURER_CODES.put("PP", "Propulsion Polymers");
		MANUFACTURER_CODES.put("PROP", "Propulsion Polymers");
		MANUFACTURER_CODES.put("PROPULSION", "Propulsion Polymers");
		MANUFACTURER_CODES.put("PROPULSION-POLYMERS", "Propulsion Polymers");
		MANUFACTURER_CODES.put("PROPULSION POLYMERS", "Propulsion Polymers");
		
		MANUFACTURER_CODES.put("Q", "Quest");
		MANUFACTURER_CODES.put("QU", "Quest");
		MANUFACTURER_CODES.put("QUEST", "Quest");
		
		MANUFACTURER_CODES.put("RATT", "RATT Works");
		MANUFACTURER_CODES.put("RATT WORKS", "RATT Works");
		MANUFACTURER_CODES.put("RT", "RATT Works");
		MANUFACTURER_CODES.put("RTW", "RATT Works");
		
		MANUFACTURER_CODES.put("RR", "Roadrunner Rocketry");
		MANUFACTURER_CODES.put("ROADRUNNER", "Roadrunner Rocketry");
		MANUFACTURER_CODES.put("ROADRUNNER ROCKETRY", "Roadrunner Rocketry");
		
		MANUFACTURER_CODES.put("RV", "Rocketvision");
		MANUFACTURER_CODES.put("ROCKETVISION", "Rocketvision");

		MANUFACTURER_CODES.put("SR", "Sky Ripper Systems");
		MANUFACTURER_CODES.put("SRS", "Sky Ripper Systems");
		MANUFACTURER_CODES.put("SKYR", "Sky Ripper Systems");
		MANUFACTURER_CODES.put("SKYRIPPER", "Sky Ripper Systems");
		MANUFACTURER_CODES.put("SKYRIPPER SYSTEMS", "Sky Ripper Systems");
		MANUFACTURER_CODES.put("SKY RIPPER SYSTEMS", "Sky Ripper Systems");
		
		MANUFACTURER_CODES.put("WCH", "West Coast Hybrids");
		MANUFACTURER_CODES.put("WCR", "West Coast Hybrids");
		MANUFACTURER_CODES.put("WEST COAST HYBRIDS", "West Coast Hybrids");
		
		MANUFACTURER_CODES.put("SF", "WECO Feuerwerk");  // Previously Sachsen Feuerwerks
		MANUFACTURER_CODES.put("SACHSEN FEUERWERK", "WECO Feuerwerk");
		MANUFACTURER_CODES.put("SACHSEN FEUERWERKS", "WECO Feuerwerk");
		MANUFACTURER_CODES.put("WECO", "WECO Feuerwerk");
		MANUFACTURER_CODES.put("WECO FEUERWERK", "WECO Feuerwerk");
		MANUFACTURER_CODES.put("WECO FEUERWERKS", "WECO Feuerwerk");
	}

	
	
	
	/**
	 * Load motors from the specified <code>InputStream</code>.  The file is read using
	 * the default charset returned by {@link #getDefaultCharset()}.
	 * 
	 * @param stream		the source of the motor definitions.
	 * @param filename		the file name of the file, may be <code>null</code> if not 
	 * 						applicable.
	 * @return				a list of motors contained in the file.
	 * @throws IOException	if an I/O exception occurs of the file format is invalid.
	 */
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		return load(new InputStreamReader(stream, getDefaultCharset()), filename);
	}
	
	
	/**
	 * Load motors from the specified <code>Reader</code>.
	 * 
	 * @param reader		the source of the motor definitions.
	 * @param filename		the file name of the file, may be <code>null</code> if not 
	 * 						applicable.
	 * @return				a list of motors contained in the file.
	 * @throws IOException	if an I/O exception occurs of the file format is invalid.
	 */
	public abstract List<Motor> load(Reader reader, String filename) throws IOException;
	

	
	/**
	 * Return the default charset to use when loading rocket files of this type.
	 * <p>
	 * If the method {@link #load(InputStream, String)} is overridden as well, this
	 * method may return <code>null</code>.
	 * 
	 * @return	the charset to use when loading the rocket file.
	 */
	protected abstract Charset getDefaultCharset(); 
	

	
	
	//////////  Helper methods  //////////
	
	
	/**
	 * Calculate the mass of a motor at distinct points in time based on the
	 * initial total mass, propellant weight and thrust.
	 * <p>
	 * This calculation assumes that the velocity of the exhaust remains constant
	 * during the burning.  This derives from the mass-flow and thrust relation
	 * <pre>F = m' * v</pre>
	 *  
	 * @param time    list of time points
	 * @param thrust  thrust at the discrete times
	 * @param total   total weight of the motor
	 * @param prop    propellant amount consumed during burning
	 * @return		  a list of the mass at the specified time points
	 */
	protected static List<Double> calculateMass(List<Double> time, List<Double> thrust,
			double total, double prop) {
		List<Double> mass = new ArrayList<Double>();
		List<Double> deltam = new ArrayList<Double>();

		double t0, f0;
		double totalMassChange = 0;
		double scale;
		
		// First calculate mass change between points
		t0 = time.get(0);
		f0 = thrust.get(0);
		for (int i=1; i < time.size(); i++) {
			double t1 = time.get(i);
			double f1 = thrust.get(i);
			
			double dm = 0.5*(f0+f1)*(t1-t0);
			deltam.add(dm);
			totalMassChange += dm;
			t0 = t1;
			f0 = f1;
		}
		
		// Scale mass change and calculate mass
		mass.add(total);
		scale = prop / totalMassChange;
		for (double dm: deltam) {
			total -= dm*scale;
			mass.add(total);
		}
		
		return mass;
	}
	
	
	/**
	 * Helper method to remove a delay (or plugged) from the end of a motor designation,
	 * if present.
	 * 
	 * @param designation	the motor designation.
	 * @return				the designation with a possible delay removed.
	 */
	protected static String removeDelay(String designation) {
		if (designation.matches(".*-([0-9]+|[pP])$")) {
			designation = designation.substring(0, designation.lastIndexOf('-'));
		}
		return designation;
	}
	
	
	
	/**
	 * Helper method to tokenize a string using whitespace as the delimiter.
	 */
	protected static String[] split(String str) {
		return split(str,"\\s+");
	}
	
	
	/**
	 * Helper method to tokenize a string using the given delimiter.
	 */
	protected static String[] split(String str, String delim) {
		String[] pieces = str.split(delim);
		if (pieces.length==0 || !pieces[0].equals(""))
			return pieces;
		return Arrays.copyOfRange(pieces, 1, pieces.length);
	}
	
	
	/**
	 * Sort the primary list and other lists in that order.
	 * 
	 * @param primary	the list to order.
	 * @param lists		lists to order in the same permutation.
	 */
	protected static void sortLists(List<Double> primary, List<?> ... lists) {
		
		// TODO: LOW: Very idiotic sort algorithm, but should be fast enough
		// since the time should be sorted already
		
		int index;
		
		do {
			for (index=0; index < primary.size()-1; index++) {
				if (primary.get(index+1) < primary.get(index)) {
					Collections.swap(primary, index, index+1);
					for (List<?> l: lists) {
						Collections.swap(l, index, index+1);
					}
					break;
				}
			}
		} while (index < primary.size()-1);
	}
	
	
	/**
	 * Convert a manufacturer string.  This should be done for all manufacturers
	 * for overall consistency.  This includes both RASP name expansions and some
	 * general renaming.  This should also be performed when loading designs in order
	 * to identify manufacturers correctly.
	 * 
	 * @param mfg	the original manufacturer / manufacturer code.
	 * @return		the manufacturer name.
	 */
	public static String convertManufacturer(String mfg) {
		// Replace underscore and trim
		mfg = mfg.replace('_', ' ').trim();
		
		// Check for conversion
		String conv = MANUFACTURER_CODES.get(mfg.toUpperCase());
		if (conv != null)
			return conv;
		else
			return mfg;
	}
	
	
	@SuppressWarnings("unchecked")
	protected static void finalizeThrustCurve(List<Double> time, List<Double> thrust,
			List ... lists) {
		
		if (time.size() == 0)
			return;
		
		// Start
		if (!MathUtil.equals(time.get(0), 0) || !MathUtil.equals(thrust.get(0), 0)) {
			time.add(0, 0.0);
			thrust.add(0, 0.0);
			for (List l: lists) {
				Object o = l.get(0);
				l.add(0, o);
			}
		}
		
		// End
		int n = time.size()-1;
		if (!MathUtil.equals(thrust.get(n), 0)) {
			time.add(time.get(n));
			thrust.add(0.0);
			for (List l: lists) {
				Object o = l.get(n);
				l.add(o);
			}
		}
	}
	
}
