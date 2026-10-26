package info.openrocket.core.file.rocksim.importt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

/**
 * Tests the import of RockSim simulations as flight configurations and OpenRocket simulations.
 */
public class RockSimSimulationImportTest extends BaseTestCase {

	private static final String DESIGN = "<RockSimDocument><FileVersion>4</FileVersion><DesignInformation>"
			+ "<RocketDesign><Name>Test</Name><StageCount>1</StageCount><Stage3Parts>"
			+ "<BodyTube><Name>Tube</Name><SerialNo>52</SerialNo><OD>24.8</OD><ID>24</ID><Len>300</Len>"
			+ "<IsMotorMount>1</IsMotorMount><AttachedParts>"
			+ "<Parachute><Name>Chute</Name><SerialNo>57</SerialNo><Dia>300</Dia><SideCount>0</SideCount>"
			+ "<Xb>10</Xb><LocationMode>0</LocationMode></Parachute>"
			+ "</AttachedParts></BodyTube></Stage3Parts></RocketDesign></DesignInformation>"
			+ "<SimulationResultsList><SimulationResults>"
			+ "<LaunchGuideLen>914.4</LaunchGuideLen><LaunchAngle>-0.10472</LaunchAngle>"
			+ "<LaunchGuideAzimuth>90.</LaunchGuideAzimuth><LaunchBarometer>759.813</LaunchBarometer>"
			+ "<LaunchLatitude>50.9</LaunchLatitude><LaunchLongitude>4.4</LaunchLongitude>"
			+ "<LaunchHumidity>50.</LaunchHumidity><LaunchTemperature>59.</LaunchTemperature>"
			+ "<LaunchAltitude>350.</LaunchAltitude><LaunchWindLowSpeed>1.34112</LaunchWindLowSpeed>"
			+ "<LaunchWindHighSpeed>3.53162</LaunchWindHighSpeed><LaunchWindTableSize>0</LaunchWindTableSize>"
			+ "<SimulationName>[B6-2] </SimulationName>"
			+ "<SimulationEvents><SimulationEvent><PartSerialNo>57</PartSerialNo><Type>5</Type>"
			+ "<DeployAltitude>100.</DeployAltitude><DeplyTime>0.</DeplyTime></SimulationEvent></SimulationEvents>"
			+ "<Stage3Engines><EngineSet><EngineCount>1</EngineCount><EngineCode>B6</EngineCode>"
			+ "<IgnitionDelay>0.</IgnitionDelay><EngineMfg>Estes</EngineMfg><MountSerialNo>52</MountSerialNo>"
			+ "<EjectionDelay>2.</EjectionDelay></EngineSet></Stage3Engines>"
			+ "</SimulationResults></SimulationResultsList></RockSimDocument>";

	/**
	 * A RockSim simulation must become a flight configuration with its motors and recovery events, and a simulation
	 * with its launch conditions, in the units RockSim writes them.
	 */
	@Test
	public void importsSimulationsAsFlightConfigurations() throws Exception {
		ThrustCurveMotor b6 = makeMotor();
		OpenRocketDocument document = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(document);
		context.setMotorFinder((type, manufacturer, designation, diameter, length, digest, warnings) ->
				"B6".equals(designation) && "Estes".equals(manufacturer) ? b6 : null);
		RockSimLoader loader = new RockSimLoader();
		loader.loadFromStream(context, new ByteArrayInputStream(DESIGN.getBytes(StandardCharsets.UTF_8)), null);

		assertEquals(1, document.getSimulations().size());
		Simulation simulation = document.getSimulations().get(0);
		assertEquals("[B6-2]", simulation.getName());
		FlightConfigurationId configurationId = simulation.getFlightConfigurationId();

		BodyTube tube = (BodyTube) document.getRocket().getChild(0).getChild(0);
		MotorConfiguration motor = tube.getMotorConfig(configurationId);
		assertNotNull(motor.getMotor());
		assertEquals(b6, motor.getMotor());
		assertEquals(2.0, motor.getEjectionDelay(), 1e-9);

		DeploymentConfiguration deployment = ((Parachute) tube.getChild(0)).getDeploymentConfigurations()
				.get(configurationId);
		assertEquals(DeployEvent.ALTITUDE, deployment.getDeployEvent());
		assertEquals(100.0, deployment.getDeployAltitude(), 1e-9);

		SimulationOptions options = simulation.getOptions();
		assertEquals(0.9144, options.getLaunchRodLength(), 1e-9);
		// A negative RockSim launch angle tilts the guide towards the opposite azimuth
		assertEquals(0.10472, options.getLaunchRodAngle(), 1e-9);
		assertEquals(Math.toRadians(270), options.getLaunchRodDirection(), 1e-9);
		assertFalse(options.getLaunchIntoWind());
		assertFalse(options.isISAAtmosphere());
		assertEquals(759.813 * 101325.0 / 760.0, options.getLaunchPressure(), 1e-6);
		// RockSim files often store 59 °F (15 °C) in the °C field
		assertEquals(288.15, options.getLaunchTemperature(), 1e-9);
		assertEquals(0.5, options.getLaunchRelativeHumidity(), 1e-9);
		assertEquals(350.0, options.getLaunchAltitude(), 1e-9);
		assertEquals(50.9, options.getLaunchLatitude(), 1e-9);
		assertEquals(4.4, options.getLaunchLongitude(), 1e-9);
		assertEquals((1.34112 + 3.53162) / 2, options.getAverageWindModel().getAverage(), 1e-9);
		assertEquals((3.53162 - 1.34112) / (2 * Math.sqrt(2)), options.getAverageWindModel().getStandardDeviation(),
				1e-9);
		assertEquals(1, loader.getWarnings().size());
	}

	private ThrustCurveMotor makeMotor() {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("B6")
				.setCode("B6")
				.setCommonName("B6")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] { 0.0, 2.0, 4.0 })
				.setDiameter(0.018)
				.setLength(0.07)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, 10.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.035, 0.0, 0.0, 0.02),
						new Coordinate(0.035, 0.0, 0.0, 0.017),
						new Coordinate(0.035, 0.0, 0.0, 0.014) })
				.setDigest("Estes-B6")
				.build();
	}
}
