package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;

/**
 * An element handler that does not allow any sub-elements.  If any are encountered
 * a warning is generated and they are ignored.
 */
public class PlainTextHandler extends AbstractElementHandler {
	public static final PlainTextHandler INSTANCE = new PlainTextHandler();

	private PlainTextHandler() {
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		// Warning from openElement is sufficient.
	}
}

