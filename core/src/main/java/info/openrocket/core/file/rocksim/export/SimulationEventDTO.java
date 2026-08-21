package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Streamer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * A RockSim recovery-device deployment event.
 * <p>
 * RockSim misspells {@code DeplyTime} in its file format; that spelling must be
 * retained for compatibility.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationEventDTO {

	private static final int NO_EVENT = 0;
	private static final int AT_MAX_EJECTION = 1;
	private static final int AT_TIME_AFTER_IGNITION = 2;
	private static final int AT_TIME_AFTER_APOGEE = 3;
	private static final int AT_APOGEE = 4;
	private static final int AT_ALTITUDE = 5;
	private static final int PARACHUTE_DEVICE = 128;
	private static final int STREAMER_DEVICE = 256;
	private static final String UNUSED_TEST_VALUES = "0,0,0";
	private static final String STANDARD_TEST_TYPES = "28,28,28";

	@XmlElement(name = RockSimCommonConstants.PART_SERIAL_NUMBER)
	private int partSerialNumber;
	@XmlElement(name = RockSimCommonConstants.EVENT_TYPE)
	private int type;
	@XmlElement(name = RockSimCommonConstants.DEPLOY_ALTITUDE)
	private double deployAltitude;
	@XmlElement(name = RockSimCommonConstants.DEPLY_TIME)
	private double deployTime;
	@XmlElement(name = RockSimCommonConstants.HAS_DEPLOYED)
	private final int hasDeployed = 0;
	@XmlElement(name = RockSimCommonConstants.DEPLOYED_AT_ALTITUDE)
	private final double deployedAtAltitude = 0.0;
	@XmlElement(name = RockSimCommonConstants.DEPLOYED_AT_VELOCITY)
	private final double deployedAtVelocity = 0.0;
	@XmlElement(name = RockSimCommonConstants.DEPLOYED_AT_RANGE)
	private final double deployedAtRange = 0.0;
	@XmlElement(name = RockSimCommonConstants.DEPLOYED_AT_TIME)
	private final double deployedAtTime = 0.0;
	@XmlElement(name = RockSimCommonConstants.DEVICE_ID)
	private int deviceId;
	@XmlElement(name = RockSimCommonConstants.TEST_TYPE)
	private final String testType = STANDARD_TEST_TYPES;
	@XmlElement(name = RockSimCommonConstants.TEST_CONDITION)
	private final String testCondition = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_ALTITUDE)
	private final String testValueAltitude = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_DEGREES)
	private final String testValueDegrees = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_PRESSURE)
	private final String testValuePressure = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_MACH)
	private final String testValueMach = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_TIME)
	private final String testValueTime = UNUSED_TEST_VALUES;
	@XmlElement(name = RockSimCommonConstants.TEST_VALUE_Q)
	private final String testValueQ = UNUSED_TEST_VALUES;

	/** Constructor required by JAXB. */
	public SimulationEventDTO() {
	}

	/**
	 * Convert an OpenRocket recovery-device configuration.
	 *
	 * @param device       the configured recovery device
	 * @param configuration its configuration for this simulation
	 * @param serialNumber  the exported component serial number
	 */
	public SimulationEventDTO(RecoveryDevice device, DeploymentConfiguration configuration, int serialNumber) {
		partSerialNumber = serialNumber;
		deviceId = device instanceof Streamer ? STREAMER_DEVICE : PARACHUTE_DEVICE;
		type = toRockSimType(configuration.getDeployEvent(), configuration.getDeployDelay());
		deployTime = configuration.getDeployDelay();
		deployAltitude = configuration.getDeployAltitude();

		if (configuration.getDeployEvent() == DeployEvent.EJECTION) {
			// RockSim's max-ejection mode has no separate post-ejection delay.  Preserve
			// the trigger; a nonzero OpenRocket delay cannot be represented exactly.
			deployTime = 0.0;
		}
		if (configuration.getDeployEvent() != DeployEvent.ALTITUDE) {
			deployAltitude = 0.0;
		}
	}

	private static int toRockSimType(DeployEvent event, double delay) {
		return switch (event) {
			case NEVER -> NO_EVENT;
			case EJECTION -> AT_MAX_EJECTION;
			case LAUNCH -> AT_TIME_AFTER_IGNITION;
			case APOGEE -> delay > 0.0 ? AT_TIME_AFTER_APOGEE : AT_APOGEE;
			case ALTITUDE -> AT_ALTITUDE;
			// RockSim Standard cannot trigger a sustainer device from lower-stage
			// separation.  "Time after ignition" is measured from launch for that
			// device, so using it here could deploy recovery while still ascending.
			case LOWER_STAGE_SEPARATION -> NO_EVENT;
		};
	}

	public int getPartSerialNumber() {
		return partSerialNumber;
	}

	public int getType() {
		return type;
	}

	public double getDeployAltitude() {
		return deployAltitude;
	}

	public double getDeployTime() {
		return deployTime;
	}

}
