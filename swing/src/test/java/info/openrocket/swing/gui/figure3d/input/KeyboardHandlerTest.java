package info.openrocket.swing.gui.figure3d.input;

import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyboardHandlerTest {

	@Test
	void autoRepeatDoesNotRearmSinglePressAction() {
		KeyboardHandler handler = new KeyboardHandler();
		AtomicInteger invocations = new AtomicInteger();
		handler.addSinglePressAction(KeyEvent.VK_F, invocations::incrementAndGet);

		handler.handleKeyEvent(KeyEvent.VK_F, 1);
		handler.handleQueuedEvents();
		handler.handleKeyEvent(KeyEvent.VK_F, 1);
		handler.handleQueuedEvents();

		assertEquals(1, invocations.get());

		handler.handleKeyEvent(KeyEvent.VK_F, 0);
		handler.handleKeyEvent(KeyEvent.VK_F, 1);
		handler.handleQueuedEvents();

		assertEquals(2, invocations.get());
	}
}
