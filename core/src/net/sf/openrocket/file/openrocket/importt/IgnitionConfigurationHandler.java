package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;
import java.util.Locale;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;

import org.xml.sax.SAXException;

class IgnitionConfigurationHandler extends AbstractElementHandler {
	/** File version where latest digest format was introduced */
	private static final int MOTOR_DIGEST_VERSION = 104;
	
	private final DocumentLoadingContext context;
	
	private Double ignitionDelay = null;
	private MotorConfiguration.IgnitionEvent ignitionEvent = null;
	
	public IgnitionConfigurationHandler(DocumentLoadingContext context) {
		this.context = context;
	}
	
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}
	
	
	public Double getIgnitionDelay() {
		return ignitionDelay;
	}
	
	public MotorConfiguration.IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		content = content.trim();
		
		if (element.equals("ignitionevent")) {
			
			for (MotorConfiguration.IgnitionEvent e : MotorConfiguration.IgnitionEvent.values()) {
				if (e.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "").equals(content)) {
					ignitionEvent = e;
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