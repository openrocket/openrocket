package info.openrocket.core.file.rocksim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.Transition;

/**
 * Tests the mapping between RockSim and OpenRocket nose cone and transition shapes.
 */
public class RockSimNoseConeCodeTest {

	/**
	 * Mapping RockSim's parabola to a power series must not change how ellipsoids and power series are exported.
	 */
	@Test
	public void exportsEllipsoidAndPowerSeriesWithTheirOwnCodes() {
		assertEquals(3, RockSimNoseConeCode.toCode(Transition.Shape.ELLIPSOID));
		assertEquals(4, RockSimNoseConeCode.toCode(Transition.Shape.POWER));
		assertEquals(1, RockSimNoseConeCode.toCode(Transition.Shape.OGIVE));
	}
}
