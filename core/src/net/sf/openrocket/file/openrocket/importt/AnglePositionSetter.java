package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.AnglePositionable;

class AnglePositionSetter implements Setter {
	
	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		AngleMethod type = (AngleMethod) DocumentConfig.findEnum(attributes.get("method"), AngleMethod.class);
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

		if ( AnglePositionable.class.isAssignableFrom( c.getClass() ) ) {
			AnglePositionable apc = (AnglePositionable)c;
			apc.setAngleMethod(type);
			apc.setAngleOffset(pos);
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
		
	}
}
