package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

/**
 * Tests the conversion of ThrustCurve motor designations and manufacturers to RockSim's engine database. The
 * expected codes are those in RockSim 11's bundled engine files.
 */
public class RockSimEngineCodeTest extends BaseTestCase {

	@Test
	public void convertsDesignationsToRockSimCodes() {
		assertCode("E22SS", "CTI", "24E22-13A", "E22", "Smoky Sam", 0.024);
		assertCode("K650SK", "CTI", "K650-SK", "K650", "Skidmark", 0.054);
		assertCode("H135W", "AeroTech", "HP-H135W", "H135", null, 0.029);
		assertCode("G72DM", "AeroTech", "G72DM-14A", "G72", null, 0.029);
		assertCode("E12J-RC", "AeroTech", "E12JRC", "E12", null, 0.024);
		assertCode("J357WW", "AMW", "WT-54", "J357", "White Wolf", 0.054);
		assertCode("I130-300CC098J", "HyperTEK", "300CC098J-I130", "I130", null, 0.054);
		assertCode("G66LR", "Loki", "G66-LR", "G66", null, 0.029);
		assertCode("G100", "Contrail", "G100-PVC", "G100", null, 0.038);
		assertCode("A10T", "Estes", "A10", "A10", null, 0.013);
		// Designations that already are RockSim codes are kept
		assertCode("H128W", "AeroTech", "H128W", "H128", null, 0.029);
		assertCode("C6", "Estes", "C6", "C6", null, 0.018);
	}

	@Test
	public void convertsManufacturersToRockSimVendors() {
		assertManufacturer("Contrail", "Contrail");
		assertManufacturer("Gorilla", "Gorilla");
		assertManufacturer("", "Loki");
		assertManufacturer("Cesaroni Technology Inc.", "CTI");
		assertManufacturer("AeroTech", "AeroTech");
	}

	/**
	 * Exported engine sets must use RockSim's code and vendor name so that RockSim finds the motor.
	 */
	@Test
	public void exportsEngineSetsWithRockSimCodes() {
		InnerTube mount = new InnerTube();
		mount.setMotorMount(true);
		FlightConfigurationId configurationId = new FlightConfigurationId();
		MotorConfiguration configuration = new MotorConfiguration(mount, configurationId);
		configuration.setMotor(makeMotor("Contrail", "G100-PVC", "G100", null, 0.038));
		mount.setMotorConfig(configuration, configurationId);

		EngineSetDTO engine = new EngineSetDTO(configuration, 1);
		assertEquals("G100", engine.getEngineCode());
		assertEquals("Contrail", engine.getEngineManufacturer());
	}

	private void assertCode(String expected, String manufacturer, String designation, String commonName,
			String propellant, double diameter) {
		Motor motor = makeMotor(manufacturer, designation, commonName, propellant, diameter);
		assertEquals(expected, RockSimEngineCode.getCode(motor));
	}

	private void assertManufacturer(String expected, String manufacturer) {
		Motor motor = makeMotor(manufacturer, "X1", "X1", null, 0.029);
		assertEquals(expected, RockSimEngineCode.getManufacturer(motor));
	}

	private ThrustCurveMotor makeMotor(String manufacturer, String designation, String commonName, String propellant,
			double diameter) {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer(manufacturer))
				.setDesignation(designation)
				.setCode(designation)
				.setCommonName(commonName)
				.setPropellantInfo(propellant)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 0.0 })
				.setDiameter(diameter)
				.setLength(0.1)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, 100.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.05, 0.0, 0.0, 0.1),
						new Coordinate(0.05, 0.0, 0.0, 0.08),
						new Coordinate(0.05, 0.0, 0.0, 0.06) })
				.setDigest(manufacturer + "-" + designation)
				.build();
	}
}
