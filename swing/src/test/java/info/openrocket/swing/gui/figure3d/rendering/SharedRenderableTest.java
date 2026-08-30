package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SharedRenderableTest {

	@Test
	void cleansDelegateOnlyAfterEveryLeaseIsReleased() {
		Renderable delegate = mock(Renderable.class);
		SharedRenderable shared = new SharedRenderable(delegate);
		Renderable first = shared.acquire();
		Renderable second = shared.acquire();

		first.render();
		verify(delegate).render();

		first.cleanup();
		first.cleanup();
		verify(delegate, never()).cleanup();

		second.cleanup();
		verify(delegate).cleanup();
		assertThrows(IllegalStateException.class, shared::acquire);
		assertThrows(IllegalStateException.class, second::render);
	}

	@Test
	void eachActiveLeaseRendersThroughTheSharedDelegate() {
		Renderable delegate = mock(Renderable.class);
		SharedRenderable shared = new SharedRenderable(delegate);
		Renderable first = shared.acquire();
		Renderable second = shared.acquire();

		first.render();
		second.render();

		verify(delegate, times(2)).render();
		first.cleanup();
		second.cleanup();
	}
}
