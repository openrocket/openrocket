package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.Comparator;

import net.sf.openrocket.motor.ThrustCurveMotor;

/**
 * Compares two ThrustCurveMotor objects for quality.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ThrustCurveMotorComparator implements Comparator<ThrustCurveMotor> {
	

	@Override
	public int compare(ThrustCurveMotor o1, ThrustCurveMotor o2) {
		return calculateGoodness(o2) - calculateGoodness(o1);
	}
	
	
	private int calculateGoodness(ThrustCurveMotor motor) {
		/*
		 * 10 chars of comments correspond to one thrust point, max ten points.
		 */
		int commentLength = Math.min(motor.getDescription().length(), 100);
		return motor.getTimePoints().length * 10 + commentLength;
	}
	

}
