package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.TubeFinSet;

class PositionSetter implements Setter {
	
	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		RocketComponent.Position type = (Position) DocumentConfig.findEnum(attributes.get("type"),
				RocketComponent.Position.class);
		if (type == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		double pos;
		try {
			pos = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		if (c instanceof FinSet) {
			((FinSet) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else if (c instanceof LaunchLug) {
			((LaunchLug) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else if (c instanceof InternalComponent) {
			((InternalComponent) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else if (c instanceof TubeFinSet) {
			((TubeFinSet) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
		
	}
}