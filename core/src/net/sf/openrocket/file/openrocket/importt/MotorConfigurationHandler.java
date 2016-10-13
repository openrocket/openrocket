package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;

class MotorConfigurationHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final Rocket rocket;
	private String name = null;
	private boolean inNameElement = false;
	
	public MotorConfigurationHandler(Rocket rocket, DocumentLoadingContext context) {
		this.rocket = rocket;
		this.context = context;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (inNameElement || !element.equals("name")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return null;
		}
		inNameElement = true;
		
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		name = content;
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		FlightConfigurationId fcid = new FlightConfigurationId(attributes.remove("configid"));
		if (!fcid.isValid()) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		rocket.createFlightConfiguration(fcid);
		
		if (name != null && name.trim().length() > 0) {
			rocket.getFlightConfiguration(fcid).setName(name);
		}
		
		if ("true".equals(attributes.remove("default"))) {
			rocket.setSelectedConfiguration( fcid);
		}
		
		super.closeElement(element, attributes, content, warnings);
	}
}