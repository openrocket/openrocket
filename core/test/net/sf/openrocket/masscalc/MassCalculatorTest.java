package net.sf.openrocket.masscalc;

//import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassCalculatorTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.000001;
	
	private final double BOOSTER_NOSE_MASS_EA = 0.0222459863653;
	private final double BOOSTER_BODY_MASS_EA = 0.129886006;
	private final double BOOSTER_MMT_MASS_EA = 0.01890610458;
	private final double BOOSTER_TOTAL_DRY_MASS_EACH = 0.4555128227852;
	private final double BOOSTER_TOTAL_DRY_CMX = 1.246297525;
	
	private final double G77_MASS_LAUNCH = 0.123;	

	@Test
	public void testRocketNoMotors() {
		Rocket rkt = TestRockets.makeNoMotorRocket();
		rkt.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
//		String treeDump = rkt.toDebugTree();
//		System.err.println( treeDump);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		//mc.debug = true;
		Coordinate rocketCM = mc.getCM( rkt.getSelectedConfiguration(), MassCalcType.NO_MOTORS);
		
		double expMass = 0.668984592;
		double expCMx = 0.558422219894;
		double calcMass = rocketCM.weight;
		Coordinate expCM = new Coordinate(expCMx,0,0, expMass);
		assertEquals(" Simple Motor Rocket mass incorrect: ", expMass, calcMass, EPSILON);
		assertEquals(" Delta Heavy Booster CM.x is incorrect: ", expCM.x, rocketCM.x, EPSILON);
		assertEquals(" Delta Heavy Booster CM.y is incorrect: ", expCM.y, rocketCM.y, EPSILON);
		assertEquals(" Delta Heavy Booster CM.z is incorrect: ", expCM.z, rocketCM.z, EPSILON);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
		
		rocketCM = mc.getCM( rkt.getSelectedConfiguration(), MassCalcType.LAUNCH_MASS);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
		rocketCM = mc.getCM( rkt.getSelectedConfiguration(), MassCalcType.BURNOUT_MASS);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
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
			expMass = BOOSTER_NOSE_MASS_EA;
			// think of the casts as an assert that ( child instanceof NoseCone) == true  
			NoseCone nose = (NoseCone) boosters.getChild(0);
			compMass = nose.getComponentMass();
			assertEquals( nose.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = BOOSTER_BODY_MASS_EA;
			BodyTube body = (BodyTube) boosters.getChild(1);
			compMass = body.getComponentMass();
			assertEquals( body.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = BOOSTER_MMT_MASS_EA;
			InnerTube mmt = (InnerTube)boosters.getChild(1).getChild(0);
			compMass = mmt.getComponentMass();
			assertEquals( mmt.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
	}
	
	@Test
	public void testComponentMOIs() {
		Rocket rkt = TestRockets.makeFalcon9Heavy();
		rkt.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		double expInertia;
		RocketComponent cc;
		double compInertia;
		
		// ====== Payload Stage ====== 
		// ====== ====== ====== ======
		{
			expInertia = 3.1698055283e-5;
			cc= rkt.getChild(0).getChild(0);
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 1.79275e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rkt.getChild(0).getChild(1);
			expInertia = 7.70416e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 8.06940e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rkt.getChild(0).getChild(2);
			expInertia = 1.43691e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 7.30265e-6;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rkt.getChild(0).getChild(3);
			expInertia = 4.22073e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.0001;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			{
				cc= rkt.getChild(0).getChild(3).getChild(0);
				expInertia = 6.23121e-7;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 7.26975e-7;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				
				cc= rkt.getChild(0).getChild(3).getChild(1);
				expInertia = 5.625e-8;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 6.5625e-8;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			}
			
			cc= rkt.getChild(0).getChild(4);
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
			cc= rkt.getChild(1).getChild(0);
			expInertia = 0.000187588;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00702105;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= rkt.getChild(1).getChild(0).getChild(0);
			expInertia = 0.00734753;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.02160236691801411;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
		}
		
		
		// ====== Booster Set Stage ====== 
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) rkt.getChild(1).getChild(1);
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
	public void testBoosterStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getSelectedConfiguration().setOnlyStage( boostNum);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.NO_MOTORS);
				
		double expMass = BOOSTER_TOTAL_DRY_MASS_EACH;
		double expCMx = BOOSTER_TOTAL_DRY_CMX;
		double calcMass = boosterSetCM.weight;
		assertEquals(" Delta Heavy Booster Mass is incorrect: ", expMass, calcMass, EPSILON);
		
		Coordinate expCM = new Coordinate(expCMx,0,0, expMass);
		assertEquals(" Delta Heavy Booster CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
		assertEquals(" Delta Heavy Booster CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
		assertEquals(" Delta Heavy Booster CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, boosterSetCM);  
	}
	

	@Test
	public void testSingleMotorMass() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		InnerTube mmt = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getId();
		Motor activeMotor = mmt.getMotorConfig( fcid ).getMotor();
		String desig = activeMotor.getDesignation();
		
		double expLaunchMass = 0.0164; // kg
		double expSpentMass = 0.0131; // kg
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expLaunchMass, activeMotor.getLaunchMass(), EPSILON);
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expSpentMass, activeMotor.getBurnoutMass(), EPSILON);
		
		// Validate Booster Launch Mass
		MassCalculator mc = new MassCalculator();
		double actPropMass = mc.getPropellantMass( rocket.getSelectedConfiguration(), MassCalcType.LAUNCH_MASS);
					
		double expPropMass = expLaunchMass - expSpentMass;
		assertEquals(" Motor Mass "+desig+" is incorrect: ", expPropMass, actPropMass, EPSILON);			
	}
	
	
	@Test
	public void testBoosterMotorMass() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		FlightConfiguration currentConfig = rocket.getSelectedConfiguration();
		currentConfig.setOnlyStage( boostNum);
		
//		String treeDump = rocket.toDebugTree();
//		System.err.println( treeDump);
		
		{
			InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
			double expX = (.564 + 0.8 - 0.150 );
			double actX = mmt.getLocations()[0].x; 
			assertEquals(" Booster motor mount tubes located incorrectly: ", expX, actX, EPSILON);
		}
		{
			// Validate Booster Launch Mass
			MassCalculator mc = new MassCalculator();
			MassData launchMotorData = mc.getMotorMassData( rocket.getSelectedConfiguration(), MassCalcType.LAUNCH_MASS);
			Coordinate launchCM = launchMotorData.getCM();
			// 1.214 = beginning of engine mmt
			// 1.364-.062 = middle of engine: 1.302
			Coordinate expLaunchCM = new Coordinate(1.31434, 0, 0, 0.123*2*4);
			assertEquals(" Booster Launch Mass is incorrect: ", expLaunchCM.weight, launchCM.weight, EPSILON);
			assertEquals(" Booster Launch CM.x is incorrect: ", expLaunchCM.x, launchCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expLaunchCM.y, launchCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expLaunchCM.z, launchCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expLaunchCM, launchCM);
			
			
			MassData spentMotorData = mc.getMotorMassData( rocket.getSelectedConfiguration(), MassCalcType.BURNOUT_MASS);
			Coordinate spentCM = spentMotorData.getCM();
			Coordinate expSpentCM = new Coordinate(1.31434, 0, 0, 0.064*2*4);
			assertEquals(" Booster Spent Mass is incorrect: ", expSpentCM.weight, spentCM.weight, EPSILON);
			assertEquals(" Booster Launch CM.x is incorrect: ", expSpentCM.x, spentCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expSpentCM.y, spentCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expSpentCM.z, spentCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expSpentCM, spentCM);
		}

	}
	
	
	@Test
	public void testBoosterTotalCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();

		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		rocket.getSelectedConfiguration().setOnlyStage( boostNum);
		
		{
			// Validate Booster Launch Mass
			MassCalculator mc = new MassCalculator();
			Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.LAUNCH_MASS);
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = BOOSTER_TOTAL_DRY_MASS_EACH + 8*G77_MASS_LAUNCH;
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expX = 1.292808951;
			Coordinate expCM = new Coordinate(expX,0,0, expTotalMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		}
		{
			// Validate Booster Burnout Mass
			MassCalculator mc = new MassCalculator();
			//mc.debug = true;
			Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.BURNOUT_MASS);
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = BOOSTER_TOTAL_DRY_MASS_EACH + 8*0.064;
			assertEquals(" Booster Burnout Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expX =  1.282305055;
			Coordinate expCM = new Coordinate(expX,0,0, expTotalMass);
			assertEquals(" Booster Burnout CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Burnout CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Burnout CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Burnout CM is incorrect: ", expCM, boosterSetCM);
		}
	}
	
	@Test
	public void testBoosterStructureMOI() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		FlightConfiguration defaultConfig = rocket.getSelectedConfiguration();
		
		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getSelectedConfiguration().setOnlyStage( boostNum);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		double expMOI_axial = .00304203;
		double boosterMOI_xx= mc.getRotationalInertia( defaultConfig, MassCalcType.NO_MOTORS);
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
		
		double expMOI_tr = 0.129566277;
		double boosterMOI_tr= mc.getLongitudinalInertia( defaultConfig, MassCalcType.NO_MOTORS);
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
	}
	
	@Test
	public void testBoosterTotalMOI() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration defaultConfig = rocket.getSelectedConfiguration();
		rocket.setName("TestRocket:F9H:Total_MOI");

		ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getSelectedConfiguration().setOnlyStage( boostNum);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		final double expMOI_axial = 0.0516919744;
		final double boosterMOI_xx= mc.getRotationalInertia( defaultConfig, MassCalcType.LAUNCH_MASS);
		
		final double expMOI_tr = 0.141508294;
		final double boosterMOI_tr= mc.getLongitudinalInertia( defaultConfig, MassCalcType.LAUNCH_MASS);
				
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}
	

	@Test
	public void testStageMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

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
			Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.NO_MOTORS);
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
			double boosterMOI_xx= mc.getRotationalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 0.142220231;
			double boosterMOI_tr= mc.getLongitudinalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}
	
	@Test
	public void testComponentMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

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
			Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.NO_MOTORS);
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
			double boosterMOI_xx= mc.getRotationalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 1.633216231;
			double boosterMOI_tr= mc.getLongitudinalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}

	@Test
	public void testCMOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

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
			//mc.debug = true;
			Coordinate boosterSetCM = mc.getCM( rocket.getSelectedConfiguration(), MassCalcType.NO_MOTORS);
			
			double expMass = BOOSTER_TOTAL_DRY_MASS_EACH;
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
			double boosterMOI_xx= mc.getRotationalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
			
			double expMOI_tr = 0.1893499746;
			double boosterMOI_tr= mc.getLongitudinalInertia( config, MassCalcType.NO_MOTORS);
			assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
		}
		
	}
}
