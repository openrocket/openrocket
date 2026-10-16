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
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ThicknessRingComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.MathUtil;

public class ComponentParameterRoundTripTest extends BaseTestCase {
	private static final double EPSILON = MathUtil.EPSILON;
	private static final double COMPONENT_RADIUS = 0.05;
	private static final double BELOW_OLD_CLAMP_BOUNDARY = 0.024;
	private static final double OLD_CLAMP_BOUNDARY = 0.025;
	private static final double ABOVE_OLD_CLAMP_BOUNDARY = 0.030;
	private static final double BODY_TUBE_LENGTH = 0.4;
	private static final double BODY_TUBE_THICKNESS = 0.002;
	private static final double RING_OUTER_RADIUS = 0.03;
	private static final double RING_THICKNESS = 0.012;

	@TempDir
	Path tempDir;

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
				automaticOuterRadius ? COMPONENT_RADIUS - BODY_TUBE_THICKNESS : RING_OUTER_RADIUS);
	}

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
				automaticOuterRadius ? COMPONENT_RADIUS - BODY_TUBE_THICKNESS : RING_OUTER_RADIUS);
	}

	private OpenRocketDocument documentWith(RocketComponent component) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);
		stage.addChild(component);
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}

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

	private ClusterConfiguration clusterConfiguration(String xmlName) {
		return Arrays.stream(ClusterConfiguration.CONFIGURATIONS)
				.filter(configuration -> configuration.getXMLName().equals(xmlName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown cluster configuration: " + xmlName));
	}

	/**
	 * Compares the parameters that are saved for every thickness ring component (inner tube,
	 * engine block, tube coupler). The outer radius and thickness are checked against fixed
	 * values, so a clamp while setting up the test would also be caught.
	 */
	private void assertThicknessRingParametersEqual(ThicknessRingComponent original,
			ThicknessRingComponent loaded, double expectedOuterRadius) {
		assertEquals(original.getName(), loaded.getName());
		assertEquals(original.getLength(), loaded.getLength(), EPSILON);
		assertEquals(original.getAxialMethod(), loaded.getAxialMethod());
		assertEquals(original.getAxialOffset(), loaded.getAxialOffset(), EPSILON);
		assertEquals(original.isOuterRadiusAutomatic(), loaded.isOuterRadiusAutomatic());
		assertEquals(expectedOuterRadius, loaded.getOuterRadius(), EPSILON);
		assertEquals(RING_THICKNESS, loaded.getThickness(), EPSILON);
		assertEquals(original.getRadialPosition(), loaded.getRadialPosition(), EPSILON);
		assertEquals(original.getRadialDirection(), loaded.getRadialDirection(), EPSILON);
	}

	private OpenRocketDocument roundTrip(OpenRocketDocument document, String fileName)
			throws IOException, RocketLoadException, DecalNotFoundException {
		File file = tempDir.resolve(fileName).toFile();
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(false);
		new GeneralRocketSaver().save(file, document, options);
		return new GeneralRocketLoader(file).load();
	}

	private RocketComponent firstComponent(OpenRocketDocument document) {
		Rocket rocket = document.getRocket();
		assertEquals(1, rocket.getChildCount());
		RocketComponent stage = rocket.getChild(0);
		assertInstanceOf(AxialStage.class, stage);
		assertEquals(1, stage.getChildCount());
		return stage.getChild(0);
	}
}
