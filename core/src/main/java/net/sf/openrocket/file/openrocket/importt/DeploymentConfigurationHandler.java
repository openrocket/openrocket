package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;

class DeploymentConfigurationHandler extends AbstractElementHandler {
	
	private final RecoveryDevice recoveryDevice;
	
	private DeployEvent event = null;
	private double delay = Double.NaN;
	private double altitude = Double.NaN;
	
	public DeploymentConfigurationHandler(RecoveryDevice component, DocumentLoadingContext context) {
		this.recoveryDevice = component;
	}
	
	public DeploymentConfiguration getConfiguration(DeploymentConfiguration def) {
		DeploymentConfiguration config = def.clone();
		if (event != null) {
			config.setDeployEvent(event);
		}
		if (!Double.isNaN(delay)) {
			config.setDeployDelay(delay);
		}
		if (!Double.isNaN(altitude)) {
			config.setDeployAltitude(altitude);
		}
		return config;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content,
			WarningSet warnings) throws SAXException {
		
		content = content.trim();
		
		if ("deployevent".equals(element)) {
			event = (DeployEvent) DocumentConfig.findEnum(content, DeployEvent.class);
			if (event == null) {
				warnings.add(Warning.FILE_INVALID_PARAMETER);
				return;
			}
			return;
		} else if ("deploydelay".equals(element)) {
			delay = parseDouble(content, warnings, Warning.FILE_INVALID_PARAMETER);
			return;
		} else if ("deployaltitude".equals(element)) {
			altitude = parseDouble(content, warnings, Warning.FILE_INVALID_PARAMETER);
			return;
		}
		super.closeElement(element, attributes, content, warnings);
		
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		FlightConfigurationId configId = new FlightConfigurationId(attributes.get("configid"));
		DeploymentConfiguration def = recoveryDevice.getDeploymentConfigurations().getDefault();
		recoveryDevice.getDeploymentConfigurations().set(configId, getConfiguration(def));
	}
	
}