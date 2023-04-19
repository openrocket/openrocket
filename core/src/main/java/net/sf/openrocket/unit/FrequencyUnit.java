package net.sf.openrocket.unit;

public class FrequencyUnit extends GeneralUnit {

	
	public FrequencyUnit(double multiplier, String unit) {
		super(multiplier, unit);
	}
	
	

	@Override
	public double toUnit(double value) {
		double hz = 1/value;
		return hz / multiplier;
	}

	
	@Override
	public double fromUnit(double value) {
		double hz = value * multiplier;
		return 1/hz;
	}

}
