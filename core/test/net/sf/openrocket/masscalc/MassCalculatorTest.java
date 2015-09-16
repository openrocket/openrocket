package net.sf.openrocket.masscalc;

//import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.BoosterSet;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class MassCalculatorTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.000001;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);
	
	public void test() {
		//		fail("Not yet implemented");
	}
	
	public Rocket createTestRocket() {
		double tubeRadius = 0.1;
		// setup
		Rocket rocket = new Rocket();
		rocket.setName("Rocket");
		
		AxialStage sustainer = new AxialStage();
		sustainer.setName("Sustainer stage");
		RocketComponent sustainerNose = new NoseCone(Transition.Shape.CONICAL, 0.2, tubeRadius);
		sustainerNose.setName("Sustainer Nosecone");
		sustainer.addChild(sustainerNose);
		RocketComponent sustainerBody = new BodyTube(0.3, tubeRadius, 0.001);
		sustainerBody.setName("Sustainer Body ");
		sustainer.addChild(sustainerBody);
		rocket.addChild(sustainer);
		
		AxialStage core = new AxialStage();
		core.setName("Core stage");
		rocket.addChild(core);
		BodyTube coreBody = new BodyTube(0.6, tubeRadius, 0.001);
		coreBody.setName("Core Body ");
		core.addChild(coreBody);
		FinSet coreFins = new TrapezoidFinSet(4, 0.4, 0.2, 0.2, 0.4);
		coreFins.setName("Core Fins");
		coreBody.addChild(coreFins);
		
		InnerTube motorCluster = new InnerTube();
		motorCluster.setName("Core Motor Cluster");
		motorCluster.setMotorMount(true);
		motorCluster.setClusterConfiguration(ClusterConfiguration.CONFIGURATIONS[5]);
		coreBody.addChild(motorCluster);
		
		return rocket;
	}
	
	public BoosterSet createBooster() {
		double tubeRadius = 0.08;
		
		BoosterSet booster = new BoosterSet();
		booster.setName("Booster Stage");
		RocketComponent boosterNose = new NoseCone(Transition.Shape.CONICAL, 0.2, tubeRadius);
		boosterNose.setName("Booster Nosecone");
		booster.addChild(boosterNose);
		RocketComponent boosterBody = new BodyTube(0.2, tubeRadius, 0.001);
		boosterBody.setName("Booster Body ");
		booster.addChild(boosterBody);
		Transition boosterTail = new Transition();
		boosterTail.setName("Booster Tail");
		boosterTail.setForeRadius(tubeRadius);
		boosterTail.setAftRadius(0.05);
		boosterTail.setLength(0.1);
		booster.addChild(boosterTail);
		
		booster.setInstanceCount(3);
		booster.setRadialOffset(0.18);
		
		return booster;
	}
	
	
	@Test
	public void testTestRocketMasses() {
		RocketComponent rocket = createTestRocket();
		String treeDump = rocket.toDebugTree();
		double expMass;
		double compMass;
		double calcMass;
		
		expMass = 0.093417755;
		compMass = rocket.getChild(0).getChild(0).getComponentMass();
		assertEquals(" NoseCone mass calculated incorrectly: ", expMass, compMass, EPSILON);
		
		expMass = 0.1275360953;
		compMass = rocket.getChild(0).getChild(1).getComponentMass();
		assertEquals(" Sustainer Body mass calculated incorrectly: ", expMass, compMass, EPSILON);
		
		expMass = 0.255072190;
		compMass = rocket.getChild(1).getChild(0).getComponentMass();
		assertEquals(" Core Body mass calculated incorrectly: ", expMass, compMass, EPSILON);
		
		expMass = 0.9792000;
		compMass = rocket.getChild(1).getChild(0).getChild(0).getComponentMass();
		assertEquals(" Core Fins mass calculated incorrectly: ", expMass, compMass, EPSILON);
		
		InnerTube motorCluster = (InnerTube) rocket.getChild(1).getChild(0).getChild(1);
		expMass = 0.0055329;
		compMass = motorCluster.getComponentMass();
		assertEquals(" Core Motor Mount Tubes: mass calculated incorrectly: ", expMass, compMass, EPSILON);
		compMass = motorCluster.getMass();
		assertEquals(" Core Motor Mount Tubes: mass calculated incorrectly: ", expMass, compMass, EPSILON);
		
		expMass = 490.061;
		//	MassCalculator mc = new BasicMassCalculator();
		// calcMass = mc.getMass();
		calcMass = rocket.getMass();
		
		assertEquals(" Simple Rocket Mass is incorrect: " + treeDump, expMass, calcMass, EPSILON);
	}
	
	@Test
	public void testTestRocketCG() {
		RocketComponent rocket = createTestRocket();
		String treeDump = rocket.toDebugTree();
		double expRelCGx;
		double expAbsCGx;
		double actualRelCGx;
		double actualAbsCGx;
		
		expRelCGx = 0.134068822;
		actualRelCGx = rocket.getChild(0).getChild(0).getComponentCG().x;
		assertEquals(" NoseCone CG calculated incorrectly: ", expRelCGx, actualRelCGx, EPSILON);
		
		expRelCGx = 0.15;
		actualRelCGx = rocket.getChild(0).getChild(1).getComponentCG().x;
		assertEquals(" Sustainer Body cg calculated incorrectly: ", expRelCGx, actualRelCGx, EPSILON);
		
		BodyTube coreBody = (BodyTube) rocket.getChild(1).getChild(0);
		expRelCGx = 0.3; // relative to parent
		actualRelCGx = coreBody.getComponentCG().x;
		assertEquals(" Core Body (relative) cg calculated incorrectly: ", expRelCGx, actualRelCGx, EPSILON);
		//expAbsCGx = 0.8;
		//actualAbsCGx = coreBody.getCG().x;
		//assertEquals(" Core Body (absolute) cg calculated incorrectly: ", expAbsCGx, actualAbsCGx, EPSILON);
		
		FinSet coreFins = (FinSet) rocket.getChild(1).getChild(0).getChild(0);
		expRelCGx = 0.244444444; // relative to parent
		actualRelCGx = coreFins.getComponentCG().x;
		assertEquals(" Core Fins (relative) cg calculated incorrectly: ", expRelCGx, actualRelCGx, EPSILON);
		//		expAbsCGx = 0.9444444444;
		//		actualAbsCGx = coreBody.getCG().x;
		//		assertEquals(" Core Fins (absolute) cg calculated incorrectly: ", expAbsCGx, actualAbsCGx, EPSILON);
		
		expRelCGx = 10.061;
		//		MassCalculator mc = new BasicMassCalculator();
		actualRelCGx = rocket.getCG().x;
		//		mc.getCG(Configuration configuration, MotorInstanceConfiguration motors) {
		//		calcMass = mc.getMass();
		
		assertEquals(" Simple Rocket CG is incorrect: " + treeDump, expRelCGx, actualRelCGx, EPSILON);
	}
	
	
	
}
