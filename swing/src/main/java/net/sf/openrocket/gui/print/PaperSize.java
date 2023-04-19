package net.sf.openrocket.gui.print;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

public enum PaperSize {
	A3("A3", PageSize.A3),
	A4("A4", PageSize.A4),
	A5("A5", PageSize.A5),
	LETTER("Letter", PageSize.LETTER),
	LEGAL("Legal", PageSize.LEGAL);
	
	private final String name;
	private final Rectangle size;
	
	private PaperSize(String name, Rectangle size) {
		this.name = name;
		this.size = size;
	}
	
	public Rectangle getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	
	
	//////////////////////////
	
	private static final Logger log = LoggerFactory.getLogger(PaperSize.class);
	private static PaperSize defaultSize = null;
	
	/**
	 * Return the default paper size for the current system.
	 * @return	the default paper size
	 */
	public static PaperSize getDefault() {
		if (defaultSize == null) {
			
			// Test environment variable "PAPERSIZE" (Unix)
			defaultSize = getDefaultFromEnvironmentVariable();
			if (defaultSize != null) {
				log.info("Selecting default paper size from PAPERSIZE environment variable: " + defaultSize);
				return defaultSize;
			}
			
			// Test /etc/papersize (Unix)
			defaultSize = getDefaultFromEtcPapersize();
			if (defaultSize != null) {
				log.info("Selecting default paper size from /etc/papersize: " + defaultSize);
				return defaultSize;
			}
			
			// Test user.country
			defaultSize = getDefaultForCountry(System.getProperty("user.country"));
			if (defaultSize != null) {
				log.info("Selecting default paper size based on user.country: " + defaultSize);
				return defaultSize;
			}
			
			// Test locale country
			defaultSize = getDefaultForCountry(Locale.getDefault().getCountry());
			if (defaultSize != null) {
				log.info("Selecting default paper size based on locale country: " + defaultSize);
				return defaultSize;
			}
			
			// Fallback to A4
			defaultSize = A4;
			log.info("Selecting default paper size fallback: " + defaultSize);
		}
		
		return defaultSize;
	}
	
	
	/**
	 * Attempt to read the default paper size from the "PAPERSIZE" environment variable.
	 * 
	 * @return	the default paper size if successful, or <code>null</code> if unable to read/parse file.
	 */
	private static PaperSize getDefaultFromEnvironmentVariable() {
		String str = System.getenv("PAPERSIZE");
		return getSizeFromString(str);
	}
	
	/**
	 * Attempt to read the default paper size from the file defined by the environment variable
	 * PAPERCONF or from /etc/papersize.
	 * 
	 * @return	the default paper size if successful, or <code>null</code> if unable to read/parse file.
	 */
	private static PaperSize getDefaultFromEtcPapersize() {
		
		// Find file to read
		String file = System.getenv("PAPERCONF");
		if (file == null) {
			file = "/etc/papersize";
		}
		
		// Attempt to read the file
		BufferedReader in = null;
		try {
			
			String str;
			in = new BufferedReader(new FileReader(file));
			while ((str = in.readLine()) != null) {
				if (str.matches("^\\s*(#.*|$)")) {
					continue;
				}
				break;
			}
			
			return getSizeFromString(str);
			
		} catch (IOException e) {
			
			// Could not read file
			return null;
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
	/**
	 * Get a paper size based on a string.  The string is trimmed and case-insensitively 
	 * compared to the base names of the paper sizes.
	 * 
	 * @param size	the size string (may be null)
	 * @return		the corresponding paper size, or null if unknown
	 */
	static PaperSize getSizeFromString(String size) {
		if (size == null) {
			return null;
		}
		
		size = size.trim();
		for (PaperSize p : PaperSize.values()) {
			if (p.name.equalsIgnoreCase(size)) {
				return p;
			}
		}
		return null;
	}
	
	
	/**
	 * Get default paper size for a specific country.  This method falls back to A4 for
	 * any country not known to use Letter.
	 * 
	 * @param country	the 2-char country code (may be null)
	 * @return			the paper size, or <code>null</code> if country is not a country code
	 */
	static PaperSize getDefaultForCountry(String country) {
		/*
		 * List is based on info from http://unicode.org/repos/cldr-tmp/trunk/diff/supplemental/territory_language_information.html
		 * OpenOffice.org agrees with this:  http://wiki.services.openoffice.org/wiki/DefaultPaperSize#Summary
		 */
		final String[] letterCountries = { "BZ", "CA", "CL", "CO", "CR", "SV", "GT", "MX", "NI", "PA", "PH", "PR", "US", "VE" };
		
		if (country == null || !country.matches("^[a-zA-Z][a-zA-Z]$")) {
			return null;
		}
		
		country = country.toUpperCase(Locale.ENGLISH);
		for (String c : letterCountries) {
			if (c.equals(country)) {
				return LETTER;
			}
		}
		return A4;
	}
	
}
