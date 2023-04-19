package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.motor.IgnitionEvent;

class IgnitionConfigurationHandler extends AbstractElementHandler {
	
	// TODO: this is pretty hacky and should be fixed eventually 
	public Double ignitionDelay = null;
	public IgnitionEvent ignitionEvent = null;
	
	
	public IgnitionConfigurationHandler(DocumentLoadingContext context) {
		
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		content = content.trim();
		
		if (element.equals("ignitionevent")) {

			for (IgnitionEvent ie : IgnitionEvent.values()) {
				if (ie.equals(content)) {
					ignitionEvent = ie;
					break;
				}
			}
			if (ignitionEvent == null) {
				warnings.add(Warning.fromString("Unknown ignition event type '" + content + "', ignoring."));
			}
			
		} else if (element.equals("ignitiondelay")) {
			try {
				ignitionDelay = Double.parseDouble(content);
			} catch (NumberFormatException nfe) {
				warnings.add(Warning.fromString("Illegal ignition delay specified, ignoring."));
			}
		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}
	
}