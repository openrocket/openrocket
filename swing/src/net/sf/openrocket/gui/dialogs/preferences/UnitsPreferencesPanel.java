package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.unit.UnitGroup;

public class UnitsPreferencesPanel extends PreferencesPanel {

	public UnitsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("", "[][]40lp[][]"));
		JComboBox<?> combo;
		
		//// Select your preferred units:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Selectprefunits")), "span, wrap paragraph");
		
		
		//// Rocket dimensions:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Rocketdimensions")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_LENGTH));
		this.add(combo, "sizegroup boxes");
		
		//// Line density:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Linedensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_LINE));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Motor dimensions:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Motordimensions")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_MOTOR_DIMENSIONS));
		this.add(combo, "sizegroup boxes");
		
		//// Surface density:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Surfacedensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_SURFACE));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Distance:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Distance")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DISTANCE));
		this.add(combo, "sizegroup boxes");
		
		//// Bulk density::
		this.add(new JLabel(trans.get("pref.dlg.lbl.Bulkdensity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_BULK));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Velocity:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Velocity")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_VELOCITY));
		this.add(combo, "sizegroup boxes");
		
		//// Surface roughness:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Surfaceroughness")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ROUGHNESS));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Acceleration:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Acceleration")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ACCELERATION));
		this.add(combo, "sizegroup boxes");
		
		//// Area:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Area")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_AREA));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Mass:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Mass")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_MASS));
		this.add(combo, "sizegroup boxes");
		
		//// Angle:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Angle")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ANGLE));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Force:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Force")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_FORCE));
		this.add(combo, "sizegroup boxes");
		
		//// Roll rate:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Rollrate")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_ROLL));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Total impulse:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Totalimpulse")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_IMPULSE));
		this.add(combo, "sizegroup boxes");
		
		//// Temperature:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Temperature")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_TEMPERATURE));
		this.add(combo, "sizegroup boxes, wrap");
		
		//// Moment of inertia:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Momentofinertia")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_INERTIA));
		this.add(combo, "sizegroup boxes");
		
		//// Pressure:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Pressure")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_PRESSURE));
		this.add(combo, "sizegroup boxes, wrap");
		
		
		//// Stability:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Stability")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_STABILITY));
		this.add(combo, "sizegroup boxes");
		
		//// Windspeed:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Windspeed")));
		combo = new JComboBox<Object>(new DefaultUnitSelector(UnitGroup.UNITS_WINDSPEED));
		this.add(combo, "sizegroup boxes, wrap para");
		
		
		
		
		//// Default metric button
		JButton button = new JButton(trans.get("pref.dlg.but.defaultmetric"));
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
		button = new JButton(trans.get("pref.dlg.but.defaultimperial"));
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
