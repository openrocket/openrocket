package net.sf.openrocket.motor;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class containing information about motor manufacturers.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Manufacturer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8124172026153369292L;

	private static class ManufacturerList extends ConcurrentHashMap<String,Manufacturer> {
		
		void add( Manufacturer m ) {
			for( String s : m.getSearchNames() ) {
				Manufacturer previousRegistered;
				if ( (previousRegistered = putIfAbsent( s, m )) != null ) {
					throw new IllegalStateException("Manufacturer name clash between " +
							"manufacturers " + previousRegistered + " and " + m + " name " + s);
				}
			}
		}
		
	}
	private static ManufacturerList manufacturers = new ManufacturerList();

	static {
		
		// AeroTech has many name combinations...
		List<String> names = new ArrayList<String>();
		for (String s : new String[] { "A", "AT", "AERO", "AEROT", "AEROTECH" }) {
			names.add(s);
			names.add(s + "-RMS");
			names.add(s + "-RCS");
			names.add("RCS-" + s);
			names.add(s + "-APOGEE");
		}
		names.add("ISP");
		
		// Aerotech has single-use, reload and hybrid motors
		manufacturers.add(new Manufacturer("AeroTech", "AeroTech", Motor.Type.UNKNOWN,
				names.toArray(new String[0])));
		
		manufacturers.add(new Manufacturer("Alpha Hybrid Rocketry LLC", "Alpha Hybrid Rocketry", Motor.Type.HYBRID,
				"AHR", "ALPHA", "ALPHA HYBRID", "ALPHA HYBRIDS", "ALPHA HYBRIDS ROCKETRY"));
		
		// TODO: HIGH: AMW/ProX - how to classify?
		
		manufacturers.add(new Manufacturer("Animal Motor Works", "Animal Motor Works", Motor.Type.RELOAD,
				"AMW", "AW", "ANIMAL"));
		
		manufacturers.add(new Manufacturer("Apogee", "Apogee", Motor.Type.SINGLE,
				"AP", "APOG", "P"));
		
		manufacturers.add(new Manufacturer("Cesaroni Technology Inc.", "Cesaroni Technology", Motor.Type.RELOAD,
				"CES", "CESARONI", "CESARONI TECHNOLOGY INCORPORATED", "CTI",
				"CS", "CSR", "PRO38", "ABC"));
		
		manufacturers.add(new Manufacturer("Contrail Rockets", "Contrail Rockets", Motor.Type.HYBRID,
				"CR", "CONTR", "CONTRAIL", "CONTRAIL ROCKET"));
		
		manufacturers.add(new Manufacturer("Estes", "Estes", Motor.Type.SINGLE,
				"E", "ES"));
		
		// Ellis Mountain has both single-use and reload motors
		manufacturers.add(new Manufacturer("Ellis Mountain", "Ellis Mountain", Motor.Type.UNKNOWN,
				"EM", "ELLIS", "ELLIS MOUNTAIN ROCKET", "ELLIS MOUNTAIN ROCKETS"));
		
		manufacturers.add(new Manufacturer("Gorilla Rocket Motors", "Gorilla Rocket Motors", Motor.Type.RELOAD,
				"GR", "GORILLA", "GORILLA ROCKET", "GORILLA ROCKETS", "GORILLA MOTOR",
				"GORILLA MOTORS", "GORILLA ROCKET MOTOR"));
		
		manufacturers.add(new Manufacturer("HyperTEK", "HyperTEK", Motor.Type.HYBRID,
				"H", "HT", "HYPER"));
		
		manufacturers.add(new Manufacturer("Kosdon by AeroTech", "Kosdon by AeroTech", Motor.Type.RELOAD,
				"K", "KBA", "K-AT", "KOS", "KOSDON", "KOSDON/AT", "KOSDON/AEROTECH"));
		
		manufacturers.add(new Manufacturer("Loki Research", "Loki Research", Motor.Type.RELOAD,
				"LOKI", "LR"));
		
		manufacturers.add(new Manufacturer("Public Missiles, Ltd.", "Public Missiles", Motor.Type.SINGLE,
				"PM", "PML", "PUBLIC MISSILES LIMITED"));
		
		manufacturers.add(new Manufacturer("Propulsion Polymers", "Propulsion Polymers", Motor.Type.HYBRID,
				"PP", "PROP", "PROPULSION"));
		
		manufacturers.add(new Manufacturer("Quest", "Quest", Motor.Type.SINGLE,
				"Q", "QU"));
		
		manufacturers.add(new Manufacturer("RATT Works", "RATT Works", Motor.Type.HYBRID,
				"RATT", "RT", "RTW"));
		
		manufacturers.add(new Manufacturer("Roadrunner Rocketry", "Roadrunner Rocketry", Motor.Type.SINGLE,
				"RR", "ROADRUNNER"));
		
		manufacturers.add(new Manufacturer("Rocketvision", "Rocketvision", Motor.Type.SINGLE,
				"RV", "ROCKET VISION"));
		
		manufacturers.add(new Manufacturer("Sky Ripper Systems", "Sky Ripper Systems", Motor.Type.HYBRID,
				"SR", "SRS", "SKYR", "SKYRIPPER", "SKY RIPPER", "SKYRIPPER SYSTEMS"));
		
		manufacturers.add(new Manufacturer("West Coast Hybrids", "West Coast Hybrids", Motor.Type.HYBRID,
				"WCH", "WCR", "WEST COAST", "WEST COAST HYBRID"));
		
		// German WECO Feuerwerk, previously Sachsen Feuerwerk
		manufacturers.add(new Manufacturer("WECO Feuerwerk", "WECO Feuerwerk", Motor.Type.SINGLE,
				"WECO", "WECO FEUERWERKS", "SF", "SACHSEN", "SACHSEN FEUERWERK",
				"SACHSEN FEUERWERKS"));
		
	}
	
	
	
	private final String displayName;
	private final String simpleName;
	private final Set<String> allNames;
	private final Set<String> searchNames;
	private final Motor.Type motorType;
	
	
	private Manufacturer(String displayName, String simpleName, Motor.Type motorType, String... alternateNames) {
		this.displayName = displayName;
		this.simpleName = simpleName;
		this.motorType = motorType;
		if (motorType == null) {
			throw new IllegalArgumentException("motorType cannot be null");
		}
		
		Set<String> all = new HashSet<String>();
		Set<String> search = new HashSet<String>();
		
		all.add(displayName);
		all.add(simpleName);
		search.add(generateSearchString(displayName));
		search.add(generateSearchString(simpleName));
		
		for (String name : alternateNames) {
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
	
	Set<String> getSearchNames() {
		return searchNames;
	}
	
	/**
	 * Return the motor type that this manufacturer produces if it produces only one motor type.
	 * If the manufacturer manufactures multiple motor types or the type is unknown,
	 * type <code>UNKNOWN</code> is returned. 
	 * 
	 * @return	the manufactured motor type, or <code>UNKNOWN</code>.
	 */
	public Motor.Type getMotorType() {
		return motorType;
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
	public static Manufacturer getManufacturer(String name) {
		String searchString = generateSearchString(name);
		Manufacturer m = manufacturers.get(searchString);
		if ( m != null ) {
			return m;
		}

		m = new Manufacturer(name.trim(), name.trim(), Motor.Type.UNKNOWN);

		// We need some additional external synchronization here so we lock on the manufacturers.
		synchronized( manufacturers ) {
			Manufacturer retest = manufacturers.get(searchString);
			if ( retest != null ) {
				// it exists now.
				return retest;
			}
			manufacturers.add(m);
		}
		return m;
	}
	
	private static String generateSearchString(String str) {
		return str.toLowerCase(Locale.getDefault()).replaceAll("[^a-zA-Z0-9]+", " ").trim();
	}
	
	private Object writeReplace() {
		return new ManufacturerSerializationProxy(this.displayName);
	}

	private static class ManufacturerSerializationProxy implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8311677686087002569L;
		String displayName;
		ManufacturerSerializationProxy( String displayName ) {
			this.displayName = displayName;
		}
		private Object readResolve() throws ObjectStreamException {
			return Manufacturer.getManufacturer(this.displayName);
		}
	}
	
}
