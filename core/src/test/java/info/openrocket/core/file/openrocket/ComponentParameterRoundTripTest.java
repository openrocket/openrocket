package info.openrocket.core.file.openrocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.DesignType;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RadiusRingComponent;
import info.openrocket.core.rocketcomponent.ReferenceType;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.ThicknessRingComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.position.AngleMethod;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.MathUtil;

/**
 * Tests that component parameters stay the same after saving and loading an OpenRocket file.
 *
 * @author Yibing Liu
 * @author Baizhen Huang
 */
public class ComponentParameterRoundTripTest extends BaseTestCase {
	// Tolerance used when comparing double values.
	private static final double EPSILON = MathUtil.EPSILON;

	// Radius used by the components in the basic round-trip tests.
	private static final double COMPONENT_RADIUS = 0.05;

	// Thickness value just below the old clamp boundary.
	private static final double BELOW_OLD_CLAMP_BOUNDARY = 0.024;

	// Thickness value at the old clamp boundary.
	private static final double OLD_CLAMP_BOUNDARY = 0.025;

	// Thickness value above the old clamp boundary.
	private static final double ABOVE_OLD_CLAMP_BOUNDARY = 0.030;

	// Length of the body tube that holds the components in the nested tests.
	private static final double BODY_TUBE_LENGTH = 0.4;

	// Wall thickness of that body tube.
	private static final double BODY_TUBE_THICKNESS = 0.002;

	// Inner radius of that body tube (0.048 m). Components with an automatic
	// outer radius should get this value.
	private static final double BODY_TUBE_INNER_RADIUS = COMPONENT_RADIUS - BODY_TUBE_THICKNESS;

	// Manual outer radius of the ring components. It is above the default inner tube
	// outer radius (0.0095 m), so a clamp to the default while loading would show.
	private static final double RING_OUTER_RADIUS = 0.03;

	// Thickness of the inner tube, engine block and tube coupler. It is also above
	// 0.0095 m, for the same reason.
	private static final double RING_THICKNESS = 0.012;

	// Manual inner radius of the centering ring.
	private static final double RING_INNER_RADIUS = 0.012;

	// Outer radius of the motor tube around the centering ring. The automatic
	// inner radius of the ring should get this value.
	private static final double MOTOR_TUBE_OUTER_RADIUS = 0.015;

	// Fixed radius used by all mass object tests. The automatic radius is not
	// tested because of #3399.
	private static final double MASS_OBJECT_RADIUS = 0.02;

	// Temporary folder used for the saved rocket files.
	@TempDir
	Path tempDir;

	/**
	 * Checks that the body tube parameters stay the same after saving and loading.
	 *
	 * @param thickness the body tube thickness used in the test
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "BodyTube thickness {0} m")
	@ValueSource(doubles = { BELOW_OLD_CLAMP_BOUNDARY, OLD_CLAMP_BOUNDARY, ABOVE_OLD_CLAMP_BOUNDARY })
	public void testBodyTubeParametersSurviveRoundTrip(double thickness)
			throws IOException, RocketLoadException, DecalNotFoundException {
		BodyTube original = new BodyTube();
		original.setName("Round-trip body tube");
		original.setLength(0.42);
		original.setOuterRadius(COMPONENT_RADIUS);
		original.setThickness(thickness);

		OpenRocketDocument loadedDocument = roundTrip(documentWith(original), "body-tube-" + thickness + ".ork");
		BodyTube loaded = assertInstanceOf(BodyTube.class, firstComponent(loadedDocument));

		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getOuterRadius(), loaded.getOuterRadius(), EPSILON);
		assertEquals(thickness, loaded.getThickness(), EPSILON);
		assertFalse(loaded.isOuterRadiusAutomatic());
		assertFalse(loaded.isFilled());
	}

	/**
	 * Checks that the transition parameters stay the same after saving and loading.
	 *
	 * @param thickness the transition thickness used in the test
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "Transition thickness {0} m")
	@ValueSource(doubles = { BELOW_OLD_CLAMP_BOUNDARY, OLD_CLAMP_BOUNDARY, ABOVE_OLD_CLAMP_BOUNDARY })
	public void testTransitionParametersSurviveRoundTrip(double thickness)
			throws IOException, RocketLoadException, DecalNotFoundException {
		Transition original = new Transition();
		original.setName("Round-trip transition");
		original.setLength(0.19);
		original.setForeRadius(0.045);
		original.setAftRadius(COMPONENT_RADIUS);
		original.setThickness(thickness);
		original.setShapeType(Transition.Shape.POWER);
		original.setShapeParameter(0.65);
		original.setClipped(false);
		original.setForeShoulderRadius(0.04);
		original.setForeShoulderLength(0.012);
		original.setForeShoulderThickness(0.003);
		original.setForeShoulderCapped(false);
		original.setAftShoulderRadius(0.048);
		original.setAftShoulderLength(0.014);
		original.setAftShoulderThickness(0.004);
		original.setAftShoulderCapped(false);

		OpenRocketDocument loadedDocument = roundTrip(documentWith(original), "transition-" + thickness + ".ork");
		Transition loaded = assertInstanceOf(Transition.class, firstComponent(loadedDocument));

		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getForeRadius(), loaded.getForeRadius(), EPSILON);
		assertEquals(original.getAftRadius(), loaded.getAftRadius(), EPSILON);
		assertEquals(thickness, loaded.getThickness(), EPSILON);
		assertEquals(original.getShapeType(), loaded.getShapeType());
		assertEquals(original.getShapeParameter(), loaded.getShapeParameter(), EPSILON);
		assertEquals(original.isClipped(), loaded.isClipped());
		assertEquals(original.getForeShoulderRadius(), loaded.getForeShoulderRadius(), EPSILON);
		assertEquals(original.getForeShoulderLength(), loaded.getForeShoulderLength(), EPSILON);
		assertEquals(original.getForeShoulderThickness(), loaded.getForeShoulderThickness(), EPSILON);
		assertEquals(original.isForeShoulderCapped(), loaded.isForeShoulderCapped());
		assertEquals(original.getAftShoulderRadius(), loaded.getAftShoulderRadius(), EPSILON);
		assertEquals(original.getAftShoulderLength(), loaded.getAftShoulderLength(), EPSILON);
		assertEquals(original.getAftShoulderThickness(), loaded.getAftShoulderThickness(), EPSILON);
		assertEquals(original.isAftShoulderCapped(), loaded.isAftShoulderCapped());
		assertFalse(loaded.isForeRadiusAutomatic());
		assertFalse(loaded.isAftRadiusAutomatic());
		assertFalse(loaded.isFilled());
	}

	/**
	 * Checks that the nose cone parameters stay the same after saving and loading.
	 *
	 * @param thickness the nose cone thickness used in the test
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "NoseCone thickness {0} m")
	@ValueSource(doubles = { BELOW_OLD_CLAMP_BOUNDARY, OLD_CLAMP_BOUNDARY, ABOVE_OLD_CLAMP_BOUNDARY })
	public void testNoseConeParametersSurviveRoundTrip(double thickness)
			throws IOException, RocketLoadException, DecalNotFoundException {
		NoseCone original = new NoseCone();
		original.setName("Round-trip nose cone");
		original.setLength(0.21);
		original.setBaseRadius(COMPONENT_RADIUS);
		original.setThickness(thickness);
		original.setShapeType(Transition.Shape.HAACK);
		original.setShapeParameter(0.2);
		original.setShoulderRadius(0.047);
		original.setShoulderLength(0.016);
		original.setShoulderThickness(0.0035);
		original.setShoulderCapped(false);

		OpenRocketDocument loadedDocument = roundTrip(documentWith(original), "nose-cone-" + thickness + ".ork");
		NoseCone loaded = assertInstanceOf(NoseCone.class, firstComponent(loadedDocument));

		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getBaseRadius(), loaded.getBaseRadius(), EPSILON);
		assertEquals(thickness, loaded.getThickness(), EPSILON);
		assertEquals(original.getShapeType(), loaded.getShapeType());
		assertEquals(original.getShapeParameter(), loaded.getShapeParameter(), EPSILON);
		assertEquals(original.getShoulderRadius(), loaded.getShoulderRadius(), EPSILON);
		assertEquals(original.getShoulderLength(), loaded.getShoulderLength(), EPSILON);
		assertEquals(original.getShoulderThickness(), loaded.getShoulderThickness(), EPSILON);
		assertEquals(original.isShoulderCapped(), loaded.isShoulderCapped());
		assertFalse(loaded.isBaseRadiusAutomatic());
		assertFalse(loaded.isFilled());
	}

	/**
	 * Checks that the inner tube parameters stay the same after saving and loading,
	 * including the cluster and motor mount settings.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testInnerTubeParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		InnerTube original = new InnerTube();
		original.setName("Round-trip inner tube");
		original.setLength(0.11);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.05);
		// Both values are above the default inner tube outer radius (0.0095 m)
		original.setOuterRadius(RING_OUTER_RADIUS);
		original.setThickness(RING_THICKNESS);
		original.setRadialPosition(0.004);
		original.setRadialDirection(0.7);
		original.setClusterConfiguration(clusterConfiguration("3-ring"));
		original.setClusterScale(1.2);
		original.setClusterRotation(0.4);
		original.setMotorMount(true);
		original.setMotorOverhang(0.006);

		OpenRocketDocument loadedDocument = roundTrip(documentWithBodyTubeChild(original), "inner-tube.ork");
		InnerTube loaded = assertInstanceOf(InnerTube.class, componentAt(loadedDocument, 0, 0, 0));

		assertThicknessRingParametersEqual(original, loaded, RING_OUTER_RADIUS);
		assertEquals("3-ring", loaded.getClusterConfiguration().getXMLName());
		assertEquals(original.getClusterScale(), loaded.getClusterScale(), EPSILON);
		assertEquals(original.getClusterRotation(), loaded.getClusterRotation(), EPSILON);
		assertTrue(loaded.isMotorMount());
		assertEquals(original.getMotorOverhang(), loaded.getMotorOverhang(), EPSILON);
	}

	/**
	 * Checks that the engine block parameters stay the same after saving and loading,
	 * with a manual and with an automatic outer radius.
	 *
	 * @param automaticOuterRadius whether the outer radius is taken from the body tube
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "EngineBlock automatic outer radius: {0}")
	@ValueSource(booleans = { false, true })
	public void testEngineBlockParametersSurviveRoundTrip(boolean automaticOuterRadius)
			throws IOException, RocketLoadException, DecalNotFoundException {
		EngineBlock original = new EngineBlock();
		// The thickness is clamped to the outer radius, which in automatic mode comes from the
		// parent body tube, so the parameters are set once the block is inside it.
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip engine block");
		original.setLength(0.02);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.1);
		if (automaticOuterRadius) {
			original.setOuterRadiusAutomatic(true);
		} else {
			original.setOuterRadius(RING_OUTER_RADIUS);
		}
		original.setThickness(RING_THICKNESS);
		original.setRadialPosition(0.003);
		original.setRadialDirection(-1.2);

		OpenRocketDocument loadedDocument = roundTrip(document, "engine-block-" + automaticOuterRadius + ".ork");
		EngineBlock loaded = assertInstanceOf(EngineBlock.class, componentAt(loadedDocument, 0, 0, 0));

		assertThicknessRingParametersEqual(original, loaded,
				automaticOuterRadius ? BODY_TUBE_INNER_RADIUS : RING_OUTER_RADIUS);
	}

	/**
	 * Checks that the tube coupler parameters stay the same after saving and loading,
	 * with a manual and with an automatic outer radius.
	 *
	 * @param automaticOuterRadius whether the outer radius is taken from the body tube
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "TubeCoupler automatic outer radius: {0}")
	@ValueSource(booleans = { false, true })
	public void testTubeCouplerParametersSurviveRoundTrip(boolean automaticOuterRadius)
			throws IOException, RocketLoadException, DecalNotFoundException {
		TubeCoupler original = new TubeCoupler();
		// Same reason as for the engine block: set the parameters once it is inside the body tube.
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip tube coupler");
		original.setLength(0.08);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.15);
		if (automaticOuterRadius) {
			original.setOuterRadiusAutomatic(true);
		} else {
			original.setOuterRadius(RING_OUTER_RADIUS);
		}
		original.setThickness(RING_THICKNESS);
		original.setRadialPosition(0.002);
		original.setRadialDirection(2.5);

		OpenRocketDocument loadedDocument = roundTrip(document, "tube-coupler-" + automaticOuterRadius + ".ork");
		TubeCoupler loaded = assertInstanceOf(TubeCoupler.class, componentAt(loadedDocument, 0, 0, 0));

		assertThicknessRingParametersEqual(original, loaded,
				automaticOuterRadius ? BODY_TUBE_INNER_RADIUS : RING_OUTER_RADIUS);
	}

	/**
	 * Checks that the bulkhead parameters stay the same after saving and loading,
	 * including the instance count and separation, with a manual and with an automatic
	 * outer radius.
	 *
	 * @param automaticOuterRadius whether the outer radius is taken from the body tube
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "Bulkhead automatic outer radius: {0}")
	@ValueSource(booleans = { false, true })
	public void testBulkheadParametersSurviveRoundTrip(boolean automaticOuterRadius)
			throws IOException, RocketLoadException, DecalNotFoundException {
		Bulkhead original = new Bulkhead();
		// The automatic outer radius comes from the parent body tube
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip bulkhead");
		original.setLength(0.006);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.03);
		original.setInstanceCount(2);
		original.setInstanceSeparation(0.05);
		if (automaticOuterRadius) {
			original.setOuterRadiusAutomatic(true);
		} else {
			original.setOuterRadius(RING_OUTER_RADIUS);
		}
		original.setRadialPosition(0.001);
		original.setRadialDirection(0.3);

		OpenRocketDocument loadedDocument = roundTrip(document, "bulkhead-" + automaticOuterRadius + ".ork");
		Bulkhead loaded = assertInstanceOf(Bulkhead.class, componentAt(loadedDocument, 0, 0, 0));

		assertRadiusRingParametersEqual(original, loaded,
				automaticOuterRadius ? BODY_TUBE_INNER_RADIUS : RING_OUTER_RADIUS);
	}

	/**
	 * Checks that the centering ring parameters stay the same after saving and loading,
	 * with manual radii and with automatic radii. A motor tube is placed around the ring
	 * so that the automatic inner radius has a value to take.
	 *
	 * @param automaticRadii whether both radii are automatic
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "CenteringRing automatic radii: {0}")
	@ValueSource(booleans = { false, true })
	public void testCenteringRingParametersSurviveRoundTrip(boolean automaticRadii)
			throws IOException, RocketLoadException, DecalNotFoundException {
		CenteringRing original = new CenteringRing();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		// The automatic inner radius follows the outer radius of an inner tube that overlaps the
		// ring along the axis, so place one over the aft end of the body tube, around the ring.
		InnerTube motorTube = new InnerTube();
		componentAt(document, 0, 0).addChild(motorTube);
		motorTube.setLength(0.1);
		motorTube.setAxialMethod(AxialMethod.BOTTOM);
		motorTube.setAxialOffset(0);
		motorTube.setOuterRadius(MOTOR_TUBE_OUTER_RADIUS);

		original.setName("Round-trip centering ring");
		original.setLength(0.003);
		original.setAxialMethod(AxialMethod.MIDDLE);
		original.setAxialOffset(0.16);
		original.setInstanceCount(2);
		original.setInstanceSeparation(0.01);
		if (automaticRadii) {
			original.setOuterRadiusAutomatic(true);
			original.setInnerRadiusAutomatic(true);
		} else {
			original.setOuterRadius(RING_OUTER_RADIUS);
			original.setInnerRadius(RING_INNER_RADIUS);
		}
		original.setRadialPosition(0.001);
		original.setRadialDirection(-0.5);

		OpenRocketDocument loadedDocument = roundTrip(document, "centering-ring-" + automaticRadii + ".ork");
		CenteringRing loaded = assertInstanceOf(CenteringRing.class, componentAt(loadedDocument, 0, 0, 0));

		assertRadiusRingParametersEqual(original, loaded,
				automaticRadii ? BODY_TUBE_INNER_RADIUS : RING_OUTER_RADIUS);
		assertEquals(original.isInnerRadiusAutomatic(), loaded.isInnerRadiusAutomatic());
		assertEquals(automaticRadii ? MOTOR_TUBE_OUTER_RADIUS : RING_INNER_RADIUS, loaded.getInnerRadius(), EPSILON);
	}

	/**
	 * Checks that the mass component parameters stay the same after saving and loading.
	 * The mass override is not tested because of #3398, and the automatic radius is not
	 * tested because of #3399.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testMassComponentParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		MassComponent original = new MassComponent();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip mass component");
		original.setAxialMethod(AxialMethod.MIDDLE);
		original.setAxialOffset(-0.05);
		original.setLength(0.06);
		// Only a fixed radius is tested. With an automatic radius, getLength() currently
		// depends on whether getRadius() was called first, even without saving the file. See #3399.
		original.setRadius(MASS_OBJECT_RADIUS);
		original.setRadialPosition(0.005);
		original.setRadialDirection(1.1);
		original.setComponentMass(0.15);
		original.setMassComponentType(MassComponent.MassComponentType.ALTIMETER);

		OpenRocketDocument loadedDocument = roundTrip(document, "mass-component.ork");
		MassComponent loaded = assertInstanceOf(MassComponent.class, componentAt(loadedDocument, 0, 0, 0));

		assertMassObjectParametersEqual(original, loaded);
		assertEquals(0.15, loaded.getComponentMass(), EPSILON);
		assertEquals(MassComponent.MassComponentType.ALTIMETER, loaded.getMassComponentType());
	}

	/**
	 * Checks that the shock cord parameters stay the same after saving and loading,
	 * with a manual cord length.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testShockCordParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		ShockCord original = new ShockCord();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip shock cord");
		original.setAxialMethod(AxialMethod.BOTTOM);
		original.setAxialOffset(-0.02);
		original.setLength(0.03);
		original.setRadius(MASS_OBJECT_RADIUS);
		original.setRadialPosition(0.002);
		original.setRadialDirection(-2.0);
		// The automatic cord length is already covered by ShockCordTest
		original.setCordLength(1.5);

		OpenRocketDocument loadedDocument = roundTrip(document, "shock-cord.ork");
		ShockCord loaded = assertInstanceOf(ShockCord.class, componentAt(loadedDocument, 0, 0, 0));

		assertMassObjectParametersEqual(original, loaded);
		assertFalse(loaded.isCordLengthAutomatic());
		assertEquals(1.5, loaded.getCordLength(), EPSILON);
	}

	/**
	 * Checks that the parachute parameters stay the same after saving and loading,
	 * including the default deployment and a deployment override for one flight
	 * configuration. Only a manual CD is tested.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testParachuteParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		Parachute original = new Parachute();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		FlightConfigurationId overrideId = new FlightConfigurationId();
		document.getRocket().createFlightConfiguration(overrideId);

		original.setName("Round-trip parachute");
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.03);
		original.setLength(0.05);
		original.setRadius(MASS_OBJECT_RADIUS);
		original.setRadialPosition(0.003);
		original.setRadialDirection(0.5);
		// Only a fixed CD is tested. A parachute that is switched back to an automatic CD
		// currently reopens with the default CD instead of the value shown before saving.
		original.setCD(1.2);
		original.setDrogue(true);
		original.setDiameter(0.45);
		original.setLineCount(8);
		original.setLineLength(0.5);

		DeploymentConfiguration defaultDeployment = original.getDeploymentConfigurations().getDefault();
		defaultDeployment.setDeployEvent(DeployEvent.ALTITUDE);
		defaultDeployment.setDeployAltitude(150);
		defaultDeployment.setDeployDelay(1.5);
		// A flight configuration can override the default deployment
		DeploymentConfiguration overrideDeployment = defaultDeployment.clone();
		overrideDeployment.setDeployEvent(DeployEvent.APOGEE);
		overrideDeployment.setDeployDelay(2.5);
		original.getDeploymentConfigurations().set(overrideId, overrideDeployment);

		OpenRocketDocument loadedDocument = roundTrip(document, "parachute.ork");
		Parachute loaded = assertInstanceOf(Parachute.class, componentAt(loadedDocument, 0, 0, 0));

		assertMassObjectParametersEqual(original, loaded);
		assertFalse(loaded.isCDAutomatic());
		assertEquals(1.2, loaded.getCD(), EPSILON);
		assertTrue(loaded.isDrogue());
		assertDeploymentEquals(DeployEvent.ALTITUDE, 150, 1.5,
				loaded.getDeploymentConfigurations().getDefault());
		assertDeploymentEquals(DeployEvent.APOGEE, 150, 2.5,
				loaded.getDeploymentConfigurations().get(overrideId));
		assertEquals(0.45, loaded.getDiameter(), EPSILON);
		assertEquals(8, loaded.getLineCount());
		assertFalse(loaded.isLineLengthAutomatic());
		assertEquals(0.5, loaded.getLineLength(), EPSILON);
	}

	/**
	 * Checks that the streamer parameters stay the same after saving and loading,
	 * with a manual and with an automatic CD.
	 *
	 * @param automaticCd whether the CD is calculated automatically
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@ParameterizedTest(name = "Streamer automatic CD: {0}")
	@ValueSource(booleans = { false, true })
	public void testStreamerParametersSurviveRoundTrip(boolean automaticCd)
			throws IOException, RocketLoadException, DecalNotFoundException {
		Streamer original = new Streamer();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip streamer");
		original.setAxialMethod(AxialMethod.BOTTOM);
		original.setAxialOffset(-0.01);
		original.setLength(0.04);
		original.setRadius(MASS_OBJECT_RADIUS);
		original.setRadialPosition(0.001);
		original.setRadialDirection(-1.0);
		original.setStripLength(1.2);
		original.setStripWidth(0.08);
		if (automaticCd) {
			original.setCDAutomatic(true);
		} else {
			original.setCD(0.9);
		}

		DeploymentConfiguration deployment = original.getDeploymentConfigurations().getDefault();
		deployment.setDeployEvent(DeployEvent.LOWER_STAGE_SEPARATION);
		deployment.setDeployAltitude(80);
		deployment.setDeployDelay(0.5);

		OpenRocketDocument loadedDocument = roundTrip(document, "streamer-" + automaticCd + ".ork");
		Streamer loaded = assertInstanceOf(Streamer.class, componentAt(loadedDocument, 0, 0, 0));

		assertMassObjectParametersEqual(original, loaded);
		assertEquals(1.2, loaded.getStripLength(), EPSILON);
		assertEquals(0.08, loaded.getStripWidth(), EPSILON);
		assertEquals(automaticCd, loaded.isCDAutomatic());
		// The automatic streamer CD is calculated from the strip size and material
		assertEquals(automaticCd ? original.getCD() : 0.9, loaded.getCD(), EPSILON);
		assertDeploymentEquals(DeployEvent.LOWER_STAGE_SEPARATION, 80, 0.5,
				loaded.getDeploymentConfigurations().getDefault());
	}

	/**
	 * Checks that the rocket design information and flight configurations stay the
	 * same after saving and loading. The designer name contains XML special characters
	 * to check that they are escaped.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testRocketParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		OpenRocketDocument document = documentWith(new BodyTube(BODY_TUBE_LENGTH, COMPONENT_RADIUS,
				BODY_TUBE_THICKNESS));
		Rocket original = document.getRocket();
		original.setName("Round-trip rocket");
		// Characters that must be escaped in XML
		original.setDesigner("Designer & <team>");
		original.setRevision("Second revision");
		original.setDesignType(DesignType.COMMERCIAL_KIT);
		original.setKitName("Round-trip kit");
		original.setReferenceType(ReferenceType.CUSTOM);
		original.setCustomReferenceLength(0.08);

		FlightConfigurationId firstId = new FlightConfigurationId();
		FlightConfigurationId selectedId = new FlightConfigurationId();
		original.createFlightConfiguration(firstId);
		original.createFlightConfiguration(selectedId).setName("Round-trip flight");
		original.setSelectedConfiguration(selectedId);

		OpenRocketDocument loadedDocument = roundTrip(document, "rocket.ork");
		Rocket loaded = loadedDocument.getRocket();

		assertEquals("Round-trip rocket", loaded.getName());
		assertEquals("Designer & <team>", loaded.getDesigner());
		assertEquals("Second revision", loaded.getRevision());
		assertEquals(DesignType.COMMERCIAL_KIT, loaded.getDesignType());
		assertEquals("Round-trip kit", loaded.getKitName());
		assertEquals(ReferenceType.CUSTOM, loaded.getReferenceType());
		assertEquals(0.08, loaded.getCustomReferenceLength(), EPSILON);
		assertTrue(loaded.containsFlightConfigurationID(firstId));
		assertTrue(loaded.containsFlightConfigurationID(selectedId));
		assertEquals("Round-trip flight", loaded.getFlightConfiguration(selectedId).getNameRaw());
		assertEquals(selectedId, loaded.getSelectedConfiguration().getFlightConfigurationID());
	}

	/**
	 * Checks that the separation settings of a second stage stay the same after saving
	 * and loading, including an override for one flight configuration. Two stages are
	 * needed because separation settings are only saved from the second stage on.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testAxialStageParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		Rocket rocket = new Rocket();
		AxialStage sustainer = new AxialStage();
		AxialStage original = new AxialStage();
		rocket.addChild(sustainer);
		sustainer.addChild(new BodyTube(BODY_TUBE_LENGTH, COMPONENT_RADIUS, BODY_TUBE_THICKNESS));
		rocket.addChild(original);
		original.addChild(new BodyTube(BODY_TUBE_LENGTH, COMPONENT_RADIUS, BODY_TUBE_THICKNESS));
		rocket.enableEvents();
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		FlightConfigurationId overrideId = new FlightConfigurationId();
		rocket.createFlightConfiguration(overrideId);

		original.setName("Round-trip booster stage");
		// Separation settings are only saved for stages after the first one
		StageSeparationConfiguration defaultSeparation = original.getSeparationConfigurations().getDefault();
		defaultSeparation.setSeparationEvent(SeparationEvent.BURNOUT);
		defaultSeparation.setSeparationAltitude(300);
		defaultSeparation.setSeparationDelay(1.0);
		// A flight configuration can override the default separation
		StageSeparationConfiguration overrideSeparation = defaultSeparation.clone();
		overrideSeparation.setSeparationEvent(SeparationEvent.ALTITUDE_ASCENDING);
		overrideSeparation.setSeparationDelay(2.0);
		original.getSeparationConfigurations().set(overrideId, overrideSeparation);

		OpenRocketDocument loadedDocument = roundTrip(document, "axial-stage.ork");
		AxialStage loaded = assertInstanceOf(AxialStage.class, componentAt(loadedDocument, 1));

		assertEquals(1, loaded.getStageNumber());
		assertEquals("Round-trip booster stage", loaded.getName());
		assertSeparationEquals(SeparationEvent.BURNOUT, 300, 1.0,
				loaded.getSeparationConfigurations().getDefault());
		assertSeparationEquals(SeparationEvent.ALTITUDE_ASCENDING, 300, 2.0,
				loaded.getSeparationConfigurations().get(overrideId));
	}

	/**
	 * Checks that the parallel stage parameters stay the same after saving and loading,
	 * using non-default radius and angle methods.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testParallelStageParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		ParallelStage original = new ParallelStage();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip parallel stage");
		original.setInstanceCount(3);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.05);
		original.setRadiusMethod(RadiusMethod.FREE);
		original.setRadiusOffset(0.08);
		original.setAngleMethod(AngleMethod.FIXED);
		original.setAngleOffset(0.6);
		StageSeparationConfiguration separation = original.getSeparationConfigurations().getDefault();
		separation.setSeparationEvent(SeparationEvent.LAUNCH);
		separation.setSeparationAltitude(100);
		separation.setSeparationDelay(0.5);

		OpenRocketDocument loadedDocument = roundTrip(document, "parallel-stage.ork");
		ParallelStage loaded = assertInstanceOf(ParallelStage.class, componentAt(loadedDocument, 0, 0, 0));

		assertEquals("Round-trip parallel stage", loaded.getName());
		assertEquals(3, loaded.getInstanceCount());
		assertEquals(AxialMethod.TOP, loaded.getAxialMethod());
		assertEquals(0.05, loaded.getAxialOffset(), EPSILON);
		assertEquals(RadiusMethod.FREE, loaded.getRadiusMethod());
		assertEquals(0.08, loaded.getRadiusOffset(), EPSILON);
		assertEquals(AngleMethod.FIXED, loaded.getAngleMethod());
		assertEquals(0.6, loaded.getAngleOffset(), EPSILON);
		assertSeparationEquals(SeparationEvent.LAUNCH, 100, 0.5,
				loaded.getSeparationConfigurations().getDefault());
	}

	/**
	 * Checks that the pod set parameters stay the same after saving and loading.
	 * The angle method is not checked because PodSet.setAngleMethod() does nothing.
	 *
	 * @throws IOException if the test file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	@Test
	public void testPodSetParametersSurviveRoundTrip()
			throws IOException, RocketLoadException, DecalNotFoundException {
		PodSet original = new PodSet();
		OpenRocketDocument document = documentWithBodyTubeChild(original);
		original.setName("Round-trip pod set");
		original.setInstanceCount(3);
		original.setAxialMethod(AxialMethod.TOP);
		original.setAxialOffset(0.1);
		original.setRadiusMethod(RadiusMethod.FREE);
		original.setRadiusOffset(0.07);
		original.setAngleOffset(0.4);

		OpenRocketDocument loadedDocument = roundTrip(document, "pod-set.ork");
		PodSet loaded = assertInstanceOf(PodSet.class, componentAt(loadedDocument, 0, 0, 0));

		assertEquals("Round-trip pod set", loaded.getName());
		assertEquals(3, loaded.getInstanceCount());
		assertEquals(AxialMethod.TOP, loaded.getAxialMethod());
		assertEquals(0.1, loaded.getAxialOffset(), EPSILON);
		assertEquals(RadiusMethod.FREE, loaded.getRadiusMethod());
		assertEquals(0.07, loaded.getRadiusOffset(), EPSILON);
		assertEquals(0.4, loaded.getAngleOffset(), EPSILON);
	}

	/**
	 * Creates a rocket document with one stage and the given component.
	 *
	 * @param component the component added to the stage
	 *
	 * @return a rocket document containing the component
	 */
	private OpenRocketDocument documentWith(RocketComponent component) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);
		stage.addChild(component);
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}

	/**
	 * Creates a rocket document with one stage and one body tube, with the given
	 * component inside the body tube.
	 *
	 * @param component the component added to the body tube
	 *
	 * @return a rocket document containing the component
	 */
	private OpenRocketDocument documentWithBodyTubeChild(RocketComponent component) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		BodyTube bodyTube = new BodyTube(BODY_TUBE_LENGTH, COMPONENT_RADIUS, BODY_TUBE_THICKNESS);
		rocket.addChild(stage);
		stage.addChild(bodyTube);
		bodyTube.addChild(component);
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}

	/**
	 * Returns the component reached from the rocket by following the given child indices,
	 * e.g. (0, 0, 0) is the first child of the first component in the first stage.
	 *
	 * @param document the rocket document to read
	 * @param childIndices the child index at each level
	 *
	 * @return the component at that position
	 */
	private RocketComponent componentAt(OpenRocketDocument document, int... childIndices) {
		RocketComponent component = document.getRocket();
		for (int index : childIndices) {
			assertTrue(index < component.getChildCount(),
					"Missing child " + index + " of " + component.getComponentName());
			component = component.getChild(index);
		}
		return component;
	}

	/**
	 * Finds a cluster configuration by the name used in the .ork file.
	 *
	 * @param xmlName the XML name of the configuration, e.g. "3-ring"
	 *
	 * @return the matching cluster configuration
	 */
	private ClusterConfiguration clusterConfiguration(String xmlName) {
		return Arrays.stream(ClusterConfiguration.CONFIGURATIONS)
				.filter(configuration -> configuration.getXMLName().equals(xmlName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown cluster configuration: " + xmlName));
	}

	/**
	 * Compares the parameters that are saved for every ring component. The outer radius is
	 * checked against a fixed value, so a clamp while setting up the test would also be caught.
	 *
	 * @param original the component before saving
	 * @param loaded the component after loading
	 * @param expectedOuterRadius the outer radius the loaded component should have
	 */
	private void assertRingParametersEqual(RingComponent original, RingComponent loaded,
			double expectedOuterRadius) {
		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getAxialMethod(), loaded.getAxialMethod());
		assertEquals(original.getAxialOffset(), loaded.getAxialOffset(), EPSILON);
		assertEquals(original.isOuterRadiusAutomatic(), loaded.isOuterRadiusAutomatic());
		assertEquals(expectedOuterRadius, loaded.getOuterRadius(), EPSILON);
		assertEquals(original.getRadialPosition(), loaded.getRadialPosition(), EPSILON);
		assertEquals(original.getRadialDirection(), loaded.getRadialDirection(), EPSILON);
	}

	/**
	 * Adds the thickness to the shared ring checks (inner tube, engine block, tube coupler).
	 *
	 * @param original the component before saving
	 * @param loaded the component after loading
	 * @param expectedOuterRadius the outer radius the loaded component should have
	 */
	private void assertThicknessRingParametersEqual(ThicknessRingComponent original,
			ThicknessRingComponent loaded, double expectedOuterRadius) {
		assertRingParametersEqual(original, loaded, expectedOuterRadius);
		assertEquals(RING_THICKNESS, loaded.getThickness(), EPSILON);
	}

	/**
	 * Adds the instance count and separation to the shared ring checks (bulkhead, centering
	 * ring). Only radius ring components save these two.
	 *
	 * @param original the component before saving
	 * @param loaded the component after loading
	 * @param expectedOuterRadius the outer radius the loaded component should have
	 */
	private void assertRadiusRingParametersEqual(RadiusRingComponent original,
			RadiusRingComponent loaded, double expectedOuterRadius) {
		assertRingParametersEqual(original, loaded, expectedOuterRadius);
		assertEquals(original.getInstanceCount(), loaded.getInstanceCount());
		assertEquals(original.getInstanceSeparation(), loaded.getInstanceSeparation(), EPSILON);
	}

	/**
	 * Compares the parameters that are saved for every mass object (mass component, shock cord,
	 * recovery devices). All of these tests use the fixed radius MASS_OBJECT_RADIUS.
	 *
	 * @param original the component before saving
	 * @param loaded the component after loading
	 */
	private void assertMassObjectParametersEqual(MassObject original, MassObject loaded) {
		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getAxialMethod(), loaded.getAxialMethod());
		assertEquals(original.getAxialOffset(), loaded.getAxialOffset(), EPSILON);
		assertFalse(loaded.isRadiusAutomatic());
		assertEquals(MASS_OBJECT_RADIUS, loaded.getRadius(), EPSILON);
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getRadialPosition(), loaded.getRadialPosition(), EPSILON);
		assertEquals(original.getRadialDirection(), loaded.getRadialDirection(), EPSILON);
	}

	/**
	 * Checks the event, altitude and delay of a loaded deployment configuration.
	 *
	 * @param expectedEvent the expected deployment event
	 * @param expectedAltitude the expected deployment altitude in metres
	 * @param expectedDelay the expected deployment delay in seconds
	 * @param loaded the deployment configuration after loading
	 */
	private void assertDeploymentEquals(DeployEvent expectedEvent, double expectedAltitude,
			double expectedDelay, DeploymentConfiguration loaded) {
		assertEquals(expectedEvent, loaded.getDeployEvent());
		assertEquals(expectedAltitude, loaded.getDeployAltitude(), EPSILON);
		assertEquals(expectedDelay, loaded.getDeployDelay(), EPSILON);
	}

	/**
	 * Checks the event, altitude and delay of a loaded stage separation configuration.
	 *
	 * @param expectedEvent the expected separation event
	 * @param expectedAltitude the expected separation altitude in metres
	 * @param expectedDelay the expected separation delay in seconds
	 * @param loaded the separation configuration after loading
	 */
	private void assertSeparationEquals(SeparationEvent expectedEvent, double expectedAltitude,
			double expectedDelay, StageSeparationConfiguration loaded) {
		assertEquals(expectedEvent, loaded.getSeparationEvent());
		assertEquals(expectedAltitude, loaded.getSeparationAltitude(), EPSILON);
		assertEquals(expectedDelay, loaded.getSeparationDelay(), EPSILON);
	}

	/**
	 * Saves a rocket document to a temporary file and loads it again.
	 *
	 * @param document the rocket document to save
	 * @param fileName the name of the temporary file
	 *
	 * @return the document loaded from the saved file
	 *
	 * @throws IOException if the file cannot be saved or read
	 * @throws RocketLoadException if the saved rocket cannot be loaded
	 * @throws DecalNotFoundException if a decal cannot be found while loading
	 */
	private OpenRocketDocument roundTrip(OpenRocketDocument document, String fileName)
			throws IOException, RocketLoadException, DecalNotFoundException {
		File file = tempDir.resolve(fileName).toFile();
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(false);
		new GeneralRocketSaver().save(file, document, options);
		return new GeneralRocketLoader(file).load();
	}

	/**
	 * Gets the only component from the first stage of a rocket document.
	 *
	 * @param document the rocket document to read
	 *
	 * @return the component in the first stage
	 */
	private RocketComponent firstComponent(OpenRocketDocument document) {
		Rocket rocket = document.getRocket();
		assertEquals(1, rocket.getChildCount());
		RocketComponent stage = rocket.getChild(0);
		assertInstanceOf(AxialStage.class, stage);
		assertEquals(1, stage.getChildCount());
		return stage.getChild(0);
	}
}
