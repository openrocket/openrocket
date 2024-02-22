package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.AxialPositionable;

class AxialPositionSetter implements Setter {

	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {

		// first check preferred attribute name:
		AxialMethod type = (AxialMethod) DocumentConfig.findEnum(attributes.get("method"), AxialMethod.class);
		// fall-back to old name
		if (null == type) {
			type = (AxialMethod) DocumentConfig.findEnum(attributes.get("type"), AxialMethod.class);
		}

		if (type == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		double pos;
		try {
			pos = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			warnings.add(String.format("Warning: invalid value radius position. value=%s    class: %s", value,
					c.getClass().getCanonicalName()));
			return;
		}

		if (AxialPositionable.class.isAssignableFrom(c.getClass())) {
			AxialPositionable apc = (AxialPositionable) c;
			apc.setAxialMethod(type);
			apc.setAxialOffset(pos);
		} else {
			warnings.add(String.format("Warning: %s is not valid for class: %s", this.getClass().getCanonicalName(),
					c.getClass().getCanonicalName()));
		}

	}
}