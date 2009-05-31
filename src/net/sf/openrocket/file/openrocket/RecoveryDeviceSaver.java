package net.sf.openrocket.file.openrocket;

import java.util.List;

import net.sf.openrocket.rocketcomponent.RecoveryDevice;


public class RecoveryDeviceSaver extends MassObjectSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		RecoveryDevice dev = (RecoveryDevice) c;

		if (dev.isCDAutomatic())
			elements.add("<cd>auto</cd>");
		else
			elements.add("<cd>" + dev.getCD() + "</cd>");

		elements.add("<deployevent>" + dev.getDeployEvent().name().toLowerCase() + "</deployevent>");
		elements.add("<deployaltitude>" + dev.getDeployAltitude() + "</deployaltitude>");
		elements.add("<deploydelay>" + dev.getDeployDelay() + "</deploydelay>");
		elements.add(materialParam(dev.getMaterial()));
	}

}
