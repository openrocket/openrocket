package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Reflection;

////  IntSetter - set an integer value
class IntSetter implements Setter {
	private final Reflection.Method setMethod;

	public IntSetter(Reflection.Method set) {
		setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		try {
			int n = Integer.parseInt(s);
			setMethod.invoke(c, n);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}