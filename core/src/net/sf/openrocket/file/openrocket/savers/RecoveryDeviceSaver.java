package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;


public class RecoveryDeviceSaver extends MassObjectSaver {
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		RecoveryDevice dev = (RecoveryDevice) c;
		
		if (dev.isCDAutomatic())
			elements.add("<cd>auto</cd>");
		else
			elements.add("<cd>" + dev.getCD() + "</cd>");
		elements.add(materialParam(dev.getMaterial()));
		
		// NOTE:  Default config must be BEFORE overridden config for proper backward compatibility later on
		DeploymentConfiguration defaultConfig = dev.getDeploymentConfiguration().getDefault();
		elements.addAll(deploymentConfiguration(defaultConfig, false));
		
		Rocket rocket = c.getRocket();
		// Note - getFlightConfigurationIDs returns at least one element.  The first element
		// is null and means "default".
		String[] configs = rocket.getFlightConfigurationIDs();
		if (configs.length > 1) {
			
			for (String id : configs) {
				if (id == null) {
					continue;
				}
				if (dev.getDeploymentConfiguration().isDefault(id)) {
					continue;
				}
				DeploymentConfiguration config = dev.getDeploymentConfiguration().get(id);
				elements.add("<deploymentconfiguration configid=\"" + id + "\">");
				elements.addAll(deploymentConfiguration(config, true));
				elements.add("</deploymentconfiguration>");
			}
		}
	}
	
	private List<String> deploymentConfiguration(DeploymentConfiguration config, boolean indent) {
		List<String> elements = new ArrayList<String>(3);
		elements.add((indent ? "  " : "") + "<deployevent>" + config.getDeployEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "") + "</deployevent>");
		elements.add((indent ? "  " : "") + "<deployaltitude>" + config.getDeployAltitude() + "</deployaltitude>");
		elements.add((indent ? "  " : "") + "<deploydelay>" + config.getDeployDelay() + "</deploydelay>");
		return elements;
		
	}
}
