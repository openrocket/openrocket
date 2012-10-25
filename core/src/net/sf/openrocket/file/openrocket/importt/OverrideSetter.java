package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

class OverrideSetter implements Setter {
	private final Reflection.Method setMethod;
	private final Reflection.Method enabledMethod;
	
	public OverrideSetter(Reflection.Method set, Reflection.Method enabledMethod) {
		this.setMethod = set;
		this.enabledMethod = enabledMethod;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		try {
			double d = Double.parseDouble(s);
			setMethod.invoke(c, d);
			enabledMethod.invoke(c, true);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}