package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;

import org.xml.sax.SAXException;

/**
 * A singleton element handler that does not accept any content in the element
 * except whitespace text.  All subelements are ignored and a warning is produced
 * of them.  It ignores any attributes.
 * <p>
 * This class can be used for elements that have no content but contain attributes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class NullElementHandler extends AbstractElementHandler {
	public static final NullElementHandler INSTANCE = new NullElementHandler();

	private static final HashMap<String, String> EMPTY_MAP = new HashMap<String,String>();
	
	private NullElementHandler() {
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		super.closeElement(element, EMPTY_MAP, content, warnings);
	}

}
