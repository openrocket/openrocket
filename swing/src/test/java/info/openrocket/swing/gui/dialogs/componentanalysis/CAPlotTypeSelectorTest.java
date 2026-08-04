package info.openrocket.swing.gui.dialogs.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests component retention while changing a component-analysis plot data type.
 */
class CAPlotTypeSelectorTest extends BaseTestCase {

	@Test
	void retainsComponentsSupportedByTheNewType() {
		Rocket rocket = new Rocket();
		BodyTube bodyTube = new BodyTube();
		TrapezoidFinSet finSet = new TrapezoidFinSet();

		List<RocketComponent> retained = CAPlotTypeSelector.retainValidComponentsOrRocket(
				List.of(bodyTube, finSet), List.of(rocket, bodyTube, finSet));

		assertEquals(List.of(bodyTube, finSet), retained);
	}

	@Test
	void dropsInvalidComponentsWhileRetainingValidSelections() {
		Rocket rocket = new Rocket();
		BodyTube bodyTube = new BodyTube();
		TrapezoidFinSet finSet = new TrapezoidFinSet();

		List<RocketComponent> retained = CAPlotTypeSelector.retainValidComponentsOrRocket(
				List.of(bodyTube, finSet), List.of(rocket, finSet));

		assertEquals(List.of(finSet), retained);
	}

	@Test
	void fallsBackToRocketWhenNoSelectedComponentIsSupported() {
		Rocket rocket = new Rocket();
		BodyTube bodyTube = new BodyTube();
		TrapezoidFinSet finSet = new TrapezoidFinSet();

		List<RocketComponent> retained = CAPlotTypeSelector.retainValidComponentsOrRocket(
				List.of(bodyTube), List.of(rocket, finSet));

		assertEquals(List.of(rocket), retained);
	}

	@Test
	void removingAPlotTypeKeepsItsComponentSelectionAligned() {
		Rocket rocket = new Rocket();
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		CAPlotConfiguration configuration = new CAPlotConfiguration("test");
		configuration.addPlotDataType(CADataType.TOTAL_CD);
		configuration.setPlotDataComponents(0, List.of(rocket));
		configuration.addPlotDataType(CADataType.TOTAL_ROLL_COEFFICIENT);
		configuration.setPlotDataComponents(1, List.of(finSet));

		configuration.removePlotDataType(0);

		assertEquals(List.of(finSet), configuration.getComponents(0));
	}
}
