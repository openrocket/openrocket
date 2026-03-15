package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.Color;
import java.awt.LayoutManager;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.simulation.SimulationConditionsPanel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.core.simulation.DefaultSimulationOptionFactory;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;

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
		SimulationConditionsPanel.addSimulationConditionsPanel(this, preferences, false);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(LaunchPreferencesPanel::updateColors);
	}

	public static void updateColors() {
		darkErrorColor = UITheme.getColor(UITheme.Keys.DARK_ERROR);
	}

	/**
	 * Saves the current launch preferences to DefaultSimulationOptionFactory
	 * so that new simulations will use these values.
	 */
	public void saveLaunchPreferencesToDefaults() {
		// Create a SimulationOptions from the current preferences
		// Since preferences implements SimulationOptionsInterface, we can create
		// a SimulationOptions and copy the values
		SimulationOptions options = new SimulationOptions();
		
		// Copy values from preferences (which implements SimulationOptionsInterface)
		options.setLaunchLatitude(preferences.getLaunchLatitude());
		options.setLaunchLongitude(preferences.getLaunchLongitude());
		options.setLaunchAltitude(preferences.getLaunchAltitude());
		options.setISAAtmosphere(preferences.isISAAtmosphere());
		options.setLaunchTemperature(preferences.getLaunchTemperature());
		options.setLaunchPressure(preferences.getLaunchPressure());
		options.setLaunchIntoWind(preferences.getLaunchIntoWind());
		options.setLaunchRodLength(preferences.getLaunchRodLength());
		options.setLaunchRodAngle(preferences.getLaunchRodAngle());
		options.setLaunchRodDirection(preferences.getLaunchRodDirection());
		
		// Copy wind model settings
		options.getAverageWindModel().setAverage(preferences.getAverageWindModel().getAverage());
		options.getAverageWindModel().setStandardDeviation(preferences.getAverageWindModel().getStandardDeviation());
		options.getAverageWindModel().setTurbulenceIntensity(preferences.getAverageWindModel().getTurbulenceIntensity());
		
		// Save to DefaultSimulationOptionFactory
		DefaultSimulationOptionFactory factory = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
		factory.saveDefault(options);
	}

}
