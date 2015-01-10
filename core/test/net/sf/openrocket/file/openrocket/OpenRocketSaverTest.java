package net.sf.openrocket.file.openrocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.simulation.extension.impl.ScriptingExtension;
import net.sf.openrocket.simulation.extension.impl.ScriptingUtil;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.TestRockets;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

public class OpenRocketSaverTest {
	
	private OpenRocketSaver saver = new OpenRocketSaver();
	private static final String TMP_DIR = "./tmp/";
	
	public static final String SIMULATION_EXTENSION_SCRIPT = "// Test <  &\n// >\n// <![CDATA[";
	
	private static Injector injector;
	
	@BeforeClass
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toProvider(new EmptyComponentDbProvider());
				bind(MotorDatabase.class).toProvider(new MotorDbProvider());
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};
		
		injector = Guice.createInjector(Modules.override(applicationModule).with(dbOverrides), pluginModule);
		Application.setInjector(injector);
		
		File tmpDir = new File("./tmp");
		if (!tmpDir.exists()) {
			boolean success = tmpDir.mkdirs();
			if (!success) {
				fail("Unable to create core/tmp dir needed for tests.");
			}
		}
	}
	
	
	@After
	public void deleteRocketFilesFromTemp() {
		final String fileNameMatchStr = String.format("%s_.*\\.ork", this.getClass().getName());
		
		File directory = new File(TMP_DIR);
		
		File[] toBeDeleted = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File theFile) {
				if (theFile.isFile()) {
					if (theFile.getName().matches(fileNameMatchStr)) {
						return true;
					}
				}
				return false;
			}
		});
		
		for (File deletableFile : toBeDeleted) {
			deletableFile.delete();
		}
	}
	
	/**
	 * Test for creating, saving, and loading various rockets with different file versions
	 * 
	 * TODO: add a deep equality check to ensure no changes after save/read
	 */
	
	@Test
	public void testCreateLoadSave() {
		
		// Create rockets
		ArrayList<OpenRocketDocument> rocketDocs = new ArrayList<OpenRocketDocument>();
		rocketDocs.add(TestRockets.makeTestRocket_v100());
		rocketDocs.add(TestRockets.makeTestRocket_v101_withFinTabs());
		rocketDocs.add(TestRockets.makeTestRocket_v101_withTubeCouplerChild());
		// no version 1.2 file type exists
		// no version 1.3 file type exists
		rocketDocs.add(TestRockets.makeTestRocket_v104_withSimulationData());
		rocketDocs.add(TestRockets.makeTestRocket_v104_withMotor());
		rocketDocs.add(TestRockets.makeTestRocket_v105_withComponentPreset());
		rocketDocs.add(TestRockets.makeTestRocket_v105_withCustomExpression());
		rocketDocs.add(TestRockets.makeTestRocket_v105_withLowerStageRecoveryDevice());
		rocketDocs.add(TestRockets.makeTestRocket_v106_withAppearance());
		rocketDocs.add(TestRockets.makeTestRocket_v106_withMotorMountIgnitionConfig());
		rocketDocs.add(TestRockets.makeTestRocket_v106_withRecoveryDeviceDeploymentConfig());
		rocketDocs.add(TestRockets.makeTestRocket_v106_withStageSeparationConfig());
		rocketDocs.add(TestRockets.makeTestRocket_v107_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT));
		rocketDocs.add(TestRockets.makeTestRocket_for_estimateFileSize());
		
		StorageOptions options = new StorageOptions();
		options.setSimulationTimeSkip(0.05);
		
		// Save rockets, load, validate
		for (OpenRocketDocument rocketDoc : rocketDocs) {
			File file = saveRocket(rocketDoc, options);
			OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
			assertNotNull(rocketDocLoaded);
		}
	}
	
	@Test
	public void testUntrustedScriptDisabledOnLoad() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v107_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT);
		StorageOptions options = new StorageOptions();
		File file = saveRocket(rocketDoc, options);
		OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
		assertEquals(1, rocketDocLoaded.getSimulations().size());
		assertEquals(1, rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().size());
		ScriptingExtension ext = (ScriptingExtension) rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().get(0);
		assertEquals(false, ext.isEnabled());
		assertEquals(SIMULATION_EXTENSION_SCRIPT, ext.getScript());
	}
	
	
	@Test
	public void testTrustedScriptEnabledOnLoad() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v107_withSimulationExtension("TESTING");
		injector.getInstance(ScriptingUtil.class).setTrustedScript("JavaScript", "TESTING", true);
		StorageOptions options = new StorageOptions();
		File file = saveRocket(rocketDoc, options);
		OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
		assertEquals(1, rocketDocLoaded.getSimulations().size());
		assertEquals(1, rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().size());
		ScriptingExtension ext = (ScriptingExtension) rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().get(0);
		assertEquals(true, ext.isEnabled());
		assertEquals("TESTING", ext.getScript());
	}
	
	
	/*
	 * Test how accurate estimatedFileSize is.
	 * 
	 * Actual file is 5822 Bytes
	 * Estimated file is 440 Bytes (yeah....)
	 */
	@Test
	public void testEstimateFileSize() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v104_withSimulationData();
		
		StorageOptions options = new StorageOptions();
		options.setSimulationTimeSkip(0.05);
		
		long estimatedSize = saver.estimateFileSize(rocketDoc, options);
		
		// TODO: fix estimateFileSize so that it's a lot more accurate
	}
	
	
	////////////////////////////////
	// Tests for File Version 1.0 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion100() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v100();
		assertEquals(100, getCalculatedFileVersion(rocketDoc));
	}
	
	////////////////////////////////
	// Tests for File Version 1.1 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion101_withFinTabs() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v101_withFinTabs();
		assertEquals(101, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion101_withTubeCouplerChild() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v101_withTubeCouplerChild();
		assertEquals(101, getCalculatedFileVersion(rocketDoc));
	}
	
	////////////////////////////////
	// Tests for File Version 1.2 // 
	////////////////////////////////
	
	// no version 1.2 file type exists
	
	////////////////////////////////
	// Tests for File Version 1.3 // 
	////////////////////////////////
	
	// no version 1.3 file type exists
	
	////////////////////////////////
	// Tests for File Version 1.4 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion104_withSimulationData() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v104_withSimulationData();
		assertEquals(104, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion104_withMotor() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v104_withMotor();
		assertEquals(104, getCalculatedFileVersion(rocketDoc));
	}
	
	////////////////////////////////
	// Tests for File Version 1.5 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion105_withComponentPresets() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v105_withComponentPreset();
		assertEquals(105, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion105_withCustomExpressions() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v105_withCustomExpression();
		assertEquals(105, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion105_withLowerStageRecoveryDevice() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v105_withLowerStageRecoveryDevice();
		assertEquals(105, getCalculatedFileVersion(rocketDoc));
	}
	
	////////////////////////////////
	// Tests for File Version 1.6 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion106_withAppearance() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v106_withAppearance();
		assertEquals(106, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion106_withMotorMountIgnitionConfig() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v106_withMotorMountIgnitionConfig();
		assertEquals(106, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion106_withRecoveryDeviceDeploymentConfig() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v106_withRecoveryDeviceDeploymentConfig();
		assertEquals(106, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion106_withStageDeploymentConfig() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v106_withStageSeparationConfig();
		assertEquals(106, getCalculatedFileVersion(rocketDoc));
	}
	
	////////////////////////////////
	// Tests for File Version 1.7 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion107_withSimulationExtension() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v107_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT);
		assertEquals(107, getCalculatedFileVersion(rocketDoc));
	}
	
	
	/*
	 * Utility Functions
	 */
	
	private int getCalculatedFileVersion(OpenRocketDocument rocketDoc) {
		int fileVersion = this.saver.testAccessor_calculateNecessaryFileVersion(rocketDoc, null);
		return fileVersion;
	}
	
	private OpenRocketDocument loadRocket(String fileName) {
		GeneralRocketLoader loader = new GeneralRocketLoader(new File(fileName));
		OpenRocketDocument rocketDoc = null;
		try {
			rocketDoc = loader.load();
		} catch (RocketLoadException e) {
			e.printStackTrace();
			fail("RocketLoadException while loading file " + fileName + " : " + e.getMessage());
		}
		return rocketDoc;
	}
	
	private File saveRocket(OpenRocketDocument rocketDoc, StorageOptions options) {
		String fileName = String.format(TMP_DIR + "%s_%s.ork", this.getClass().getName(), rocketDoc.getRocket().getName());
		File file = new File(fileName);
		
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			this.saver.save(out, rocketDoc, options);
		} catch (FileNotFoundException e) {
			fail("FileNotFound saving file " + fileName + ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException saving file " + fileName + ": " + e.getMessage());
		}
		
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			fail("Unable to close output stream for file " + fileName + ": " + e.getMessage());
		}
		
		return file;
	}
	
	
	private static ThrustCurveMotor readMotor() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		InputStream is = OpenRocketSaverTest.class.getResourceAsStream("/net/sf/openrocket/Estes_A8.rse");
		assertNotNull("Problem in unit test, cannot find Estes_A8.rse", is);
		try {
			for (Motor m : loader.load(is, "Estes_A8.rse")) {
				return (ThrustCurveMotor) m;
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException: " + e);
		}
		throw new RuntimeException("Could not load motor");
	}
	
	private static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {
		
		final ComponentPresetDao db = new ComponentPresetDatabase();
		
		@Override
		public ComponentPresetDao get() {
			return db;
		}
	}
	
	private static class MotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {
		
		final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
		
		public MotorDbProvider() {
			db.addMotor(readMotor());
			
			assertEquals(1, db.getMotorSets().size());
		}
		
		@Override
		public ThrustCurveMotorSetDatabase get() {
			return db;
		}
	}
	
	
}
