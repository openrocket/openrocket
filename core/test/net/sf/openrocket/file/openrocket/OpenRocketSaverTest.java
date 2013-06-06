package net.sf.openrocket.file.openrocket;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.util.TestRockets;

import org.junit.Test;

public class OpenRocketSaverTest {
	
	////////////////////////////////
	// Tests for File Version 1.0 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion100() {
		Rocket rocket = TestRockets.makeTestRocket_v100();
		assertEquals(100, getCalculatedFileVersion(rocket));
	}
	
	////////////////////////////////
	// Tests for File Version 1.1 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion101_withFinTabs() {
		Rocket rocket = TestRockets.makeTestRocket_v101_withFinTabs();
		assertEquals(101, getCalculatedFileVersion(rocket));
	}
	
	@Test
	public void testFileVersion101_withTubeCouplerChild() {
		Rocket rocket = TestRockets.makeTestRocket_v101_withTubeCouplerChild();
		assertEquals(101, getCalculatedFileVersion(rocket));
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
		Rocket rocket = TestRockets.makeTestRocket_v100();
		rocket.setName("v104_withSimulationData");
		
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		Simulation simulation = new Simulation(rocket);
		rocketDoc.addSimulation(simulation);
		
		SimulationListener simulationListener = new AbstractSimulationListener();
		try {
			simulation.simulate(simulationListener);
		} catch (SimulationException e) {
			// do nothing, we don't care
		}
		
		assertEquals(104, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion104_withMotor() {
		Rocket rocket = TestRockets.makeTestRocket_v104_withMotor();
		assertEquals(104, getCalculatedFileVersion(rocket));
	}
	
	////////////////////////////////
	// Tests for File Version 1.5 // 
	////////////////////////////////
	
	@Test
	public void testFileVersion105_withComponentPresets() {
		Rocket rocket = TestRockets.makeTestRocket_v105_withComponentPreset();
		assertEquals(105, getCalculatedFileVersion(rocket));
	}
	
	@Test
	public void testFileVersion105_withCustomExpressions() {
		Rocket rocket = TestRockets.makeTestRocket_v100();
		rocket.setName("v105_withCustomExpressions");
		
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		CustomExpression expression = new CustomExpression(rocketDoc, "name", "symbol", "unit", "expression");
		rocketDoc.addCustomExpression(expression);
		
		assertEquals(105, getCalculatedFileVersion(rocketDoc));
	}
	
	@Test
	public void testFileVersion105_withLowerStageRecoveryDevice() {
		Rocket rocket = TestRockets.makeTestRocket_v105_withLowerStageRecoveryDevice();
		assertEquals(105, getCalculatedFileVersion(rocket));
	}
	
	////////////////////////////////
	// Tests for File Version 1.6 // 
	///////////////////////////////////////////////
	
	@Test
	public void testFileVersion106_withAppearance() {
		Rocket rocket = TestRockets.makeTestRocket_v106_withAppearance();
		assertEquals(106, getCalculatedFileVersion(rocket));
	}
	
	@Test
	public void testFileVersion106_withMotorMountIgnitionConfig() {
		Rocket rocket = TestRockets.makeTestRocket_v106_withMotorMountIgnitionConfig();
		assertEquals(106, getCalculatedFileVersion(rocket));
	}
	
	@Test
	public void testFileVersion106_withRecoveryDeviceDeploymentConfig() {
		Rocket rocket = TestRockets.makeTestRocket_v106_withRecoveryDeviceDeploymentConfig();
		assertEquals(106, getCalculatedFileVersion(rocket));
	}
	
	@Test
	public void testFileVersion106_withStageDeploymentConfig() {
		Rocket rocket = TestRockets.makeTestRocket_v106_withStageSeparationConfig();
		assertEquals(106, getCalculatedFileVersion(rocket));
	}
	
	/*
	 * Utility Functions
	 */
	
	static int getCalculatedFileVersion(Rocket rocket) {
		OpenRocketSaver saver = new OpenRocketSaver();
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		int fileVersion = saver.testAccessor_calculateNecessaryFileVersion(rocketDoc, null);
		return fileVersion;
	}
	
	static int getCalculatedFileVersion(OpenRocketDocument rocketDoc) {
		OpenRocketSaver saver = new OpenRocketSaver();
		int fileVersion = saver.testAccessor_calculateNecessaryFileVersion(rocketDoc, null);
		return fileVersion;
	}
	
}
