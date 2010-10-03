package net.sf.openrocket.rocketcomponent;

import org.junit.Test;

public class RocketTest {
	
	@Test
	public void testCopyFrom() {
		Rocket r1 = net.sf.openrocket.util.TestRockets.makeIsoHaisu();
		Rocket r2 = net.sf.openrocket.util.TestRockets.makeBigBlue();
		
		Rocket copy = (Rocket) r2.copy();
		
		ComponentCompare.assertDeepEquality(r2, copy);
		
		r1.copyFrom(copy);
		
		ComponentCompare.assertDeepEquality(r1, r2);
	}
	
}
