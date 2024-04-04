package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.Color;
import java.awt.LayoutManager;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.simulation.SimulationConditionsPanel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

public class LaunchPreferencesPanel extends PreferencesPanel {
	private static Color darkErrorColor;

	static {
		initColors();
	}

	public LaunchPreferencesPanel(JDialog parent, LayoutManager layout) {
		super(parent, layout);
		// TODO Auto-generated constructor stub
	}

	public LaunchPreferencesPanel() {
		super(new MigLayout("fillx, ins 30lp n n n"));

		// Warning message
		StyledLabel warning = new StyledLabel(String.format(
				"<html>%s</html>", trans.get("pref.dlg.lbl.launchWarning")),
				0.5f, StyledLabel.Style.BOLD);
		warning.setFontColor(darkErrorColor);
		warning.setToolTipText(trans.get("pref.dlg.lbl.launchWarning.ttip"));
		add(warning, "spanx, growx 0, gapbottom para, wrap");

		// Simulation conditions
		SimulationConditionsPanel.addSimulationConditionsPanel(this, preferences);

	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(LaunchPreferencesPanel::updateColors);
	}

	private static void updateColors() {
		darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
	}

}
