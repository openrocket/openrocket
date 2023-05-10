package net.sf.openrocket.gui.dialogs.optimization;

import net.sf.openrocket.optimization.general.Point;

/**
 * Value object for optimization step data.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OptimizationStepData {
	
	private final Point oldPoint;
	private final double oldValue;
	private final Point newPoint;
	private final double newValue;
	private final double stepSize;
	
	
	public OptimizationStepData(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
		this.oldPoint = oldPoint;
		this.oldValue = oldValue;
		this.newPoint = newPoint;
		this.newValue = newValue;
		this.stepSize = stepSize;
	}
	
	
	public Point getOldPoint() {
		return oldPoint;
	}
	
	
	public double getOldValue() {
		return oldValue;
	}
	
	
	public Point getNewPoint() {
		return newPoint;
	}
	
	
	public double getNewValue() {
		return newValue;
	}
	
	
	public double getStepSize() {
		return stepSize;
	}
	
}
