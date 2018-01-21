package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusPositionable;

class RadiusPositionSetter implements Setter {
	
	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		RadiusMethod method = (RadiusMethod) DocumentConfig.findEnum(attributes.get("method"), RadiusMethod.class);
		if (method == null) {
			method = RadiusMethod.SURFACE;
		}
		
		
		double offset;
		try {
			offset = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			warnings.add(String.format("Warning: invalid value radius position. value=%s    class: %s", value, c.getClass().getCanonicalName() ));
			return;
		}
		
		if ( RadiusPositionable.class.isAssignableFrom( c.getClass() ) ) {
			RadiusPositionable rp = (RadiusPositionable)c;
			rp.setRadiusMethod(method);
			rp.setRadiusOffset(offset);
		} else {
			warnings.add("Warning: radiusPositionable is not valid for this class: "+c.getClass().getCanonicalName());
		}
		
	}
}
