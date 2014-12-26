package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.UnitGroup;

public class DesignPreferencesPanel extends PreferencesPanel {

	public DesignPreferencesPanel() {
		super(new MigLayout("fillx, ins 30lp n n n"));

		// // Position to insert new body components:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Positiontoinsert")),
				"gapright para");
		this.add(
				new JComboBox<Object>(new PrefChoiceSelector(
						Preferences.BODY_COMPONENT_INSERT_POSITION_KEY,
						// // Always ask
						// // Insert in middle
						// // Add to end
						trans.get("pref.dlg.PrefChoiseSelector1"), trans
								.get("pref.dlg.PrefChoiseSelector2"), trans
								.get("pref.dlg.PrefChoiseSelector3"))),
				"wrap para, growx, sg combos");

		// Font size of information in panel window
		this.add(new JLabel(trans.get("pref.dlg.lbl.Rocketinfofontsize")),
				"gapright para");

		this.add(
				new JComboBox<Object>(new PrefChoiceSelector(
						Preferences.ROCKET_INFO_FONT_SIZE,
						// // Small
						// // Medium
						// // Large
						trans.get("pref.dlg.PrefFontSmall"), trans
								.get("pref.dlg.PrefFontMedium"), trans
								.get("pref.dlg.PrefFontLarge"))),
				"wrap para, growx, sg combos");

		// // Default Mach number
		JLabel dfn = new JLabel(trans.get("pref.dlg.lbl.DefaultMach"));
		this.add(dfn, "gapright para");
		dfn.setToolTipText(trans.get("pref.dlg.ttip.DefaultMach1")
				+ trans.get("pref.dlg.ttip.DefaultMach2"));

		DoubleModel m = new DoubleModel(preferences, "DefaultMach", 1.0,
				UnitGroup.UNITS_COEFFICIENT, 0.1, 0.9);

		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(trans.get("pref.dlg.ttip.DefaultMach1")
				+ trans.get("pref.dlg.ttip.DefaultMach2"));
		this.add(spin, "wrap");

		final JCheckBox autoOpenDesignFile = new JCheckBox(
				trans.get("pref.dlg.but.openlast"));
		autoOpenDesignFile.setSelected(preferences
				.isAutoOpenLastDesignOnStartupEnabled());
		autoOpenDesignFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoOpenLastDesignOnStartup(autoOpenDesignFile
						.isSelected());
			}
		});
		this.add(autoOpenDesignFile, "wrap, growx, span 2");

		// // Update flight estimates in the design window
		final JCheckBox updateEstimates = new JCheckBox(
				trans.get("pref.dlg.checkbox.Updateestimates"));
		updateEstimates.setSelected(preferences.computeFlightInBackground());
		updateEstimates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setComputeFlightInBackground(updateEstimates
						.isSelected());
			}
		});
		this.add(updateEstimates, "wrap, growx, sg combos ");

	}
}
