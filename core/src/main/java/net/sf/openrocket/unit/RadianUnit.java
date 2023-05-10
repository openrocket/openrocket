package net.sf.openrocket.unit;

import java.text.DecimalFormat;

public class RadianUnit extends GeneralUnit {

	public RadianUnit() {
		super(1,"rad");
	}

	@Override
	public double round(double v) {
		return Math.rint(v*10.0)/10.0;
	}

	private final DecimalFormat decFormat = new DecimalFormat("0.0");
	@Override
	public String toString(double value) {
		double val = toUnit(value);
		return decFormat.format(val);
	}
}
