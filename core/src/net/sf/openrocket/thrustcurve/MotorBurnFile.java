package net.sf.openrocket.thrustcurve;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.openrocket.file.motor.RASPMotorLoader;
import net.sf.openrocket.file.motor.RockSimMotorLoader;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorBurnFile {
	
	private Integer motorId;
	private Integer simfileId;
	private String filetype;
	private ThrustCurveMotor.Builder thrustCurveMotor;
	private String data;
	
	public void init() {
		this.motorId = null;
		this.filetype = null;
		this.thrustCurveMotor = null;
	}
	
	@Override
	public MotorBurnFile clone() {
		MotorBurnFile clone = new MotorBurnFile();
		clone.motorId = this.motorId;
		clone.filetype = this.filetype;
		clone.thrustCurveMotor = this.thrustCurveMotor;
		return clone;
	}
	
	public void decodeFile(String _data) throws IOException {
		_data = Base64Decoder.decodeData(_data);
		data = _data;
		try {
			if (SupportedFileTypes.RASP_FORMAT.equals(filetype)) {
				RASPMotorLoader loader = new RASPMotorLoader();
				List<ThrustCurveMotor.Builder> motors = loader.load(new StringReader(data), "download");
				this.thrustCurveMotor = motors.get(0);
			} else if (SupportedFileTypes.ROCKSIM_FORMAT.equals(filetype)) {
				RockSimMotorLoader loader = new RockSimMotorLoader();
				List<ThrustCurveMotor.Builder> motors = loader.load(new StringReader(data), "download");
				this.thrustCurveMotor = motors.get(0);
			}
		} catch (IOException ex) {
			this.thrustCurveMotor = null;
		}
	}
	
	/**
	 * @return the motor id
	 */
	public Integer getMotorId() {
		return motorId;
	}
	
	/**
	 * @param motorId the motor id to set
	 */
	public void setMotorId(Integer motorId) {
		this.motorId = motorId;
	}
	
	/**
	 * @return the simfile id
	 */
	public Integer getSimfileId() {
		return simfileId;
	}
	
	/**
	 * @param simfileId the simfileId to set
	 */
	public void setSimfileId(Integer simfileId) {
		this.simfileId = simfileId;
	}
	
	/**
	 * @return the filetype
	 */
	public String getFiletype() {
		return filetype;
	}
	
	/**
	 * @param filetype the filetype to set
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	
	/**
	 * @return the thrustCurveMotor
	 */
	public ThrustCurveMotor.Builder getThrustCurveMotor() {
		return thrustCurveMotor;
	}

	/**
	 * @return the file contents
	 */
	public String getContents() {
		return data;
	}
	
}
