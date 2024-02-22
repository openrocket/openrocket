package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Reflection;

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