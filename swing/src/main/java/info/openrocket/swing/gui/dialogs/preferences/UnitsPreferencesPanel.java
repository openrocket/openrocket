package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class UnitsPreferencesPanel extends PreferencesPanel {

	public UnitsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("", "[]40lp[]"));
		JComboBox<?> combo;
		JPanel leftPanel = new JPanel(new MigLayout("ins 0"));
		JPanel rightPanel = new JPanel(new MigLayout("ins 0"));

		//// Select your preferred units:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Selectprefunits")), "span, wrap paragraph");


		// -------------- LEFT PANEL

		//// Rocket dimensions:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Rocketdimensions")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_LENGTH));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Motor dimensions:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Motordimensions")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_MOTOR_DIMENSIONS));
		leftPanel.add(combo, "sizegroup boxes, wrap");
		
		//// Distance:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Distance")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DISTANCE));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Velocity:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Velocity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_VELOCITY));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Acceleration:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Acceleration")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ACCELERATION));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Mass:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Mass")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_MASS));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Force:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Force")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_FORCE));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Total impulse:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Totalimpulse")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_IMPULSE));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Moment of inertia:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Momentofinertia")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_INERTIA));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Stability:
		leftPanel.add(new JLabel(trans.get("pref.dlg.lbl.Stability")));
		combo = new JComboBox<>(new DefaultUnitSelector(UnitGroup.UNITS_STABILITY));
		leftPanel.add(combo, "sizegroup boxes, wrap");

		//// Secondary stability:
		final JLabel labelSecStab = new JLabel(trans.get("pref.dlg.lbl.SecondaryStability"));
		labelSecStab.setToolTipText(trans.get("pref.dlg.lbl.SecondaryStability.ttip"));
		labelSecStab.setEnabled(preferences.isDisplaySecondaryStability());
		leftPanel.add(labelSecStab);
		final JComboBox<?> comboSecStab = new JComboBox<>(new DefaultUnitSelector(UnitGroup.UNITS_SECONDARY_STABILITY));
		comboSecStab.setToolTipText(trans.get("pref.dlg.lbl.SecondaryStability.ttip"));
		comboSecStab.setEnabled(preferences.isDisplaySecondaryStability());
		leftPanel.add(comboSecStab, "sizegroup boxes, wrap");

		//// Display secondary stability unit:
		JCheckBox displaySecondary = new JCheckBox(trans.get("pref.dlg.checkbox.DisplaySecondaryStability"));
		displaySecondary.setToolTipText(trans.get("pref.dlg.checkbox.DisplaySecondaryStability.ttip"));
		displaySecondary.setSelected(preferences.isDisplaySecondaryStability());
		displaySecondary.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				preferences.setDisplaySecondaryStability(e.getStateChange() == ItemEvent.SELECTED);
				labelSecStab.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				comboSecStab.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		leftPanel.add(displaySecondary, "spanx, wrap");


		// -------------- RIGHT PANEL

		//// Line density:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Linedensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_LINE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Surface density:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Surfacedensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_SURFACE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Bulk density::
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Bulkdensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_BULK));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Surface roughness:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Surfaceroughness")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ROUGHNESS));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Area:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Area")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_AREA));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Angle:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Angle")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ANGLE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Roll rate:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Rollrate")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ROLL));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Temperature:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Temperature")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_TEMPERATURE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Pressure:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Pressure")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_PRESSURE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Windspeed:
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Windspeed")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_WINDSPEED));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Latitude
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Latitude")));
		combo = new JComboBox<>(new DefaultUnitSelector(UnitGroup.UNITS_LATITUDE));
		rightPanel.add(combo, "sizegroup boxes, wrap");

		//// Longitude
		rightPanel.add(new JLabel(trans.get("pref.dlg.lbl.Longitude")));
		combo = new JComboBox<>(new DefaultUnitSelector(UnitGroup.UNITS_LONGITUDE));
		rightPanel.add(combo, "sizegroup boxes, wrap");


		this.add(leftPanel, "top");
		this.add(rightPanel, "top, wrap para");
		
		
		//// Default metric button
		JButton button = new SelectColorButton(trans.get("pref.dlg.but.defaultmetric"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultMetricUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		this.add(button, "spanx, split 2, grow");
		
		//// Default imperial button
		button = new SelectColorButton(trans.get("pref.dlg.but.defaultimperial"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultImperialUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		this.add(button, "grow, wrap para");
		
		//// The effects will take place the next time you open a window.
		this.add(new StyledLabel(
				trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
				"spanx, wrap");
		
	}

	public UnitsPreferencesPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

}
