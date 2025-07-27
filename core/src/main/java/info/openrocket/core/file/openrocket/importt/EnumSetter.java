package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfigurableParameterSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Reflection;
import info.openrocket.core.util.Reflection.Method;

////  EnumSetter  -  sets a generic enum type
class EnumSetter<T extends Enum<T>> implements Setter {
	private final Reflection.Method configurationGetter;
	private final Reflection.Method setter;
	private final Class<T> enumClass;

	public EnumSetter(Reflection.Method setter, Class<T> enumClass) {
		this(null, setter, enumClass);
	}

	public EnumSetter(Method configurationGetter, Method setter, Class<T> enumClass) {
		this.configurationGetter = configurationGetter;
		this.setter = setter;
		this.enumClass = enumClass;
	}

	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {

		Enum<?> setEnum = DocumentConfig.findEnum(name, enumClass);
		if (setEnum == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		if (configurationGetter == null) {
			setter.invoke(c, setEnum);
		} else {
			FlightConfigurableParameterSet<?> config = (FlightConfigurableParameterSet<?>) configurationGetter
					.invoke(c);
			Object obj = config.getDefault();
			setter.invoke(obj, setEnum);
		}
	}
}