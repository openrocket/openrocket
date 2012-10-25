package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

////  EnumSetter  -  sets a generic enum type
class EnumSetter<T extends Enum<T>> implements Setter {
	private final Reflection.Method setter;
	private final Class<T> enumClass;
	
	public EnumSetter(Reflection.Method set, Class<T> enumClass) {
		this.setter = set;
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
		
		setter.invoke(c, setEnum);
	}
}