package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
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
		Rocket r1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		FlightConfiguration configClone = config.clone();
		
		assertTrue(config.getRocket() == configClone.getRocket());
	}
	

	@Test
	public void testFlightConfigurationRocketLength() {
		Rocket rocket = TestRockets.makeBeta();
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration( config.getId() );

		config.setAllStages();

		// preconditions
		assertThat("active stage count doesn't match", config.getActiveStageCount(), equalTo(2));

		final double expectedLength = 0.33;
		final double calculatedLength = config.getLength();
		assertEquals("source config length doesn't match: ", expectedLength, calculatedLength, EPSILON);

		double expectedReferenceLength = 0.024;
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, config.getReferenceLength(), EPSILON);

		double expectedReferenceArea = Math.pow(expectedReferenceLength/2,2)*Math.PI;
		double actualReferenceArea = config.getReferenceArea();
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, actualReferenceArea, EPSILON);
	}
	

	@Test
	public void testCloneBasic() {
		Rocket rkt1 = TestRockets.makeBeta();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		
		// preconditions
		config1.setAllStages();
		int expectedStageCount = 2;
		int actualStageCount = config1.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		int expectedMotorCount = 2;
		int actualMotorCount = config1.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		double expectedLength = 0.33;
		assertEquals("source config length doesn't match: ", expectedLength, config1.getLength(), EPSILON);
		double expectedReferenceLength = 0.024;
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, config1.getReferenceLength(), EPSILON);
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
		assertEquals("source config length doesn't match: ", expectedLength, config2.getLength(), EPSILON);
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, config2.getReferenceLength(), EPSILON);
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, config2.getReferenceArea(), EPSILON);

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
	public void testDefaultConfigurationIsEmpty() {
		Rocket r1 = TestRockets.makeEstesAlphaIII();
				
		// don't change the configuration: 
		FlightConfiguration defaultConfig = r1.getSelectedConfiguration();
	
		assertThat( "Empty configuration has motors! it should be empty!", r1.getEmptyConfiguration().getActiveMotors().size(), equalTo(0));
		assertThat( "Default configuration is not the empty configuration. It should be!", defaultConfig.getActiveMotors().size(), equalTo(0)); 
	}

    @Test
    public void testCreateConfigurationNullId() {
		/* Setup */
        Rocket rkt = TestRockets.makeEstesAlphaIII();

        // PRE-CONDITION:
        // test that all configurations correctly loaded:
        int expectedConfigCount = 5;
        int actualConfigCount = rkt.getConfigurationCount();
        assertThat("number of loaded configuration counts doesn't actually match.", actualConfigCount, equalTo(expectedConfigCount));

        // create with
        rkt.createFlightConfiguration( (FlightConfigurationId)null);
        expectedConfigCount = 6;
        actualConfigCount = rkt.getConfigurationCount();
        assertThat("createFlightConfiguration with null: doesn't actually work.", actualConfigCount, equalTo(expectedConfigCount));
    }

    @Test
	public void testMotorConfigurations() {
		/* Setup */
		Rocket rkt = TestRockets.makeEstesAlphaIII();
		
		InnerTube smmt = (InnerTube)rkt.getChild(0).getChild(1).getChild(2);
		
		int expectedMotorCount = 5;
		int actualMotorCount = smmt.getMotorCount();
		assertThat("number of motor configurations doesn't match.", actualMotorCount, equalTo(expectedMotorCount));

    }
    
    @Test
    public void testFlightConfigurationGetters(){
		Rocket rkt = TestRockets.makeEstesAlphaIII();

		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertThat("number of loaded configuration counts doesn't actually match.", actualConfigCount, equalTo(expectedConfigCount));

        actualConfigCount = rkt.getIds().size();
        assertThat("number of configuration array ids doesn't actually match.",
                actualConfigCount, equalTo(expectedConfigCount));

        // upon success, these silently complete.
        // upon failure, they'll throw exceptions:
        rkt.getFlightConfigurationByIndex(4);
        rkt.getFlightConfigurationByIndex(5, true);
    }
    

    @Test(expected=java.lang.IndexOutOfBoundsException.class)
    public void testGetFlightConfigurationOutOfBounds(){
    	Rocket rkt = TestRockets.makeEstesAlphaIII();

		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertThat("number of loaded configuration counts doesn't actually match.", actualConfigCount, equalTo(expectedConfigCount));

		// this SHOULD throw an exception -- 
		//      it's out of bounds on, and no configuration exists at index 5.
    	rkt.getFlightConfigurationByIndex(5);  
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
	
	@Test
	public void testIterateComponents() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration selected = rocket.getSelectedConfiguration();

		selected.clearAllStages();
		selected.toggleStage(2);
		
		// vvvv Test Target vvvv
		InstanceMap instances = selected.getActiveInstances();
		// ^^^^ Test Target ^^^^
		
		// Payload Stage
		final AxialStage coreStage = (AxialStage)rocket.getChild(1);
		{ // Core Stage
			final List<InstanceContext> coreStageContextList = instances.getInstanceContexts(coreStage);
			final InstanceContext coreStageContext = coreStageContextList.get(0);
			assertThat(coreStageContext.component.getClass(), equalTo(AxialStage.class));
			assertThat(coreStageContext.component.getID(), equalTo(rocket.getChild(1).getID()));
			assertThat(coreStageContext.component.getInstanceCount(), equalTo(1));
	
			final Coordinate coreLocation = coreStageContext.getLocation(); 
			assertEquals(coreLocation.x, 0.564, EPSILON);
			assertEquals(coreLocation.y, 0.0, EPSILON);
			assertEquals(coreLocation.z, 0.0, EPSILON);

			//... skip uninteresting component
		}

		// Booster Stage
		{ // instance #1
			final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
			final List<InstanceContext> boosterStageContextList = instances.getInstanceContexts(boosterStage);
			final InstanceContext boosterStage0Context = boosterStageContextList.get(0);
			assertThat(boosterStage0Context.component.getClass(), equalTo(ParallelStage.class));
			assertThat(boosterStage0Context.component.getID(), equalTo(boosterStage.getID()));
			assertThat(boosterStage0Context.instanceNumber, equalTo(0));
			{
				final Coordinate loc = boosterStage0Context.getLocation(); 
				assertEquals(loc.x, 0.484, EPSILON);
				assertEquals(loc.y, 0.077, EPSILON);
				assertEquals(loc.z, 0.0, EPSILON);
			}
			
			final InstanceContext boosterStage1Context = boosterStageContextList.get(1);
			assertThat(boosterStage1Context.component.getClass(), equalTo(ParallelStage.class));
			assertThat(boosterStage1Context.component.getID(), equalTo(boosterStage.getID()));
			assertThat(boosterStage1Context.instanceNumber, equalTo(1));
			{
				final Coordinate loc = boosterStage1Context.getLocation(); 
				assertEquals(loc.x, 0.484, EPSILON);
				assertEquals(loc.y, -0.077, EPSILON);
				assertEquals(loc.z, 0.0, EPSILON);
			}
			
			{ // Booster Body:
				final BodyTube boosterBody = (BodyTube)boosterStage.getChild(1);
				final List<InstanceContext> boosterBodyContextList = instances.getInstanceContexts(boosterBody);
				
				// this is the instance number rocket-wide
				final InstanceContext boosterBodyContext = boosterBodyContextList.get(1);
				
				// this is the instance number per-parent
				assertThat(boosterBodyContext.instanceNumber, equalTo(0));
				
				assertThat(boosterBodyContext.component.getClass(), equalTo(BodyTube.class));
				
				final Coordinate bodyTubeLocation = boosterBodyContext.getLocation(); 
				assertEquals(bodyTubeLocation.x, 0.564, EPSILON);
				assertEquals(bodyTubeLocation.y, -0.077, EPSILON);
				assertEquals(bodyTubeLocation.z, 0.0, EPSILON);
								
				{ // Booster::Motor Tubes ( x2 x4)
					final InnerTube boosterMMT = (InnerTube)boosterBody.getChild(0);
					final List<InstanceContext> mmtContextList = instances.getInstanceContexts(boosterMMT);
					assertEquals(8, mmtContextList.size());
					
					final InstanceContext motorTubeContext0 = mmtContextList.get(4);
					assertThat(motorTubeContext0.component.getClass(), equalTo(InnerTube.class));
					assertThat(motorTubeContext0.instanceNumber, equalTo(0));
					final Coordinate motorTube0Location = motorTubeContext0.getLocation(); 
					assertEquals(motorTube0Location.x, 1.214, EPSILON);
					assertEquals(motorTube0Location.y, -0.062, EPSILON);
					assertEquals(motorTube0Location.z, -0.015, EPSILON);

					final InstanceContext motorTubeContext1 = mmtContextList.get(5);
					assertThat(motorTubeContext1.component.getClass(), equalTo(InnerTube.class));
					assertThat(motorTubeContext1.instanceNumber, equalTo(1));
					final Coordinate motorTube1Location = motorTubeContext1.getLocation(); 
					assertEquals(motorTube1Location.x, 1.214, EPSILON);
					assertEquals(motorTube1Location.y, -0.092, EPSILON);
					assertEquals(motorTube1Location.z, -0.015, EPSILON);

					final InstanceContext motorTubeContext2 = mmtContextList.get(6);
					assertThat(motorTubeContext2.component.getClass(), equalTo(InnerTube.class));
					assertThat(motorTubeContext2.instanceNumber, equalTo(2));
					final Coordinate motorTube2Location = motorTubeContext2.getLocation(); 
					assertEquals(motorTube2Location.x, 1.214, EPSILON);
					assertEquals(motorTube2Location.y, -0.092, EPSILON);
					assertEquals(motorTube2Location.z, 0.015, EPSILON);

					final InstanceContext motorTubeContext3 = mmtContextList.get(7);
					assertThat(motorTubeContext3.component.getClass(), equalTo(InnerTube.class));
					assertThat(motorTubeContext3.instanceNumber, equalTo(3));
					final Coordinate motorTube3Location = motorTubeContext3.getLocation(); 
					assertEquals(motorTube3Location.x, 1.214, EPSILON);
					assertEquals(motorTube3Location.y, -0.062, EPSILON);
					assertEquals(motorTube3Location.z, 0.015, EPSILON);

				}{ // Booster::Fins::Instances ( x2 x3)
					final FinSet fins = (FinSet)boosterBody.getChild(1);
					final List<InstanceContext> finContextList = instances.getInstanceContexts(fins);
					assertEquals(6, finContextList.size());
					
					final InstanceContext boosterFinContext0 = finContextList.get(3);
					assertThat(boosterFinContext0.component.getClass(), equalTo(TrapezoidFinSet.class));
					assertThat(boosterFinContext0.instanceNumber, equalTo(0));
					final Coordinate boosterFin0Location = boosterFinContext0.getLocation(); 
					assertEquals(boosterFin0Location.x,  1.044, EPSILON);
					assertEquals(boosterFin0Location.y, -0.104223611, EPSILON);
					assertEquals(boosterFin0Location.z, -0.027223611, EPSILON);
					
					final InstanceContext boosterFinContext1 = finContextList.get(4);
					assertThat(boosterFinContext1.component.getClass(), equalTo(TrapezoidFinSet.class));
					assertThat(boosterFinContext1.instanceNumber, equalTo(1));
					final Coordinate boosterFin1Location = boosterFinContext1.getLocation(); 
					assertEquals(boosterFin1Location.x,  1.044, EPSILON);
					assertEquals(boosterFin1Location.y, -0.03981186, EPSILON);
					assertEquals(boosterFin1Location.z, -0.00996453, EPSILON);
					
					final InstanceContext boosterFinContext2 = finContextList.get(5);
					assertThat(boosterFinContext2.component.getClass(), equalTo(TrapezoidFinSet.class));
					assertThat(boosterFinContext2.instanceNumber, equalTo(2));
					final Coordinate boosterFin2Location = boosterFinContext2.getLocation(); 
					assertEquals(boosterFin2Location.x,  1.044, EPSILON);
					assertEquals(boosterFin2Location.y, -0.08696453, EPSILON);
					assertEquals(boosterFin2Location.z,  0.03718814, EPSILON);
					
				}

			}
			
		}
	}
	
}


