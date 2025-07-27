package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.core.unit.UnitGroup;

public class DesignPreferencesPanel extends PreferencesPanel {

	public DesignPreferencesPanel() {
		super(new MigLayout("fillx, ins 30lp n n n"));

		// // Position to insert new body components:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Positiontoinsert")),
				"gapright para");
		this.add(
				new JComboBox<>(new PrefChoiceSelector(
						ApplicationPreferences.BODY_COMPONENT_INSERT_POSITION_KEY,
						// // Always ask
						// // Insert in middle
						// // Add to end
						trans.get("pref.dlg.PrefChoiseSelector1"), trans
								.get("pref.dlg.PrefChoiseSelector2"), trans
								.get("pref.dlg.PrefChoiseSelector3"))),
				"wrap para, growx, sg combos");

		// // Position to insert new stages:
		this.add(new JLabel(trans.get("pref.dlg.lbl.PositiontoinsertStages")),
				"gapright para");
		this.add(
				new JComboBox<>(new PrefChoiceSelector(
						ApplicationPreferences.STAGE_INSERT_POSITION_KEY,
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
				new JComboBox<>(new PrefChoiceSelector(
						ApplicationPreferences.ROCKET_INFO_FONT_SIZE,
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

		DoubleModel m = new DoubleModel(preferences, "DefaultMach", 1.0,
				UnitGroup.UNITS_COEFFICIENT, 0.1, 0.9);

		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		this.add(spin, "wrap");

		// // Always open leftmost tab when opening a component edit dialog
		final JCheckBox alwaysOpenLeftmostTab = new JCheckBox(
				trans.get("pref.dlg.checkbox.AlwaysOpenLeftmost"));
		alwaysOpenLeftmostTab.setSelected(preferences.isAlwaysOpenLeftmostTab());
		alwaysOpenLeftmostTab.setToolTipText(trans.get("pref.dlg.checkbox.AlwaysOpenLeftmost.ttip"));
		alwaysOpenLeftmostTab.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				preferences.setAlwaysOpenLeftmostTab(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		this.add(alwaysOpenLeftmostTab, "wrap, growx, spanx");

		// // Show confirmation dialog for discarding component configuration changes
		final JCheckBox showDiscardConfirmation = new JCheckBox(
				trans.get("pref.dlg.checkbox.ShowDiscardConfirmation"));
		showDiscardConfirmation.setSelected(preferences.isShowDiscardConfirmation());
		showDiscardConfirmation.setToolTipText(trans.get("pref.dlg.checkbox.ShowDiscardConfirmation.ttip"));
		showDiscardConfirmation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				preferences.setShowDiscardConfirmation(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		this.add(showDiscardConfirmation, "wrap, growx, spanx");

		// // Show confirmation dialog for discarding simulation configuration changes
		final JCheckBox showDiscardSimulationConfirmation = new JCheckBox(
				trans.get("pref.dlg.checkbox.ShowDiscardSimulationConfirmation"));
		showDiscardSimulationConfirmation.setSelected(preferences.isShowDiscardSimulationConfirmation());
		showDiscardSimulationConfirmation.setToolTipText(trans.get("pref.dlg.checkbox.ShowDiscardSimulationConfirmation.ttip"));
		showDiscardSimulationConfirmation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				preferences.setShowDiscardSimulationConfirmation(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		this.add(showDiscardSimulationConfirmation, "wrap, growx, spanx");

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

		// // Only show pod set/booster markers when they are selected
		final JCheckBox showMarkers = new JCheckBox(
				trans.get("pref.dlg.checkbox.Markers"));
		showMarkers.setToolTipText(trans.get("pref.dlg.checkbox.Markers.ttip"));
		showMarkers.setSelected(preferences.isShowMarkers());
		showMarkers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setShowMarkers(showMarkers
						.isSelected());
				// Update all BasicFrame rocket panel figures because it can change due to the preference change
				for (BasicFrame frame : BasicFrame.getAllFrames()) {
					frame.getRocketPanel().updateFigures();
				}
			}
		});
		this.add(showMarkers, "wrap, growx, spanx");
	}
}
