package info.openrocket.core.file.openrocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.MathUtil;

public class ComponentParameterRoundTripTest extends BaseTestCase {
	private static final double EPSILON = MathUtil.EPSILON;
	private static final double COMPONENT_RADIUS = 0.05;
	private static final double BELOW_OLD_CLAMP_BOUNDARY = 0.024;
	private static final double OLD_CLAMP_BOUNDARY = 0.025;
	private static final double ABOVE_OLD_CLAMP_BOUNDARY = 0.030;

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

	private OpenRocketDocument documentWith(RocketComponent component) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);
		stage.addChild(component);
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
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
