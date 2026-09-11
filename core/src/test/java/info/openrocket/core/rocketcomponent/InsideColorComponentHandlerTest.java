package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InsideColorComponentHandlerTest extends BaseTestCase {

	@Test
	void materialPartitionChangesFireGraphicEvents() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube tube = rocket.getAllChildren().stream()
				.filter(BodyTube.class::isInstance)
				.map(BodyTube.class::cast)
				.findFirst()
				.orElseThrow();
		InsideColorComponentHandler handler = tube.getInsideColorComponentHandler();
		AtomicReference<ComponentChangeEvent> event = new AtomicReference<>();
		rocket.addComponentChangeListener(event::set);

		handler.setSeparateInsideOutside(true);
		assertEquals(ComponentChangeEvent.GRAPHIC_CHANGE, event.get().getType());

		event.set(null);
		handler.setEdgesSameAsInside(true);
		assertEquals(ComponentChangeEvent.GRAPHIC_CHANGE, event.get().getType());

		event.set(null);
		handler.setEdgesSameAsInside(true);
		assertNull(event.get(), "Assigning the current partition setting should not rebuild the scene");
	}
}
