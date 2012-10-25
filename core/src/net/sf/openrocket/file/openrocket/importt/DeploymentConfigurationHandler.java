package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;

import org.xml.sax.SAXException;

class DeploymentConfigurationHandler extends AbstractElementHandler {
	private final RecoveryDevice recoveryDevice;
	private DeploymentConfiguration config;

	public DeploymentConfigurationHandler( RecoveryDevice recoveryDevice, DocumentLoadingContext context ) {
		this.recoveryDevice = recoveryDevice;
		config = new DeploymentConfiguration();
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
		
		if ( "deployevent".equals(element) ) {
			DeployEvent type = (DeployEvent) DocumentConfig.findEnum(content, DeployEvent.class);
			if ( type == null ) {
				warnings.add(Warning.FILE_INVALID_PARAMETER);
				return;
			}
			config.setDeployEvent( type );
			return;
		} else if ( "deployaltitude".equals(element) ) {
			config.setDeployAltitude( Double.parseDouble(content));
			return;
		} else if ( "deploydelay".equals(element) ) {
			config.setDeployDelay( Double.parseDouble(content));
			return;
		}
		super.closeElement(element, attributes, content, warnings);

	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		String configId = attributes.get("configid");
		recoveryDevice.setFlightConfiguration(configId, config);
	}
	
}