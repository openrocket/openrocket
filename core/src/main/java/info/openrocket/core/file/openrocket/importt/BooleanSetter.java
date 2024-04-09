package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Reflection;

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