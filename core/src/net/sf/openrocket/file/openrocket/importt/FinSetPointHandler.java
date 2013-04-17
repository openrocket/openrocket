package net.sf.openrocket.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.util.Coordinate;

import org.xml.sax.SAXException;

/**
 * A handler that reads the <point> specifications within the freeformfinset's
 * <finpoints> elements.
 */
class FinSetPointHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FreeformFinSet finset;
	private final ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
	
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
		try {
			finset.setPoints(coordinates.toArray(new Coordinate[0]));
		} catch (IllegalFinPointException e) {
			warnings.add(Warning.fromString("Freeform fin set point definitions illegal, ignoring."));
		}
	}
}