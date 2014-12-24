package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import net.miginfocom.swing.MigLayout;

public class SimulationPreferencesPanel extends PreferencesPanel {

	/*
	 * private double launchRodLength = 1;
	 * 
	 * private boolean launchIntoWind = true; private double launchRodAngle = 0;
	 * 
	 * private double windDirection = Math.PI / 2; private double
	 * launchRodDirection = 0;
	 * 
	 * 
	 * private double windAverage = 2.0; private double windTurbulence = 0.1;
	 * 
	 * private double launchAltitude = 0; private double launchLatitude = 45;
	 * private double launchLongitude = 0; private GeodeticComputationStrategy
	 * geodeticComputation = GeodeticComputationStrategy.SPHERICAL;
	 */

	public SimulationPreferencesPanel() {
		super(new MigLayout("fillx, ins 30lp n n n"));

		// Confirm deletion of simulations:
		final JCheckBox confirmDelete = new JCheckBox(
				trans.get("pref.dlg.lbl.Confirmdeletion"));
		confirmDelete.setSelected(preferences.getConfirmSimDeletion());
		confirmDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoRunSimulations(confirmDelete.isSelected());
			}
		});
		this.add(confirmDelete, "wrap, growx, sg combos ");

		// Automatically run all simulation out-dated by design changes.
		final JCheckBox automaticallyRunSimsBox = new JCheckBox(
				trans.get("pref.dlg.checkbox.Runsimulations"));
		automaticallyRunSimsBox
				.setSelected(preferences.getAutoRunSimulations());
		automaticallyRunSimsBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoRunSimulations(automaticallyRunSimsBox
						.isSelected());
			}
		});
		this.add(automaticallyRunSimsBox, "wrap, growx, sg combos ");

		// private double launchRodLength = 1;

		// Keep launch rod aligned with the wind
		final JCheckBox launchIntoWind = new JCheckBox(
				trans.get("simedtdlg.checkbox.Intowind"));
		launchIntoWind.setSelected(preferences.getLaunchIntoWind());
		launchIntoWind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setLaunchIntoWind(launchIntoWind.isSelected());
			}
		});
		this.add(launchIntoWind, "wrap, growx, sg combos ");

		// private double launchRodAngle = 0;
		// launchRodDirection = 0;

		// private double windDirection = Math.PI / 2; private double
		// private double windAverage = 2.0;
		// private double windTurbulence = 0.1;

		// private double launchAltitude = 0;
		// private double launchLatitude = 45;
		// private double launchLongitude = 0;
		// private GeodeticComputationStrategy
		// geodeticComputation = GeodeticComputationStrategy.SPHERICAL;

	}
}
