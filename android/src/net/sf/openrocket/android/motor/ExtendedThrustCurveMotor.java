package net.sf.openrocket.android.motor;

import net.sf.openrocket.motor.ThrustCurveMotor;

public class ExtendedThrustCurveMotor extends ThrustCurveMotor {

	private Long id;
	private String caseInfo;
	private String impulseClass;
	
	public ExtendedThrustCurveMotor( ThrustCurveMotor tcm ) {
		super(tcm);
	}
	
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

	
}
