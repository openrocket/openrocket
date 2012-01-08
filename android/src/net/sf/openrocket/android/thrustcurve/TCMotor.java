package net.sf.openrocket.android.thrustcurve;

import java.util.Date;
import java.util.Vector;

public class TCMotor implements Cloneable {

	private Integer motor_id;
	private String manufacturer;
	private String manufacturer_abbr;
	private String designation;
	private String brand_name;
	private String common_name;
	private String impulse_class;
	private Float diameter;
	private Float length;
	private String type;
	private String cert_org;
	private Float avg_thrust_n;
	private Float max_thrust_n;
	private Float tot_impulse_ns;
	private Float burn_time_s;
	private Integer data_files;
	private String info_url;
	private Double tot_mass_g;
	private Double prop_mass_g;
	private String delays;
	private String case_info;
	private String prop_info;
	private Date updated_on;
	private Vector<Double> burndata;
	
	public void init() {
		motor_id = null;
		manufacturer = null;
		manufacturer_abbr = null;
		designation = null;
		brand_name = null;
		common_name = null;
		impulse_class = null;
		diameter = null;
		length = null;
		type = null;
		cert_org = null;
		avg_thrust_n = null;
		max_thrust_n = null;
		tot_impulse_ns = null;
		burn_time_s = null;
		data_files = null;
		info_url = null;
		tot_mass_g = null;
		prop_mass_g = null;
		delays = null;
		case_info = null;
		prop_info = null;
		updated_on = null;
		burndata = null;
	}
	
	@Override
	public TCMotor clone() {
		TCMotor clone = new TCMotor();
		clone.motor_id = this.motor_id;
		clone.manufacturer = this.manufacturer;
		clone.manufacturer_abbr = this.manufacturer_abbr;
		clone.designation = this.designation;
		clone.brand_name = this.brand_name;
		clone.common_name = this.common_name;
		clone.impulse_class = this.impulse_class;
		clone.diameter = this.diameter;
		clone.length = this.length;
		clone.type = this.type;
		clone.cert_org = this.cert_org;
		clone.avg_thrust_n = this.avg_thrust_n;
		clone.max_thrust_n = this.max_thrust_n;
		clone.tot_impulse_ns = this.tot_impulse_ns;
		clone.burn_time_s = this.burn_time_s;
		clone.data_files = this.data_files;
		clone.info_url = this.info_url;
		clone.tot_mass_g = this.tot_mass_g;
		clone.prop_mass_g = this.prop_mass_g;
		clone.delays = this.delays;
		clone.case_info = this.case_info;
		clone.prop_info = this.prop_info;
		clone.updated_on = this.updated_on;
		clone.burndata = this.burndata;
		return clone;
	}

	public Integer getMotor_id() {
		return motor_id;
	}

	public void setMotor_id(Integer motor_id) {
		this.motor_id = motor_id;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturer_abbr() {
		return manufacturer_abbr;
	}

	public void setManufacturer_abbr(String manufacturer_abbr) {
		this.manufacturer_abbr = manufacturer_abbr;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getBrand_name() {
		return brand_name;
	}

	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}

	public String getCommon_name() {
		return common_name;
	}

	public void setCommon_name(String common_name) {
		this.common_name = common_name;
	}

	public String getImpulse_class() {
		return impulse_class;
	}

	public void setImpulse_class(String impulse_class) {
		this.impulse_class = impulse_class;
	}

	public Float getDiameter() {
		return diameter;
	}

	public void setDiameter(Float diameter) {
		this.diameter = diameter;
	}

	public Float getLength() {
		return length;
	}

	public void setLength(Float length) {
		this.length = length;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCert_org() {
		return cert_org;
	}

	public void setCert_org(String cert_org) {
		this.cert_org = cert_org;
	}

	public Float getAvg_thrust_n() {
		return avg_thrust_n;
	}

	public void setAvg_thrust_n(Float avg_thrust_n) {
		this.avg_thrust_n = avg_thrust_n;
	}

	public Float getMax_thrust_n() {
		return max_thrust_n;
	}

	public void setMax_thrust_n(Float max_thrust_n) {
		this.max_thrust_n = max_thrust_n;
	}

	public Float getTot_impulse_ns() {
		return tot_impulse_ns;
	}

	public void setTot_impulse_ns(Float tot_impulse_ns) {
		this.tot_impulse_ns = tot_impulse_ns;
	}

	public Float getBurn_time_s() {
		return burn_time_s;
	}

	public void setBurn_time_s(Float burn_time_s) {
		this.burn_time_s = burn_time_s;
	}

	public Integer getData_files() {
		return data_files;
	}

	public void setData_files(Integer data_files) {
		this.data_files = data_files;
	}

	public String getInfo_url() {
		return info_url;
	}

	public void setInfo_url(String info_url) {
		this.info_url = info_url;
	}

	public Double getTot_mass_g() {
		return tot_mass_g;
	}

	public void setTot_mass_g(Double tot_mass_g) {
		this.tot_mass_g = tot_mass_g;
	}

	public Double getProp_mass_g() {
		return prop_mass_g;
	}

	public void setProp_mass_g(Double prop_mass_g) {
		this.prop_mass_g = prop_mass_g;
	}

	public String getDelays() {
		return delays;
	}

	public void setDelays(String delays) {
		this.delays = delays;
	}

	public String getCase_info() {
		return case_info;
	}

	public void setCase_info(String case_info) {
		this.case_info = case_info;
	}

	public String getProp_info() {
		return prop_info;
	}

	public void setProp_info(String prop_info) {
		this.prop_info = prop_info;
	}

	public Date getUpdated_on() {
		return updated_on;
	}

	public void setUpdated_on(Date updated_on) {
		this.updated_on = updated_on;
	}

	public Vector<Double> getBurndata() {
		return burndata;
	}

	public void setBurndata(Vector<Double> burndata) {
		this.burndata = burndata;
	}

	@Override
	public String toString() {
		return "TCMotor [motor_id=" + motor_id + ", manufacturer="
				+ manufacturer + ", manufacturer_abbr=" + manufacturer_abbr
				+ ", designation=" + designation + ", brand_name=" + brand_name
				+ ", common_name=" + common_name + ", impulse_class="
				+ impulse_class + ", diameter=" + diameter + ", length="
				+ length + ", type=" + type + ", cert_org=" + cert_org
				+ ", avg_thrust_n=" + avg_thrust_n + ", max_thrust_n="
				+ max_thrust_n + ", tot_impulse_ns=" + tot_impulse_ns
				+ ", burn_time_s=" + burn_time_s + ", data_files=" + data_files
				+ ", info_url=" + info_url + ", tot_mass_g=" + tot_mass_g
				+ ", prop_mass_g=" + prop_mass_g + ", delays=" + delays
				+ ", case_info=" + case_info + ", prop_info=" + prop_info
				+ ", updated_on=" + updated_on + "]";
	}
	
}
