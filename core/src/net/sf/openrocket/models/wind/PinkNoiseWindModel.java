package net.sf.openrocket.models.wind;

import java.util.Random;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PinkNoise;

/**
 * A wind simulator that generates wind speed as pink noise from a specified average wind speed
 * and standard deviance.  Currently the wind is always directed in the direction of the negative
 * X-axis.  The simulated wind is unaffected by the altitude.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PinkNoiseWindModel implements WindModel {
	
	/** Random value with which to XOR the random seed value */
	private static final int SEED_RANDOMIZATION = 0x7343AA03;
	
	
	
	/** Pink noise alpha parameter. */
	private static final double ALPHA = 5.0 / 3.0;
	
	/** Number of poles to use in the pink noise IIR filter. */
	private static final int POLES = 2;
	
	/** The standard deviation of the generated pink noise with the specified number of poles. */
	private static final double STDDEV = 2.252;
	
	/** Time difference between random samples. */
	private static final double DELTA_T = 0.05;
	
	
	private double average = 0;
	private double direction = Math.PI / 2; // this is an East wind
	private double standardDeviation = 0;
	
	private final int seed;
	
	private PinkNoise randomSource = null;
	private double time1;
	private double value1, value2;
	
	
	/**
	 * Construct a new wind simulation with a specific seed value.
	 * @param seed	the seed value.
	 */
	public PinkNoiseWindModel(int seed) {
		this.seed = seed ^ SEED_RANDOMIZATION;
	}
	
	
	
	/**
	 * Return the average wind speed.
	 * 
	 * @return the average wind speed.
	 */
	public double getAverage() {
		return average;
	}
	
	/**
	 * Set the average wind speed.  This method will also modify the
	 * standard deviation such that the turbulence intensity remains constant.
	 * 
	 * @param average the average wind speed to set
	 */
	public void setAverage(double average) {
		double intensity = getTurbulenceIntensity();
		this.average = Math.max(average, 0);
		setTurbulenceIntensity(intensity);
	}
	
	public void setDirection(double direction) {
		this.direction = direction;
	}
	
	public double getDirection() {
		return this.direction;
	}
	
	/**
	 * Return the standard deviation from the average wind speed.
	 * 
	 * @return the standard deviation of the wind speed
	 */
	public double getStandardDeviation() {
		return standardDeviation;
	}
	
	/**
	 * Set the standard deviation of the average wind speed.
	 * 
	 * @param standardDeviation the standardDeviation to set
	 */
	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = Math.max(standardDeviation, 0);
	}
	
	
	/**
	 * Return the turbulence intensity (standard deviation / average).
	 * 
	 * @return  the turbulence intensity
	 */
	public double getTurbulenceIntensity() {
		if (MathUtil.equals(average, 0)) {
			if (MathUtil.equals(standardDeviation, 0))
				return 0;
			else
				return 1000;
		}
		return standardDeviation / average;
	}
	
	/**
	 * Set the standard deviation to match the turbulence intensity.
	 * 
	 * @param intensity   the turbulence intensity
	 */
	public void setTurbulenceIntensity(double intensity) {
		setStandardDeviation(intensity * average);
	}
	
	
	
	
	
	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		if (time < 0) {
			throw new IllegalArgumentException("Requesting wind speed at t=" + time);
		}
		
		if (randomSource == null) {
			randomSource = new PinkNoise(ALPHA, POLES, new Random(seed));
			time1 = 0;
			value1 = randomSource.nextValue();
			value2 = randomSource.nextValue();
		}
		
		if (time < time1) {
			reset();
			return getWindVelocity(time, altitude);
		}
		
		while (time1 + DELTA_T < time) {
			value1 = value2;
			value2 = randomSource.nextValue();
			time1 += DELTA_T;
		}
		
		double a = (time - time1) / DELTA_T;
		
		double speed = average + (value1 * (1 - a) + value2 * a) * standardDeviation / STDDEV;
		return new Coordinate(speed * Math.sin(direction), speed * Math.cos(direction), 0);
		
	}
	
	
	private void reset() {
		randomSource = null;
	}
	
	
	
	@Override
	public int getModID() {
		return (int) (average * 1000 + standardDeviation);
	}
	
}
