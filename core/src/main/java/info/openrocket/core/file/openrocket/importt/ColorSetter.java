package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Reflection;

////  ColorSetter  -  sets a Color value
class ColorSetter implements Setter {
	private final Reflection.Method setMethod;
	
	public ColorSetter(Reflection.Method set) {
		setMethod = set;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		ORColor color = ORColor.fromXMLAttributes(attributes);
		if (color == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		setMethod.invoke(c, color);

		if (!s.trim().isEmpty()) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}