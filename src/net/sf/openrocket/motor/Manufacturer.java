package net.sf.openrocket.motor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Manufacturer {
	
	private static Set<Manufacturer> manufacturers = new HashSet<Manufacturer>();
	static {

		// AeroTech has many name combinations...
		List<String> names = new ArrayList<String>();
		for (String s: new String[] { "A", "AT", "AERO", "AEROT", "AEROTECH" }) {
			names.add(s);
			names.add(s+"-RMS");
			names.add(s+"-RCS");
			names.add("RCS-" + s);
			names.add(s+"-APOGEE");
		}
		names.add("ISP");

		manufacturers.add(new Manufacturer("AeroTech", "AeroTech",
				names.toArray(new String[0])));
		
		manufacturers.add(new Manufacturer("Alpha Hybrid Rocketry LLC",
				"Alpha Hybrid Rocketry",
				"AHR", "ALPHA", "ALPHA HYBRID", "ALPHA HYBRIDS", "ALPHA HYBRIDS ROCKETRY"));

		manufacturers.add(new Manufacturer("Animal Motor Works","Animal Motor Works",
				"AMW", "AW", "ANIMAL"));

		manufacturers.add(new Manufacturer("Apogee","Apogee",
				"AP", "APOG", "P"));

		manufacturers.add(new Manufacturer("Cesaroni Technology Inc.",
				"Cesaroni Technology",
				"CES", "CESARONI", "CESARONI TECHNOLOGY INCORPORATED", "CTI",
				"CS", "CSR", "PRO38", "ABC"));

		manufacturers.add(new Manufacturer("Contrail Rockets","Contrail Rockets",
				"CR", "CONTR", "CONTRAIL", "CONTRAIL ROCKET"));
		
		manufacturers.add(new Manufacturer("Estes","Estes",
				"E", "ES"));
		
		manufacturers.add(new Manufacturer("Ellis Mountain","Ellis Mountain",
				"EM", "ELLIS", "ELLIS MOUNTAIN ROCKET", "ELLIS MOUNTAIN ROCKETS"));

		manufacturers.add(new Manufacturer("Gorilla Rocket Motors",
				"Gorilla Rocket Motors",
				"GR", "GORILLA", "GORILLA ROCKET", "GORILLA ROCKETS", "GORILLA MOTOR", 
				"GORILLA MOTORS", "GORILLA ROCKET MOTOR"));
		
		manufacturers.add(new Manufacturer("HyperTEK", "HyperTEK",
				"H", "HT", "HYPER"));
		
		manufacturers.add(new Manufacturer("Kosdon by AeroTech", "Kosdon by AeroTech",
				"K", "KBA", "K-AT", "KOS", "KOSDON", "KOSDON/AT", "KOSDON/AEROTECH"));
		
		manufacturers.add(new Manufacturer("Loki Research", "Loki Research",
				"LOKI", "LR"));
		
		manufacturers.add(new Manufacturer("Public Missiles, Ltd.", "Public Missiles",
				"PM", "PML", "PUBLIC MISSILES LIMITED"));

		manufacturers.add(new Manufacturer("Propulsion Polymers", "Propulsion Polymers",
				"PP", "PROP", "PROPULSION"));
		
		manufacturers.add(new Manufacturer("Quest", "Quest",
				"Q", "QU"));
		
		manufacturers.add(new Manufacturer("RATT Works", "RATT Works",
				"RATT", "RT", "RTW"));
		
		manufacturers.add(new Manufacturer("Roadrunner Rocketry","Roadrunner Rocketry",
				"RR", "ROADRUNNER"));
		
		manufacturers.add(new Manufacturer("Rocketvision", "Rocketvision",
				"RV", "ROCKET VISION"));
		
		manufacturers.add(new Manufacturer("Sky Ripper Systems","Sky Ripper Systems",
				"SR", "SRS", "SKYR", "SKYRIPPER", "SKY RIPPER", "SKYRIPPER SYSTEMS"));
		
		manufacturers.add(new Manufacturer("West Coast Hybrids", "West Coast Hybrids",
				"WCH", "WCR", "WEST COAST", "WEST COAST HYBRID"));
		
		// German WECO Feuerwerk, previously Sachsen Feuerwerk
		manufacturers.add(new Manufacturer("WECO Feuerwerk", "WECO Feuerwerk",
				"WECO", "WECO FEUERWERKS", "SF", "SACHSEN", "SACHSEN FEUERWERK",
				"SACHSEN FEUERWERKS"));
		
		
		// Check that no duplicates have appeared
		for (Manufacturer m1: manufacturers) {
			for (Manufacturer m2: manufacturers) {
				if (m1 == m2)
					continue;
				for (String name: m1.getAllNames()) {
					if (m2.matches(name)) {
						throw new IllegalStateException("Manufacturer name clash between " +
								"manufacturers " + m1 + " and " + m2 + " name " + name);
					}
				}
			}
		}
	}
	
	

	private final String displayName;
	private final String simpleName;
	private final Set<String> allNames;
	private final Set<String> searchNames;
	
	
	private Manufacturer(String displayName, String simpleName, String... alternateNames) {
		this.displayName = displayName;
		this.simpleName = simpleName;
		
		Set<String> all = new HashSet<String>();
		Set<String> search = new HashSet<String>();
		
		all.add(displayName);
		all.add(simpleName);
		search.add(generateSearchString(displayName));
		search.add(generateSearchString(simpleName));
		
		for (String name: alternateNames) {
			all.add(name);
			search.add(generateSearchString(name));
		}
		
		this.allNames = Collections.unmodifiableSet(all);
		this.searchNames = Collections.unmodifiableSet(search);
	}
	
	
	/**
	 * Returns the display name of the manufacturer.  This is the value that
	 * should be presented in the UI to the user.
	 * 
	 * @return	the display name
	 */
	public String getDisplayName() {
		return displayName;
	}


	/**
	 * Returns the simple name of the manufacturer.  This should be used for example
	 * when saving the manufacturer for compatibility.
	 * 
	 * @return	the simple name.
	 */
	public String getSimpleName() {
		return simpleName;
	}


	/**
	 * Return all names of the manufacturer.  This includes all kinds of
	 * codes that correspond to the manufacturer (for example "A" for AeroTech).
	 * 
	 * @return	an unmodifiable set of the alternative names.
	 */
	public Set<String> getAllNames() {
		return allNames;
	}

	
	/**
	 * Check whether the display, simple or any of the alternative names matches the
	 * specified name.  Matching is performed case insensitively and ignoring any
	 * non-letter and non-number characters.
	 * 
	 * @param name	the name to search for.
	 * @return		whether this manufacturer matches the request.
	 */
	public boolean matches(String name) {
		if (name == null)
			return false;
		return this.searchNames.contains(generateSearchString(name));
	}
	
	
	/**
	 * Return the display name of the manufacturer.
	 */
	@Override
	public String toString() {
		return displayName;
	}
	
	
	/**
	 * Returns a manufacturer for the given name.  The manufacturer is searched for
	 * within the manufacturers and if a match is found the corresponding
	 * object is returned.  If not, a new manufacturer is returned with display and
	 * simple name the name specified.  Subsequent requests for the same (or corresponding)
	 * manufacturer name will return the same object.
	 * 
	 * @param name	the manufacturer name to search for.
	 * @return		the Manufacturer object corresponding the name.
	 */
	public static synchronized Manufacturer getManufacturer(String name) {
		for (Manufacturer m: manufacturers) {
			if (m.matches(name))
				return m;
		}
		
		Manufacturer m = new Manufacturer(name.trim(), name.trim());
		manufacturers.add(m);
		return m;
	}
	
	
	
		
	private String generateSearchString(String str) {
		return str.toLowerCase().replaceAll("[^a-zA-Z0-9]+", " ").trim();
	}
	
}
