package info.openrocket.core.models.atmosphere;

import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.ModID;

/**
 * Represents atmospheric conditions at a specific point, containing fundamental
 * properties (temperature and pressure) and methods to calculate derived properties.
 * This class serves as the basic unit of atmospheric data in the simulation.
 */
public class AtmosphericConditions implements Cloneable, Monitorable {

	/** Specific gas constant of dry air (J/(kg*K)). */
	public static final double R = 287.053;

	/** Specific heat ratio of air (dimensionless). */
	public static final double GAMMA = 1.4;

	/** Ratio of the molar masse of water vapor and dry air */
	public static final double EPSILON = 0.622;

	/** The standard air pressure (Pa). */
	public static final double STANDARD_PRESSURE = 101325.0;

	/** The standard air temperature (K). */
	public static final double STANDARD_TEMPERATURE = 293.15;

	/** The standard air humidity. */
	public static final double STANDARD_HUMIDITY = 0;

	/** Air pressure, in Pascals. */
	private double pressure;

	/** Air temperature, in Kelvins. */
	private double temperature;

	/** Relative air humidity. */
	private double humidity;

	private ModID modID;

	/**
	 * Construct standard atmospheric conditions.
	 */
	public AtmosphericConditions() {
		this(STANDARD_TEMPERATURE, STANDARD_PRESSURE, STANDARD_HUMIDITY);
	}

	public AtmosphericConditions(double temperature, double pressure) {
		this.setTemperature(temperature);
		this.setPressure(pressure);
		this.setHumidity(STANDARD_HUMIDITY);
		this.modID = new ModID();
	}

	/**
	 * Construct specified atmospheric conditions.
	 * 
	 * @param temperature the temperature in Kelvins.
	 * @param pressure    the pressure in Pascals.
	 */
	public AtmosphericConditions(double temperature, double pressure, double humidity) {
		this.setTemperature(temperature);
		this.setPressure(pressure);
		this.setHumidity(humidity);
		this.modID = new ModID();
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		if (pressure <= 0) {
			throw new IllegalArgumentException("Pressure must be positive (Pascals)");
		}
		this.pressure = pressure;
		this.modID = new ModID();
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		if (temperature <= 0) {
			throw new IllegalArgumentException("Temperature must be positive (Kelvin)");
		}
		this.temperature = temperature;
		this.modID = new ModID();
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		if (humidity < 0 || humidity > 1) {
			throw new IllegalArgumentException("Humidity must be between 0 and 1");
		}
		this.humidity = humidity;
		this.modID = new ModID();
	}

	/**
	 * Calculate the saturation water pressure using the Clausius-Clapeyron equation.
	 * @return The saturation vapor pressure in Pa
	 */
	public double vaporPressureSaturation() {
		// 611.3 * Math.exp(5423 * (1/273.15 - 1/getTemperature()));
		return 611.3 * Math.exp(19.854 - 5423/getTemperature());
	}

	/**
	 * Calculate the gas constant of humid air.
     * - EPSILON is the ratio of the molar masse of water vapor and dry air
     * - R is the gas constant of dry air
     * - e_s(T) is the temperature-dependent saturation vapor pressure
     * - RH is the relative humidity
     *
     * @return The gas constant of air in J/kg*K
	 * @return R if humidity is 0
	 */
    public double getGasConstant() {
		if (getHumidity() != 0) {
			double numerator = EPSILON * getHumidity() * vaporPressureSaturation();
			double denominator = getPressure() - getHumidity() * vaporPressureSaturation() * (1 - EPSILON);
			double scalingFactor = (1/EPSILON - 1);
		
			return R * (1 + numerator*scalingFactor/denominator);
		} else {
			return R;
		}
    }
	/**
	 * Calculate the current density of air using the ideal gas law for dry air.
	 * The formula used is rho = P/(R*T) where:
	 * - rho is the density in kg/m3
	 * - P is the pressure in Pa
	 * - R is the gas constant for air
	 * - T is the temperature in Kelvin
	 *
	 * @return The current air density in kg/m3
	 */
	public double getDensity() {
		return getPressure() / (getGasConstant() * getTemperature());
	}

	/**
	 * Return the current speed of sound for dry air.
	 * <p>
	 * The speed of sound is calculated using the expansion around the temperature 0
	 * C
	 * <code> c = 331.3 + 0.606*T </code> where T is in Celsius. The result is
	 * accurate
	 * to about 0.5 m/s for temperatures between -30 and 30 C, and within 2 m/s
	 * for temperatures between -55 and 30 C.
	 * 
	 * @return the current speed of sound.
	 */
	public double getMachSpeed() {
		return 165.77 + 0.606 * getTemperature();
	}

	/**
	 * Return the current kinematic viscosity of the air.
	 * <p>
	 * The effect of temperature on the viscosity of a gas can be computed using
	 * Sutherland's formula. In the region of -40 ... 40 degrees Celsius the effect
	 * is highly linear, and thus a linear approximation is used in its stead.
	 * This is divided by the result of {@link #getDensity()} to achieve the
	 * kinematic viscosity.
	 * 
	 * @return the current kinematic viscosity.
	 */
	public double getKinematicViscosity() {
		double v = 3.7291e-06 + 4.9944e-08 * getTemperature();
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
			throw new BugException("CloneNotSupportedException encountered!");
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof AtmosphericConditions))
			return false;
		AtmosphericConditions o = (AtmosphericConditions) other;
		return MathUtil.equals(this.pressure, o.pressure) && MathUtil.equals(this.temperature, o.temperature);
	}

	@Override
	public int hashCode() {
		return (int) (this.pressure + this.temperature * 1000);
	}

	@Override
	public ModID getModID() {
		return modID;
	}

	@Override
	public String toString() {
		return String.format("AtmosphericConditions[T=%.2f,P=%.2f]", getTemperature(), getPressure());
	}

}
