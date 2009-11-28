package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.util.BugException;

public class AtmosphericConditions implements Cloneable {

	/** Specific gas constant of dry air. */
	public static final double R = 287.053;
	
	/** Specific heat ratio of air. */
	public static final double GAMMA = 1.4;
	
	/** The standard air pressure (1.01325 bar). */
	public static final double STANDARD_PRESSURE = 101325.0;
	
	/** The standard air temperature (20 degrees Celcius). */
	public static final double STANDARD_TEMPERATURE = 293.15;
	
	
	
	/** Air pressure, in Pascals. */
	public double pressure = STANDARD_PRESSURE;
	
	/** Air temperature, in Kelvins. */
	public double temperature = STANDARD_TEMPERATURE;
	
	
	/**
	 * Construct standard atmospheric conditions.
	 */
	public AtmosphericConditions() {
		
	}
	
	/**
	 * Construct specified atmospheric conditions.
	 * 
	 * @param temperature	the temperature in Kelvins.
	 * @param pressure		the pressure in Pascals.
	 */
	public AtmosphericConditions(double temperature, double pressure) {
		this.temperature = temperature;
		this.pressure = pressure;
	}
	
	
	
	/**
	 * Return the current density of air for dry air.
	 * 
	 * @return   the current density of air.
	 */
	public double getDensity() {
		return pressure / (R*temperature);
	}
	
	
	/**
	 * Return the current speed of sound for dry air.
	 * <p>
	 * The speed of sound is calculated using the expansion around the temperature 0 C
	 * <code> c = 331.3 + 0.606*T </code> where T is in Celcius.  The result is accurate
	 * to about 0.5 m/s for temperatures between -30 and 30 C, and within 2 m/s
	 * for temperatures between -55 and 30 C.
	 * 
	 * @return   the current speed of sound.
	 */
	public double getMachSpeed() {
		return 165.77 + 0.606 * temperature;
	}
	
	
	/**
	 * Return the current kinematic viscosity of the air.
	 * <p>
	 * The effect of temperature on the viscosity of a gas can be computed using
	 * Sutherland's formula.  In the region of -40 ... 40 degrees Celcius the effect
	 * is highly linear, and thus a linear approximation is used in its stead.
	 * This is divided by the result of {@link #getDensity()} to achieve the
	 * kinematic viscosity.
	 * 
	 * @return	the current kinematic viscosity.
	 */
	public double getKinematicViscosity() {
		double v = 3.7291e-06 + 4.9944e-08 * temperature;
		return v / getDensity();
	}
	
	/**
	 * Return a copy of the atmospheric conditions.
	 */
	@Override
	public AtmosphericConditions clone() {
		try {
			return (AtmosphericConditions) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("BUG:  CloneNotSupportedException encountered!");
		}
	}
	
	
	@Override
	public String toString() {
		return String.format("AtmosphericConditions[T=%.2f,P=%.2f]", temperature, pressure);
	}
	
}
