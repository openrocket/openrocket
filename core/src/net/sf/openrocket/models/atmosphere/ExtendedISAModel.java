package net.sf.openrocket.models.atmosphere;

import static net.sf.openrocket.models.atmosphere.AtmosphericConditions.R;
import net.sf.openrocket.util.MathUtil;


/**
 * An atmospheric temperature/pressure model based on the International Standard Atmosphere
 * (ISA).  The no-argument constructor creates an {@link AtmosphericModel} that corresponds
 * to the ISA model.  It is extended by the other constructors to allow defining a custom
 * first layer.  The base temperature and pressure are as given, and all other values
 * are calculated based on these.
 * <p>
 * TODO:  LOW:  Values at altitudes over 32km differ from standard results by ~5%.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ExtendedISAModel extends InterpolatingAtmosphericModel {
	
	public static final double STANDARD_TEMPERATURE = 288.15;
	public static final double STANDARD_PRESSURE = 101325;
	
	private static final double G = 9.80665;
	
	private final double[] layer = { 0, 11000, 20000, 32000, 47000, 51000, 71000, 84852 };
	private final double[] baseTemperature = {
			288.15, 216.65, 216.65, 228.65, 270.65, 270.65, 214.65, 186.95
	};
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
	 * @param temperature	the temperature at MSL.
	 * @param pressure		the pressure at MSL.
	 */
	public ExtendedISAModel(double temperature, double pressure) {
		this(0, temperature, pressure);
	}
	
	
	/**
	 * Construct an extended model with the given temperature and pressure at the
	 * specified altitude.  Conditions below the given altitude cannot be calculated,
	 * and the values at the specified altitude will be returned instead.  The altitude
	 * must be lower than the altitude of the next ISA standard layer (below 11km).
	 * 
	 * @param altitude		the altitude of the measurements.
	 * @param temperature	the temperature.
	 * @param pressure		the pressure.
	 * @throws IllegalArgumentException  if the altitude exceeds the second layer boundary
	 * 									 of the ISA model (over 11km).
	 */
	public ExtendedISAModel(double altitude, double temperature, double pressure) {
		if (altitude >= layer[1]) {
			throw new IllegalArgumentException("Too high first altitude: " + altitude);
		}
		
		layer[0] = altitude;
		baseTemperature[0] = temperature;
		basePressure[0] = pressure;
		
		for (int i = 1; i < basePressure.length; i++) {
			basePressure[i] = getExactConditions(layer[i] - 1).getPressure();
		}
	}
	
	
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
	
	
	public static void main(String foo[]) {
		ExtendedISAModel model1 = new ExtendedISAModel();
		ExtendedISAModel model2 = new ExtendedISAModel(278.15, 100000);
		
		for (double alt = 0; alt < 80000; alt += 500) {
			AtmosphericConditions cond1 = model1.getConditions(alt);
			AtmosphericConditions cond2 = model2.getConditions(alt);
			
			AtmosphericConditions diff = new AtmosphericConditions();
			diff.setPressure((cond2.getPressure() - cond1.getPressure()) / cond1.getPressure() * 100);
			diff.setTemperature((cond2.getTemperature() - cond1.getTemperature()) / cond1.getTemperature() * 100);
			//System.out.println("alt=" + alt +	": std:" + cond1 + " mod:" + cond2 + " diff:" + diff);
		}
	}
	
	@Override
	public int getModID() {
		return 0;
	}
	
}
