package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

//// BooleanSetter - set a boolean value
class BooleanSetter implements Setter {
	private final Reflection.Method setMethod;
	private Object[] extraParameters = null;
	
	public BooleanSetter(Reflection.Method set, Object... parameters) {
		setMethod = set;
		this.extraParameters = parameters;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		s = s.trim();
		final boolean setValue;
		if (s.equalsIgnoreCase("true")) {
			setValue = true;
		} else if (s.equalsIgnoreCase("false")) {
			setValue = false;
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		if (extraParameters != null) {
			Object[] parameters = new Object[extraParameters.length + 1];
			parameters[0] = setValue;
			System.arraycopy(extraParameters, 0, parameters, 1, extraParameters.length);
			setMethod.invoke(c, parameters);
		} else {
			setMethod.invoke(c, setValue);
		}
	}
}