package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import java.text.MessageFormat;

import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;

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
		String name = motor.getMotorName();
		if (motor.isUserDefined()) {
			return MessageFormat.format(Application.getTranslator().get("TCMotorSelPan.curve.user"), name);
		}
		return name;
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
