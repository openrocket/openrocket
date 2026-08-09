package info.openrocket.swing.gui.dialogs.preferences;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

/**
 * Verifies the side effects used by the Preferences dialog's Apply action.
 */
class PreferencesDialogTest extends BaseTestCase {

	@Test
	void applyStoresDefaultUnitsAndRefreshesTheVisibleRocket() {
		SwingPreferences preferences = mock(SwingPreferences.class);
		BasicFrame parent = mock(BasicFrame.class);
		RocketPanel rocketPanel = mock(RocketPanel.class);
		when(parent.getRocketPanel()).thenReturn(rocketPanel);

		PreferencesDialog.storeAndApplyPreferences(preferences, parent);

		verify(preferences).storeDefaultUnits();
		verify(rocketPanel).updateExtras();
		verify(rocketPanel).updateFigures();
		verify(rocketPanel).updateRulers();
	}

	@Test
	void applyStoresDefaultUnitsWithoutAnOpenRocketFrame() {
		SwingPreferences preferences = mock(SwingPreferences.class);

		PreferencesDialog.storeAndApplyPreferences(preferences, null);

		verify(preferences).storeDefaultUnits();
	}
}
