package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FinSetTest extends BaseTestCase {

	@Test
	public void testMultiplicity() {
		final TrapezoidFinSet trapFins = new TrapezoidFinSet();
		assertEquals(1, trapFins.getFinCount());

		final FreeformFinSet fffins = new FreeformFinSet();
		assertEquals(1, fffins.getFinCount());
		
		final EllipticalFinSet efins = new EllipticalFinSet();
		assertEquals(1, efins.getFinCount());
	}
	
}
