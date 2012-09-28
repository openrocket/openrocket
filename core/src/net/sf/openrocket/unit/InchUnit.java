package net.sf.openrocket.unit;

/**
 * Special unit for inches, which always provides 3-decimal precision.
 */
public class InchUnit extends GeneralUnit {
	
	public InchUnit(double multiplier, String unit) {
		super(multiplier, unit);
	}
	
	
	@Override
	protected double roundForDecimalFormat(double val) {
		double mul = 1000.0;
		val = Math.rint(val * mul) / mul;
		return val;
	}
	
}
