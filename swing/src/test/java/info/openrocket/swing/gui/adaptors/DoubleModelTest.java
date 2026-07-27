package info.openrocket.swing.gui.adaptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.StateChangeListener;

class DoubleModelTest {
	@Test
	void externalChangeSourceUpdatesModelListeners() {
		StubSource source = new StubSource(1.0);
		DoubleModel model = new DoubleModel(source, "Value");
		int[] eventCount = { 0 };
		ChangeListener listener = event -> eventCount[0]++;
		model.addChangeListener(listener);

		source.setValue(4.2);

		assertEquals(4.2, model.getValue());
		assertEquals(1, eventCount[0]);

		model.removeChangeListener(listener);
		model.invalidateMe();
	}

	/**
	 * Observable bean used to verify updates made outside the adaptor.
	 */
	public static class StubSource implements ChangeSource {
		private final List<StateChangeListener> listeners = new ArrayList<>();
		private double value;

		StubSource(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
			for (StateChangeListener listener : List.copyOf(listeners)) {
				listener.stateChanged(null);
			}
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
			listeners.remove(listener);
		}
	}
}
