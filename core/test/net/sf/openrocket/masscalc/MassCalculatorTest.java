package net.sf.openrocket.masscalc;

//import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.rocketcomponent.BoosterSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassCalculatorTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = MathUtil.EPSILON;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);

	
	@Test
	public void testRocketNoMotors() {
		Rocket rkt = TestRockets.makeNoMotorRocket();
		rkt.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
//		String treeDump = rkt.toDebugTree();
//		System.err.println( treeDump);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		//mc.debug = true;
		Coordinate rocketCM = mc.getCM( rkt.getDefaultConfiguration(), MassCalcType.NO_MOTORS);
		
		double expMass = 0.668984592;
		double expCMx = 0.558422219894;
		double calcMass = rocketCM.weight;
		Coordinate expCM = new Coordinate(expCMx,0,0, expMass);
		assertEquals(" Simple Motor Rocket mass incorrect: ", expMass, calcMass, EPSILON);
		assertEquals(" Delta Heavy Booster CM.x is incorrect: ", expCM.x, rocketCM.x, EPSILON);
		assertEquals(" Delta Heavy Booster CM.y is incorrect: ", expCM.y, rocketCM.y, EPSILON);
		assertEquals(" Delta Heavy Booster CM.z is incorrect: ", expCM.z, rocketCM.z, EPSILON);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
		
		rocketCM = mc.getCM( rkt.getDefaultConfiguration(), MassCalcType.LAUNCH_MASS);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
		rocketCM = mc.getCM( rkt.getDefaultConfiguration(), MassCalcType.BURNOUT_MASS);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, rocketCM);
	}
	
	@Test
	public void testTestComponentMasses() {
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
		BoosterSet boosters = (BoosterSet) rkt.getChild(1).getChild(1);
		{
			expMass = 0.01530561538;
			cc= boosters.getChild(0);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.08374229377;
			cc= boosters.getChild(1);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
			
			expMass = 0.018906104589303415;
			cc= boosters.getChild(1).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName()+" mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
	}
	
	@Test
	public void testTestComponentMOIs() {
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
		BoosterSet boosters = (BoosterSet) rkt.getChild(1).getChild(1);
		{
			cc= boosters.getChild(0);
			expInertia = 5.20107e-6;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName()+" Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			
			cc= boosters.getChild(1);
			expInertia = 5.02872e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName()+" Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00449140;
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
	public void testTestBoosterStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		BoosterSet boosters = (BoosterSet) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getDefaultConfiguration().setAllStages(false);
		rocket.getDefaultConfiguration().setOnlyStage( boostNum);
//		String treeDump = rocket.toDebugTree();
//		System.err.println( treeDump);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		Coordinate boosterSetCM = mc.getCM( rocket.getDefaultConfiguration(), MassCalcType.NO_MOTORS);
				
		double expMass = 0.23590802751203407;
		double expCMx = 0.9615865040919498;
		double calcMass = boosterSetCM.weight;
		assertEquals(" Delta Heavy Booster Mass is incorrect: ", expMass, calcMass, EPSILON);
		
		Coordinate expCM = new Coordinate(expCMx,0,0, expMass);
		assertEquals(" Delta Heavy Booster CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
		assertEquals(" Delta Heavy Booster CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
		assertEquals(" Delta Heavy Booster CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
		assertEquals(" Delta Heavy Booster CM is incorrect: ", expCM, boosterSetCM);  
	}
	
//	@Test
//	public void testBoosterMotorCM() {
//		Rocket rocket = TestRockets.makeFalcon9Heavy();
//		FlightConfiguration defaultConfig = rocket.getDefaultConfiguration();
//		FlightConfigurationID fcid = defaultConfig.getFlightConfigurationID();
//		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
//
//		BoosterSet boosters = (BoosterSet) rocket.getChild(1).getChild(1);
//		int boostNum = boosters.getStageNumber();
//		rocket.getDefaultConfiguration().setAllStages(false);
//		rocket.getDefaultConfiguration().setOnlyStage( boostNum);
////		String treeDump = rocket.toDebugTree();
////		System.err.println( treeDump);
//		
//		// Validate Boosters
//		InnerTube inner = (InnerTube)boosters.getChild(1).getChild(0);
//		MassCalculator mc = new MassCalculator();
//		
//		double innerStartX = inner.getLocations()[0].x; 
//		double innerLength = inner.getLength(); 
//		MotorInstance moto= inner.getMotorInstance(fcid);
//		double motorStart = innerStartX + innerLength + inner.getMotorOverhang() - moto.getMotor().getLength();
//		
//		int instanceCount = boosters.getInstanceCount()*inner.getInstanceCount();
//		assertEquals(" Total engine count is incorrect: ", 8, instanceCount);
//		
//		// LAUNCH
//		{
//		
//			Coordinate motorCM = mc.getMotorCG( rocket.getDefaultConfiguration(), MassCalcType.LAUNCH_MASS);
//		
//			double expMotorMass = 0.123*instanceCount;
//			double actMotorMass = motorCM.weight;
//			assertEquals(" Booster motor launch mass is incorrect: ", expMotorMass, actMotorMass, EPSILON);
//		
//			double expMotorCMX = motorStart + moto.getMotor().getLaunchCG().x;
//		 	Coordinate expMotorCM = new Coordinate(expMotorCMX, 0, 0, expMotorMass); 
//			assertEquals(" Booster Motor CM.x is incorrect: ", expMotorCM.x, motorCM.x, EPSILON);
//			assertEquals(" Booster Motor CM.y is incorrect: ", expMotorCM.y, motorCM.y, EPSILON);
//			assertEquals(" Booster Motor CM.z is incorrect: ", expMotorCM.z, motorCM.z, EPSILON);
//			assertEquals(" Booster Motor CM is incorrect: ", expMotorCM, motorCM );
//		}
//		
//		
//		// EMPTY / BURNOUT
//		{
//			Coordinate motorCM = mc.getMotorCG( rocket.getDefaultConfiguration(), MassCalcType.BURNOUT_MASS);
//
//			double expMotorMass = 0.064*instanceCount;
//			double actMotorMass = motorCM.weight;
//			assertEquals(" Booster motor burnout mass is incorrect: ", expMotorMass, actMotorMass, EPSILON);
//			
//			// vvvv DEVEL vvvv	
////			System.err.println("\n ====== ====== ");
////			System.err.println(String.format(" final position: %g = %g innerTube start", motorStart, innerStart ));
////			System.err.println(String.format("                         + %g innerTube Length ", innerLength ));
////			System.err.println(String.format("                         + %g overhang", inner.getMotorOverhang() ));
////			System.err.println(String.format("                         - %g motor length", moto.getMotor().getLength() ));
////			double motorOffs = innerLength + inner.getMotorOverhang() - moto.getMotor().getLength();
////			System.err.println(String.format("                         [ %g motor Offset ]", motorOffs ));
////			System.err.println(String.format("                         [ %g motor CM ]", moto.getMotor().getEmptyCG().x));
//			// ^^^^ DEVEL ^^^^		
//			
//			double expCMX = motorStart + moto.getMotor().getEmptyCG().x; 
//		 	Coordinate expMotorCM = new Coordinate(expCMX, 0, 0, expMotorMass); 
//			assertEquals(" Booster Motor CM.x is incorrect: ", expMotorCM.x, motorCM.x, EPSILON);
//			assertEquals(" Booster Motor CM.y is incorrect: ", expMotorCM.y, motorCM.y, EPSILON);
//			assertEquals(" Booster Motor CM.z is incorrect: ", expMotorCM.z, motorCM.z, EPSILON);
//			assertEquals(" Booster Motor CM is incorrect: ", expMotorCM, motorCM );
//		}
//	}
	
	@Test
	public void testBoosterTotalCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		BoosterSet boosters = (BoosterSet) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		rocket.getDefaultConfiguration().setAllStages(false);
		rocket.getDefaultConfiguration().setOnlyStage( boostNum);
		
		//String treeDump = rocket.toDebugTree();
		//System.err.println( treeDump);
		{
			// Validate Booster Launch Mass
			MassCalculator mc = new MassCalculator();
			Coordinate boosterSetCM = mc.getCM( rocket.getDefaultConfiguration(), MassCalcType.LAUNCH_MASS);
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = 1.219908027512034;
			double expX = 1.2461238889997992;
			Coordinate expCM = new Coordinate(expX,0,0, expTotalMass);
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		}
		{
			// Validate Booster Burnout Mass
			MassCalculator mc = new MassCalculator();
			Coordinate boosterSetCM = mc.getCM( rocket.getDefaultConfiguration(), MassCalcType.BURNOUT_MASS);
			double calcTotalMass = boosterSetCM.weight;
			
			double expTotalMass = 0.7479080275020341;
			assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);
			
			double expX =  1.2030731351529202;
			Coordinate expCM = new Coordinate(expX,0,0, expTotalMass);
			assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
			assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
			assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
			assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);
		}
	}
	
	@Test
	public void testTestBoosterStructureMOI() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
		FlightConfiguration defaultConfig = rocket.getDefaultConfiguration();
		
		BoosterSet boosters = (BoosterSet) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getDefaultConfiguration().setAllStages(false);
		rocket.getDefaultConfiguration().setOnlyStage( boostNum);
//		String treeDump = rocket.toDebugTree();
//		System.err.println( treeDump);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		//mc.debug = true;
		double expMOI_axial = .00144619;
		double boosterMOI_xx= mc.getRotationalInertia( defaultConfig, MassCalcType.NO_MOTORS);
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
		
		double expMOI_tr = 0.01845152840733412;
		double boosterMOI_tr= mc.getLongitudinalInertia( defaultConfig, MassCalcType.NO_MOTORS);
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);	
	}
	
	@Test
	public void testBoosterTotalMOI() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration defaultConfig = rocket.getDefaultConfiguration();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		BoosterSet boosters = (BoosterSet) rocket.getChild(1).getChild(1);
		int boostNum = boosters.getStageNumber();
		
		rocket.getDefaultConfiguration().setAllStages(false);
		rocket.getDefaultConfiguration().setOnlyStage( boostNum);
		//String treeDump = rocket.toDebugTree();
		//System.err.println( treeDump);
		
		// Validate Boosters
		MassCalculator mc = new MassCalculator();
		mc.debug = true;
		double expMOI_axial = 0.00752743;
		double boosterMOI_xx= mc.getRotationalInertia( defaultConfig, MassCalcType.LAUNCH_MASS);
		
		double expMOI_tr = 0.0436639;
		double boosterMOI_tr= mc.getLongitudinalInertia( defaultConfig, MassCalcType.LAUNCH_MASS);
				
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}
	
}
