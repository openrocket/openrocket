package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OverrideTest extends BaseTestCase {
	
	@Test
	public void testCDAncestorOverrides() {
		// create test rocket
		Rocket rocket = TestRockets.makeEstesAlphaIII();

		// obtain sustainer, nose cone, body tube, and fin set
		AxialStage sustainer = null;
		for (RocketComponent c : rocket.getChildren()){
			if (c instanceof AxialStage)
				sustainer = (AxialStage) c;
		}
		assertNotNull(sustainer);

		NoseCone nosecone = null;
		BodyTube bodytube = null;
		for (RocketComponent c : sustainer.getChildren()) {
			if (c instanceof NoseCone)
				nosecone = (NoseCone) c;
			
			if (c instanceof BodyTube)
				bodytube = (BodyTube) c;
		}
		assertNotNull(nosecone);
		assertNotNull(bodytube);

		FinSet finset = null;
		for (RocketComponent c : bodytube.getChildren()) {
			if (c instanceof FinSet)
				finset = (FinSet) c;
		}
		assertNotNull(finset);

		// We start by just checking the override flags
		// Initially no overrides
		assertFalse(sustainer.isCDOverridden());
		assertFalse(sustainer.isSubcomponentsOverriddenCD());
		assertFalse(sustainer.isCDOverriddenByAncestor());
		
		assertFalse(bodytube.isCDOverridden());		
		assertFalse(bodytube.isSubcomponentsOverriddenCD());
		assertFalse(bodytube.isCDOverriddenByAncestor());
		
		assertFalse(finset.isCDOverridden());
		assertFalse(finset.isSubcomponentsOverriddenCD());
		assertFalse(finset.isCDOverriddenByAncestor());

		// Override sustainer CD and subcomponents
		sustainer.setSubcomponentsOverriddenCD(true);
		sustainer.setCDOverridden(true);
		sustainer.setOverrideCD(0.5);

		assertTrue(bodytube.isCDOverriddenByAncestor());
		assertTrue(finset.isCDOverriddenByAncestor());

		// Set body tube to override subcomponents, override its CD; it's still
		// overridden by ancestor
		bodytube.setCDOverridden(true);
		bodytube.setSubcomponentsOverriddenCD(true);
		bodytube.setOverrideCD(0.25);

		assertTrue(bodytube.isCDOverriddenByAncestor());

		// Now see if it's actually working
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(null);
		WarningSet warnings = new WarningSet();
		BarrowmanCalculator calc = new BarrowmanCalculator();

		// total CD should be overrideCD of sustainer
		AerodynamicForces forces = calc.getAerodynamicForces(configuration, conditions, warnings);
		assertEquals(sustainer.getOverrideCD(), forces.getCD(), MathUtil.EPSILON);

		// Turn off sustainer subcomponents override; body tube and nose cone aren't overridden by ancestor but fin set is
		sustainer.setSubcomponentsOverriddenCD(false);

		// CD of rocket should be overridden CD of sustainer plus body tube plus calculated CD of nose cone
		Map<RocketComponent, AerodynamicForces> forceMap = calc.getForceAnalysis(configuration, conditions, warnings);
		assertEquals(sustainer.getOverrideCD() + bodytube.getOverrideCD() + forceMap.get(nosecone).getCD(), forceMap.get(rocket).getCD(), MathUtil.EPSILON);
	}

	/**
	 * Test whether children components of a parent that has subcomponents overridden for mass, CG, or CD have the correct
	 * overriddenBy object.
	 */
	@Test
	public void testOverriddenBy() {
		// Create test rocket
		Rocket rocket = TestRockets.makeEstesAlphaIII();

		// Obtain the necessary components
		AxialStage sustainer = rocket.getStage(0);
		NoseCone noseCone = (NoseCone) sustainer.getChild(0);
		BodyTube bodyTube = (BodyTube) sustainer.getChild(1);
		FinSet finSet = (FinSet) bodyTube.getChild(0);
		LaunchLug launchLug = (LaunchLug) bodyTube.getChild(1);
		InnerTube innerTube = (InnerTube) bodyTube.getChild(2);
		EngineBlock engineBlock = (EngineBlock) innerTube.getChild(0);
		Parachute parachute = (Parachute) bodyTube.getChild(3);
		CenteringRing bulkhead = (CenteringRing) bodyTube.getChild(4);

		// Check initial override by components
		assertNull(rocket.getMassOverriddenBy());
		assertNull(rocket.getCGOverriddenBy());
		assertNull(rocket.getCDOverriddenBy());
		assertNull(sustainer.getMassOverriddenBy());
		assertNull(sustainer.getCGOverriddenBy());
		assertNull(sustainer.getCDOverriddenBy());
		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertNull(finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertNull(launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertNull(innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertNull(engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertNull(parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertNull(bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());

		// Override body tube mass, CG, and CD without for subcomponents
		bodyTube.setMassOverridden(true);
		bodyTube.setCGOverridden(true);
		bodyTube.setCDOverridden(true);
		
		assertNull(sustainer.getMassOverriddenBy());
		assertNull(sustainer.getCGOverriddenBy());
		assertNull(sustainer.getCDOverriddenBy());
		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertNull(finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertNull(launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertNull(innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertNull(engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertNull(parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertNull(bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());
		
		// Override body tube mass for subcomponents
		bodyTube.setSubcomponentsOverriddenMass(true);
		
		assertNull(sustainer.getMassOverriddenBy());
		assertNull(sustainer.getCGOverriddenBy());
		assertNull(sustainer.getCDOverriddenBy());
		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertEquals(bodyTube, finSet.getMassOverriddenBy());
		assertNull(finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertEquals(bodyTube, launchLug.getMassOverriddenBy());
		assertNull(launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertEquals(bodyTube, innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(bodyTube, engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertEquals(bodyTube, parachute.getMassOverriddenBy());
		assertNull(parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertEquals(bodyTube, bulkhead.getMassOverriddenBy());
		assertNull(bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());

		// Undo override body tube mass for subcomponents, do override of CG and CD for subcomponents
		bodyTube.setSubcomponentsOverriddenMass(false);
		bodyTube.setSubcomponentsOverriddenCG(true);
		bodyTube.setSubcomponentsOverriddenCD(true);

		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertEquals(bodyTube, finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertEquals(bodyTube, launchLug.getCDOverriddenBy());
		assertNull(innerTube.getMassOverriddenBy());
		assertEquals(bodyTube, innerTube.getCGOverriddenBy());
		assertEquals(bodyTube, innerTube.getCDOverriddenBy());
		assertNull(engineBlock.getMassOverriddenBy());
		assertEquals(bodyTube, engineBlock.getCGOverriddenBy());
		assertEquals(bodyTube, engineBlock.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertEquals(bodyTube, parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCDOverriddenBy());

		// Move the inner tube from the body tube to the nose cone
		bodyTube.removeChild(innerTube);
		noseCone.addChild(innerTube);

		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertNull(engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertEquals(bodyTube, finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertEquals(bodyTube, launchLug.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertEquals(bodyTube, parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCDOverriddenBy());

		// Override mass of nose cone
		noseCone.setMassOverridden(true);

		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertNull(engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertEquals(bodyTube, finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertEquals(bodyTube, launchLug.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertEquals(bodyTube, parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCDOverriddenBy());

		// Override nose cone mass for all subcomponents
		noseCone.setSubcomponentsOverriddenMass(true);

		assertNull(sustainer.getMassOverriddenBy());
		assertNull(sustainer.getCGOverriddenBy());
		assertNull(sustainer.getCDOverriddenBy());
		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertEquals(noseCone, innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(noseCone, engineBlock.getMassOverriddenBy());
		assertNull(engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertEquals(bodyTube, finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertEquals(bodyTube, launchLug.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertEquals(bodyTube, parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCDOverriddenBy());

		// Override inner tube CG for all subcomponents
		innerTube.setCGOverridden(true);
		innerTube.setSubcomponentsOverriddenCG(true);

		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertEquals(noseCone, innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(noseCone, engineBlock.getMassOverriddenBy());
		assertEquals(innerTube, engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertNull(finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertEquals(bodyTube, finSet.getCDOverriddenBy());
		assertNull(launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertEquals(bodyTube, launchLug.getCDOverriddenBy());
		assertNull(parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertEquals(bodyTube, parachute.getCDOverriddenBy());
		assertNull(bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCDOverriddenBy());

		// Set body tube mass override, reset CG & CD, and move inner tube back to body tube
		bodyTube.setMassOverridden(true);
		bodyTube.setSubcomponentsOverriddenMass(true);
		bodyTube.setCGOverridden(false);
		bodyTube.setCDOverridden(false);
		noseCone.removeChild(innerTube);
		bodyTube.addChild(innerTube);

		assertNull(noseCone.getMassOverriddenBy());
		assertNull(noseCone.getCGOverriddenBy());
		assertNull(noseCone.getCDOverriddenBy());
		assertNull(bodyTube.getMassOverriddenBy());
		assertNull(bodyTube.getCGOverriddenBy());
		assertNull(bodyTube.getCDOverriddenBy());
		assertEquals(bodyTube, finSet.getMassOverriddenBy());
		assertNull(finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertEquals(bodyTube, launchLug.getMassOverriddenBy());
		assertNull(launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertEquals(bodyTube, parachute.getMassOverriddenBy());
		assertNull(parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertEquals(bodyTube, bulkhead.getMassOverriddenBy());
		assertNull(bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());
		assertEquals(bodyTube, innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(bodyTube, engineBlock.getMassOverriddenBy());
		assertEquals(innerTube, engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());

		// Toggle the body tube CG override for all subcomponents
		bodyTube.setCGOverridden(true);
		bodyTube.setSubcomponentsOverriddenCG(true);

		assertEquals(bodyTube, finSet.getMassOverriddenBy());
		assertEquals(bodyTube, finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertEquals(bodyTube, launchLug.getMassOverriddenBy());
		assertEquals(bodyTube, launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertEquals(bodyTube, parachute.getMassOverriddenBy());
		assertEquals(bodyTube, parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertEquals(bodyTube, bulkhead.getMassOverriddenBy());
		assertEquals(bodyTube, bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());
		assertEquals(bodyTube, innerTube.getMassOverriddenBy());
		assertEquals(bodyTube, innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(bodyTube, engineBlock.getMassOverriddenBy());
		assertEquals(bodyTube, engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());

		// Toggle back
		bodyTube.setSubcomponentsOverriddenCG(false);
		assertEquals(bodyTube, finSet.getMassOverriddenBy());
		assertNull(finSet.getCGOverriddenBy());
		assertNull(finSet.getCDOverriddenBy());
		assertEquals(bodyTube, launchLug.getMassOverriddenBy());
		assertNull(launchLug.getCGOverriddenBy());
		assertNull(launchLug.getCDOverriddenBy());
		assertEquals(bodyTube, parachute.getMassOverriddenBy());
		assertNull(parachute.getCGOverriddenBy());
		assertNull(parachute.getCDOverriddenBy());
		assertEquals(bodyTube, bulkhead.getMassOverriddenBy());
		assertNull(bulkhead.getCGOverriddenBy());
		assertNull(bulkhead.getCDOverriddenBy());
		assertEquals(bodyTube, innerTube.getMassOverriddenBy());
		assertNull(innerTube.getCGOverriddenBy());
		assertNull(innerTube.getCDOverriddenBy());
		assertEquals(bodyTube, engineBlock.getMassOverriddenBy());
		assertEquals(innerTube, engineBlock.getCGOverriddenBy());
		assertNull(engineBlock.getCDOverriddenBy());
	}
}
