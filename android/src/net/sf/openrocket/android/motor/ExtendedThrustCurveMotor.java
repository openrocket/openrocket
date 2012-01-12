package net.sf.openrocket.android.motor;

import net.sf.openrocket.motor.ThrustCurveMotor;

public class ExtendedThrustCurveMotor {

	private Long id;
	private String caseInfo;
	private String impulseClass;
	private ThrustCurveMotor thrustCurveMotor;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the caseInfo
	 */
	public String getCaseInfo() {
		return caseInfo;
	}
	/**
	 * @param caseInfo the caseInfo to set
	 */
	public void setCaseInfo(String caseInfo) {
		this.caseInfo = caseInfo;
	}
	/**
	 * @return the impulseClass
	 */
	public String getImpulseClass() {
		return impulseClass;
	}
	/**
	 * @param impulseClass the impulseClass to set
	 */
	public void setImpulseClass(String impulseClass) {
		this.impulseClass = impulseClass;
	}
	/**
	 * @return the thrustCurveMotor
	 */
	public ThrustCurveMotor getThrustCurveMotor() {
		return thrustCurveMotor;
	}
	/**
	 * @param thrustCurveMotor the thrustCurveMotor to set
	 */
	public void setThrustCurveMotor(ThrustCurveMotor thrustCurveMotor) {
		this.thrustCurveMotor = thrustCurveMotor;
	}
	
	
	
}
