package info.openrocket.core.file.openrocket;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSQLiteDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.logging.MessagePriority;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.xml.OpenRocketComponentLoader;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ExampleFilesTest extends BaseTestCase {

	private static volatile boolean initialized = false;
	private static Path coreModuleRoot;
	private static Injector previousInjector;
	private static final String NO_MOTORS = "[No motors]";
	private static final int SIMULATION_RANDOM_SEED = 0;
	private static final List<Double> AERODYNAMIC_MACHES = List.of(0.3, 0.8, 1.0, 1.2, 2.0, 3.0);
	// Exact design calculations use tight engineering-unit tolerances: one milligram and one micrometer.
	private static final double MASS_EPSILON = 1.0e-6;
	private static final double POSITION_EPSILON = 1.0e-6;
	private static final double COEFFICIENT_EPSILON = 1.0e-6;
	// Randomness is pinned below; this allowance is for integrator and platform-level numerical drift.
	private static final double MAX_ALTITUDE_EPSILON = 0.5;

	private static final Map<String, ExpectedWarnings> EXPECTATIONS = new HashMap<>();
	private static final Map<String, ExpectedFlightConfigurations> EXPECTED_FLIGHT_CONFIGURATIONS = new HashMap<>();
	private static final Map<String, ExpectedMetrics> EXPECTED_METRICS = new HashMap<>();
	static {
		EXPECTATIONS.put("A simple model rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Two stage high power rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 1, 0, 0)
				.simulationWarnings("Simulation 2", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Three stage low power rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 0, 0)
				.build());

		EXPECTATIONS.put("ARC payload rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Tube fin rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("Deployable payload.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 2, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Airstart timing.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 1, 0)
				.simulationWarnings("Simulation 2", 0, 1, 0)
				.simulationWarnings("Simulation 3", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 1, 0)
				.simulationWarnings("Simulation 5", 0, 1, 0)
				.build());

		EXPECTATIONS.put("Base drag hack (short-wide).ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 1, 0, 0)
				.simulationWarnings("Simulation 2", 1, 0, 0)
				.simulationWarnings("Simulation 3", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Chute release.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Dual parachute deployment.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 1, 0)
				.simulationWarnings("Simulation 2", 0, 1, 0)
				.simulationWarnings("Simulation 3", 0, 2, 0)
				.simulationWarnings("Simulation 4", 0, 1, 0)
				.simulationWarnings("Simulation 5", 0, 1, 0)
				.simulationWarnings("Simulation 6", 0, 1, 0)
				.build());

		EXPECTATIONS.put("Clustered motors.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Parallel booster staging.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Pods--airframes and winglets.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 0, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Pods--powered with recovery deployment.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("Simulation extensions.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Active roll control", 0, 0, 0)
				.simulationWarnings("No controlling", 0, 0, 0)
				.simulationWarnings("Roll control + air-start", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Simulation scripting.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Active roll control", 0, 0, 0)
				.simulationWarnings("No controlling", 0, 0, 0)
				.simulationWarnings("Roll control + air-start", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("3D printable nose cone and fins.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("3D printable nose cone and fins.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[A8-3]", "Inner Tube -> A8-3 x1")
				.configuration("[B6-4]", "Inner Tube -> B6-4 x1")
				.configuration("[C6-3]", "Inner Tube -> C6-3 x1")
				.configuration("[C6-5]", "Inner Tube -> C6-5 x1")
				.configuration("[C6-7]", "Inner Tube -> C6-7 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("A simple model rocket.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[A8-3]", "Inner Tube -> A8-3 x1")
				.configuration("[B4-4]", "Inner Tube -> B4-4 x1")
				.configuration("[C6-3]", "Inner Tube -> C6-3 x1")
				.configuration("[C6-5]", "Inner Tube -> C6-5 x1")
				.configuration("[C6-7]", "Inner Tube -> C6-7 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("ARC payload rocket.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[None; F50-9]", "Inner Tube -> F50-9 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Airstart timing.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[3\u00d7 I211-P, K550-P]",
						"38mm airstart -> I211-P x3",
						"54mm center -> K550-P x1")
				.configuration("Airstart @2s",
						"38mm airstart -> I211-P x3",
						"54mm center -> K550-P x1")
				.configuration("Airstart @1s",
						"38mm airstart -> I211-P x3",
						"54mm center -> K550-P x1")
				.configuration("airstart @4s",
						"38mm airstart -> I211-P x3",
						"54mm center -> K550-P x1")
				.configuration("airstart @6s",
						"38mm airstart -> I211-P x3",
						"54mm center -> K550-P x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Base drag hack (short-wide).ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[C11-5]", "24mm Motor Mount Tube -> C11-5 x1")
				.configuration("[D12-3]", "24mm Motor Mount Tube -> D12-3 x1")
				.configuration("[E12-4]", "24mm Motor Mount Tube -> E12-4 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Chute release.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[G40-7]", "Inner Tube -> G40-7 x1")
				.configuration("[G80-10]", "Inner Tube -> G80-10 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Clustered motors.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[4\u00d7 A8-3]", "Clustered Inner Tube -> A8-3 x4")
				.configuration("[4\u00d7 B4-4]", "Clustered Inner Tube -> B4-4 x4")
				.configuration("[4\u00d7 C6-3]", "Clustered Inner Tube -> C6-3 x4")
				.configuration("[4\u00d7 C6-5]", "Clustered Inner Tube -> C6-5 x4")
				.configuration("[4\u00d7 C6-7]", "Clustered Inner Tube -> C6-7 x4")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Deployable payload.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[None; A8-3]", "Inner Tube -> A8-3 x1")
				.configuration("[None; B4-4]", "Inner Tube -> B4-4 x1")
				.configuration("[None; C6-3]", "Inner Tube -> C6-3 x1")
				.configuration("[None; C6-5]", "Inner Tube -> C6-5 x1")
				.configuration("[None; C6-7]", "Inner Tube -> C6-7 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Dual parachute deployment.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[H669-P]", "Inner Tube -> H669-P x1")
				.configuration("[H242-P]", "Inner Tube -> H242-P x1")
				.configuration("[J570-P]", "Inner Tube -> J570-P x1")
				.configuration("[H999-P]", "Inner Tube -> H999-P x1")
				.configuration("[I1299-P]", "Inner Tube -> I1299-P x1")
				.configuration("[G64-P]", "Inner Tube -> G64-P x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Parallel booster staging.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[I115-10; 2\u00d7 E12-0]",
						"Body Tube -> I115-10 x1",
						"Booster Motor Tube -> E12-0 x2")
				.configuration("[I115-10; None]", "Body Tube -> I115-10 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Pods--airframes and winglets.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[A8-3]", "18mm Motor mount -> A8-3 x1")
				.configuration("[B6-4]", "18mm Motor mount -> B6-4 x1")
				.configuration("[C6-5]", "18mm Motor mount -> C6-5 x1")
				.configuration("[C12-6]", "18mm Motor mount -> C12-6 x1")
				.configuration("[D16-6]", "18mm Motor mount -> D16-6 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Pods--powered with recovery deployment.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[2\u00d7 A10-3, A8-P]",
						"Inner Tube -> A10-3 x2",
						"Inner Tube -> A8-P x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Simulation extensions.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[L540-P]", "Inner Tube -> L540-P x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Simulation scripting.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[L540-P]", "Inner Tube -> L540-P x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Three stage low power rocket.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[A8-5; B6-0; B6-0]",
						"Inner Tube -> A8-5 x1",
						"Inner Tube -> B6-0 x1",
						"Inner Tube -> B6-0 x1")
				.configuration("[C6-5; B6-0; B6-0]",
						"Inner Tube -> B6-0 x1",
						"Inner Tube -> B6-0 x1",
						"Inner Tube -> C6-5 x1")
				.configuration("[C6-7; C6-0; C6-0]",
						"Inner Tube -> C6-0 x1",
						"Inner Tube -> C6-0 x1",
						"Inner Tube -> C6-7 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Tube fin rocket.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[D12-7]", "Body tube -> D12-7 x1")
				.build());

		EXPECTED_FLIGHT_CONFIGURATIONS.put("Two stage high power rocket.ork", ExpectedFlightConfigurations.builder()
				.configuration(NO_MOTORS)
				.configuration("[H148R-0; H148R-0]",
						"Booster motor mount -> H148-0 x1",
						"Sustainer Motor Mount -> H148-0 x1")
				.configuration("[I59WN-P; I357T-14]",
						"Booster motor mount -> I357-14 x1",
						"Sustainer Motor Mount -> I59-P x1")
				.build());

		EXPECTED_METRICS.put("3D printable nose cone and fins.ork", ExpectedMetrics.builder()
				.design(0.057419604, 0.214561205, 0.000218314, 0.000066745, 0.354000000,
						0.000000000, -0.022063625, -0.037215320, 0.354000000, 0.042395200,
						0.036215320)
				.aerodynamics(0.3, 0.269160482, 0.772230385)
				.aerodynamics(0.8, 0.272833141, 0.938378897)
				.aerodynamics(1.0, 0.275089490, 1.024030296)
				.aerodynamics(1.2, 0.275963518, 0.992291810)
				.aerodynamics(2.0, 0.254288319, 0.780300301)
				.aerodynamics(3.0, 0.229694915, 0.634933624)
				.simulation("Simulation 1", 0.073769604, 0.233852135, 0.000169928, 0.000051952, 38.527531869)
				.simulation("Simulation 2", 0.073019604, 0.233156282, 0.000171673, 0.000052486, 116.924671859)
				.simulation("Simulation 3 - too short delay", 0.080519604, 0.239531474,
						0.000155683, 0.000047597, 241.992437712)
				.simulation("Simulation 4", 0.080519604, 0.239531474, 0.000155683, 0.000047597, 270.290597708)
				.simulation("Simulation 5", 0.080519604, 0.239531474, 0.000155683, 0.000047597, 271.042340423)
				.build());

		EXPECTED_METRICS.put("A simple model rocket.ork", ExpectedMetrics.builder()
				.design(0.048159131, 0.205520726, 0.000140925, 0.000048525, 0.425400000,
						0.000000000, -0.022116025, -0.037306080, 0.425400000, 0.042500000,
						0.036306080)
				.aerodynamics(0.3, 0.330558027, 0.637648221)
				.aerodynamics(0.8, 0.334770851, 0.807991801)
				.aerodynamics(1.0, 0.337095157, 0.900091037)
				.aerodynamics(1.2, 0.337695939, 0.876988207)
				.aerodynamics(2.0, 0.309035047, 0.689469038)
				.aerodynamics(3.0, 0.277388013, 0.567382662)
				.simulation("Simulation 1", 0.064509131, 0.247968766, 0.000105208, 0.000036226, 50.461283726)
				.simulation("Simulation 2", 0.067059131, 0.252723221, 0.000101207, 0.000034848, 134.804540725)
				.simulation("Simulation 3 - too short delay", 0.071259131, 0.259812312,
						0.000095242, 0.000032794, 277.739071145)
				.simulation("Simulation 4", 0.071259131, 0.259812312, 0.000095242, 0.000032794, 315.464455424)
				.simulation("Simulation 5", 0.071259131, 0.259812312, 0.000095242, 0.000032794, 318.480495989)
				.build());

		EXPECTED_METRICS.put("ARC payload rocket.ork", ExpectedMetrics.builder()
				.design(0.267512145, 0.509532877, 0.000000000, 0.000000000, 1.066800000,
						0.000000000, -0.043076988, -0.073111532, 1.066800000, 0.083555900,
						0.071611532)
				.aerodynamics(0.3, 0.831719401, 0.665475612)
				.aerodynamics(0.8, 0.849325364, 0.752012332)
				.aerodynamics(1.0, 0.856741756, 0.798856979)
				.aerodynamics(1.2, 0.834119436, 0.776327322)
				.aerodynamics(2.0, 0.610801694, 0.597702893)
				.aerodynamics(3.0, 0.496521928, 0.488251307)
				.simulation("Simulation 1", 0.357312145, 0.637271014, 0.000000000, 0.000000000, 450.039668874)
				.build());

		EXPECTED_METRICS.put("Airstart timing.ork", ExpectedMetrics.builder()
				.design(9.859292436, 0.920262936, 0.000000000, 0.000000000, 2.566202786,
						0.000000000, -0.110976238, -0.190716483, 2.566202786, 0.219354400,
						0.189216483)
				.aerodynamics(0.3, 1.719587673, 0.360224115)
				.aerodynamics(0.8, 1.747074534, 0.423734710)
				.aerodynamics(1.0, 1.747337261, 0.458894343)
				.aerodynamics(1.2, 1.746385457, 0.437416393)
				.aerodynamics(2.0, 1.450921861, 0.270142506)
				.aerodynamics(3.0, 1.189405316, 0.184524395)
				.simulation("Simulation 1", 12.745756436, 1.255733620, 0.000000000, 0.000000000, 1312.626508948)
				.simulation("Simulation 2", 12.745756436, 1.255733620, 0.000000000, 0.000000000, 1283.961263427)
				.simulation("Simulation 3", 12.745756436, 1.255733620, 0.000000000, 0.000000000, 1286.934653122)
				.simulation("Simulation 4", 12.745756436, 1.255733620, 0.000000000, 0.000000000, 1292.324182636)
				.simulation("Simulation 5", 12.745756436, 1.255733620, 0.000000000, 0.000000000, 1256.775679343)
				.build());

		EXPECTED_METRICS.put("Base drag hack (short-wide).ork", ExpectedMetrics.builder()
				.design(0.122951327, 0.070549328, -0.000027208, 0.000000000, 0.414024885,
						0.000000000, -0.067642881, -0.067642881, 0.414024885, 0.067642881,
						0.067642881)
				.aerodynamics(0.3, 0.165048546, 0.278585606)
				.aerodynamics(0.8, 0.166576609, 0.611396883)
				.aerodynamics(1.0, 0.168564581, 0.796323486)
				.aerodynamics(1.2, 0.170541105, 0.886952720)
				.aerodynamics(2.0, 0.172971679, 0.938434790)
				.aerodynamics(3.0, 0.173222434, 0.902911741)
				.simulation("Simulation 1", 0.158251327, 0.086281697, -0.000021139, 0.000000000, 97.510441273)
				.simulation("Simulation 2", 0.165551327, 0.088697952, -0.000020207, 0.000000000, 196.884888832)
				.simulation("Simulation 3", 0.182851327, 0.089558913, -0.000018295, 0.000000000, 312.199189993)
				.build());

		EXPECTED_METRICS.put("Chute release.ork", ExpectedMetrics.builder()
				.design(0.910751564, 0.421265938, -0.000111763, 0.000000000, 1.102146640,
						0.000000000, -0.057561223, -0.097317713, 1.102146640, 0.110998000,
						0.094936463)
				.aerodynamics(0.3, 0.859854217, 0.538751642)
				.aerodynamics(0.8, 0.870619450, 0.707244775)
				.aerodynamics(1.0, 0.876820330, 0.799563822)
				.aerodynamics(1.2, 0.878818332, 0.781063297)
				.aerodynamics(2.0, 0.808642509, 0.612155438)
				.aerodynamics(3.0, 0.729260003, 0.503503435)
				.simulation("Simulation 2", 1.033751564, 0.488052792, -0.000098465, 0.000000000, 306.635548402)
				.simulation("Simulation 3", 1.015651564, 0.479239876, -0.000100220, 0.000000000, 510.162252899)
				.build());

		EXPECTED_METRICS.put("Clustered motors.ork", ExpectedMetrics.builder()
				.design(0.169955760, 0.350398815, 0.000161170, 0.000028419, 0.705000000,
						0.000000000, -0.045049038, -0.076527223, 0.705000000, 0.087500000,
						0.075027223)
				.aerodynamics(0.3, 0.549481999, 0.628446332)
				.aerodynamics(0.8, 0.563084510, 0.781219011)
				.aerodynamics(1.0, 0.570408159, 0.893819226)
				.aerodynamics(1.2, 0.556761674, 0.925946816)
				.aerodynamics(2.0, 0.398683533, 0.877818412)
				.aerodynamics(3.0, 0.321419999, 0.789779196)
				.simulation("Simulation 1", 0.235355760, 0.440042329, 0.000116385, 0.000020522, 57.697089973)
				.simulation("Simulation 2", 0.245555760, 0.449719025, 0.000111550, 0.000019669, 141.651988796)
				.simulation("Simulation 3 - too short delay", 0.262355760, 0.464016863,
						0.000104407, 0.000018410, 277.916172643)
				.simulation("Simulation 4", 0.262355760, 0.464016863, 0.000104407, 0.000018410, 305.142914961)
				.simulation("Simulation 5", 0.262355760, 0.464016863, 0.000104407, 0.000018410, 305.704810293)
				.build());

		EXPECTED_METRICS.put("Deployable payload.ork", ExpectedMetrics.builder()
				.design(0.065362828, 0.230802713, 0.000103833, 0.000035753, 0.506600000,
						0.000000000, -0.022116025, -0.037306080, 0.506600000, 0.042500000,
						0.036306080)
				.aerodynamics(0.3, 0.409771450, 0.671180597)
				.aerodynamics(0.8, 0.415263987, 0.839663147)
				.aerodynamics(1.0, 0.419181545, 0.929531734)
				.aerodynamics(1.2, 0.417128350, 0.903859958)
				.aerodynamics(2.0, 0.362252931, 0.709141659)
				.aerodynamics(3.0, 0.308693031, 0.580297513)
				.simulation("Simulation 1", 0.081712828, 0.279584352, 0.000083057, 0.000028599, 31.720105774)
				.simulation("Simulation 2", 0.084262828, 0.285486004, 0.000080544, 0.000027733, 95.211002105)
				.simulation("Simulation 3 - too short delay", 0.088462828, 0.294464677,
						0.000076720, 0.000026417, 230.443880143)
				.simulation("Simulation 4", 0.088462828, 0.294464677, 0.000076720, 0.000026417, 261.088187032)
				.simulation("Simulation 5", 0.088462828, 0.294464677, 0.000076720, 0.000026417, 262.319051721)
				.build());

		EXPECTED_METRICS.put("Dual parachute deployment.ork", ExpectedMetrics.builder()
				.design(1.360777109, 0.885775814, -0.000056928, 0.000000000, 1.477010000,
						0.000000000, -0.053283363, -0.091108391, 1.477010000, 0.104521000,
						0.089927291)
				.aerodynamics(0.3, 1.233658682, 0.817700126)
				.aerodynamics(0.8, 1.242077600, 0.899589201)
				.aerodynamics(1.0, 1.251703409, 0.938357388)
				.aerodynamics(1.2, 1.262490750, 0.905412252)
				.aerodynamics(2.0, 1.191623817, 0.702838307)
				.aerodynamics(3.0, 1.075151017, 0.569778819)
				.simulation("Simulation 1", 1.612777109, 0.962082593, -0.000047270, 0.000000000, 592.018467244)
				.simulation("Simulation 2", 1.629577109, 0.966738611, -0.000046783, 0.000000000, 660.314539436)
				.simulation("Simulation 3", 2.246921109, 1.025063116, -0.000033929, 0.000000000, 2219.003994812)
				.simulation("Simulation 4", 1.691777109, 0.978182861, -0.000045063, 0.000000000, 895.933154796)
				.simulation("Simulation 5", 1.782777109, 0.993667955, -0.000042763, 0.000000000, 1156.476267928)
				.simulation("Simulation 6", 1.511977109, 0.933373665, -0.000050422, 0.000000000, 225.919093339)
				.build());

		EXPECTED_METRICS.put("Parallel booster staging.ork", ExpectedMetrics.builder()
				.design(0.608323060, 0.563665061, 0.000000000, -0.000091181, 1.143000000,
						0.000000000, -0.095594428, -0.058461467, 1.143000000, 0.095594428,
						0.058461467)
				.aerodynamics(0.3, 1.000159223, 1.621998911)
				.aerodynamics(0.8, 1.007210684, 1.949674986)
				.aerodynamics(1.0, 1.012177226, 2.237113748)
				.aerodynamics(1.2, 1.010776934, 2.313743861)
				.aerodynamics(2.0, 0.935690274, 2.057921971)
				.aerodynamics(3.0, 0.859280655, 1.882448987)
				.simulation("Simulation 1", 1.308123060, 0.839750466, 0.000000000, -0.000042402, 1117.700336058)
				.simulation("Simulation 2", 1.188323060, 0.813006991, 0.000000000, -0.000046677, 846.737497406)
				.build());

		EXPECTED_METRICS.put("Pods--airframes and winglets.ork", ExpectedMetrics.builder()
				.design(0.058709249, 0.212237045, 0.000265925, -0.000000000, 0.441325000,
						0.000000000, -0.028911746, -0.081368900, 0.441325000, 0.049758600,
						0.081368900)
				.aerodynamics(0.3, 0.346316323, 0.821718035)
				.aerodynamics(0.8, 0.351444751, 1.007636150)
				.aerodynamics(1.0, 0.355908243, 1.113618245)
				.aerodynamics(1.2, 0.357813866, 1.084838004)
				.aerodynamics(2.0, 0.338274902, 0.850365291)
				.aerodynamics(3.0, 0.315263350, 0.709725705)
				.simulation("Simulation 1", 0.075059249, 0.255898027, 0.000207999, -0.000000000, 29.826989419)
				.simulation("Simulation 2", 0.074309249, 0.254315684, 0.000210098, -0.000000000, 92.350436331)
				.simulation("Simulation 3", 0.081809249, 0.268833540, 0.000190837, -0.000000000, 196.236148374)
				.simulation("Simulation 4", 0.082409249, 0.268644179, 0.000189448, -0.000000000, 205.261032084)
				.simulation("Simulation 5", 0.084409249, 0.271954973, 0.000184959, -0.000000000, 243.625610375)
				.build());

		EXPECTED_METRICS.put("Pods--powered with recovery deployment.ork", ExpectedMetrics.builder()
				.design(0.065375956, 0.054377603, 0.000000000, 0.000075460, 0.342310000,
						-0.142535000, -0.071838936, -0.050096536, 0.199775000, 0.071838936,
						0.050096536)
				.aerodynamics(0.3, 0.139542077, 2.424203830)
				.aerodynamics(0.8, 0.144256136, 2.802372537)
				.aerodynamics(1.0, 0.148211933, 3.034594111)
				.aerodynamics(1.2, 0.148165315, 3.041411948)
				.aerodynamics(2.0, 0.116403821, 2.766620740)
				.aerodynamics(3.0, 0.095927996, 2.557657227)
				.simulation("Simulation 1", 0.098725956, 0.088755626, 0.000000000, 0.000049969, 90.808973921)
				.build());

		EXPECTED_METRICS.put("Simulation extensions.ork", ExpectedMetrics.builder()
				.design(7.003644847, 1.359884995, -0.000000000, 0.000000000, 2.705000000,
						0.000000000, -0.217426743, -0.170000000, 2.705000000, 0.224246213,
						0.255000000)
				.aerodynamics(0.3, 1.829863039, 0.567308624)
				.aerodynamics(0.8, 1.807889809, 0.688743234)
				.aerodynamics(1.0, 1.795690477, 0.754337716)
				.aerodynamics(1.2, 1.872173847, 0.729775546)
				.aerodynamics(2.0, 1.977280718, 0.545401992)
				.aerodynamics(3.0, 1.825413004, 0.433534502)
				.simulation("Active roll control", 12.659644847, 1.774095862,
						-0.000000000, 0.000000000, 2409.556289273)
				.simulation("No controlling", 12.659644847, 1.774095862,
						-0.000000000, 0.000000000, 2409.788229823)
				.simulation("Roll control + air-start", 12.659644847, 1.774095862,
						-0.000000000, 0.000000000, 2883.098817217)
				.build());

		EXPECTED_METRICS.put("Simulation scripting.ork", ExpectedMetrics.builder()
				.design(7.003641463, 1.359885249, -0.000000000, 0.000000000, 2.705000000,
						0.000000000, -0.217426743, -0.170000000, 2.705000000, 0.224246213,
						0.255000000)
				.aerodynamics(0.3, 1.829858157, 0.567308164)
				.aerodynamics(0.8, 1.807880862, 0.688742800)
				.aerodynamics(1.0, 1.795676203, 0.754337312)
				.aerodynamics(1.2, 1.872162141, 0.729775178)
				.aerodynamics(2.0, 1.977282801, 0.545401722)
				.aerodynamics(3.0, 1.825413770, 0.433534324)
				.simulation("Active roll control", 12.659641463, 1.774096113,
						-0.000000000, 0.000000000, 2410.145768488)
				.simulation("No controlling", 12.659641463, 1.774096113,
						-0.000000000, 0.000000000, 2410.145768488)
				.simulation("Roll control + air-start", 12.659641463, 1.774096113,
						-0.000000000, 0.000000000, 2410.249916711)
				.build());

		EXPECTED_METRICS.put("Three stage low power rocket.ork", ExpectedMetrics.builder()
				.design(0.079890089, 0.315866459, 0.000169905, 0.000058503, 0.560000000,
						0.000000000, -0.027116025, -0.045966334, 0.560000000, 0.052500000,
						0.044966334)
				.aerodynamics(0.3, 0.433827752, 1.190211918)
				.aerodynamics(0.8, 0.436751396, 1.638487224)
				.aerodynamics(1.0, 0.440596280, 1.883371749)
				.aerodynamics(1.2, 0.443931326, 1.860449296)
				.aerodynamics(2.0, 0.438041610, 1.638295249)
				.aerodynamics(3.0, 0.422432711, 1.444875685)
				.simulation("Simulation 1", 0.127440089, 0.363962391, 0.000106510, 0.000036674, 263.632015842)
				.simulation("Simulation 2", 0.134190089, 0.364165490, 0.000101153, 0.000034830, 469.098287617)
				.simulation("Simulation 3", 0.149190089, 0.376314539, 0.000090983, 0.000031328, 617.061091167)
				.build());

		EXPECTED_METRICS.put("Tube fin rocket.ork", ExpectedMetrics.builder()
				.design(0.033801428, 0.332328366, -0.000066012, 0.000000000, 0.577088000,
						0.000000000, -0.027666716, -0.024790400, 0.577088000, 0.027666716,
						0.024790400)
				.aerodynamics(0.3, 0.496639644, 1.784827589)
				.aerodynamics(0.8, 0.260395035, 1.913825697)
				.aerodynamics(1.0, 0.048051098, 1.989120709)
				.aerodynamics(1.2, -0.073846670, 1.970916577)
				.aerodynamics(2.0, 0.387844953, 1.783745978)
				.aerodynamics(3.0, 0.499185356, 1.634371956)
				.simulation("Simulation 1", 0.076401428, 0.452827035, -0.000029205, 0.000000000, 281.866439856)
				.build());

		EXPECTED_METRICS.put("Two stage high power rocket.ork", ExpectedMetrics.builder()
				.design(1.956061854, 1.226799029, -0.000094066, 0.000000000, 2.051050000,
						0.000000000, -0.109249038, -0.187724885, 2.051050000, 0.215900000,
						0.186224885)
				.aerodynamics(0.3, 1.558244177, 0.896841955)
				.aerodynamics(0.8, 1.575521369, 1.016227137)
				.aerodynamics(1.0, 1.590011846, 1.083939204)
				.aerodynamics(1.2, 1.586656106, 1.071367976)
				.aerodynamics(2.0, 1.428040616, 0.913285239)
				.aerodynamics(3.0, 1.296000437, 0.803539806)
				.simulation("Simulation 1", 2.574301854, 1.330247457, -0.000071475, 0.000000000, 667.083070858)
				.simulation("Simulation 2", 2.792501854, 1.330015190, -0.000065890, 0.000000000, 1371.496561284)
				.build());
	}

	@BeforeAll
	public static void setUp() throws Exception {
		if (initialized) {
			return;
		}

		if (Application.getInjector() == null) {
			BaseTestCase.setUp();
		}
		previousInjector = Application.getInjector();

		coreModuleRoot = findCoreModuleRoot();

		ComponentPresetDatabase componentPresetDatabase = loadComponentPresetDatabase(coreModuleRoot);
		ThrustCurveMotorSetDatabase motorDatabase = loadMotorDatabase(coreModuleRoot);

		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toInstance(componentPresetDatabase);
				bind(ThrustCurveMotorSetDatabase.class).toInstance(motorDatabase);
				bind(MotorDatabase.class).to(ThrustCurveMotorSetDatabase.class);
			}
		};
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(dbOverrides), pluginModule);

		Application.setInjector(injector);
		initialized = true;
	}

	@AfterAll
	public static void tearDown() {
		if (previousInjector != null) {
			Application.setInjector(previousInjector);
		}
	}

	static Stream<Path> exampleOrkFiles() {
		Path root = coreModuleRoot != null ? coreModuleRoot : findCoreModuleRoot();
		Path examplesDir = root.resolve("src/main/resources/datafiles/examples");
		try (Stream<Path> stream = Files.list(examplesDir)) {
			return stream
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".ork"))
					.sorted(Comparator.comparing(p -> p.getFileName().toString()))
					.toList()
					.stream();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("exampleOrkFiles")
	public void exampleFileMatchesExpectedValues(Path orkFile) throws RocketLoadException {
		String fileName = orkFile.getFileName().toString();
		ExpectedWarnings expected = EXPECTATIONS.get(fileName);
		ExpectedFlightConfigurations expectedFlightConfigurations = EXPECTED_FLIGHT_CONFIGURATIONS.get(fileName);
		ExpectedMetrics expectedMetrics = EXPECTED_METRICS.get(fileName);

		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();
		Rocket rocket = doc.getRocket();

		WarningCounts openWarnings = countRelevantWarnings(loader.getWarnings());
		if (expected != null) {
			assertEquals(expected.openWarnings, openWarnings, () -> "Warnings when opening " + orkFile + " (expected=" + expected.openWarnings +
					", actual=" + openWarnings + "):\n" + formatWarnings(loader.getWarnings()));
		}

		List<FlightConfigurationSnapshot> actualFlightConfigurations = describeFlightConfigurations(rocket);
		if (expectedFlightConfigurations == null) {
			fail("Missing expected flight configuration data for example file: " + fileName +
					".\n\n" + flightConfigurationExpectationSnippet(fileName, actualFlightConfigurations));
		}
		assertEquals(expectedFlightConfigurations.configurations, actualFlightConfigurations,
				() -> "Flight configurations when opening " + orkFile +
						" did not match expected values.\n\nExpected:\n" +
						formatFlightConfigurations(expectedFlightConfigurations.configurations) +
						"\nActual:\n" + formatFlightConfigurations(actualFlightConfigurations));

		DesignMetrics actualDesignMetrics = describeDesignMetrics(rocket);
		if (expectedMetrics != null) {
			assertDesignMetrics(fileName, expectedMetrics.design, actualDesignMetrics);
		}

		Map<String, WarningCounts> actualSimWarnings = new HashMap<>();
		Map<String, SimulationMetrics> actualSimulationMetrics = new LinkedHashMap<>();
		for (Simulation simulation : doc.getSimulations()) {
			SimulationMetrics preflightMetrics = describePreflightMetrics(simulation);
			pinSimulationRandomness(simulation);
			try {
				simulation.simulate();
			} catch (Exception e) {
				fail("Simulation failed for " + orkFile + " (" + simulation.getName() + "): " + e.getMessage(), e);
			}

			WarningSet warnings = simulation.getSimulatedWarnings();
			WarningCounts simulationWarnings = warnings == null ? WarningCounts.MISSING : countRelevantWarnings(warnings);
			actualSimWarnings.put(simulation.getName(), simulationWarnings);
			SimulationMetrics actualMetrics = preflightMetrics.withMaxAltitude(
					simulation.getSimulatedData().getMaxAltitude());
			actualSimulationMetrics.put(simulation.getName(), actualMetrics);

			if (expectedMetrics != null) {
				SimulationMetrics expectedSimulationMetrics = expectedMetrics.simulations.get(simulation.getName());
				if (expectedSimulationMetrics == null) {
					fail("Missing expected metrics for " + fileName + " simulation '" + simulation.getName() +
							"'.\n\n" + metricsExpectationSnippet(fileName, actualDesignMetrics,
							actualSimulationMetrics));
				}
				assertSimulationMetrics(fileName, simulation.getName(), expectedSimulationMetrics, actualMetrics);
			}

			if (expected != null) {
				WarningCounts expectedSimWarnings = expected.simulationWarnings.get(simulation.getName());
				if (expectedSimWarnings == null) {
					fail("Missing expected warnings configuration for " + fileName + " simulation '" +
							simulation.getName() + "'.\n\n" + expectationSnippet(fileName, openWarnings, actualSimWarnings));
				}

				assertEquals(expectedSimWarnings, simulationWarnings, () -> "Warnings when simulating " + orkFile + " (" + simulation.getName() + ") " +
						"(expected=" + expectedSimWarnings + ", actual=" + simulationWarnings + "):\n" +
						(warnings == null ? "<null WarningSet>" : formatWarnings(warnings)));
			}

			assertFalse(simulation.hasErrors(),
					() -> "Simulation aborted for " + orkFile + " (" + simulation.getName() + ")");
		}

		if (expected == null) {
			fail("Missing expected warnings configuration for example file: " + fileName +
					".\n\n" + expectationSnippet(fileName, openWarnings, actualSimWarnings));
		}

		if (expectedMetrics == null) {
			fail("Missing expected design and simulation metrics for example file: " + fileName +
					".\n\n" + metricsExpectationSnippet(fileName, actualDesignMetrics, actualSimulationMetrics));
		}
		assertEquals(expectedMetrics.simulations.keySet(), actualSimulationMetrics.keySet(),
				() -> "Saved simulations for " + fileName + " did not match the metrics baseline");
	}

	/**
	 * Calculates the fixed, motorless properties of the complete example rocket.
	 */
	private static DesignMetrics describeDesignMetrics(Rocket rocket) {
		FlightConfiguration configuration = findNoMotorsConfiguration(rocket).clone();
		configuration.setAllStages();

		RigidBody structure = MassCalculator.calculateStructure(configuration);
		BoundingBox bounds = configuration.getBoundingBox();
		Map<Double, AerodynamicMetrics> aerodynamics = calculateAerodynamicMetrics(configuration);
		return new DesignMetrics(structure.getMass(), CoordinateSnapshot.from(structure.getCM()),
				configuration.getLength(), BoundsSnapshot.from(bounds), aerodynamics);
	}

	/**
	 * Calculates launch properties before a simulation can change any flight state.
	 */
	private static SimulationMetrics describePreflightMetrics(Simulation simulation) {
		RigidBody launch = MassCalculator.calculateLaunch(simulation.getActiveConfiguration());
		return new SimulationMetrics(launch.getMass(), CoordinateSnapshot.from(launch.getCM()), Double.NaN);
	}

	private static FlightConfiguration findNoMotorsConfiguration(Rocket rocket) {
		for (FlightConfiguration configuration : rocket.getFlightConfigurations()) {
			if (NO_MOTORS.equals(configuration.getName())) {
				return configuration;
			}
		}
		throw new IllegalStateException("Example rocket is missing its " + NO_MOTORS + " flight configuration");
	}

	/**
	 * Calculates CP and CD at fixed sea-level, zero-angle-of-attack conditions across
	 * the subsonic, transonic, and supersonic regimes.  The complete motorless rocket
	 * is used so motor selection cannot change the aerodynamic regression baseline.
	 */
	private static Map<Double, AerodynamicMetrics> calculateAerodynamicMetrics(
			FlightConfiguration configuration) {
		Map<Double, AerodynamicMetrics> metrics = new LinkedHashMap<>();
		BarrowmanCalculator calculator = new BarrowmanCalculator();
		for (double mach : AERODYNAMIC_MACHES) {
			FlightConditions conditions = new FlightConditions(configuration);
			conditions.setMach(mach);
			conditions.setAOA(0.0);
			WarningSet warnings = new WarningSet();
			CoordinateIF cp = calculator.getCP(configuration, conditions, warnings);
			AerodynamicForces forces = calculator.getAerodynamicForces(configuration, conditions, warnings);
			metrics.put(mach, new AerodynamicMetrics(cp.getX(), forces.getCD()));
		}
		return Map.copyOf(metrics);
	}

	/**
	 * A fixed stepper seed and wind-model seed make altitude a property of the saved
	 * design.  The nonzero assertion epsilon still permits harmless numerical drift.
	 */
	private static void pinSimulationRandomness(Simulation simulation) {
		SimulationOptions options = simulation.getOptions();
		options.setRandomSeed(SIMULATION_RANDOM_SEED);
		options.setRandomSeedFixed(true);
		options.getWindModel().setSeed(SIMULATION_RANDOM_SEED);
	}

	private static List<FlightConfigurationSnapshot> describeFlightConfigurations(Rocket rocket) {
		List<FlightConfigurationSnapshot> configurations = new ArrayList<>();
		for (FlightConfiguration configuration : rocket.getFlightConfigurations()) {
			configurations.add(new FlightConfigurationSnapshot(configuration.getName(),
					describeActiveMotors(configuration.getActiveMotors())));
		}
		return List.copyOf(configurations);
	}

	private static List<String> describeActiveMotors(Collection<MotorConfiguration> activeMotors) {
		List<String> motors = new ArrayList<>();
		for (MotorConfiguration motorConfiguration : activeMotors) {
			MotorMount mount = motorConfiguration.getMount();
			RocketComponent component = (RocketComponent) mount;
			motors.add(component.getName() + " -> " + motorConfiguration.toMotorName() +
					" x" + mount.getMotorCountIncludingAssemblyCopies());
		}
		motors.sort(Comparator.naturalOrder());
		return List.copyOf(motors);
	}

	private static WarningCounts countRelevantWarnings(WarningSet warnings) {
		int informative = 0;
		int normal = 0;
		int critical = 0;

		for (Warning w : warnings) {
			MessagePriority priority = w.getPriority();
			if (priority == null) {
				normal++;
				continue;
			}

			switch (priority) {
				case LOW -> informative++;
				case NORMAL -> normal++;
				case HIGH -> critical++;
			}
		}
		return new WarningCounts(informative, normal, critical);
	}

	private static String formatWarnings(WarningSet warnings) {
		StringBuilder sb = new StringBuilder();
		for (Warning w : warnings) {
			sb.append("- ").append(w.getPriority()).append(": ").append(w).append("\n");
		}
		return sb.toString();
	}

	private static ComponentPresetDatabase loadComponentPresetDatabase(Path coreRoot) throws IOException {
		Path presetsDir = coreRoot.resolve("src/main/resources/datafiles/components");
		if (!Files.isDirectory(presetsDir)) {
			throw new IOException("Component preset directory not found: " + presetsDir);
		}

		ComponentPresetDatabase dao = new ComponentPresetDatabase();
		OpenRocketComponentLoader loader = new OpenRocketComponentLoader();

		try (Stream<Path> files = Files.walk(presetsDir)) {
			List<Path> presetFiles = files
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".orc"))
					.sorted()
					.toList();

			for (Path file : presetFiles) {
				try (InputStream is = Files.newInputStream(file)) {
					Collection<ComponentPreset> presets = loader.load(is, file.getFileName().toString());
					dao.addAll(presets);
				}
			}
		}

		return dao;
	}

	private static ThrustCurveMotorSetDatabase loadMotorDatabase(Path coreRoot) throws IOException {
		Path bundledDb = coreRoot.resolve("src/main/resources/datafiles/thrustcurves/initial_motors.db");
		if (!Files.isRegularFile(bundledDb)) {
			throw new IOException("Bundled motor database not found: " + bundledDb);
		}

		Path tmpDir = coreRoot.resolve("build/tmp/example-ork-tests");
		Files.createDirectories(tmpDir);
		Path dbCopy = tmpDir.resolve("initial_motors.db");
		Files.copy(bundledDb, dbCopy, StandardCopyOption.REPLACE_EXISTING);

		List<ThrustCurveMotor> motors;
		try {
			motors = ThrustCurveMotorSQLiteDatabase.readDatabase(dbCopy.toFile());
		} catch (Exception e) {
			throw new IOException("Failed to read motor database: " + dbCopy, e);
		}

		ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
		for (ThrustCurveMotor motor : motors) {
			db.addMotor(motor);
		}
		return db;
	}

	private static Path findCoreModuleRoot() {
		Path cwd = Path.of("").toAbsolutePath().normalize();

		if (Files.isDirectory(cwd.resolve("src/main/resources/datafiles/examples"))) {
			return cwd;
		}
		if (Files.isDirectory(cwd.resolve("core/src/main/resources/datafiles/examples"))) {
			return cwd.resolve("core");
		}
		throw new IllegalStateException("Unable to locate core module root from working directory: " + cwd);
	}

	private static String expectationSnippet(String fileName, WarningCounts openWarnings, Map<String, WarningCounts> simWarnings) {
		StringBuilder sb = new StringBuilder();
		sb.append("EXPECTATIONS.put(\"").append(fileName).append("\", ExpectedWarnings.builder()\n");
		sb.append("\t\t.openWarnings(").append(openWarnings.informative).append(", ").append(openWarnings.normal)
				.append(", ").append(openWarnings.critical).append(")\n");
		simWarnings.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(e -> sb.append("\t\t.simulationWarnings(\"").append(e.getKey()).append("\", ")
						.append(e.getValue().informative).append(", ").append(e.getValue().normal).append(", ")
						.append(e.getValue().critical).append(")\n"));
		sb.append("\t\t.build());\n");
		return sb.toString();
	}

	private static String flightConfigurationExpectationSnippet(String fileName, List<FlightConfigurationSnapshot> configurations) {
		StringBuilder sb = new StringBuilder();
		sb.append("EXPECTED_FLIGHT_CONFIGURATIONS.put(\"").append(fileName)
				.append("\", ExpectedFlightConfigurations.builder()\n");
		for (FlightConfigurationSnapshot configuration : configurations) {
			sb.append("\t\t.configuration(\"").append(configuration.name).append("\"");
			for (String motor : configuration.motors) {
				sb.append(", \"").append(motor).append("\"");
			}
			sb.append(")\n");
		}
		sb.append("\t\t.build());\n");
		return sb.toString();
	}

	private static String formatFlightConfigurations(List<FlightConfigurationSnapshot> configurations) {
		StringBuilder sb = new StringBuilder();
		for (FlightConfigurationSnapshot configuration : configurations) {
			sb.append("- ").append(configuration.name).append("\n");
			if (configuration.motors.isEmpty()) {
				sb.append("  <no active motors>\n");
				continue;
			}
			for (String motor : configuration.motors) {
				sb.append("  ").append(motor).append("\n");
			}
		}
		return sb.toString();
	}

	private static void assertDesignMetrics(String fileName, DesignMetrics expected, DesignMetrics actual) {
		assertEquals(expected.dryMass, actual.dryMass, MASS_EPSILON,
				"Dry mass changed for " + fileName);
		assertCoordinateEquals(expected.dryCg, actual.dryCg, "Dry CG changed for " + fileName);
		assertEquals(expected.length, actual.length, POSITION_EPSILON,
				"Physical length changed for " + fileName);
		assertBoundsEquals(expected.bounds, actual.bounds, "Bounding box changed for " + fileName);
		if (expected.aerodynamics.isEmpty()) {
			fail("Missing expected aerodynamic metrics for " + fileName + ".\n\n" +
					metricsExpectationSnippet(fileName, actual, Map.of()));
		}
		assertEquals(AERODYNAMIC_MACHES.size(), expected.aerodynamics.size(),
				"Aerodynamic Mach baseline count changed for " + fileName);
		assertTrue(expected.aerodynamics.keySet().containsAll(AERODYNAMIC_MACHES),
				"Aerodynamic Mach baseline changed for " + fileName);
		assertEquals(expected.aerodynamics.keySet(), actual.aerodynamics.keySet(),
				"Calculated aerodynamic Mach points changed for " + fileName);
		for (double mach : AERODYNAMIC_MACHES) {
			AerodynamicMetrics expectedAtMach = expected.aerodynamics.get(mach);
			AerodynamicMetrics actualAtMach = actual.aerodynamics.get(mach);
			String context = fileName + " at Mach " + formatMach(mach);
			assertEquals(expectedAtMach.cpX, actualAtMach.cpX, POSITION_EPSILON,
					() -> "CP changed for " + context + ".\n\nActual baseline:\n" +
							metricsExpectationSnippet(fileName, actual, Map.of()));
			assertEquals(expectedAtMach.cd, actualAtMach.cd, COEFFICIENT_EPSILON,
					() -> "CD changed for " + context + ".\n\nActual baseline:\n" +
							metricsExpectationSnippet(fileName, actual, Map.of()));
		}
	}

	private static void assertSimulationMetrics(String fileName, String simulationName,
			SimulationMetrics expected, SimulationMetrics actual) {
		String context = fileName + " simulation '" + simulationName + "'";
		assertEquals(expected.launchMass, actual.launchMass, MASS_EPSILON,
				"Launch mass changed for " + context);
		assertCoordinateEquals(expected.launchCg, actual.launchCg, "Launch CG changed for " + context);
		assertEquals(expected.maxAltitude, actual.maxAltitude, MAX_ALTITUDE_EPSILON,
				"Maximum altitude changed for " + context);
	}

	private static void assertCoordinateEquals(CoordinateSnapshot expected, CoordinateSnapshot actual,
			String message) {
		assertEquals(expected.x, actual.x, POSITION_EPSILON, message + " (x)");
		assertEquals(expected.y, actual.y, POSITION_EPSILON, message + " (y)");
		assertEquals(expected.z, actual.z, POSITION_EPSILON, message + " (z)");
	}

	private static void assertBoundsEquals(BoundsSnapshot expected, BoundsSnapshot actual, String message) {
		assertCoordinateEquals(expected.min, actual.min, message + " (minimum)");
		assertCoordinateEquals(expected.max, actual.max, message + " (maximum)");
	}

	/**
	 * Produces a paste-ready baseline when a new example or simulation is added.
	 */
	private static String metricsExpectationSnippet(String fileName, DesignMetrics design,
			Map<String, SimulationMetrics> simulations) {
		StringBuilder sb = new StringBuilder();
		sb.append("EXPECTED_METRICS.put(\"").append(fileName).append("\", ExpectedMetrics.builder()\n");
		sb.append(String.format(Locale.ROOT,
				"\t\t.design(%s, %s, %s, %s, %s,\n" +
						"\t\t\t\t%s, %s, %s, %s, %s,\n" +
						"\t\t\t\t%s)\n",
				formatDouble(design.dryMass),
				formatDouble(design.dryCg.x), formatDouble(design.dryCg.y), formatDouble(design.dryCg.z),
				formatDouble(design.length),
				formatDouble(design.bounds.min.x), formatDouble(design.bounds.min.y),
				formatDouble(design.bounds.min.z), formatDouble(design.bounds.max.x),
				formatDouble(design.bounds.max.y), formatDouble(design.bounds.max.z)));
		for (double mach : AERODYNAMIC_MACHES) {
			AerodynamicMetrics aerodynamics = design.aerodynamics.get(mach);
			sb.append(String.format(Locale.ROOT, "\t\t.aerodynamics(%s, %s, %s)\n",
					formatMach(mach), formatDouble(aerodynamics.cpX), formatDouble(aerodynamics.cd)));
		}
		simulations.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> {
					SimulationMetrics simulation = entry.getValue();
					sb.append(String.format(Locale.ROOT,
							"\t\t.simulation(\"%s\", %s, %s, %s, %s, %s)\n",
							entry.getKey(), formatDouble(simulation.launchMass),
							formatDouble(simulation.launchCg.x), formatDouble(simulation.launchCg.y),
							formatDouble(simulation.launchCg.z), formatDouble(simulation.maxAltitude)));
				});
		sb.append("\t\t.build());\n");
		return sb.toString();
	}

	private static String formatDouble(double value) {
		if (Double.isNaN(value)) {
			return "Double.NaN";
		}
		if (Double.isInfinite(value)) {
			return value > 0 ? "Double.POSITIVE_INFINITY" : "Double.NEGATIVE_INFINITY";
		}
		return String.format(Locale.ROOT, "%.9f", value);
	}

	private static String formatMach(double mach) {
		return String.format(Locale.ROOT, "%.1f", mach);
	}

	private static final class CoordinateSnapshot {
		private final double x;
		private final double y;
		private final double z;

		private CoordinateSnapshot(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		private static CoordinateSnapshot from(CoordinateIF coordinate) {
			return new CoordinateSnapshot(coordinate.getX(), coordinate.getY(), coordinate.getZ());
		}
	}

	private static final class BoundsSnapshot {
		private final CoordinateSnapshot min;
		private final CoordinateSnapshot max;

		private BoundsSnapshot(CoordinateSnapshot min, CoordinateSnapshot max) {
			this.min = min;
			this.max = max;
		}

		private static BoundsSnapshot from(BoundingBox bounds) {
			return new BoundsSnapshot(CoordinateSnapshot.from(bounds.min), CoordinateSnapshot.from(bounds.max));
		}
	}

	private static final class DesignMetrics {
		private final double dryMass;
		private final CoordinateSnapshot dryCg;
		private final double length;
		private final BoundsSnapshot bounds;
		private final Map<Double, AerodynamicMetrics> aerodynamics;

		private DesignMetrics(double dryMass, CoordinateSnapshot dryCg, double length, BoundsSnapshot bounds,
				Map<Double, AerodynamicMetrics> aerodynamics) {
			this.dryMass = dryMass;
			this.dryCg = dryCg;
			this.length = length;
			this.bounds = bounds;
			this.aerodynamics = aerodynamics;
		}
	}

	private static final class AerodynamicMetrics {
		private final double cpX;
		private final double cd;

		private AerodynamicMetrics(double cpX, double cd) {
			this.cpX = cpX;
			this.cd = cd;
		}
	}

	private static final class SimulationMetrics {
		private final double launchMass;
		private final CoordinateSnapshot launchCg;
		private final double maxAltitude;

		private SimulationMetrics(double launchMass, CoordinateSnapshot launchCg, double maxAltitude) {
			this.launchMass = launchMass;
			this.launchCg = launchCg;
			this.maxAltitude = maxAltitude;
		}

		private SimulationMetrics withMaxAltitude(double altitude) {
			return new SimulationMetrics(launchMass, launchCg, altitude);
		}
	}

	private static final class ExpectedMetrics {
		private final DesignMetrics design;
		private final Map<String, SimulationMetrics> simulations;

		private ExpectedMetrics(DesignMetrics design, Map<String, SimulationMetrics> simulations) {
			this.design = design;
			this.simulations = simulations;
		}

		private static Builder builder() {
			return new Builder();
		}

		private static final class Builder {
			private DesignMetrics design;
			private final Map<Double, AerodynamicMetrics> aerodynamics = new LinkedHashMap<>();
			private final Map<String, SimulationMetrics> simulations = new LinkedHashMap<>();

			private Builder design(double dryMass, double dryCgX, double dryCgY, double dryCgZ,
					double length, double minX, double minY, double minZ,
					double maxX, double maxY, double maxZ) {
				CoordinateSnapshot dryCg = new CoordinateSnapshot(dryCgX, dryCgY, dryCgZ);
				BoundsSnapshot bounds = new BoundsSnapshot(new CoordinateSnapshot(minX, minY, minZ),
						new CoordinateSnapshot(maxX, maxY, maxZ));
				this.design = new DesignMetrics(dryMass, dryCg, length, bounds, Map.of());
				return this;
			}

			private Builder aerodynamics(double mach, double cpX, double cd) {
				this.aerodynamics.put(mach, new AerodynamicMetrics(cpX, cd));
				return this;
			}

			private Builder simulation(String name, double launchMass, double launchCgX,
					double launchCgY, double launchCgZ, double maxAltitude) {
				this.simulations.put(name, new SimulationMetrics(launchMass,
						new CoordinateSnapshot(launchCgX, launchCgY, launchCgZ), maxAltitude));
				return this;
			}

			private ExpectedMetrics build() {
				if (design == null) {
					throw new IllegalStateException("Expected design metrics are required");
				}
				DesignMetrics completedDesign = new DesignMetrics(design.dryMass, design.dryCg,
						design.length, design.bounds, Map.copyOf(aerodynamics));
				return new ExpectedMetrics(completedDesign, Map.copyOf(simulations));
			}
		}
	}

	private static final class WarningCounts {
		private static final WarningCounts MISSING = new WarningCounts(-1, -1, -1);

		private final int informative;
		private final int normal;
		private final int critical;

		private WarningCounts(int informative, int normal, int critical) {
			this.informative = informative;
			this.normal = normal;
			this.critical = critical;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof WarningCounts other)) {
				return false;
			}
			return this.informative == other.informative &&
					this.normal == other.normal &&
					this.critical == other.critical;
		}

		@Override
		public int hashCode() {
			int result = Integer.hashCode(informative);
			result = 31 * result + Integer.hashCode(normal);
			result = 31 * result + Integer.hashCode(critical);
			return result;
		}

		@Override
		public String toString() {
			return "{informative=" + informative + ", normal=" + normal + ", critical=" + critical + "}";
		}
	}

	private static final class FlightConfigurationSnapshot {
		private final String name;
		private final List<String> motors;

		private FlightConfigurationSnapshot(String name, List<String> motors) {
			this.name = name;
			this.motors = List.copyOf(motors);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof FlightConfigurationSnapshot other)) {
				return false;
			}
			return this.name.equals(other.name) && this.motors.equals(other.motors);
		}

		@Override
		public int hashCode() {
			int result = name.hashCode();
			result = 31 * result + motors.hashCode();
			return result;
		}
	}

	private static final class ExpectedWarnings {
		private final WarningCounts openWarnings;
		private final Map<String, WarningCounts> simulationWarnings;

		private ExpectedWarnings(WarningCounts openWarnings, Map<String, WarningCounts> simulationWarnings) {
			this.openWarnings = openWarnings;
			this.simulationWarnings = simulationWarnings;
		}

		private static Builder builder() {
			return new Builder();
		}

		private static final class Builder {
			private WarningCounts openWarnings;
			private final Map<String, WarningCounts> simulationWarnings = new HashMap<>();

			private Builder openWarnings(int informative, int normal, int critical) {
				this.openWarnings = new WarningCounts(informative, normal, critical);
				return this;
			}

			private Builder simulationWarnings(String simulationName, int informative, int normal, int critical) {
				this.simulationWarnings.put(simulationName, new WarningCounts(informative, normal, critical));
				return this;
			}

			private ExpectedWarnings build() {
				return new ExpectedWarnings(openWarnings, Map.copyOf(simulationWarnings));
			}
		}
	}

	private static final class ExpectedFlightConfigurations {
		private final List<FlightConfigurationSnapshot> configurations;

		private ExpectedFlightConfigurations(List<FlightConfigurationSnapshot> configurations) {
			this.configurations = List.copyOf(configurations);
		}

		private static Builder builder() {
			return new Builder();
		}

		private static final class Builder {
			private final List<FlightConfigurationSnapshot> configurations = new ArrayList<>();

			private Builder configuration(String configurationName, String... motors) {
				List<String> motorList = new ArrayList<>(List.of(motors));
				motorList.sort(Comparator.naturalOrder());
				this.configurations.add(new FlightConfigurationSnapshot(configurationName, motorList));
				return this;
			}

			private ExpectedFlightConfigurations build() {
				return new ExpectedFlightConfigurations(configurations);
			}
		}
	}
}
