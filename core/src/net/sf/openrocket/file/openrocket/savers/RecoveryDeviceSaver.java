package net.sf.openrocket.file.openrocket.savers;

import java.util.List;
import java.util.Locale;

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
		
		elements.add("<deployevent>" + dev.getDefaultDeployEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "") + "</deployevent>");
		elements.add("<deployaltitude>" + dev.getDefaultDeployAltitude() + "</deployaltitude>");
		elements.add("<deploydelay>" + dev.getDefaultDeployDelay() + "</deploydelay>");
		elements.add(materialParam(dev.getMaterial()));
		
		// FIXME - add mapping.
	}
	
}
