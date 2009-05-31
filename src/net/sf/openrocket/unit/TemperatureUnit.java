package net.sf.openrocket.unit;

public class TemperatureUnit extends FixedPrecisionUnit {

	protected final double addition;
	
	public TemperatureUnit(double multiplier, double addition, String unit) {
		super(unit, 1, multiplier);

		this.addition = addition;
	}
	
	@Override
	public boolean hasSpace() {
		return false;
	}
	
	@Override
	public double toUnit(double value) {
		return value/multiplier - addition;
	}
	
	@Override
	public double fromUnit(double value) {
		return (value + addition)*multiplier;
	}
}
