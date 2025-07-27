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

	/** Gravitational acceleration in m/s2 */
	private static final double G = 9.80665;

	/**
	 * ISA atmospheric layers.
	 * Each element represents the altitude in meters where a new layer begins.
	 */
	private static final double[] STANDARD_LAYERS = { 0, 11000, 20000, 32000, 47000, 51000, 71000, 84852 };
	/**
	 * Base temperatures for each ISA layer in Kelvin.
	 * These define the temperature profile of the standard atmosphere.
	 */
	private static final double[] STANDARD_TEMPERATURES = { 288.15, 216.65, 216.65, 228.65, 270.65, 270.65, 214.65, 186.95 };

	// The actual layer and temperature arrays used by this model
	private final double[] layer;
	private final double[] baseTemperature;

	/**
	 * Base pressures for each layer, computed based on the temperature profile and the barometric formula.
	 */
	private final double[] basePressure;

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
		if (altitude >= STANDARD_LAYERS[1]) {
			throw new IllegalArgumentException("Too high first altitude: " + altitude);
		}
		if (temperature <= 0) {
			throw new IllegalArgumentException("Temperature must be positive (Kelvin)");
		}
		if (pressure <= 0) {
			throw new IllegalArgumentException("Pressure must be positive (Pascals)");
		}

		// If altitude is not 0, we need to create a new layer structure
		if (altitude > 0) {
			// Create new arrays with one extra layer
			final int newSize = STANDARD_LAYERS.length + 1;
			layer = new double[newSize];
			baseTemperature = new double[newSize];
			basePressure = new double[newSize];

			// Standard second layer values (11km)
			double layer1Alt = STANDARD_LAYERS[1];
			double layer1Temp = STANDARD_TEMPERATURES[1];

			// Calculate temperature lapse rate between altitude and 11km
			double tempRate = (layer1Temp - temperature) / (layer1Alt - altitude);

			// Back-calculate sea level temperature using the same lapse rate
			double seaLevelTemp = temperature - tempRate * altitude;

			// Set up the layers
			layer[0] = 0;                  // Sea level
			layer[1] = altitude;           // Custom altitude
			baseTemperature[0] = seaLevelTemp;
			baseTemperature[1] = temperature;
			basePressure[0] = calculatePressure(0, seaLevelTemp, altitude, temperature, pressure);
			basePressure[1] = pressure;

			// Copy remaining standard layers
			for (int i = 2; i < layer.length; i++) {
				layer[i] = STANDARD_LAYERS[i-1];
				baseTemperature[i] = STANDARD_TEMPERATURES[i-1];
			}
		} else {
			layer = STANDARD_LAYERS.clone();
			baseTemperature = STANDARD_TEMPERATURES.clone();
			basePressure = new double[layer.length];
			layer[0] = 0;
			baseTemperature[0] = temperature;
			basePressure[0] = pressure;
		}

		// Calculate pressures for all remaining layers
		for (int i = (altitude > 0 ? 2 : 1); i < basePressure.length; i++) {
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
		// Clamp altitude to be within defined layers
		altitude = MathUtil.clamp(altitude, layer[0], layer[layer.length - 1]);

		// Find the correct layer
		int startLayer;
		for (startLayer = 0; startLayer < layer.length - 1; startLayer++) {
			if (layer[startLayer + 1] > altitude) {
				break;
			}
		}

		double altDiff = altitude - layer[startLayer];

		double startTemp = baseTemperature[startLayer];
		// Temperature lapse rate
		double tempRate = (baseTemperature[startLayer + 1] - startTemp) / (layer[startLayer + 1] - layer[startLayer]);

		double temp = startTemp + altDiff * tempRate;
		double startPress = basePressure[startLayer];
		double press = calculatePressure(altitude, temp, layer[startLayer], startTemp, startPress);

		return new AtmosphericConditions(temp, press);
	}

	/**
	 * Calculate pressure at sea level given conditions at altitude.
	 * Uses the barometric formula (<a href="https://en.wikipedia.org/wiki/Barometric_formula">source</a>).
	 */
	private double calculatePressure(double alt1, double temp1, double alt2, double temp2, double press2) {
		double tempRate = (temp2 - temp1) / (alt2 - alt1);
		if (Math.abs(tempRate) > 0.000001) {
			// Non-isothermal case
			return press2 / Math.pow(1 + (alt2 - alt1) * tempRate / temp1, -G / (tempRate * R));
		} else {
			// Isothermal case
			return press2 / Math.exp(-(alt2 - alt1) * G / (R * temp1));
		}
	}

	@Override
	protected double getMaxAltitude() {
		return layer[layer.length - 1];
	}

	/**
	 * Get the maximum allowed launch site altitude where the model is valid.
	 * @return The maximum altitude in meters
	 */
	public static double getMaximumAllowedAltitude() {
		return STANDARD_LAYERS[1] - 1;
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
