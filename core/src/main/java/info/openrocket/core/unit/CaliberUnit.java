package info.openrocket.core.unit;

import java.util.Iterator;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MathUtil;

public class CaliberUnit extends GeneralUnit {

	public static final double DEFAULT_CALIBER = 0.01;

	private final FlightConfiguration configuration;
	private final Rocket rocket;

	private int rocketModId = -1;
	private int configurationModId = -1;

	private double caliber = -1;

	public CaliberUnit(FlightConfiguration configuration) {
		super(1.0, "cal");
		this.configuration = configuration;

		if (configuration == null) {
			this.rocket = null;
		} else {
			this.rocket = configuration.getRocket();
		}
	}

	public CaliberUnit(Rocket rocket) {
		super(1.0, "cal");
		this.configuration = null;
		this.rocket = rocket;
	}

	public CaliberUnit(double reference) {
		super(1.0, "cal");
		this.configuration = null;
		this.rocket = null;
		this.caliber = reference;

		if (reference <= 0) {
			throw new IllegalArgumentException("Illegal reference = " + reference);
		}
	}

	@Override
	public double fromUnit(double value) {
		checkCaliber();

		return value * caliber;
	}

	@Override
	public double toUnit(double value) {
		checkCaliber();

		return value / caliber;
	}

	private void checkCaliber() {
		if (configuration != null && configuration.getModID() != configurationModId) {
			caliber = -1;
			configurationModId = configuration.getModID();
		}
		if (rocket != null && rocket.getModID() != rocketModId) {
			caliber = -1;
			rocketModId = rocket.getModID();
		}
		if (caliber < 0) {
			if (configuration != null) {
				caliber = calculateCaliber(configuration);
			} else if (rocket != null) {
				caliber = calculateCaliber(rocket);
			} else {
				throw new BugException("Both rocket and configuration are null");
			}
		}
	}

	/**
	 * Calculate the caliber of a rocket configuration.
	 * 
	 * @param config the rocket configuration
	 * @return the caliber of the rocket, or the default caliber.
	 */
	public static double calculateCaliber(FlightConfiguration config) {
		return calculateCaliber(config.getActiveComponents().iterator());
	}

	/**
	 * Calculate the caliber of a rocket.
	 * 
	 * @param rocket the rocket
	 * @return the caliber of the rocket, or the default caliber.
	 */
	public static double calculateCaliber(Rocket rocket) {
		return calculateCaliber(rocket.iterator());
	}

	private static double calculateCaliber(Iterator<RocketComponent> iterator) {
		double cal = 0;

		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			if (c instanceof SymmetricComponent) {
				double r1 = ((SymmetricComponent) c).getForeRadius() * 2;
				double r2 = ((SymmetricComponent) c).getAftRadius() * 2;
				cal = MathUtil.max(cal, r1, r2);
			}
		}

		if (cal < 0.0001)
			cal = DEFAULT_CALIBER;

		return cal;
	}
}
