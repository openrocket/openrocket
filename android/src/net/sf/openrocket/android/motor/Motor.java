package net.sf.openrocket.android.motor;

import java.io.Serializable;
import java.util.Vector;

public class Motor implements Serializable {
	
	private Long motor_id;
	private String name;
	private String impulseClass;
	private String manufacturer;
	private Long diameter;
	private String caseInfo;
	private Float avgThrust;
	private Float maxThrust;
	private Float totalImpulse;
	private Float burnTime;
	private Float length;
	private Double propMass;
	private Double totMass;
	private Vector<Double> burndata;
	public Long getMotor_id() {
		return motor_id;
	}
	public void setMotor_id(Long motor_id) {
		this.motor_id = motor_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImpulseClass() {
		return impulseClass;
	}
	public void setImpulseClass(String impulseClass) {
		this.impulseClass = impulseClass;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public Long getDiameter() {
		return diameter;
	}
	public void setDiameter(Long diameter) {
		this.diameter = diameter;
	}
	public String getCaseInfo() {
		return caseInfo;
	}
	public void setCaseInfo(String caseInfo) {
		this.caseInfo = caseInfo;
	}
	public Float getAvgThrust() {
		return avgThrust;
	}
	public void setAvgThrust(Float avgThrust) {
		this.avgThrust = avgThrust;
	}
	public Float getMaxThrust() {
		return maxThrust;
	}
	public void setMaxThrust(Float maxThrust) {
		this.maxThrust = maxThrust;
	}
	public Float getTotalImpulse() {
		return totalImpulse;
	}
	public void setTotalImpulse(Float totalImpulse) {
		this.totalImpulse = totalImpulse;
	}
	public Float getBurnTime() {
		return burnTime;
	}
	public void setBurnTime(Float burnTime) {
		this.burnTime = burnTime;
	}
	public Float getLength() {
		return length;
	}
	public void setLength(Float length) {
		this.length = length;
	}
	public Double getPropMass() {
		return propMass;
	}
	public void setPropMass(Double propMass) {
		this.propMass = propMass;
	}
	public Double getTotMass() {
		return totMass;
	}
	public void setTotMass(Double totMass) {
		this.totMass = totMass;
	}
	public Vector<Double> getBurndata() {
		return burndata;
	}
	public void setBurndata(Vector<Double> burndata) {
		this.burndata = burndata;
	}
	
}
