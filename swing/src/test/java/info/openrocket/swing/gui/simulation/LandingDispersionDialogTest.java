package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.simulation.LandingDispersionDialog.DistributionComboBox;
import info.openrocket.swing.util.BaseTestCase;

public class LandingDispersionDialogTest extends BaseTestCase {
	@Test
	public void testSimulationPanelEntryPointDescribesTheWholeAnalysis() {
		ResourceBundle bundle = ResourceBundle.getBundle("l10n.messages", Locale.ROOT);

		assertEquals("Monte Carlo", bundle.getString("simpanel.but.landingDispersion"));
		String tooltip = bundle.getString("simpanel.but.ttip.landingDispersion");
		assertTrue(tooltip.contains("landing dispersion"), tooltip);
		assertTrue(tooltip.contains("flight-metric"), tooltip);
	}

	@Test
	public void testSavedSettingsTakePrecedenceOverCachedSettings() {
		MonteCarloSettings saved = settings(101, 1, 0.1);
		MonteCarloSettings cached = settings(202, 1, 0.2);
		MonteCarloResult cachedResult = new MonteCarloResult(cached, null, List.of(), 0);

		assertSame(saved, LandingDispersionDialog.selectInitialSettings(saved, cachedResult));
		assertSame(cached, LandingDispersionDialog.selectInitialSettings(null, cachedResult));
		assertNull(LandingDispersionDialog.selectInitialSettings(null, null));
	}

	@Test
	public void testPersistSettingsIgnoresExecutionOnlyWorkerCount() {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		MonteCarloSettings original = settings(303, 1, 0.1);
		LandingDispersionDialog.persistSettings(simulation, original);
		assertSame(original, simulation.getLandingDispersionSettings());

		LandingDispersionDialog.persistSettings(simulation, settings(303, 8, 0.1));
		assertSame(original, simulation.getLandingDispersionSettings(),
				"Worker count is machine-specific and must not dirty persisted settings");

		MonteCarloSettings changed = settings(303, 8, 0.2);
		LandingDispersionDialog.persistSettings(simulation, changed);
		assertSame(changed, simulation.getLandingDispersionSettings());
	}

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

	private static MonteCarloSettings settings(int seed, int threadCount, double spread) {
		return MonteCarloSettings.builder()
				.runCount(20)
				.seed(seed)
				.threadCount(threadCount)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG,
						MonteCarloDistribution.NORMAL, spread)
				.build();
	}
}
