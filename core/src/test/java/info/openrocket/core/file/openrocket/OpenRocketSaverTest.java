package info.openrocket.core.file.openrocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.motor.GeneralMotorLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.extension.impl.ScriptingExtension;
import info.openrocket.core.simulation.extension.impl.ScriptingUtil;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.TestRockets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

public class OpenRocketSaverTest {
	
	private final OpenRocketSaver saver = new OpenRocketSaver();
	private static final File TMP_DIR = new File("./tmp/");
	
	public static final String SIMULATION_EXTENSION_SCRIPT = "// Test <  &\n// >\n// <![CDATA[";
	
	private static Injector injector;

	@BeforeAll
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

		if (!(TMP_DIR.exists() && TMP_DIR.isDirectory())) {
			boolean success = TMP_DIR.mkdirs();
			if (!success) {
				fail("Unable to create core/tmp dir needed for tests.");
			}
		}
	}

	@AfterEach
	public void deleteRocketFilesFromTemp() {
		final String fileNameMatchStr = String.format("%s_.*\\.ork", this.getClass().getName());
		
		File[] toBeDeleted = TMP_DIR.listFiles(new FileFilter() {
			@Override
			public boolean accept(File theFile) {
				if (theFile.isFile()) {
					return theFile.getName().matches(fileNameMatchStr);
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
		rocketDocs.add(TestRockets.makeTestRocket_v110_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT));
        rocketDocs.add(TestRockets.makeTestRocket_v108_withBoosters());
		rocketDocs.add(TestRockets.makeTestRocket_v108_withDisabledStage());
		rocketDocs.add(TestRockets.makeTestRocket_for_estimateFileSize());
		
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(true);
		
		// Save rockets, load, validate
		for (OpenRocketDocument rocketDoc : rocketDocs) {
			File file = saveRocket(rocketDoc, options);
			OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
			assertNotNull(rocketDocLoaded);
		}
	}

	@Test
	public void testSaveStageActiveness() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v108_withDisabledStage();
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(true);

		// Save rockets, load, validate
		File file = saveRocket(rocketDoc, options);
		OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());

		// Check that the stages activeness is saved
		FlightConfiguration config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		assertFalse(config.isStageActive(0), " selected config, stage 0 should have been disabled after saving");
		assertTrue(config.isStageActive(1), " selected config, stage 1 should have been enabled after saving");
		assertTrue(config.isStageActive(2), " selected config, stage 2 should have been enabled after saving");

		// Disable second stage
		config._setStageActive(1, false, false);
		file = saveRocket(rocketDocLoaded, options);
		rocketDocLoaded = loadRocket(file.getPath());
		config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		assertFalse(config.isStageActive(0), " selected config, stage 0 should have been disabled after saving");
		assertFalse(config.isStageActive(1), " selected config, stage 1 should have been disabled after saving");
		assertTrue(config.isStageActive(2), " selected config, stage 2 should have been enabled after saving");

		// Re-enable first stage
		config._setStageActive(0, true, false);
		file = saveRocket(rocketDocLoaded, options);
		rocketDocLoaded = loadRocket(file.getPath());
		config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		assertTrue(config.isStageActive(0), " selected config, stage 0 should have been enabled after saving");
		assertFalse(config.isStageActive(1), " selected config, stage 1 should have been disabled after saving");
		assertTrue(config.isStageActive(2), " selected config, stage 2 should have been enabled after saving");

		// Check that other configurations are not affected
		FlightConfiguration extraConfig = rocketDocLoaded.getRocket()
				.createFlightConfiguration(TestRockets.TEST_FCID_0);
		extraConfig.setAllStages();
		file = saveRocket(rocketDocLoaded, options);
		rocketDocLoaded = loadRocket(file.getPath());
		config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		extraConfig = rocketDocLoaded.getRocket().getFlightConfiguration(TestRockets.TEST_FCID_0);
		assertTrue(config.isStageActive(0), " selected config, stage 0 should have been enabled after saving");
		assertFalse(config.isStageActive(1), " selected config, stage 1 should have been disabled after saving");
		assertTrue(config.isStageActive(2), " selected config, stage 2 should have been enabled after saving");
		assertTrue(extraConfig.isStageActive(0), " extra config, stage 0 should have been enabled after saving");
		assertTrue(extraConfig.isStageActive(1), " extra config, stage 1 should have been enabled after saving");
		assertTrue(extraConfig.isStageActive(2), " extra config, stage 2 should have been enabled after saving");

		// Disable a stage in the extra config, and an extra one in the selected config
		extraConfig._setStageActive(0, false, false);
		config._setStageActive(2, false, false);
		file = saveRocket(rocketDocLoaded, options);
		rocketDocLoaded = loadRocket(file.getPath());
		config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		extraConfig = rocketDocLoaded.getRocket().getFlightConfiguration(TestRockets.TEST_FCID_0);
		assertTrue(config.isStageActive(0), " selected config, stage 0 should have been enabled after saving");
		assertFalse(config.isStageActive(1), " selected config, stage 1 should have been disabled after saving");
		assertFalse(config.isStageActive(2), " selected config, stage 2 should have been disabled after saving");
		assertFalse(extraConfig.isStageActive(0), " extra config, stage 0 should have been disabled after saving");
		assertTrue(extraConfig.isStageActive(1), " extra config, stage 1 should have been enabled after saving");
		assertTrue(extraConfig.isStageActive(2), " extra config, stage 2 should have been enabled after saving");

		// Test an empty rocket with no configurations
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		file = saveRocket(document, options);
		rocketDocLoaded = loadRocket(file.getPath());
		rocketDocLoaded.getRocket().getStage(0).addChild(new BodyTube()); // Add a child, otherwise the stage is always
																			// marked inactive
		config = rocketDocLoaded.getRocket().getSelectedConfiguration();
		assertTrue(config.isStageActive(0), " empty rocket, selected config, stage 0 should have been enabled after saving");
	}

	@Test
	public void testUntrustedScriptDisabledOnLoad() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v110_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT);
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
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v110_withSimulationExtension("TESTING");
		injector.getInstance(ScriptingUtil.class).setTrustedScript("JavaScript", "TESTING", true);
		StorageOptions options = new StorageOptions();
		File file = saveRocket(rocketDoc, options);
		OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
		assertEquals(1, rocketDocLoaded.getSimulations().size());
		assertEquals(1, rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().size());
		ScriptingExtension ext = (ScriptingExtension) rocketDocLoaded.getSimulations().get(0).getSimulationExtensions().get(0);
		assertEquals(true, ext.isEnabled());
		assertEquals(ext.getScript(), "TESTING");
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
		options.setSaveSimulationData(true);
		
		long estimatedSize = saver.estimateFileSize(rocketDoc, options);
		
		// TODO: fix estimateFileSize so that it's a lot more accurate
	}

	/**
	 * Test sim status with/without sim data in file.
	 */
	@Test
	public void TestSimStatus() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		
		// Hook up some simulations.
		// First sim will not have options set
		Simulation sim1 = new Simulation(rocket);
		rocketDoc.addSimulation(sim1);

		// Second sim has options, but hasn't been simulated
		Simulation sim2 = new Simulation(rocket);
		sim2.getOptions().setISAAtmosphere(true);
		sim2.getOptions().setTimeStep(0.05);
		sim2.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		rocketDoc.addSimulation(sim2);

		// Third sim has been executed
		Simulation sim3 = new Simulation(rocket);
		sim3.getOptions().setISAAtmosphere(true);
		sim3.getOptions().setTimeStep(0.05);
		sim3.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		try {
			sim3.simulate();
		} catch (Exception e) {
			fail(e.toString());
		}
		rocketDoc.addSimulation(sim3);

		// Fourth sim has been executed, then configuration changed
		Simulation sim4 = new Simulation(rocket);
		sim4.getOptions().setISAAtmosphere(true);
		sim4.getOptions().setTimeStep(0.05);
		sim4.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		try {
			sim4.simulate();
		} catch (Exception e) {
			fail(e.toString());
		}
		sim4.getOptions().setTimeStep(0.1);
		rocketDoc.addSimulation(sim4);

		// save, then load document
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(true);

		File file = saveRocket(rocketDoc, options);
		OpenRocketDocument rocketDocLoaded = loadRocket(file.getPath());
		
		assertEquals(Simulation.Status.CANT_RUN, rocketDocLoaded.getSimulations().get(0).getStatus());
		assertEquals(Simulation.Status.NOT_SIMULATED, rocketDocLoaded.getSimulations().get(1).getStatus());
		assertEquals(Simulation.Status.LOADED, rocketDocLoaded.getSimulations().get(2).getStatus());
		assertEquals(Simulation.Status.OUTDATED, rocketDocLoaded.getSimulations().get(3).getStatus());
	}
	
	////////////////////////////////
	// Tests for File Version 1.10 //
	////////////////////////////////
	
	@Test
	public void testFileVersion110_withSimulationExtension() {
		OpenRocketDocument rocketDoc = TestRockets.makeTestRocket_v110_withSimulationExtension(SIMULATION_EXTENSION_SCRIPT);
		assertEquals(110, getCalculatedFileVersion(rocketDoc));
	}
	

	////////////////////////////////
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
		File file = null;
		OutputStream out = null;
		try {
			file = File.createTempFile(TMP_DIR.getName(), ".ork");
			out = new FileOutputStream(file);
			this.saver.save(out, rocketDoc, options, new WarningSet(), new ErrorSet());
		} catch (FileNotFoundException e) {
			fail("FileNotFound saving temp file in: " + TMP_DIR.getName() + ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException saving temp file in: " + TMP_DIR.getName() + ": " + e.getMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				fail("Unable to close output stream for temp file in " + TMP_DIR.getName() + ": " + e.getMessage());
			}
		}
		
		return file;
	}
	
	
	private static ThrustCurveMotor readMotor() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		InputStream is = OpenRocketSaverTest.class.getResourceAsStream("/Estes_A8.rse");
		assertNotNull(is, "Problem in unit test, cannot find Estes_A8.rse");
		try {
			for (ThrustCurveMotor.Builder m : loader.load(is, "Estes_A8.rse")) {
				return m.build();
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException: " + e);
		}
		throw new RuntimeException("Could not load motor");
	}
	
	public static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {
		
		final ComponentPresetDao db = new ComponentPresetDatabase();
		
		@Override
		public ComponentPresetDao get() {
			return db;
		}
	}

	public static class MotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {
		
		final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
		
		public MotorDbProvider() {
			db.addMotor(readMotor());
			db.addMotor(new ThrustCurveMotor.Builder()
					.setManufacturer(Manufacturer.getManufacturer("A"))
					.setDesignation("F12X")
					.setDescription("Desc")
					.setMotorType(Motor.Type.UNKNOWN)
					.setStandardDelays(new double[] {})
					.setDiameter(0.024)
					.setLength(0.07)
					.setTimePoints(new double[] { 0, 1, 2 })
					.setThrustPoints(new double[] { 0, 1, 0 })
					.setCGPoints(new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL })
					.setDigest("digestA")
					.build());

			assertEquals(2, db.getMotorSets().size());
		}
		
		@Override
		public ThrustCurveMotorSetDatabase get() {
			return db;
		}
	}
	
	
}
