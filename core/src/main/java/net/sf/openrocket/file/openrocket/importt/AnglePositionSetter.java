package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.AnglePositionable;

class AnglePositionSetter implements Setter {
	
	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		AngleMethod method = (AngleMethod) DocumentConfig.findEnum(attributes.get("method"), AngleMethod.class);
		if (null==method) {
			method=AngleMethod.RELATIVE;
		}
		
		double pos;
		try {
			pos = Math.toRadians(Double.parseDouble(value));
		} catch (NumberFormatException e) {
			warnings.add(String.format("Warning: invalid angle position. value=%s  (degrees)  class: %s", value, c.getClass().getCanonicalName() ));
			return;
		}

		if ( AnglePositionable.class.isAssignableFrom( c.getClass() ) ) {
			AnglePositionable apc = (AnglePositionable)c;
			apc.setAngleMethod(method);
			apc.setAngleOffset(pos);
		} else {
			warnings.add(String.format("Warning: %s is not valid for class: %s", this.getClass().getCanonicalName(), c.getClass().getCanonicalName()));
		}
		
	}
}
