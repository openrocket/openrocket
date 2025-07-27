package info.openrocket.core.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.util.Coordinate;

/**
 * A handler that reads the <point> specifications within the freeformfinset's
 * <finpoints> elements.
 */
class FinSetPointHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FreeformFinSet finset;
	private final ArrayList<Coordinate> coordinates = new ArrayList<>();

	public FinSetPointHandler(FreeformFinSet finset, DocumentLoadingContext context) {
		this.finset = finset;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		String strx = attributes.remove("x");
		String stry = attributes.remove("y");
		if (strx == null || stry == null) {
			warnings.add(Warning.fromString("Illegal fin points specification, ignoring."));
			return;
		}
		try {
			double x = Double.parseDouble(strx);
			double y = Double.parseDouble(stry);
			coordinates.add(new Coordinate(x, y));
		} catch (NumberFormatException e) {
			warnings.add(Warning.fromString("Illegal fin points specification, ignoring."));
			return;
		}

		super.closeElement(element, attributes, content, warnings);
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		finset.setPoints(coordinates.toArray(new Coordinate[0]));
		// Update the tab position. This is because the tab position relies on the
		// finset length, but because the
		// <tabposition> tag comes before the <finpoints> tag in the .ork file, the tab
		// position will be set first,
		// using the default finset length, not the intended finset length that we
		// extract in this part. So we update
		// the tab position here to cope for the wrongly calculated tab position
		// earlier.
		finset.updateTabPosition();
	}
}