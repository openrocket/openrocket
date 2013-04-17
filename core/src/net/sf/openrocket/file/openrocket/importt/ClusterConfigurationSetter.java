package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.Clusterable;
import net.sf.openrocket.rocketcomponent.RocketComponent;

class ClusterConfigurationSetter implements Setter {
	
	@Override
	public void set(RocketComponent component, String value, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (!(component instanceof Clusterable)) {
			warnings.add("Illegal component defined as cluster.");
			return;
		}
		
		ClusterConfiguration config = null;
		for (ClusterConfiguration c : ClusterConfiguration.CONFIGURATIONS) {
			if (c.getXMLName().equals(value)) {
				config = c;
				break;
			}
		}
		
		if (config == null) {
			warnings.add("Illegal cluster configuration specified.");
			return;
		}
		
		((Clusterable) component).setClusterConfiguration(config);
	}
}