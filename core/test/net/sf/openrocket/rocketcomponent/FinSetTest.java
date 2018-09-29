package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
