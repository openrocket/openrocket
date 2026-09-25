package info.openrocket.swing.gui.scalefigure;

import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaleSelectorTest extends BaseTestCase {

	@Test
	void manualEntryRemainsVisibleWhileZoomModelAppliesRequestAsynchronously() {
		DeferredZoomModel zoomModel = new DeferredZoomModel();
		ScaleSelector selector = new ScaleSelector(zoomModel);
		JComboBox<String> combo = selector.getScaleSelectorCombo();

		combo.setSelectedItem("250%");

		assertEquals(2.5, zoomModel.requestedScale);
		assertEquals("250%", combo.getSelectedItem());
	}

	/** Zoom model that records requests without immediately changing its published scale. */
	private static class DeferredZoomModel implements ScaleSelector.ZoomModel {
		private double requestedScale = Double.NaN;

		@Override
		public double getScale() {
			return 1.0;
		}

		@Override
		public boolean isFit() {
			return true;
		}

		@Override
		public void setScale(double scale) {
			requestedScale = scale;
		}

		@Override
		public void setFit() {
			requestedScale = 1.0;
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
			// The deferred model deliberately does not publish the request during the test.
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
			// No listener is retained by this test model.
		}
	}
}
