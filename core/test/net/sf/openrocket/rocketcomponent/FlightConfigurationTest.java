package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FlightConfigurationTest extends BaseTestCase {
	
	private final static double EPSILON = MathUtil.EPSILON*1E3; 
	
	/**
	 * Empty rocket (no components) specific configuration tests
	 */
	@Test
	public void testEmptyRocket() {
		Rocket r1 = TestRockets.makeSmallFlyable();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		FlightConfiguration configClone = config.clone();
		
		assertTrue(config.getRocket() == configClone.getRocket());
	}
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testCloneBasic() {
		Rocket rkt1 = TestRockets.makeBeta();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		
//		final String treedump = rkt1.toDebugTree();
//		System.err.println("treedump: \n" + treedump);
		
		// preconditions
		config1.setAllStages();
		int expectedStageCount = 2;
		int actualStageCount = config1.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		int expectedMotorCount = 2;
		int actualMotorCount = config1.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		double expectedLength = 0.33;
		double actualLength = config1.getLength();
		assertEquals("source config length doesn't match: ", expectedLength, actualLength, EPSILON);
		double expectedReferenceLength = 0.024;
		double actualReferenceLength = config1.getReferenceLength();
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, actualReferenceLength, EPSILON);
		double expectedReferenceArea = Math.pow(expectedReferenceLength/2,2)*Math.PI;
		double actualReferenceArea = config1.getReferenceArea();
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, actualReferenceArea, EPSILON);

		// vvvv test target vvvv 
		FlightConfiguration config2= config1.clone();
		// ^^^^ test target ^^^^
		
		// postconditions
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		actualLength = config2.getLength();
		assertEquals("source config length doesn't match: ", expectedLength, actualLength, EPSILON);
		actualReferenceLength = config2.getReferenceLength();
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, actualReferenceLength, EPSILON);
		actualReferenceArea = config2.getReferenceArea();
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, actualReferenceArea, EPSILON);

	}
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testCloneIndependence() {
		Rocket rkt1 = TestRockets.makeBeta();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		int expectedStageCount;
		int actualStageCount;
		int expectedMotorCount;
		int actualMotorCount;
		
		// test that cloned configurations operate independently:
		// change #1, test clone #2 -- verify that cloned configurations change independent.
		config1.setAllStages();
		// vvvv test target vvvv 
		FlightConfiguration config2 = config1.clone();
		// ^^^^ test target ^^^^
		config1.clearAllStages();

		// postcondition: config #1
		expectedStageCount = 0;
		actualStageCount = config1.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 0;
		actualMotorCount = config1.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		
		// postcondition: config #2
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
	}
	
	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testSingleStageRocket() {
		Rocket r1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		// test explicitly setting only first stage active
		config.clearAllStages();
		config.setOnlyStage(0);
		
		// test that getStageCount() returns correct value
		int expectedStageCount = 1;
		int stageCount = config.getStageCount();
		assertTrue("stage count doesn't match", stageCount == expectedStageCount);
		
		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertThat("active stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		// test explicitly setting all stages up to first stage active
		config.setOnlyStage(0);
		
		// test explicitly setting all stages active
		config.setAllStages();
		
	}
	
	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testConfigurationSwitching() {
		/* Setup */
		Rocket rkt = TestRockets.makeEstesAlphaIII();
		
		InnerTube smmt = (InnerTube)rkt.getChild(0).getChild(1).getChild(2);
		
		int expectedMotorCount = 5;
		int actualMotorCount = smmt.getMotorCount();
		assertThat("number of motor configurations doesn't match.", actualMotorCount, equalTo(expectedMotorCount));
		
		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertThat("number of loaded configuration counts doesn't actually match.", actualConfigCount, equalTo(expectedConfigCount));
		
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMultiStageRocket() {
		
		/* Setup */
		Rocket rkt = TestRockets.makeBeta();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		
		int expectedStageCount;
		int stageCount;
		
		expectedStageCount = 2;
		stageCount = config.getStageCount();
		assertThat("stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		config.clearAllStages();
		assertThat(" clear all stages: check #0: ", config.isStageActive(0), equalTo(false));
		assertThat(" clear all stages: check #1: ", config.isStageActive(1), equalTo(false));
		
		// test explicitly setting only first stage active
		config.setOnlyStage(0);
		
		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertThat("active stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		assertThat(" setting single stage active: ", config.isStageActive(0), equalTo(true));
		
		// test explicitly setting all stages up to second stage active
		config.setOnlyStage(1);
		assertThat("Setting single stage active: ", config.isStageActive(0), equalTo(false));
		assertThat("Setting single stage active: ", config.isStageActive(1), equalTo(true));
		
		config.clearStage(0);
		assertThat(" deactivate stage #0: ", config.isStageActive(0), equalTo(false));
		assertThat("     active stage #1: ", config.isStageActive(1), equalTo(true));
		
		// test explicitly setting all two stages active
		config.setAllStages();
		assertThat(" activate all stages: check stage #0: ", config.isStageActive(0), equalTo(true));
		assertThat(" activate all stages: check stage #1: ", config.isStageActive(1), equalTo(true));
		
		// test toggling single stage
		config.setAllStages();
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(false));
		
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(true));
		
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(false));
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMotorClusters() {
		
		/* Setup */
		Rocket rkt = TestRockets.makeBeta();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		
		
		config.clearAllStages();
		int expectedMotorCount = 0;
		int actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		
		config.setOnlyStage(0);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setOnlyStage(1);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setAllStages();
		expectedMotorCount = 2;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));
	}
	

}
