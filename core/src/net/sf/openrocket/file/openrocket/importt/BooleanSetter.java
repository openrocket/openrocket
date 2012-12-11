package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

//// BooleanSetter - set a boolean value
class BooleanSetter implements Setter {
	private final Reflection.Method setMethod;
	
	public BooleanSetter(Reflection.Method set) {
		setMethod = set;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		s = s.trim();
		if (s.equalsIgnoreCase("true")) {
			setMethod.invoke(c, true);
		} else if (s.equalsIgnoreCase("false")) {
			setMethod.invoke(c, false);
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}