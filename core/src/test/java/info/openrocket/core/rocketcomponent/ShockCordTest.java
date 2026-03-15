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

public class ShockCordTest extends BaseTestCase {
	private static final double EPSILON = MathUtil.EPSILON;

	@TempDir
	Path tempDir;

	@Test
	public void testShockCordLengthAutomaticDefault() {
		OpenRocketDocument document = createDocumentWithRecoveryComponents();
		Rocket rocket = document.getRocket();
		ShockCord cord = findComponent(rocket, ShockCord.class);
		assertTrue(cord.isCordLengthAutomatic(), "Expected cord length automatic by default");
		assertEquals(rocket.getLength() * 3.0, cord.getCordLength(), EPSILON,
				"Auto shock cord length should be 3x rocket length");
	}

	@Test
	public void testShockCordLengthAutomaticSet() {
		OpenRocketDocument document = createDocumentWithRecoveryComponents();
		Rocket rocket = document.getRocket();
		ShockCord cord = findComponent(rocket, ShockCord.class);
		cord.setCordLength(1.2);
		cord.setCordLengthAutomatic(true);
		assertTrue(cord.isCordLengthAutomatic(), "Cord length should be automatic after enabling");
		assertEquals(rocket.getLength() * 3.0, cord.getCordLength(), EPSILON,
				"Auto shock cord length should be 3x rocket length");
	}

	@Test
	public void testShockCordLengthManualDisablesAutomatic() {
		OpenRocketDocument document = createDocumentWithRecoveryComponents();
		ShockCord cord = findComponent(document.getRocket(), ShockCord.class);
		cord.setCordLengthAutomatic(true);
		cord.setCordLength(1.2);
		assertFalse(cord.isCordLengthAutomatic(), "Manual cord length should disable automatic");
		assertEquals(1.2, cord.getCordLength(), EPSILON, "Manual cord length should be retained");
	}

	@Test
	public void testShockCordLengthSaveLoadAutomatic() {
		OpenRocketDocument document = createDocumentWithRecoveryComponents();
		ShockCord cord = findComponent(document.getRocket(), ShockCord.class);
		cord.setCordLengthAutomatic(true);

		OpenRocketDocument loadedDocument = saveAndLoad(document);
		Rocket loadedRocket = loadedDocument.getRocket();
		ShockCord loadedCord = findComponent(loadedRocket, ShockCord.class);

		assertTrue(loadedCord.isCordLengthAutomatic(), "Shock cord length automatic should persist");
		assertEquals(loadedRocket.getLength() * 3.0, loadedCord.getCordLength(), EPSILON,
				"Loaded shock cord should keep auto length");
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
		File file = tempDir.resolve("shockcord-auto-length.ork").toFile();
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
