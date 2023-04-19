package net.sf.openrocket.gui.plot;

import net.sf.openrocket.util.BugException;

public class Axis implements Cloneable {

	private double minValue = Double.NaN;
	private double maxValue = Double.NaN;
	
	
	
	public void addBound(double value) {
		
		if (value < minValue  ||  Double.isNaN(minValue)) {
			minValue = value;
		}
		if (value > maxValue  ||  Double.isNaN(maxValue)) {
			maxValue = value;
		}
		
	}
	
	
	public double getMinValue() {
		return minValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getRangeLength() {
		return maxValue - minValue;
	}
	
	public void reset() {
		minValue = Double.NaN;
		maxValue = Double.NaN;
	}
	
	
	
	@Override
	public Axis clone() {
		try {
			
			return (Axis) super.clone();
			
		} catch (CloneNotSupportedException e) {
			throw new BugException("BUG! Could not clone().");
		}
	}
	
}
