package net.sf.openrocket.unit;

import java.text.DecimalFormat;

import net.sf.openrocket.util.Chars;

public class DegreeUnit extends GeneralUnit {

	public DegreeUnit() {
		super(Math.PI/180.0, ""+Chars.DEGREE);
	}

	@Override
	public boolean hasSpace() {
		return false;
	}

	@Override
	public double round(double v) {
		return Math.rint(v);
	}

	private final DecimalFormat decFormat = new DecimalFormat("0.#");
	@Override
	public String toString(double value) {
		double val = toUnit(value);
		return decFormat.format(val);
	}
}
