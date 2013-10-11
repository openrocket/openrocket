package net.sf.openrocket.preset.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StringUtil;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Primary entry point for parsing component CSV files that are in Rocksim format.
 */
public abstract class RocksimComponentFileLoader {
	
	private static final PrintStream LOGGER = System.err;
	
	private String basePath = "";
	
	private File dir;
	
	protected List<RocksimComponentFileColumnParser> fileColumns = new ArrayList<RocksimComponentFileColumnParser>();
	
	/**
	 * Constructor.
	 *
	 * @param theBasePathToLoadFrom base path
	 */
	public RocksimComponentFileLoader(File theBasePathToLoadFrom) {
		dir = theBasePathToLoadFrom;
		basePath = dir.getAbsolutePath();
	}
	
	/**
	 * Constructor.
	 *
	 * @param theBasePathToLoadFrom base path
	 */
	public RocksimComponentFileLoader(String theBasePathToLoadFrom) {
		dir = new File(basePath);
		basePath = theBasePathToLoadFrom;
	}
	
	protected abstract RocksimComponentFileType getFileType();
	
	public void load() {
		try {
			load(getFileType());
		} catch (FileNotFoundException fex) {
			LOGGER.println(fex.getLocalizedMessage());
		}
	}
	
	/**
	 * Read a comma separated component file and return the parsed contents as a list of string arrays.  Not for
	 * production use - just here for smoke testing.
	 *
	 * @param type the type of component file to read; uses the default file name
	 *
	 * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
	 *         component data file; the element in the list itself is an array of String, where each item in the array
	 *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
	 */
	private void load(RocksimComponentFileType type) throws FileNotFoundException {
		if (!dir.exists()) {
			throw new IllegalArgumentException(basePath + " does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(basePath + " is not directory");
		}
		if (!dir.canRead()) {
			throw new IllegalArgumentException(basePath + " is not readable");
		}
		FileInputStream fis = new FileInputStream(new File(dir, type.getDefaultFileName()));
		load(fis);
	}
	
	/**
	 * Read a comma separated component file and return the parsed contents as a list of string arrays.
	 *
	 * @param file the file to read and parse
	 *
	 * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
	 *         component data file; the element in the list itself is an array of String, where each item in the array
	 *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
	 */
	@SuppressWarnings("unused")
	private void load(File file) throws FileNotFoundException {
		load(new FileInputStream(file));
	}
	
	/**
	 * Read a comma separated component file and return the parsed contents as a list of string arrays.
	 *
	 * @param is the stream to read and parse
	 *
	 * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
	 *         component data file; the element in the list itself is an array of String, where each item in the array
	 *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
	 */
	private void load(InputStream is) {
		if (is == null) {
			return;
		}
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(is);
			
			// Create the CSV reader.  Use comma separator.
			CSVReader reader = new CSVReader(r, ',', '\'', '\\');
			
			//Read and throw away the header row.
			parseHeaders(reader.readNext());
			
			String[] data = null;
			while ((data = reader.readNext()) != null) {
				// detect empty lines and skip:
				if (data.length == 0) {
					continue;
				}
				if (data.length == 1 && StringUtil.isEmpty(data[0])) {
					continue;
				}
				parseData(data);
			}
			//Read the rest of the file as data rows.
			return;
		} catch (IOException e) {
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	
	protected void parseHeaders(String[] headers) {
		for (RocksimComponentFileColumnParser column : fileColumns) {
			column.configure(headers);
		}
	}
	
	protected void parseData(String[] data) {
		if (data == null || data.length == 0) {
			return;
		}
		TypedPropertyMap props = new TypedPropertyMap();
		
		preProcess(data);
		
		for (RocksimComponentFileColumnParser column : fileColumns) {
			column.parse(data, props);
		}
		postProcess(props);
	}
	
	protected void preProcess(String[] data) {
		for (int i = 0; i < data.length; i++) {
			String d = data[i];
			if (d == null) {
				continue;
			}
			d = d.trim();
			d = stripAll(d, '"');
			
			data[i] = d;
		}
	}
	
	protected abstract void postProcess(TypedPropertyMap props);
	
	/**
	 * Rocksim CSV units are either inches or mm.  A value of 0 or "in." indicate inches.  A value of 1 or "mm" indicate
	 * millimeters.
	 *
	 * @param units the value from the file
	 *
	 * @return true if it's inches
	 */
	protected static boolean isInches(String units) {
		String tmp = units.trim().toLowerCase();
		return "0".equals(tmp) || tmp.startsWith("in");
	}
	
	/**
	 * Convert inches or millimeters to meters.
	 *
	 * @param units a Rocksim CSV string representing the kind of units.
	 * @param value the original value within the CSV file
	 *
	 * @return the value in meters
	 */
	protected static double convertLength(String units, double value) {
		if (isInches(units)) {
			return UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(value);
		}
		else {
			return UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(value);
		}
	}
	
	protected static double convertMass(String units, double value) {
		if ("oz".equals(units)) {
			Unit u = UnitGroup.UNITS_MASS.getUnit(2);
			return u.fromUnit(value);
		}
		return value;
	}
	
	/**
	 * Remove all occurrences of the given character.  Note: this is done because some manufacturers embed double quotes
	 * in their descriptions or material names.  Those are stripped away because they cause all sorts of matching/lookup
	 * issues.
	 *
	 * @param target      the target string to be operated upon
	 * @param toBeRemoved the character to remove
	 *
	 * @return target, minus every occurrence of toBeRemoved
	 */
	protected static String stripAll(String target, Character toBeRemoved) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < target.length(); i++) {
			Character c = target.charAt(i);
			if (!c.equals(toBeRemoved)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convert all words in a given string to Camel Case (first letter capitalized). Words are assumed to be separated
	 * by a space.  Note: this is done because some manufacturers define their material name in Camel Case but the
	 * component part references the material in lower case.  That causes matching/lookup issues that's easiest handled
	 * this way (rather than converting everything to lower case.
	 *
	 * @param target the target string to be operated upon
	 *
	 * @return target, with the first letter of each word in uppercase
	 */
	protected static String toCamelCase(String target) {
		StringBuilder sb = new StringBuilder();
		String[] t = target.split("[ ]");
		if (t != null && t.length > 0) {
			for (String aT : t) {
				String s = aT;
				s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
				sb.append(s).append(" ");
			}
			return sb.toString().trim();
		}
		else {
			return target;
		}
	}
	
}

//Errata:
//The oddities I've found thus far in the stock Rocksim data:
//1. BTDATA.CSV - Totally Tubular goofed up their part no. and description columns (They messed up TCDATA also)
//2. NCDATA.CSV - Estes Balsa nose cones are classified as G10 Fiberglass
//3. TRDATA.CSV - Apogee Saturn LEM Transition has no part number; Balsa Machining transitions have blank diameter
