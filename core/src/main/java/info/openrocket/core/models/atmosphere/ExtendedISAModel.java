package info.openrocket.core.models.atmosphere;

import static info.openrocket.core.models.atmosphere.AtmosphericConditions.R;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;

/**
 * An atmospheric model based on the International Standard Atmosphere (ISA)
 * with extensions for custom launch site conditions. This model divides the
 * atmosphere into distinct layers based on the ISA standard, each with its
 * own temperature and pressure characteristics.
 *
 * Key Features:
 * 1. Implements standard ISA model by default
 * 2. Supports custom launch site conditions
 * 3. Maintains ISA behavior above launch site
 * 4. Uses interpolation for efficient computation
 *
 * Layer Structure:
 * - 0-11km (Troposphere): Temperature decreases linearly (288.15K -> 216.65K, -6.5 degC/km)
 * - 11-20km (Tropopause): Temperature constant (216.65K)
 * - 20-32km (Stratosphere 1): Temperature increases linearly (216.65K -> 228.65K, +1.0 degC/km)
 * - 32-47km (Stratosphere 2): Temperature increases linearly (228.65K -> 270.65K, +2.8 degC/km)
 * - 47-51km (Stratopause): Temperature constant (270.65K)
 * - 51-71km (Mesosphere 1): Temperature decreases linearly (270.65K -> 214.65K, -2.8 degC/km)
 * - 71-84.852km (Mesosphere 2): Temperature decreases linearly (214.65K -> 186.95K, -2.0 degC/km)
 * - > 84.852km (Mesopause): Temperature constant (186.95K)
 *
 * Usage:
 * 1. Standard ISA: new ExtendedISAModel()
 * 2. Custom sea level: new ExtendedISAModel(temperature, pressure)
 * 3. Custom altitude: new ExtendedISAModel(altitude, temperature, pressure)
 *
 *
 * TODO: LOW: Values at altitudes over 32km differ from standard results by ~5%.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ExtendedISAModel extends InterpolatingAtmosphericModel {
	/** Standard sea level temperature in Kelvin */
	public static final double STANDARD_TEMPERATURE = 288.15;

	/** Standard sea level pressure in Pascal */
	public static final double STANDARD_PRESSURE = 101325;

	/** Gravitational acceleration in m/s² */
	private static final double G = 9.80665;

	/**
	 * ISA atmospheric layers.
	 * Each element represents the altitude in meters where a new layer begins.
	 */
	private final double[] layer = { 0, 11000, 20000, 32000, 47000, 51000, 71000, 84852 };

	/**
	 * Base temperatures for each ISA layer in Kelvin.
	 * These define the temperature profile of the standard atmosphere.
	 */
	private final double[] baseTemperature = {
			288.15, 216.65, 216.65, 228.65, 270.65, 270.65, 214.65, 186.95
	};

	/**
	 * Base pressures for each layer, computed based on the temperature profile
	 * and the hydrostatic equation.
	 */
	private final double[] basePressure = new double[layer.length];

	/**
	 * Construct the standard ISA model.
	 */
	public ExtendedISAModel() {
		this(STANDARD_TEMPERATURE, STANDARD_PRESSURE);
	}

	/**
	 * Construct an extended model with the given temperature and pressure at MSL.
	 * 
	 * @param temperature the temperature at MSL.
	 * @param pressure    the pressure at MSL.
	 */
	public ExtendedISAModel(double temperature, double pressure) {
		this(0, temperature, pressure);
	}

	/**
	 * Construct an extended model with the given temperature and pressure at the
	 * specified altitude. Conditions below the given altitude cannot be calculated,
	 * and the values at the specified altitude will be returned instead. The
	 * altitude
	 * must be lower than the altitude of the next ISA standard layer (below 11km).
	 * 
	 * @param altitude    the altitude of the measurements.
	 * @param temperature the temperature.
	 * @param pressure    the pressure.
	 * @throws IllegalArgumentException if the altitude exceeds the second layer boundary of the ISA model (over 11km).
	 */
	public ExtendedISAModel(double altitude, double temperature, double pressure) {
		if (altitude >= layer[1]) {
			throw new IllegalArgumentException("Too high first altitude: " + altitude);
		}
		if (temperature <= 0) {
			throw new IllegalArgumentException("Temperature must be positive (Kelvin)");
		}
		if (pressure <= 0) {
			throw new IllegalArgumentException("Pressure must be positive (Pascals)");
		}

		layer[0] = altitude;
		baseTemperature[0] = temperature;
		basePressure[0] = pressure;

		for (int i = 1; i < basePressure.length; i++) {
			basePressure[i] = getExactConditions(layer[i] - 1).getPressure();
		}
	}

	/**
	 * Calculates exact atmospheric conditions at the specified altitude by interpolating between ISA layers.
	 * The pressure is calculated using the barometric formula, and the temperature is interpolated linearly.
	 * @param altitude The altitude in meters
	 * @return Atmospheric conditions at the specified altitude
	 */
	@Override
	protected AtmosphericConditions getExactConditions(double altitude) {
		altitude = MathUtil.clamp(altitude, layer[0], layer[layer.length - 1]);
		int n;
		for (n = 0; n < layer.length - 1; n++) {
			if (layer[n + 1] > altitude)
				break;
		}

		double rate = (baseTemperature[n + 1] - baseTemperature[n]) / (layer[n + 1] - layer[n]);

		double t = baseTemperature[n] + (altitude - layer[n]) * rate;
		double p;
		if (Math.abs(rate) > 0.001) {
			p = basePressure[n] *
					Math.pow(1 + (altitude - layer[n]) * rate / baseTemperature[n], -G / (rate * R));
		} else {
			p = basePressure[n] *
					Math.exp(-(altitude - layer[n]) * G / (R * baseTemperature[n]));
		}

		return new AtmosphericConditions(t, p);
	}

	@Override
	protected double getMaxAltitude() {
		return layer[layer.length - 1];
	}

	public static void main(String[] foo) {
		ExtendedISAModel model1 = new ExtendedISAModel();
		ExtendedISAModel model2 = new ExtendedISAModel(278.15, 100000);

		for (double alt = 0; alt < 80000; alt += 500) {
			AtmosphericConditions cond1 = model1.getConditions(alt);
			AtmosphericConditions cond2 = model2.getConditions(alt);

			AtmosphericConditions diff = new AtmosphericConditions();
			diff.setPressure((cond2.getPressure() - cond1.getPressure()) / cond1.getPressure() * 100);
			diff.setTemperature((cond2.getTemperature() - cond1.getTemperature()) / cond1.getTemperature() * 100);
			// System.out.println("alt=" + alt + ": std:" + cond1 + " mod:" + cond2 + "
			// diff:" + diff);
		}
	}

	@Override
	public ModID getModID() {
		return ModID.ZERO;
	}

}
