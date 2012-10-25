package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

////  StringSetter - sets the value to the contained String
class StringSetter implements Setter {
	private final Reflection.Method setMethod;
	
	public StringSetter(Reflection.Method set) {
		setMethod = set;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		setMethod.invoke(c, s);
	}
}