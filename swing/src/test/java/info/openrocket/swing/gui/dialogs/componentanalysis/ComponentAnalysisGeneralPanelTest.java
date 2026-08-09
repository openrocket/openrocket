package info.openrocket.swing.gui.dialogs.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests which aggregation levels are shown in the component-analysis general tables.
 */
class ComponentAnalysisGeneralPanelTest extends BaseTestCase {

	@Test
	void stagesAreShownInEveryGeneralTable() {
		AxialStage stage = new AxialStage();

		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInStabilityTable(stage));
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInDragTable(stage));
	}

	@Test
	void rocketTotalsAreShownInEveryGeneralTable() {
		Rocket rocket = new Rocket();

		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInStabilityTable(rocket));
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInDragTable(rocket));
	}
}
