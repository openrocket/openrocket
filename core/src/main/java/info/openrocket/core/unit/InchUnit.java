package info.openrocket.core.unit;

/**
 * Special unit for inches, which always provides 3-decimal precision.
 */
public class InchUnit extends GeneralUnit {
	private final double precision;

	public InchUnit(double multiplier, String unit) {
		this(multiplier, unit, 1);
	}

	/**
	 *
	 * @param multiplier
	 * @param unit
	 * @param precision  The precision of the unit, in inches.
	 */
	public InchUnit(double multiplier, String unit, double precision) {
		super(multiplier, unit);
		this.precision = precision;
	}

	@Override
	protected double roundForDecimalFormat(double val) {
		double mul = 1000.0;
		val = Math.rint(val * mul) / mul;
		return val;
	}

	@Override
	public double getNextValue(double value) {
		return value + precision;
	}

	@Override
	public double getPreviousValue(double value) {
		return value - precision;
	}

}
