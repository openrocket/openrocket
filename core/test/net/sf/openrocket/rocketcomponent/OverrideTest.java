package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;

import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

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
}
