package info.openrocket.core.file.rocksim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.ExternalComponent.Finish;

/**
 * Tests the mapping between OpenRocket and RockSim surface finishes.
 */
public class RockSimFinishCodeTest {

	/**
	 * Each OpenRocket finish must export to the RockSim finish with the nearest roughness: polished (0.0001 in),
	 * gloss (0.0005 in), matt (0.001 in) or unfinished (0.008 in).
	 */
	@Test
	public void exportsTheNearestRockSimFinish() {
		assertEquals(RockSimFinishCode.UNFINISHED.ordinal(), RockSimFinishCode.toCode(Finish.ROUGH));
		assertEquals(RockSimFinishCode.UNFINISHED.ordinal(), RockSimFinishCode.toCode(Finish.ROUGHUNFINISHED));
		assertEquals(RockSimFinishCode.UNFINISHED.ordinal(), RockSimFinishCode.toCode(Finish.UNFINISHED));
		assertEquals(RockSimFinishCode.MATT.ordinal(), RockSimFinishCode.toCode(Finish.NORMAL));
		assertEquals(RockSimFinishCode.GLOSS.ordinal(), RockSimFinishCode.toCode(Finish.SMOOTH));
		assertEquals(RockSimFinishCode.POLISHED.ordinal(), RockSimFinishCode.toCode(Finish.OPTIMUM));
		assertEquals(RockSimFinishCode.POLISHED.ordinal(), RockSimFinishCode.toCode(Finish.POLISHED));
		assertEquals(RockSimFinishCode.POLISHED.ordinal(), RockSimFinishCode.toCode(Finish.FINISHPOLISHED));
		assertEquals(RockSimFinishCode.POLISHED.ordinal(), RockSimFinishCode.toCode(Finish.MIRROR));
	}

	/**
	 * Exporting an imported RockSim finish must give back the same RockSim finish.
	 */
	@Test
	public void roundTripsRockSimFinishes() {
		for (RockSimFinishCode code : RockSimFinishCode.values()) {
			assertEquals(code.ordinal(), RockSimFinishCode.toCode(code.asOpenRocket()));
		}
	}
}
