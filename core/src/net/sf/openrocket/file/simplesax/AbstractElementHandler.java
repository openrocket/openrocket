package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;

import org.xml.sax.SAXException;


/**
 * An abstract base class for creating an ElementHandler.  This implements the close
 * methods so that warnings are generated for spurious content.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractElementHandler implements ElementHandler {
	
	@Override
	public abstract ElementHandler openElement(String element,
			HashMap<String, String> attributes, WarningSet warnings) throws SAXException;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation is to add warnings for any textual content or attributes.
	 * This is useful for generating warnings for unknown XML attributes.
	 */
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		if (!content.trim().equals("")) {
			warnings.add(Warning.fromString("Unknown text in element '" + element
					+ "', ignoring."));
		}
		if (!attributes.isEmpty()) {
			warnings.add(Warning.fromString("Unknown attributes in element '" + element
					+ "', ignoring."));
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation is a no-op.
	 */
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		// No-op
	}
	
	
	/**
	 * Helper method for parsing a double value safely.
	 * 
	 * @param str		the string to parse
	 * @param warnings	the warning set
	 * @param warn		the warning to add if the value fails to parse
	 * @return			the double value, or NaN if an error occurred
	 */
	protected double parseDouble(String str, WarningSet warnings, Warning warn) {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			warnings.add(warn);
			return Double.NaN;
		}
	}
}
