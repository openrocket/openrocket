package info.openrocket.core.models.wind;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Random;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.PinkNoise;
import info.openrocket.core.util.StateChangeListener;

/**
 * A wind simulator that generates wind speed as pink noise from a specified
 * average wind speed
 * and standard deviance. Currently the wind is always directed in the direction
 * of the negative
 * X-axis. The simulated wind is unaffected by the altitude.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PinkNoiseWindModel implements WindModel {
	private static final Translator trans = Application.getTranslator();

	/** Random value with which to XOR the random seed value */
	private static final int SEED_RANDOMIZATION = 0x7343AA03;

	/** Pink noise alpha parameter. */
	private static final double ALPHA = 5.0 / 3.0;

	/** Number of poles to use in the pink noise IIR filter. */
	private static final int POLES = 2;

	/**
	 * The standard deviation of the generated pink noise with the specified number
	 * of poles.
	 */
	private static final double STDDEV = 2.252;

	/** Time difference between random samples. */
	public static final double DELTA_T = 0.05;

	private double average = 0;
	private double direction = Math.PI / 2; // this is an East wind
	private double standardDeviation = 0;

	private final int seed;

	private PinkNoise randomSource = null;
	private double time1;
	private double value1, value2;

	private final List<StateChangeListener> listeners = new ArrayList<>();

	/**
	 * Construct a new wind simulation with a specific seed value.
	 * 
	 * @param seed the seed value.
	 */
	public PinkNoiseWindModel(int seed) {
		this.seed = seed ^ SEED_RANDOMIZATION;
	}

	public PinkNoiseWindModel() {
		this(new Random().nextInt());
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
	 * Set the average wind speed. This method will also modify the
	 * standard deviation such that the turbulence intensity remains constant.
	 * If the average wind speed is negative, the direction will be reversed.
	 * 
	 * @param average the average wind speed to set
	 */
	public void setAverage(double average) {
		if (average < 0) {
			average = -average;
			setDirection(Math.PI + getDirection());
		}
		if (average == this.average) {
			return;
		}
		double intensity = getTurbulenceIntensity();
		this.average = average;
		setTurbulenceIntensity(intensity);
		fireChangeEvent();
	}

	public void setDirection(double direction) {
		if (direction == this.direction) {
			return;
		}
		this.direction = MathUtil.reduce2Pi(direction);
		fireChangeEvent();
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
		if (standardDeviation == this.standardDeviation) {
			return;
		}
		this.standardDeviation = Math.max(standardDeviation, 0);
		fireChangeEvent();
	}

	/**
	 * Return the turbulence intensity (standard deviation / average).
	 * 
	 * @return the turbulence intensity
	 */
	public double getTurbulenceIntensity() {
		if (MathUtil.equals(average, 0)) {
			if (MathUtil.equals(standardDeviation, 0))
				return 0;
			else
				return 1;
		}
		return standardDeviation / average;
	}

	/**
	 * Set the standard deviation to match the turbulence intensity.
	 * 
	 * @param intensity the turbulence intensity
	 */
	public void setTurbulenceIntensity(double intensity) {
		setStandardDeviation(intensity * average);
	}

	public String getIntensityDescription() {
		double i = getTurbulenceIntensity();
		if (i < 0.001)
		//// None
			return trans.get("simedtdlg.IntensityDesc.None");
		if (i < 0.05)
		//// Very low
			return trans.get("simedtdlg.IntensityDesc.Verylow");
		if (i < 0.10)
		//// Low
			return trans.get("simedtdlg.IntensityDesc.Low");
		if (i < 0.15)
		//// Medium
			return trans.get("simedtdlg.IntensityDesc.Medium");
		if (i < 0.20)
		//// High
			return trans.get("simedtdlg.IntensityDesc.High");
		if (i < 0.25)
		//// Very high
			return trans.get("simedtdlg.IntensityDesc.Veryhigh");
		//// Extreme
		return trans.get("simedtdlg.IntensityDesc.Extreme");
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitudeMSL, double altitudeAGL) {
		return getWindVelocity(time, altitudeMSL);
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

	public void loadFrom(PinkNoiseWindModel source) {
		this.average = source.average;
		this.direction = source.direction;
		this.standardDeviation = source.standardDeviation;
	}

	@Override
	public ModID getModID() {
		return ModID.ZERO;
	}

	@Override
	public PinkNoiseWindModel clone() {
		try {
			PinkNoiseWindModel clone = (PinkNoiseWindModel) super.clone();
			clone.loadFrom(this);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // This should never happen
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PinkNoiseWindModel that = (PinkNoiseWindModel) o;
		return Double.compare(that.average, average) == 0 &&
				Double.compare(that.standardDeviation, standardDeviation) == 0 &&
				Double.compare(that.direction, direction) == 0 &&
				seed == that.seed;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + Double.hashCode(average);
		result = 31 * result + Double.hashCode(standardDeviation);
		result = 31 * result + Double.hashCode(direction);
		result = 31 * result + seed;
		return result;
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeEvent() {
		EventObject event = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}
}
