package net.sf.openrocket.android.thrustcurve;

import java.util.Vector;

public class MotorBurnFile {

	private Integer motor_id;
	private String filetype;
	private Float length;
	private String delays;
	private Double propWeightG;
	private Double totWeightG;
	private Vector<Double> datapoints = new Vector<Double>();
	
	public void init() {
		this.motor_id = null;
		this.filetype = null;
		this.length = null;
		this.delays = null;
		this.propWeightG = null;
		this.totWeightG = null;
		this.datapoints = new Vector<Double>();
	}
	
	@Override
	public MotorBurnFile clone() {
		MotorBurnFile clone = new MotorBurnFile();
		clone.motor_id = this.motor_id;
		clone.filetype = this.filetype;
		clone.length = this.length;
		clone.delays = this.delays;
		clone.propWeightG = this.propWeightG;
		clone.totWeightG = this.totWeightG;
		clone.datapoints = this.datapoints;
		return clone;
	}

	public void decodeFile(String data){
		if (SupportedFileTypes.RASP_FORMAT.equals(filetype)) {
			RaspBurnFile.parse(this,data);
		} else if (SupportedFileTypes.ROCKSIM_FORMAT.equals(filetype) ){
			RSEBurnFile.parse(this,data);
		}
	}
	
	public Integer getMotorId() {
		return motor_id;
	}
	public String getFileType() {
		return filetype;
	}
	public Float getLength() {
		return length;
	}
	public String getDelays() {
		return delays;
	}
	public Double getPropWeightG() {
		return propWeightG;
	}
	public Double getTotWeightG() {
		return totWeightG;
	}
	public Vector<Double> getDatapoints() {
		return datapoints;
	}

	void setMotor_id(Integer motor_id) {
		this.motor_id = motor_id;
	}
	void setFiletype(String filetype ) {
		this.filetype = filetype;
	}
	void setLength(Float length) {
		this.length = length;
	}
	void setDelays(String delays) {
		this.delays = delays;
	}
	void setPropWeightG(Double propWeightG) {
		this.propWeightG = propWeightG;
	}
	void setTotWeightG(Double totWeightG) {
		this.totWeightG = totWeightG;
	}
	void setDatapoints(Vector<Double> datapoints) {
		this.datapoints = datapoints;
	}
}
