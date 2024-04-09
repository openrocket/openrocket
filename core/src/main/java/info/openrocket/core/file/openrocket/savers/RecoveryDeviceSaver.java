package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.FlightConfigurableParameterSet;
import info.openrocket.core.rocketcomponent.RecoveryDevice;

public class RecoveryDeviceSaver extends MassObjectSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		RecoveryDevice dev = (RecoveryDevice) c;

		if (dev.isCDAutomatic())
			elements.add("<cd>auto</cd>");
		else
			elements.add("<cd>" + dev.getCD() + "</cd>");
		elements.add(materialParam(dev.getMaterial()));

		// NOTE: Default config must be BEFORE overridden config for proper backward
		// compatibility later on
		FlightConfigurableParameterSet<DeploymentConfiguration> configSet = dev.getDeploymentConfigurations();
		DeploymentConfiguration defaultConfig = configSet.getDefault();
		elements.addAll(addDeploymentConfigurationParams(defaultConfig, false));

		for (FlightConfigurationId fcid : configSet.getIds()) {
			if (dev.getDeploymentConfigurations().isDefault(fcid)) {
				continue;
			} else {
				// only print configurations which override the default.
				DeploymentConfiguration deployConfig = dev.getDeploymentConfigurations().get(fcid);
				elements.add("<deploymentconfiguration configid=\"" + fcid.key + "\">");
				elements.addAll(addDeploymentConfigurationParams(deployConfig, true));
				elements.add("</deploymentconfiguration>");
			}
		}
	}

	private List<String> addDeploymentConfigurationParams(DeploymentConfiguration config, boolean indent) {
		List<String> elements = new ArrayList<String>(3);
		elements.add((indent ? "  " : "") + "<deployevent>"
				+ config.getDeployEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "") + "</deployevent>");
		elements.add((indent ? "  " : "") + "<deployaltitude>" + config.getDeployAltitude() + "</deployaltitude>");
		elements.add((indent ? "  " : "") + "<deploydelay>" + config.getDeployDelay() + "</deploydelay>");
		return elements;

	}
}
