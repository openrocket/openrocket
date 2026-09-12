package info.openrocket.core.file.openrocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.TestRockets;

/**
 * Tests the optional {@code <designinfo>} block: it is written only when the preference
 * is enabled, and it is ignored (no error, no warning) on load.
 */
public class DesignInfoSaveTest {

	private static final File TMP_DIR = new File("./tmp/");

	@BeforeAll
	public static void setup() {
		Module applicationModule = new info.openrocket.core.ServicesForTesting();
		Module pluginModule = new PluginModule();
		// The default test preferences ignore booleans; use one that honours them so the
		// ExportDesignInfoToFile flag can be toggled.
		final Map<String, Boolean> booleanPrefs = new HashMap<>();
		final ApplicationPreferences togglablePrefs = new info.openrocket.core.ServicesForTesting.PreferencesForTesting() {
			@Override
			public boolean getBoolean(String key, boolean defaultValue) {
				return booleanPrefs.getOrDefault(key, defaultValue);
			}

			@Override
			public void putBoolean(String key, boolean value) {
				booleanPrefs.put(key, value);
			}
		};

		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toProvider(new Provider<>() {
					final ComponentPresetDao db = new ComponentPresetDatabase();
					@Override public ComponentPresetDao get() { return db; }
				});
				bind(MotorDatabase.class).toProvider(new Provider<>() {
					final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
					@Override public ThrustCurveMotorSetDatabase get() { return db; }
				});
				bind(Translator.class).toInstance(new DebugTranslator(null));
				bind(ApplicationPreferences.class).toInstance(togglablePrefs);
			}
		};
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(dbOverrides), pluginModule);
		Application.setInjector(injector);
		if (!TMP_DIR.exists()) {
			TMP_DIR.mkdirs();
		}
	}

	@AfterEach
	public void reset() {
		Application.getPreferences().setExportDesignInfoToFile(false);
	}

	private String saveToString(OpenRocketDocument doc) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new OpenRocketSaver().save(baos, doc, new StorageOptions(), new WarningSet(), new ErrorSet());
		return baos.toString(StandardCharsets.UTF_8);
	}

	@Test
	public void notWrittenByDefault() throws Exception {
		OpenRocketDocument doc = TestRockets.makeTestRocket_v104_withMotor();
		String xml = saveToString(doc);
		assertFalse(xml.contains("<designinfo>"), "designinfo must not be written when the preference is off");
	}

	@Test
	public void writtenWhenEnabled() throws Exception {
		Application.getPreferences().setExportDesignInfoToFile(true);
		OpenRocketDocument doc = TestRockets.makeTestRocket_v104_withMotor();
		String xml = saveToString(doc);

		assertTrue(xml.contains("<designinfo>"), xml);
		assertTrue(xml.contains("<statistics scope=\"rocket\">"), xml);
		assertTrue(xml.contains("field=\"Length\""), xml);
		assertTrue(xml.contains("unit=\"m\""), xml);

		// Values are rounded (no long full-precision doubles like 2499.4524984392865)
		assertFalse(xml.matches("(?s).*value=\"-?\\d*\\.\\d{6,}\".*"),
				"design info values should be rounded:\n" + xml);
	}

	@Test
	public void ignoredOnLoadWithoutWarningOrError() throws Exception {
		Application.getPreferences().setExportDesignInfoToFile(true);
		OpenRocketDocument doc = TestRockets.makeTestRocket_v104_withMotor();

		File file = File.createTempFile("designinfo", ".ork", TMP_DIR);
		file.deleteOnExit();
		try (OutputStream out = new FileOutputStream(file)) {
			new OpenRocketSaver().save(out, doc, new StorageOptions(), new WarningSet(), new ErrorSet());
		}

		GeneralRocketLoader loader = new GeneralRocketLoader(file);
		assertDoesNotThrow(() -> { loader.load(); }, "loading a file with <designinfo> must not error");

		String warnings = loader.getWarnings().toString().toLowerCase();
		assertFalse(warnings.contains("designinfo"), "designinfo should be ignored silently: " + warnings);
	}
}
