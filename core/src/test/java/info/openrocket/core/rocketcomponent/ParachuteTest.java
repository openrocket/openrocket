package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;

public class ParachuteTest extends BaseTestCase {
	private static final double EPSILON = MathUtil.EPSILON;

	@TempDir
	Path tempDir;

	@Test
	public void testParachuteLineLengthAutomaticDefault() {
		Parachute parachute = new Parachute();
		assertTrue(parachute.isLineLengthAutomatic(), "Expected line length automatic by default");
		assertEquals(parachute.getDiameter() * 1.5, parachute.getLineLength(), EPSILON,
				"Auto line length should be 1.5x diameter");
	}

	@Test
	public void testParachuteLineLengthManualDisablesAutomatic() {
		Parachute parachute = new Parachute();
		parachute.setLineLengthAutomatic(true);
		parachute.setLineLength(0.25);
		assertFalse(parachute.isLineLengthAutomatic(), "Manual line length should disable automatic");
		assertEquals(0.25, parachute.getLineLength(), EPSILON, "Manual line length should be retained");
	}

	@Test
	public void testParachuteLineLengthAutomaticSet() {
		Parachute parachute = new Parachute();
		parachute.setLineLength(0.2);
		parachute.setDiameter(0.4);
		parachute.setLineLengthAutomatic(true);
		assertTrue(parachute.isLineLengthAutomatic(), "Line length should be automatic after enabling");
		assertEquals(0.6, parachute.getLineLength(), EPSILON, "Auto line length should match diameter");
	}

	@Test
	public void testParachuteLineLengthSaveLoadAutomatic() {
		OpenRocketDocument document = createDocumentWithRecoveryComponents();
		Rocket rocket = document.getRocket();
		Parachute parachute = findComponent(rocket, Parachute.class);
		parachute.setLineLengthAutomatic(true);

		OpenRocketDocument loadedDocument = saveAndLoad(document);
		Parachute loadedParachute = findComponent(loadedDocument.getRocket(), Parachute.class);

		assertTrue(loadedParachute.isLineLengthAutomatic(), "Line length automatic should persist");
		assertEquals(loadedParachute.getDiameter() * 1.5, loadedParachute.getLineLength(), EPSILON,
				"Loaded parachute should keep auto line length");
	}

	private OpenRocketDocument createDocumentWithRecoveryComponents() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = (AxialStage) rocket.getChild(0);
		BodyTube body = new BodyTube();
		body.setLength(1.0);
		body.setOuterRadius(0.05);
		stage.addChild(body);
		body.addChild(new Parachute());
		body.addChild(new ShockCord());
		return document;
	}

	private OpenRocketDocument saveAndLoad(OpenRocketDocument document) {
		File file = tempDir.resolve("parachute-auto-length.ork").toFile();
		try (OutputStream out = new FileOutputStream(file)) {
			new OpenRocketSaver().save(out, document, new StorageOptions(), new WarningSet(), new ErrorSet());
		} catch (IOException e) {
			fail("Failed to save test rocket: " + e.getMessage());
		}

		GeneralRocketLoader loader = new GeneralRocketLoader(file);
		try {
			return loader.load();
		} catch (RocketLoadException e) {
			fail("Failed to load test rocket: " + e.getMessage());
		}
		return null;
	}

	private <T extends RocketComponent> T findComponent(Rocket rocket, Class<T> type) {
		Iterator<RocketComponent> iterator = rocket.iterator(true);
		while (iterator.hasNext()) {
			RocketComponent component = iterator.next();
			if (type.isInstance(component)) {
				return type.cast(component);
			}
		}
		fail("Expected component not found: " + type.getSimpleName());
		return null;
	}
}
