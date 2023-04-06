package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.gui.widgets.SelectColorButton;

public class UnitsPreferencesPanel extends PreferencesPanel {

	public UnitsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("", "[][]40lp[][]"));
		JComboBox<?> combo;

		// Create left and right panels
		JPanel leftPanel = new JPanel(new MigLayout("", "[][]"));
		JPanel rightPanel = new JPanel(new MigLayout("", "[][]"));
		
		//// Select your preferred units:
		this.add(new JLabel(trans.get("pref.dlg.lbl.Selectprefunits")), "span, wrap paragraph");


		// Add widgets to the left panel
		String[] leftLabels = {
				"Rocketdimensions", "Motordimensions", "Distance", "Velocity",
				"Acceleration", "Mass", "Force", "Totalimpulse",
				"Momentofinertia", "Stability", "Linedensity"
		};
		UnitGroup[] leftUnitGroups = {
				UnitGroup.UNITS_LENGTH, UnitGroup.UNITS_MOTOR_DIMENSIONS, UnitGroup.UNITS_DISTANCE,
				UnitGroup.UNITS_VELOCITY, UnitGroup.UNITS_ACCELERATION, UnitGroup.UNITS_MASS,
				UnitGroup.UNITS_FORCE, UnitGroup.UNITS_IMPULSE, UnitGroup.UNITS_INERTIA,
				UnitGroup.UNITS_STABILITY, UnitGroup.UNITS_DENSITY_LINE
		};

		for (int i = 0; i < leftLabels.length; i++) {
			leftPanel.add(new JLabel(trans.get("pref.dlg.lbl." + leftLabels[i])));
			combo = new JComboBox<>(new DefaultUnitSelector(leftUnitGroups[i]));
			leftPanel.add(combo, "sizegroup boxes, wrap");
		}

		// Add widgets to the right panel
		String[] rightLabels = {
				"Surfacedensity", "Bulkdensity", "Surfaceroughness",
				"Area", "Angle", "Rollrate", "Temperature",
				"Pressure", "Windspeed", "Latitude", "Longitude"
		};
		UnitGroup[] rightUnitGroups = {
				UnitGroup.UNITS_DENSITY_SURFACE, UnitGroup.UNITS_DENSITY_BULK,
				UnitGroup.UNITS_ROUGHNESS, UnitGroup.UNITS_AREA, UnitGroup.UNITS_ANGLE,
				UnitGroup.UNITS_ROLL, UnitGroup.UNITS_TEMPERATURE, UnitGroup.UNITS_PRESSURE,
				UnitGroup.UNITS_WINDSPEED, UnitGroup.UNITS_LATITUDE, UnitGroup.UNITS_LONGITUDE
		};

		for (int i = 0; i < rightLabels.length; i++) {
			rightPanel.add(new JLabel(trans.get("pref.dlg.lbl." + rightLabels[i])));
			combo = new JComboBox<>(new DefaultUnitSelector(rightUnitGroups[i]));
			rightPanel.add(combo, "sizegroup boxes, wrap");
		}

		// Add left and right panels to the UnitsPreferencesPanel
		this.add(leftPanel);
		this.add(rightPanel, "wrap para");
		
		
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
