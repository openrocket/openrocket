package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import info.openrocket.core.motor.ThrustCurveMotor;

class MotorHolder {
	
	private final ThrustCurveMotor motor;
	private final int index;
	
	public MotorHolder(ThrustCurveMotor motor, int index) {
		this.motor = motor;
		this.index = index;
	}
	
	public ThrustCurveMotor getMotor() {
		return motor;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return motor.getMotorName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MotorHolder))
			return false;
		MotorHolder other = (MotorHolder) obj;
		return this.motor.equals(other.motor);
	}
	
	@Override
	public int hashCode() {
		return motor.hashCode();
	}
}
