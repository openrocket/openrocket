package net.sf.openrocket.file.openrocket.importt;

import java.util.Arrays;
import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameterSet;
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
	private String separator;
	private Object[] extraParameters = null;
	
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
	 * @param parameters	(optional) extra parameter set to use for the setter method.
	 */
	public DoubleSetter(Reflection.Method set, String special,
						Reflection.Method specialMethod, Object... parameters) {
		this.setMethod = set;
		this.configGetter = null;
		this.specialString = special;
		this.specialMethod = specialMethod;
		this.multiplier = 1.0;
		this.extraParameters = parameters;
	}

	/**
	 * Set the double value, or if the value equals the special string, use the
	 * special setter and set it to true. If the input string contains more information
	 * besides the special string, you can specify which separator should be used for
	 * this extra information. The part before the separator is then the special string
	 * and the part after the separator is the set value.
	 *
	 * @param set			double setter.
	 * @param special		special string
	 * @param specialMethod	boolean setter.
	 * @param parameters	(optional) extra parameter set to use for the setter method.
	 */
	public DoubleSetter(Reflection.Method set, String special, String separator,
						Reflection.Method specialMethod, Object... parameters) {
		this(set, special, specialMethod);
		this.separator = separator;
		this.extraParameters = parameters;
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
		String special = s;
		String data = s;
		String[] args = null;

		// Extract special string and data if s contains multiple data elements, separated by separator
		if (separator != null) {
			args = s.split(this.separator);
			if (args.length > 1) {
				special = args[0];
				data = String.join(separator, Arrays.copyOfRange(args, 1, args.length));
			}
		}
		
		// Normal case
		if (!special.equalsIgnoreCase(specialString) || (args != null && args.length > 1)) {
			try {
				double d = Double.parseDouble(data);

				Object obj = c;
				if (configGetter != null) {
					FlightConfigurableParameterSet<?> config = (FlightConfigurableParameterSet<?>) configGetter.invoke(c);
					obj = config.getDefault();
				}
				if (extraParameters != null) {
					Object[] parameters = new Object[extraParameters.length + 1];
					parameters[0] = d * multiplier;
					System.arraycopy(extraParameters, 0, parameters, 1, extraParameters.length);
					setMethod.invoke(obj, parameters);
				} else {
					setMethod.invoke(obj, d * multiplier);
				}
			} catch (NumberFormatException e) {
				warnings.add(Warning.FILE_INVALID_PARAMETER + " data: '" + data + "' - " + c.getName());
			}
		}

		// Check for special case
		if (specialMethod != null && special.equalsIgnoreCase(specialString)) {
			specialMethod.invoke(c, true);
		}
	}
}