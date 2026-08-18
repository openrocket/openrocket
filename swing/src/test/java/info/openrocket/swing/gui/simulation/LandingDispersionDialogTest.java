package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.swing.gui.simulation.LandingDispersionDialog.DistributionComboBox;

public class LandingDispersionDialogTest {
	/**
	 * The symmetric distributions apply to every parameter; a log-normal multiplier only
	 * makes sense where the sampled value is a relative fraction. The dialog lists them
	 * all on every row and greys the rest, so this rule decides what is selectable.
	 */
	@Test
	public void testLogNormalIsAvailableOnlyForRelativeParameters() {
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			for (MonteCarloDistribution distribution : MonteCarloDistribution.values()) {
				boolean available = LandingDispersionDialog.isDistributionAvailable(distribution, parameter);
				if (distribution == MonteCarloDistribution.LOG_NORMAL) {
					assertEquals(parameter.isRelative(), available,
							distribution + " on " + parameter + " must follow isRelative()");
				} else {
					assertTrue(available, distribution + " must apply to " + parameter);
				}
			}
		}
		assertTrue(LandingDispersionDialog.isDistributionAvailable(
				MonteCarloDistribution.LOG_NORMAL, MonteCarloParameter.RECOVERY_DRAG));
		assertFalse(LandingDispersionDialog.isDistributionAvailable(
				MonteCarloDistribution.LOG_NORMAL, MonteCarloParameter.WIND_DIRECTION));
	}

	/**
	 * Every distribution stays listed on every row so the option is discoverable, but the
	 * combo must refuse to select one the edited row cannot use.
	 */
	@Test
	public void testComboListsEveryDistributionButBlocksUnavailableOnes() {
		DistributionComboBox combo = new DistributionComboBox();
		assertEquals(MonteCarloDistribution.values().length, combo.getItemCount());

		combo.setParameter(MonteCarloParameter.WIND_DIRECTION);
		combo.setSelectedItem(MonteCarloDistribution.LOG_NORMAL);
		assertNotEquals(MonteCarloDistribution.LOG_NORMAL, combo.getSelectedItem());
		combo.setSelectedItem(MonteCarloDistribution.UNIFORM);
		assertEquals(MonteCarloDistribution.UNIFORM, combo.getSelectedItem());

		combo.setParameter(MonteCarloParameter.RECOVERY_DRAG);
		combo.setSelectedItem(MonteCarloDistribution.LOG_NORMAL);
		assertEquals(MonteCarloDistribution.LOG_NORMAL, combo.getSelectedItem());
	}
}
