package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;

import java.util.Locale;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * A motor installed in one physical RockSim motor mount.
 * <p>
 * RockSim requires clustered motors to be represented by repeated EngineSet
 * entries, each with {@code EngineCount} equal to one and its own mount serial
 * number.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineSetDTO {

	private static final double ROCKSIM_NO_EJECTION_CHARGE = -2.0;

	@XmlElement(name = RockSimCommonConstants.ENGINE_COUNT)
	private final int engineCount = 1;
	@XmlElement(name = RockSimCommonConstants.ENGINE_CODE)
	private String engineCode = "";
	@XmlElement(name = RockSimCommonConstants.IGNITION_DELAY)
	private double ignitionDelay;
	@XmlElement(name = RockSimCommonConstants.ENGINE_MFG)
	private String engineManufacturer = "";
	@XmlElement(name = RockSimCommonConstants.ENGINE_OVERHANG)
	private double engineOverhang;
	@XmlElement(name = RockSimCommonConstants.CASING_CG)
	private final double casingCG = 0.0;
	@XmlElement(name = RockSimCommonConstants.MOUNT_SERIAL_NUMBER)
	private int mountSerialNumber;
	@XmlElement(name = RockSimCommonConstants.EJECTION_DELAY)
	private double ejectionDelay;
	@XmlElement(name = RockSimCommonConstants.ROTATE_X_ABOUT_Y)
	private final double rotateXAboutY = 0.0;
	@XmlElement(name = RockSimCommonConstants.ROTATE_ENGINE_AXIS_ABOUT_X)
	private final double rotateEngineAxisAboutX = 0.0;

	/**
	 * Constructor required by JAXB.
	 */
	public EngineSetDTO() {
	}

	/**
	 * Convert one OpenRocket motor configuration for one exported physical mount.
	 *
	 * @param motorConfiguration the configured OpenRocket motor
	 * @param serialNumber       the exported RockSim mount serial number
	 */
	public EngineSetDTO(MotorConfiguration motorConfiguration, int serialNumber) {
		this(motorConfiguration, serialNumber, motorConfiguration.getIgnitionDelay(),
				motorConfiguration.getEjectionDelay());
	}

	/**
	 * Convert one motor while applying RockSim-compatible staging timings.
	 *
	 * @param motorConfiguration the configured OpenRocket motor
	 * @param serialNumber       the exported RockSim mount serial number
	 * @param exportedIgnitionDelay delay after RockSim stage separation
	 * @param exportedEjectionDelay delay used by RockSim for ejection/staging
	 */
	public EngineSetDTO(MotorConfiguration motorConfiguration, int serialNumber, double exportedIgnitionDelay,
			double exportedEjectionDelay) {
		Motor motor = motorConfiguration.getMotor();
		String code = motor.getCode();
		engineCode = code == null || code.isBlank() ? motor.getDesignation() : code;
		ignitionDelay = exportedIgnitionDelay;
		engineOverhang = motorConfiguration.getMount().getMotorOverhang()
				* RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
		mountSerialNumber = serialNumber;

		if (exportedEjectionDelay == Motor.PLUGGED_DELAY) {
			ejectionDelay = ROCKSIM_NO_EJECTION_CHARGE;
		} else {
			ejectionDelay = exportedEjectionDelay;
		}

		if (motor instanceof ThrustCurveMotor) {
			ThrustCurveMotor thrustCurveMotor = (ThrustCurveMotor) motor;
			// The display name is generally the canonical vendor name expected by
			// RockSim's engine database (for example, "Cesaroni Technology Inc.").
			engineManufacturer = thrustCurveMotor.getManufacturer().getDisplayName();
		}
	}

	public int getEngineCount() {
		return engineCount;
	}

	public String getEngineCode() {
		return engineCode;
	}

	public double getIgnitionDelay() {
		return ignitionDelay;
	}

	public String getEngineManufacturer() {
		return engineManufacturer;
	}

	public double getEngineOverhang() {
		return engineOverhang;
	}

	public int getMountSerialNumber() {
		return mountSerialNumber;
	}

	public double getEjectionDelay() {
		return ejectionDelay;
	}

	/**
	 * Format this motor as RockSim displays it in the simulation list.
	 *
	 * @return the motor code, ejection delay, and optional ignition delay
	 */
	public String getDisplayName() {
		StringBuilder result = new StringBuilder(engineCode);
		if (ejectionDelay == ROCKSIM_NO_EJECTION_CHARGE) {
			result.append("-Plugged");
		} else {
			result.append('-').append(formatNumber(ejectionDelay));
		}
		if (ignitionDelay > 0.0) {
			result.append('-').append(String.format(Locale.ROOT, "%.2f", ignitionDelay));
		}
		return result.toString();
	}

	private static String formatNumber(double value) {
		if (value == Math.rint(value)) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}
}
