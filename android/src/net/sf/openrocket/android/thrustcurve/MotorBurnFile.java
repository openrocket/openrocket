package net.sf.openrocket.android.thrustcurve;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.openrocket.file.motor.RASPMotorLoader;
import net.sf.openrocket.file.motor.RockSimMotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorBurnFile {

	private Integer motorId;
	private String filetype;
	private ThrustCurveMotor thrustCurveMotor;

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

	public void decodeFile(String data) {
		try {
			if (SupportedFileTypes.RASP_FORMAT.equals(filetype)) {
				RASPMotorLoader loader = new RASPMotorLoader();
				List<Motor> motors = loader.load( new StringReader(data), "download");
				this.thrustCurveMotor = (ThrustCurveMotor) motors.get(0);
			} else if (SupportedFileTypes.ROCKSIM_FORMAT.equals(filetype) ){
				RockSimMotorLoader loader = new RockSimMotorLoader();
				List<Motor> motors = loader.load( new StringReader(data), "download");
				this.thrustCurveMotor = (ThrustCurveMotor) motors.get(0);
			}
		} catch ( IOException ex ) {
			this.thrustCurveMotor = null;
		}
	}

	/**
	 * @return the motor_id
	 */
	public Integer getMotorId() {
		return motorId;
	}

	/**
	 * @param motor_id the motor_id to set
	 */
	public void setMotorId(Integer motorId) {
		this.motorId = motorId;
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
	public ThrustCurveMotor getThrustCurveMotor() {
		return thrustCurveMotor;
	}

}
