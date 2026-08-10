package info.openrocket.swing.gui.dialogs.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.TestRockets;
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
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInRollTable(stage));
	}

	@Test
	void rocketTotalsAreShownInEveryGeneralTable() {
		Rocket rocket = new Rocket();

		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInStabilityTable(rocket));
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInDragTable(rocket));
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInRollTable(rocket));
	}

	@Test
	void individualComponentsKeepTheirExistingTableMembership() {
		BodyTube bodyTube = new BodyTube();

		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInStabilityTable(bodyTube));
		assertTrue(ComponentAnalysisGeneralPanel.isComponentSupportedInDragTable(bodyTube));
		assertFalse(ComponentAnalysisGeneralPanel.isComponentSupportedInRollTable(bodyTube));
	}

	@Test
	void componentHierarchyDepthCountsAncestors() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		BodyTube bodyTube = new BodyTube();
		rocket.addChild(stage);
		stage.addChild(bodyTube);

		assertEquals(0, ComponentAnalysisGeneralPanel.getComponentHierarchyDepth(rocket));
		assertEquals(1, ComponentAnalysisGeneralPanel.getComponentHierarchyDepth(stage));
		assertEquals(2, ComponentAnalysisGeneralPanel.getComponentHierarchyDepth(bodyTube));
	}

	@Test
	void activeMotorImmediatelyFollowsItsMount() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		FlightConfiguration configuration = rocket.getFlightConfigurationByIndex(0, false);
		MotorConfiguration motorConfiguration = configuration.getActiveMotors().iterator().next();

		List<Object> sources = ComponentAnalysisGeneralPanel.getStabilityTableSources(configuration);
		int mountIndex = sources.indexOf(motorConfiguration.getMount());

		assertTrue(mountIndex >= 0);
		assertEquals(motorConfiguration, sources.get(mountIndex + 1));
	}

	@Test
	void componentCellToolTipUsesTheFullNameOnlyInTheComponentColumn() {
		String componentName = "A component name that is wider than its table cell";

		assertEquals(componentName, ComponentAnalysisGeneralPanel.getComponentCellToolTip(componentName, 0));
		assertNull(ComponentAnalysisGeneralPanel.getComponentCellToolTip(componentName, 1));
		assertNull(ComponentAnalysisGeneralPanel.getComponentCellToolTip(null, 0));
	}
}
