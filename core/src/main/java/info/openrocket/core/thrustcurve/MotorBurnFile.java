package info.openrocket.core.thrustcurve;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import info.openrocket.core.file.motor.RASPMotorLoader;
import info.openrocket.core.file.motor.RockSimMotorLoader;
import info.openrocket.core.motor.ThrustCurveMotor;

public class MotorBurnFile {

	private String motorId;
	private String simfileId;
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
	 * @return the motor id as a MongoDB Hex String
	 */
	public String getMotorId() {
		return motorId;
	}

	/**
	 * @param motorId the motor id to set as a MongoDB Hex String
	 */
	public void setMotorId(String motorId) {
		this.motorId = motorId;
	}

	/**
	 * @return the simfile id as a MongoDB Hex String
	 */
	public String getSimfileId() {
		return simfileId;
	}

	/**
	 * @param simfileId the simfileId to set as a MongoDB Hex String
	 */
	public void setSimfileId(String simfileId) {
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
