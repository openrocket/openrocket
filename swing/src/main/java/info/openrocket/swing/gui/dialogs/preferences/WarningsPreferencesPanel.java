package info.openrocket.swing.gui.dialogs.preferences;

import info.openrocket.swing.gui.simulation.RecoveryWarningsPanel;
import net.miginfocom.swing.MigLayout;

/** Application-wide defaults for simulation warning thresholds. */
public class WarningsPreferencesPanel extends PreferencesPanel {
	private static final long serialVersionUID = 8863789557314951341L;

	/** Creates the warning preferences panel. */
	public WarningsPreferencesPanel() {
		super(new MigLayout("fillx", "[grow]"));
		add(new RecoveryWarningsPanel(preferences), "growx, wmin 0");
	}
}
