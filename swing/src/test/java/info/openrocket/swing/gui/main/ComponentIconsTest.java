package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MassComponent.MassComponentType;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests component-instance icon lookup shared by component renderers.
 */
class ComponentIconsTest extends BaseTestCase {

	@Test
	void componentInstanceUsesItsClassIcon() {
		BodyTube bodyTube = new BodyTube();

		assertNotNull(ComponentIcons.getSmallIcon(bodyTube));
		assertSame(ComponentIcons.getSmallIcon(BodyTube.class), ComponentIcons.getSmallIcon(bodyTube));
	}

	@Test
	void massComponentInstanceUsesItsConfiguredSubtypeIcon() {
		MassComponent massComponent = new MassComponent();
		massComponent.setMassComponentType(MassComponentType.ALTIMETER);

		assertNotNull(ComponentIcons.getSmallIcon(massComponent));
		assertSame(ComponentIcons.getSmallMassTypeIcon(MassComponentType.ALTIMETER),
				ComponentIcons.getSmallIcon(massComponent));
	}
}
