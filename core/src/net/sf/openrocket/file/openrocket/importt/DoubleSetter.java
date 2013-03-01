package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.Reflection.Method;

////  DoubleSetter - sets a double value or (alternatively) if a specific string is encountered
////  calls a setXXX(boolean) method.
class DoubleSetter implements Setter {
	private final Reflection.Method configGetter;
	private final Reflection.Method setMethod;
	private final String specialString;
	private final Reflection.Method specialMethod;
	private final double multiplier;
	
	/**
	 * Set only the double value.
	 * @param set	set method for the double value. 
	 */
	public DoubleSetter(Reflection.Method set) {
		this.setMethod = set;
		this.configGetter = null;
		this.specialString = null;
		this.specialMethod = null;
		this.multiplier = 1.0;
	}
	
	/**
	 * Multiply with the given multiplier and set the double value.
	 * @param set	set method for the double value.
	 * @param mul	multiplier.
	 */
	public DoubleSetter(Reflection.Method set, double mul) {
		this.setMethod = set;
		this.configGetter = null;
		this.specialString = null;
		this.specialMethod = null;
		this.multiplier = mul;
	}
	
	/**
	 * Set the double value, or if the value equals the special string, use the
	 * special setter and set it to true.
	 * 
	 * @param set			double setter.
	 * @param special		special string
	 * @param specialMethod	boolean setter.
	 */
	public DoubleSetter(Reflection.Method set, String special,
			Reflection.Method specialMethod) {
		this.setMethod = set;
		this.configGetter = null;
		this.specialString = special;
		this.specialMethod = specialMethod;
		this.multiplier = 1.0;
	}
	
	
	/**
	 * Set a double value of the default configuration of a FlightConfiguration object.
	 * 
	 * @param configGetter	getter method for the FlightConfiguration object
	 * @param setter		setter method for the configuration object
	 */
	public DoubleSetter(Method configGetter, Method setter) {
		this.setMethod = setter;
		this.configGetter = configGetter;
		this.specialString = null;
		this.specialMethod = null;
		this.multiplier = 1.0;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		s = s.trim();
		
		// Check for special case
		if (specialMethod != null && s.equalsIgnoreCase(specialString)) {
			specialMethod.invoke(c, true);
			return;
		}
		
		// Normal case
		try {
			double d = Double.parseDouble(s);
			
			if (configGetter == null) {
				setMethod.invoke(c, d * multiplier);
			} else {
				FlightConfiguration<?> config = (FlightConfiguration<?>) configGetter.invoke(c);
				Object obj = config.getDefault();
				setMethod.invoke(obj, d * multiplier);
			}
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}