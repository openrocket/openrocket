package net.sf.openrocket.masscalc;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassCalculatorTest extends BaseTestCase {
	
	// tolerance for compared double test results
	private static final double EPSILON = 0.000001;
	
	private static final double G77_MASS_LAUNCH = 0.123;
	private static final double G77_MASS_SPENT = 0.064;


	private static final double M1350_MASS_LAUNCH = 4.808;
	private static final double M1350_MASS_SPENT = 1.970;
	
	
	private static final double BOOSTER_SET_NO_MOTORS_MASS = 0.4555128227852;
	private static final double BOOSTER_SET_NO_MOTORS_CMX = 1.246297525;
	private static final double BOOSTER_SET_SPENT_MASS = BOOSTER_SET_NO_MOTORS_MASS + G77_MASS_SPENT*8;
	
	

	@Test
	public void testRocketNoMotors() {
		Rocket rkt = TestRockets.makeNoMotorRocket();
		FlightConfiguration config = rkt.getEmptyConfiguration();
		config.setAllStages();
		rkt.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		//  any config will do, beceause the rocket literally has no defined motors.
		Coordinate rocketCM = mc.getRocketSpentMassData( config).getCM( );
		
		double expMass = 0.668984592;
		double expCMx = 0.558422219894;
		double calcMass = rocketCM.weight;
		Coordinate expCM = new Coordinate(expCMx,0,0, expMass);
		assertEquals("Simple Motor Rocket mass incorrect: ", expMass, calcMass, EPSILON);
		assertEquals("Simple Rocket CM.x is incorrect: ", expCM.x, rocketCM.x, EPSILON);
		assertEquals("Simple Rocket CM.y is incorrect: ", expCM.y, rocketCM.y, EPSILON);
		assertEquals("Simple Rocket CM.z is incorrect: ", expCM.z, rocketCM.z, EPSILON);
		assertEquals("Simple Rocket CM is incorrect: ", expCM, rocketCM);
		
		rocketCM = mc.getRocketLaunchMassData(config).getCM( );
		assertEquals("Simple Rocket w/no Motors: CM is incorrect: ", expCM, rocketCM);
	}
	
	@Test
	public void testComponentMasses() {
		Rocket rkt = TestRockets.makeFalcon9Heavy();
		rkt.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		double expMass;
		RocketComponent cc;
		double compMass;
		
		// ====== Payload Stage ====== 
		// ====== ====== ====== ======
		{
			expMass = 0.022549558353;
			cc= rkt.getChild(0).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals("P/L NoseCone mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.02904490372;
			cc= rkt.getChild(0).getChild(1);
			compMass = cc.getComponentMass();
			assertEquals("P/L Body mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.007289284477103441;
			cc= rkt.getChild(0).getChild(2);
			compMass = cc.getComponentMass();
			assertEquals("P/L Transition mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.029224351500753608;
			cc= rkt.getChild(0).getChild(3);
			compMass = cc.getComponentMass();
			assertEquals("P/L Upper Stage Body mass calculated incorrectly: ", expMass, compMass, EPSILON);
			{
				expMass = 0.0079759509252;
				cc= rkt.getChild(0).getChild(3).getChild(0);
				compMass = cc.getComponentMass();
					assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
				
				expMass = 0.00072;
				cc= rkt.getChild(0).getChild(3).getChild(1);
				compMass = cc.getComponentMass();
				assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
			}
			
			expMass = 0.01948290100050243;
			cc= rkt.getChild(0).getChild(4);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
		
		// ====== Core Stage ====== 
		// ====== ====== ======
		{
			expMass = 0.1298860066700161;
			cc= rkt.getChild(1).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.21326976;
			cc= rkt.getChild(1).getChild(0).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
		
		
		// ====== Booster Set Stage ====== 
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) rkt.getChild(1).getChild(1);
		{
			expMass = 0.0222459863653;
			// think of the casts as an assert that ( child instanceof NoseCone) == true  
			NoseCone nose = (NoseCone) boosters.getChild(0);
			compMass = nose.getComponentMass();
			assertEquals( nose.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass =  0.129886006;
			BodyTube body = (BodyTube) boosters.getChild(1);
			compMass = body.getComponentMass();
			assertEquals( body.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass =  0.01890610458;
			InnerTube mmt = (InnerTube)boosters.getChild(1).getChild(0);
			compMass = mmt.getComponentMass();
			assertEquals( mmt.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
	}
	
	@Test
	public void testComponentMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration emptyConfig = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration( emptyConfig.getFlightConfigurationID() ); 
		
		
		double expInertia;
		RocketComponent cc;
		double compInertia;
		
		// ====== Payload Stage ====== 
		// ====== ====== ====== ======
		{
			expInertia = 3.1698055283e-5;
			cc= rocket.getChild(0).getChild(0);
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 1.79275e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rocket.getChild(0).getChild(1);
			expInertia = 7.70416e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 8.06940e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rocket.getChild(0).getChild(2);
			expInertia = 1.43691e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 7.30265e-6;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rocket.getChild(0).getChild(3);
			expInertia = 4.22073e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.0001;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			{
				cc= rocket.getChild(0).getChild(3).getChild(0);
				expInertia = 6.23121e-7;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 7.26975e-7;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				
				cc= rocket.getChild(0).getChild(3).getChild(1);
				expInertia = 5.625e-8;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 6.5625e-8;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			}
			
			cc= rocket.getChild(0).getChild(4);
			expInertia = 2.81382e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 3.74486e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
		}
		
		// ====== Core Stage ====== 
		// ====== ====== ======
		{
			cc= rocket.getChild(1).getChild(0);
			expInertia = 0.000187588;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00702105;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rocket.getChild(1).getChild(0).getChild(0);
			expInertia = 0.00734753;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.02160236691801411;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
		}
		
		
		// ====== Booster Set Stage ====== 
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		{
			cc= boosters.getChild(0);
			expInertia = 1.82665797857e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 1.96501191666e-7;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= boosters.getChild(1);
			expInertia = 1.875878651e-4;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00702104762;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= boosters.getChild(1).getChild(0);
			expInertia = 4.11444e-6;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 3.75062e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
		}
	}
	

	@Test
	public void testPropellantMasses() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1) );
		config.setAllStages();
		
		MassCalculator calc = new MassCalculator();
		{ // test core stage motors
			AxialStage core = (AxialStage) rocket.getChild(1);
			final int coreNum = core.getStageNumber(); 
			config.setOnlyStage( coreNum);
			
			MassData corePropInertia = calc.calculatePropellantMassData(config);
			final Coordinate actCM= corePropInertia.getCM();
			final double actCorePropMass = corePropInertia.getMass();		
			final MotorMount mnt = (MotorMount)core.getChild(0);
			final Motor coreMotor = mnt.getMotorConfig( config.getFlightConfigurationID()).getMotor();

			final double expCorePropMassEach = M1350_MASS_LAUNCH - M1350_MASS_SPENT;
			final double coreMotorCount = 1.;
			final double expCorePropMass = expCorePropMassEach * coreMotorCount;
			
			final Coordinate expCM = new Coordinate( 1.053, 0, 0, expCorePropMass);
			
			assertEquals(core.getName()+" => "+coreMotor.getDesignation()+" propellant mass is incorrect: ", expCorePropMass, actCorePropMass, EPSILON);
			assertEquals(core.getName()+" => "+coreMotor.getDesignation()+" propellant CoM x is incorrect: ", expCM.x, actCM.x, EPSILON);
			assertEquals(core.getName()+" => "+coreMotor.getDesignation()+" propellant CoM y is incorrect: ", expCM.y, actCM.y, EPSILON);
			assertEquals(core.getName()+" => "+coreMotor.getDesignation()+" propellant CoM z is incorrect: ", expCM.z, actCM.z, EPSILON);
			
			
		}
		
		{  // test booster stage motors
			ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
			final int boostNum = boosters.getStageNumber();
			config.setOnlyStage( boostNum);

			MassData boosterPropInertia = calc.calculatePropellantMassData(config);
			final Coordinate actCM= boosterPropInertia.getCM();
			final double actBoosterPropMass = boosterPropInertia.getMass();
			final MotorMount mnt = (MotorMount)boosters.getChild(1).getChild(0);
			final Motor boosterMotor = mnt.getMotorConfig( config.getFlightConfigurationID()).getMotor();

			final double expBoosterPropMassEach = G77_MASS_LAUNCH - G77_MASS_SPENT;
			final double boosterSetMotorCount = 8.; /// it's a double merely to prevent type-casting issues
			final double expBoosterPropMass = expBoosterPropMassEach * boosterSetMotorCount;
			
			final Coordinate expCM = new Coordinate( 1.31434, 0, 0, expBoosterPropMass);
			
			assertEquals( boosters.getName()+" => "+boosterMotor.getDesignation()+" propellant mass is incorrect: ", expBoosterPropMass, actBoosterPropMass, EPSILON);
			assertEquals( boosters.getName()+" => "+boosterMotor.getDesignation()+" propellant CoM x is incorrect: ", expCM.x, actCM.x, EPSILON);
			assertEquals( boosters.getName()+" => "+boosterMotor.getDesignation()+" propellant CoM y is incorrect: ", expCM.y, actCM.y, EPSILON);
			assertEquals( boosters.getName()+" => "+boosterMotor.getDesignation()+" propellant CoM z is incorrect: ", expCM.z, actCM.z, EPSILON);
		}
		

	}
	
	@Test
	public void testPropellantMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1) );
				
		{ // test core stage motors
			AxialStage core = (AxialStage) rocket.getChild(1);
			final int coreNum = core.getStageNumber(); 
			config.setOnlyStage( coreNum);
			
			MassCalculator calc = new MassCalculator();	
			MassData corePropInertia = calc.calculatePropellantMassData(config);
			final double actCorePropMass = corePropInertia.getMass();
			final MotorMount mnt = (MotorMount)core.getChild(0);
			final Motor coreMotor = mnt.getMotorConfig( config.getFlightConfigurationID()).getMotor();

			// validated against a specific motor/radius/length
			final double expIxxEach = 0.00199546875;
			final double expIyyEach = 0.092495800375;
			
			final double actIxxEach = coreMotor.getUnitIxx()*actCorePropMass;
			final double actIyyEach = coreMotor.getUnitIyy()*actCorePropMass;
			final double coreMotorCount = mnt.getInstanceCount();
			final double actCorePropIxx = actIxxEach*coreMotorCount;
			final double actCorePropIyy = actIyyEach*coreMotorCount;
			
			assertEquals(core.getName()+" propellant axial MOI is incorrect: ", expIxxEach, actCorePropIxx, EPSILON);
			assertEquals(core.getName()+" propellant longitudinal MOI is incorrect: ", expIyyEach, actCorePropIyy, EPSILON);
		}
		
		{  // test booster stage motors
			ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
			final int boostNum = boosters.getStageNumber();
			config.setOnlyStage( boostNum);
			
			MassCalculator calc = new MassCalculator();
			MassData boosterPropInertia = calc.calculatePropellantMassData(config);
			final double actBoosterPropMass = boosterPropInertia.getMass();
			final MotorMount mnt = (MotorMount)boosters.getChild(1).getChild(0);
			final Motor boosterMotor = mnt.getMotorConfig( config.getFlightConfigurationID()).getMotor();

			final double expBrIxxEach = 3.96952E-4;
			final double expBrIyyEach = 0.005036790;

			final double actIxxEach = boosterMotor.getUnitIxx()*actBoosterPropMass;
			final double actIyyEach = boosterMotor.getUnitIyy()*actBoosterPropMass;
			final int boosterMotorCount = mnt.getInstanceCount();
			assertThat( boosters.getName()+" booster motor count is not as expected! ", boosterMotorCount, equalTo(8));
			final double actBoosterPropIxx = actIxxEach*boosterMotorCount;
			final double actBoosterPropIyy = actIyyEach*boosterMotorCount;

			assertEquals(boosters.getName()+" propellant axial MOI is incorrect: ", expBrIxxEach, actBoosterPropIxx, EPSILON);
			assertEquals(boosters.getName()+" propellant longitudinal MOI is incorrect: ", expBrIyyEach, actBoosterPropIyy, EPSILON);
		}
	
	}
	
	@Test
	public void testBoosterStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		FlightConfiguration config = rocket.getEmptyConfiguration();
		MassCalculator mc = new MassCalculator();
		
		{
			// validate payload stage
			AxialStage payloadStage = (AxialStage) rocket.getChild(0);
			int plNum = payloadStage.getStageNumber();
			config.setOnlyStage( plNum );
			
//			System.err.println( config.toStageListDetail());
//			System.err.println( rocket.toDebugTree());
			
			MassData upperMass = mc.calculateBurnoutMassData( config );
			Coordinate actCM = upperMass.getCM(); 
			
			double expMass = 0.116287;
			double expCMx = 0.278070785749;
			assertEquals("Upper Stage Mass is incorrect: ", expMass, upperMass.getCM().weight, EPSILON);
			
			assertEquals("Upper Stage CM.x is incorrect: ", expCMx, upperMass.getCM().x, EPSILON);
			assertEquals("Upper Stage CM.y is incorrect: ", 0.0f, upperMass.getCM().y, EPSILON);
			assertEquals("Upper Stage CM.z is incorrect: ", 0.0f, upperMass.getCM().z, EPSILON);
		}
		{
			// Validate Boosters
			ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
			int boostNum = boosters.getStageNumber();
			config.setOnlyStage( boostNum );
			
			//System.err.println( config.toStageListDetail());
			//System.err.println( rocket.toDebugTree());
		
			MassData boosterMass = mc.calculateBurnoutMassData( config);
			
			double expMass = BOOSTER_SET_NO_MOTORS_MASS;
			double expCMx = BOOSTER_SET_NO_MOTORS_CMX;
			assertEquals("Heavy Booster Mass is incorrect: ", expMass, boosterMass.getCM().weight, EPSILON);
			
			assertEquals("Heavy Booster CM.x is incorrect: ", expCMx, boosterMass.getCM().x, EPSILON);
			assertEquals("Heavy Booster CM.y is incorrect: ", 0.0f, boosterMass.getCM().y, EPSILON);
			assertEquals("Heavy Booster CM.z is incorrect: ", 0.0f, boosterMass.getCM().z, EPSILON);
		}
	}
		
	@Test
	public void testCMCache() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		FlightConfiguration config = rocket.getEmptyConfiguration();
		MassCalculator mc = new MassCalculator();
		
		{
			// validate payload stage
			AxialStage payloadStage = (AxialStage) rocket.getChild(0);
			int plNum = payloadStage.getStageNumber();
			config.setOnlyStage( plNum );
			
			MassData calcMass = mc.calculateBurnoutMassData( config );
			
			double expMass = 0.116287;
			double expCMx = 0.278070785749;
			assertEquals("Upper Stage Mass is incorrect: ", expMass, calcMass.getCM().weight, EPSILON);
			assertEquals("Upper Stage CM.x is incorrect: ", expCMx, calcMass.getCM().x, EPSILON);
			assertEquals("Upper Stage CM.y is incorrect: ", 0.0f, calcMass.getCM().y, EPSILON);
			assertEquals("Upper Stage CM.z is incorrect: ", 0.0f, calcMass.getCM().z, EPSILON);
		
			MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
			assertEquals("Upper Stage Mass (cache) is incorrect: ", expMass, rocketLaunchMass.getCM().weight, EPSILON);
			assertEquals("Upper Stage CM.x (cache) is incorrect: ", expCMx, rocketLaunchMass.getCM().x, EPSILON);
			
			MassData rocketSpentMass = mc.getRocketSpentMassData( config);
			assertEquals("Upper Stage Mass (cache) is incorrect: ", expMass, rocketSpentMass.getCM().weight, EPSILON);
			assertEquals("Upper Stage CM.x (cache) is incorrect: ", expCMx, rocketSpentMass.getCM().x, EPSILON);
		}{
			ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
			int boostNum = boosters.getStageNumber();
			config.setOnlyStage( boostNum );
			
			mc.voidMassCache();
			MassData boosterMass = mc.calculateBurnoutMassData( config);
			
			double expMass = BOOSTER_SET_NO_MOTORS_MASS;
			double expCMx = BOOSTER_SET_NO_MOTORS_CMX;
			assertEquals("Heavy Booster Mass is incorrect: ", expMass, boosterMass.getCM().weight, EPSILON);
			assertEquals("Heavy Booster CM.x is incorrect: ", expCMx, boosterMass.getCM().x, EPSILON);
			assertEquals("Heavy Booster CM.y is incorrect: ", 0.0f, boosterMass.getCM().y, EPSILON);
			assertEquals("Heavy Booster CM.z is incorrect: ", 0.0f, boosterMass.getCM().z, EPSILON);
			
			MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
			assertEquals(" Booster Stage Mass (cache) is incorrect: ", expMass, rocketLaunchMass.getCM().weight, EPSILON);
			assertEquals(" Booster Stage CM.x (cache) is incorrect: ", expCMx, rocketLaunchMass.getCM().x, EPSILON);
			
			MassData rocketSpentMass = mc.getRocketSpentMassData( config);
			assertEquals(" Booster Stage Mass (cache) is incorrect: ", expMass, rocketSpentMass.getCM().weight, EPSILON);
			assertEquals(" Booster Stage CM.x (cache) is incorrect: ", expCMx, rocketSpentMass.getCM().x, EPSILON);
		}
	}
	

	@Test
	public void testSingleMotorMass() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		InnerTube mmt = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0, false);
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		Motor activeMotor = mmt.getMotorConfig( fcid ).getMotor();
		String desig = activeMotor.getDesignation();
		
		double expLaunchMass = 0.0164; // kg
		double expSpentMass = 0.0131; // kg
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expLaunchMass, activeMotor.getLaunchMass(), EPSILON);
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expSpentMass, activeMotor.getBurnoutMass(), EPSILON);
		
		// Validate Booster Launch Mass
		MassCalculator mc = new MassCalculator();
		MassData propMassData = mc.calculatePropellantMassData( config);
		double actPropMass = propMassData.getCM().weight;
		
		double expPropMass = expLaunchMass - expSpentMass;
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expPropMass, actPropMass, EPSILON);			
	}
	
	@Test
	public void testBoosterPropellantInertia() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1));
		config.setOnlyStage( boostNum);
		
		InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
		{
			double expX = (.564 + 0.8 - 0.150 );
			double actX = mmt.getLocations()[0].x; 
			assertEquals(" Booster motor mount tubes located incorrectly: ", expX, actX, EPSILON);
		}
		{
			// Validate Booster Propellant Mass
			Motor mtr = mmt.getMotorConfig( config.getId()).getMotor();
			double propMassEach = mtr.getLaunchMass()-mtr.getBurnoutMass();
			MassCalculator mc = new MassCalculator();
			MassData propMotorData = mc.calculatePropellantMassData( config );
			Coordinate propCM = propMotorData.getCM();
			Coordinate expPropCM = new Coordinate(1.31434, 0, 0, propMassEach*2*4);
			assertEquals(" Booster Prop Mass is incorrect: ", expPropCM.weight, propCM.weight, EPSILON);
			assertEquals(" Booster Prop CM.x is incorrect: ", expPropCM.x, propCM.x, EPSILON);
			assertEquals(" Booster Prop CM.y is incorrect: ", expPropCM.y, propCM.y, EPSILON);
			assertEquals(" Booster Prop CM.z is incorrect: ", expPropCM.z, propCM.z, EPSILON);
			assertEquals(" Booster Prop CM is incorrect: ", expPropCM, propCM);
		}
	}	

	@Test
	public void testBoosterSpentCM(){
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1));
		config.setOnlyStage( boostNum);
		
		{
			// Validate Booster Launch Mass
			MassCalculator mc = new MassCalculator();
			MassData launchData = mc.calculateBurnoutMassData( config );
			Coordinate launchCM = launchData.getCM();
			double expLaunchCMx = 1.2823050552779347;
			double expLaunchMass = BOOSTER_SET_SPENT_MASS;
			Coordinate expLaunchCM = new Coordinate( expLaunchCMx, 0, 0,  expLaunchMass);
			assertEquals(" Booster Launch Mass is incorrect: ", expLaunchCM.weight, launchCM.weight, EPSILON);
			assertEquals(" Booster Launch CM.x is incorrect: ", expLaunchCM.x, launchCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expLaunchCM.y, launchCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expLaunchCM.z, launchCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expLaunchCM, launchCM);
		}
	}
	
	
	@Test
	public void testBoosterLaunchCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1));
		config.setOnlyStage( boostNum);
		
		{
			// Validate Booster Launch Mass
			MassCalculator mc = new MassCalculator();
			Coordinate boosterSetLaunchCM = mc.getRocketLaunchMassData( rocket.getSelectedConfiguration()).getCM();
			double calcTotalMass = boosterSetLaunchCM.weight;
			
			double expTotalMass = BOOSTER_SET_NO_MOTORS_MASS + 2*4*G77_MASS_LAUNCH;
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expX = 1.292808951;
			Coordinate expCM = new Coordinate(expX,0,0, expTotalMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetLaunchCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetLaunchCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetLaunchCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetLaunchCM);
		}
	}
	
	@Test
	public void testBoosterSpentMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1));
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		config.setOnlyStage( boostNum);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		
		MassData spent = mc.calculateBurnoutMassData( config);
		
		double expMOIRotational = .0062065449;
		double boosterMOIRotational = spent.getRotationalInertia();
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOIRotational, boosterMOIRotational, EPSILON);
		
		double expMOI_tr = 0.13136525;
		double boosterMOI_tr= spent.getLongitudinalInertia();
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
	}
	
	@Test
	public void testBoosterLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket:F9H:Total_MOI");
		FlightConfiguration config = rocket.getFlightConfiguration( new FlightConfigurationId( TestRockets.FALCON_9_FCID_1));
		
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		config.setOnlyStage( boostNum);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();

		
		final MassData launch= mc.getRocketLaunchMassData( config);
		final double expIxx = 0.00912327349;
		final double actIxx= launch.getRotationalInertia();
		final double expIyy = 0.132320;
		final double actIyy= launch.getLongitudinalInertia();
				
		assertEquals(" Booster x-axis MOI is incorrect: ", expIxx, actIxx, EPSILON);
		assertEquals(" Booster transverse MOI is incorrect: ", expIyy, actIyy, EPSILON);
	}
	

	@Test
	public void testStageMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration( config.getId() );
		
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		config.setOnlyStage( boostNum);
		
		double overrideMass = 0.5;
		boosters.setMassOverridden(true);
		boosters.setOverrideMass(overrideMass);
		
		boosters.setCGOverridden(true);
		boosters.setOverrideCGX(6.0);
		{
			// Validate Mass
			MassCalculator mc = new MassCalculator();
			
			MassData burnout = mc.calculateBurnoutMassData( config);
			Coordinate boosterSetCM = burnout.getCM();
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = overrideMass;
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expCMx = 6.0;
			Coordinate expCM = new Coordinate( expCMx, 0, 0, expTotalMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		
			// Validate MOI
			double expMOI_axial = .00333912717;
			double boosterMOI_xx= burnout.getRotationalInertia();
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 0.142220231;
			double boosterMOI_tr= burnout.getLongitudinalInertia();
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}
	
	@Test
	public void testComponentMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration( config.getId() );
		
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		config.setOnlyStage( boostNum);
		
		NoseCone nose = (NoseCone)boosters.getChild(0);
		nose.setMassOverridden(true);
		nose.setOverrideMass( 0.71 );

		BodyTube body = (BodyTube)boosters.getChild(1);
		body.setMassOverridden(true);
		body.setOverrideMass( 0.622 );

		InnerTube mmt = (InnerTube)boosters.getChild(1).getChild(0);
		mmt.setMassOverridden(true);
		mmt.setOverrideMass( 0.213 );
		
		{
			// Validate Mass
			MassCalculator mc = new MassCalculator();
			MassData burnout = mc.calculateBurnoutMassData( config);
			Coordinate boosterSetCM = burnout.getCM();
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = 4.368;
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expCMx = 1.20642422735;
			Coordinate expCM = new Coordinate( expCMx, 0, 0, expTotalMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		
			// Validate MOI
			double expMOI_axial = 0.0257485;
			double boosterMOI_xx= burnout.getRotationalInertia();
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 1.633216231;
			double boosterMOI_tr= burnout.getLongitudinalInertia();
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}

	@Test
	public void testCMOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration( config.getId() );
		
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		config.setOnlyStage( boostNum);
		
		NoseCone nose = (NoseCone)boosters.getChild(0);
		nose.setCGOverridden(true);
		nose.setOverrideCGX(0.22);

		BodyTube body = (BodyTube)boosters.getChild(1);
		body.setCGOverridden(true);
		body.setOverrideCGX( 0.433);

		InnerTube mmt = (InnerTube)boosters.getChild(1).getChild(0);
		mmt.setCGOverridden(true);
		mmt.setOverrideCGX( 0.395 );

		{
			// Validate Mass
			MassCalculator mc = new MassCalculator();
			
			MassData burnout = mc.calculateBurnoutMassData( config);
			Coordinate boosterSetCM = burnout.getCM();
			
			double expMass = BOOSTER_SET_NO_MOTORS_MASS;
			double calcTotalMass = boosterSetCM.weight;
			assertEquals(" Booster Launch Mass is incorrect: ", expMass, calcTotalMass, EPSILON);
			
			double expCMx = 1.38741685552577;
			Coordinate expCM = new Coordinate( expCMx, 0, 0, expMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		
			// Validate MOI
			double expMOI_axial = 0.00304203;
			double boosterMOI_xx= burnout.getRotationalInertia();
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 0.1893499746;
			double boosterMOI_tr= burnout.getLongitudinalInertia();
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}
}
