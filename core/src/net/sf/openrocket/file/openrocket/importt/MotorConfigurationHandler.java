package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.Rocket;

import org.xml.sax.SAXException;

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
		
		String configid = attributes.remove("configid");
		if (configid == null || configid.equals("")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		if (!rocket.addMotorConfigurationID(configid)) {
			warnings.add("Duplicate motor configuration ID used.");
			return;
		}
		
		if (name != null && name.trim().length() > 0) {
			rocket.setFlightConfigurationName(configid, name);
		}
		
		if ("true".equals(attributes.remove("default"))) {
			rocket.getDefaultConfiguration().setFlightConfigurationID(configid);
		}
		
		super.closeElement(element, attributes, content, warnings);
	}
}