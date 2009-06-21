package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

public class PreferencesDialog extends JDialog {
	
	private final List<DefaultUnitSelector> unitSelectors = new ArrayList<DefaultUnitSelector>();

	private PreferencesDialog() {
		super((JFrame)null, "Preferences", true);
		
		JPanel panel = new JPanel(new MigLayout("fill, gap unrel","[grow]","[grow][]"));
				
		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane,"grow, wrap");
		

		tabbedPane.addTab("Units", null, unitsPane(), "Default units");
		tabbedPane.addTab("Confirmation", null, confirmationPane(), "Confirmation dialog settings");
		
		
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PreferencesDialog.this.setVisible(false);
				PreferencesDialog.this.dispose();
			}
		});
		panel.add(close,"span, right, tag close");
		
		this.setContentPane(panel);
		pack();
		setAlwaysOnTop(true);
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Prefs.storeDefaultUnits();
			}
		});

		GUIUtil.setDefaultButton(close);
		GUIUtil.installEscapeCloseOperation(this);
	}
	
	
	private JPanel confirmationPane() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel("Position to insert new body components:"));
		panel.add(new JComboBox(new PrefChoiseSelector(Prefs.BODY_COMPONENT_INSERT_POSITION_KEY,
				"Always ask", "Insert in middle", "Add to end")), "wrap para, sg combos");
		
		panel.add(new JLabel("Confirm deletion of simulations:"));
		panel.add(new JComboBox(new PrefBooleanSelector(Prefs.CONFIRM_DELETE_SIMULATION,
				"Delete", "Confirm", true)), "wrap para, sg combos");
		
		return panel;
	}
	
	private JPanel unitsPane() {
		JPanel panel = new JPanel(new MigLayout("", "[][]40lp[][]"));
		JComboBox combo;
		
		panel.add(new JLabel("Select your preferred units:"), "span, wrap paragraph");
		
/*
		public static final UnitGroup UNITS_LENGTH;
		public static final UnitGroup UNITS_MOTOR_DIMENSIONS;
		public static final UnitGroup UNITS_DISTANCE;
		
		public static final UnitGroup UNITS_VELOCITY;
		public static final UnitGroup UNITS_ACCELERATION;
		public static final UnitGroup UNITS_MASS;
		public static final UnitGroup UNITS_FORCE;
		public static final UnitGroup UNITS_IMPULSE;

		public static final UnitGroup UNITS_STABILITY;
		public static final UnitGroup UNITS_FLIGHT_TIME;
		public static final UnitGroup UNITS_ROLL;
		
		public static final UnitGroup UNITS_AREA;
		public static final UnitGroup UNITS_DENSITY_LINE;
		public static final UnitGroup UNITS_DENSITY_SURFACE;
		public static final UnitGroup UNITS_DENSITY_BULK;
		public static final UnitGroup UNITS_ROUGHNESS;
		
		public static final UnitGroup UNITS_TEMPERATURE;
		public static final UnitGroup UNITS_PRESSURE;
		public static final UnitGroup UNITS_ANGLE;
*/
		
		panel.add(new JLabel("Rocket dimensions:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_LENGTH));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Line density:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_LINE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		
		panel.add(new JLabel("Motor dimensions:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MOTOR_DIMENSIONS));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Surface density:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_SURFACE));
		panel.add(combo, "sizegroup boxes, wrap");
		

		
		panel.add(new JLabel("Distance:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DISTANCE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Bulk density::"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_BULK));
		panel.add(combo, "sizegroup boxes, wrap");
		

		
		panel.add(new JLabel("Velocity:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_VELOCITY));
		panel.add(combo, "sizegroup boxes");

		panel.add(new JLabel("Surface roughness:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROUGHNESS));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		
		panel.add(new JLabel("Acceleration:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ACCELERATION));
		panel.add(combo, "sizegroup boxes");

		panel.add(new JLabel("Area:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_AREA));
		panel.add(combo, "sizegroup boxes, wrap");
		
		

		panel.add(new JLabel("Mass:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MASS));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Angle:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ANGLE));
		panel.add(combo, "sizegroup boxes, wrap");
		

		
		panel.add(new JLabel("Force:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_FORCE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Roll rate:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROLL));
		panel.add(combo, "sizegroup boxes, wrap");
		

		
		panel.add(new JLabel("Total impulse:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_IMPULSE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Temperature:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_TEMPERATURE));
		panel.add(combo, "sizegroup boxes, wrap");
		

		
		panel.add(new JLabel("Stability:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_STABILITY));
		panel.add(combo, "sizegroup boxes");

		panel.add(new JLabel("Pressure:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_PRESSURE));
		panel.add(combo, "sizegroup boxes, wrap para");
		
		
		
		JButton button = new JButton("Default metric");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultMetricUnits();
				for (DefaultUnitSelector s: unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "spanx, split 2, grow");
		
		button = new JButton("Default imperial");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultImperialUnits();
				for (DefaultUnitSelector s: unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "grow, wrap para");
		
		
		panel.add(new ResizeLabel("The effects will take place the next time you open a window.",-2),
				"spanx, wrap");
		

		return panel;
	}
	
	
	
	
	private class DefaultUnitSelector extends AbstractListModel implements ComboBoxModel {
		
		private final UnitGroup group;
		public DefaultUnitSelector(UnitGroup group) {
			this.group = group;
			unitSelectors.add(this);
		}
		
		@Override
		public Object getSelectedItem() {
			return group.getDefaultUnit();
		}
		@Override
		public void setSelectedItem(Object item) {
			if (!(item instanceof Unit)) {
				throw new IllegalArgumentException("Illegal argument "+item);
			}
			group.setDefaultUnit(group.getUnitIndex((Unit)item));
		}
		@Override
		public Object getElementAt(int index) {
			return group.getUnit(index);
		}
		@Override
		public int getSize() {
			return group.getUnitCount();
		}
		
		
		public void fireChange() {
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}
	

	
	private class PrefChoiseSelector extends AbstractListModel implements ComboBoxModel {
		private final String preference;
		private final String[] descriptions;
		
		public PrefChoiseSelector(String preference, String ... descriptions) {
			this.preference = preference;
			this.descriptions = descriptions;
		}
		
		@Override
		public Object getSelectedItem() {
			return descriptions[Prefs.getChoise(preference, descriptions.length, 0)];
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument "+item);
			}
			int index;
			for (index = 0; index < descriptions.length; index++) {
				if (((String)item).equalsIgnoreCase(descriptions[index]))
					break;
			}
			if (index >= descriptions.length) {
				throw new IllegalArgumentException("Illegal argument "+item);
			}
			
			Prefs.putChoise(preference, index);
		}
		
		@Override
		public Object getElementAt(int index) {
			return descriptions[index];
		}
		@Override
		public int getSize() {
			return descriptions.length;
		}
	}
	

	private class PrefBooleanSelector extends AbstractListModel implements ComboBoxModel {
		private final String preference;
		private final String trueDesc, falseDesc;
		private final boolean def;
		
		public PrefBooleanSelector(String preference, String falseDescription, 
				String trueDescription, boolean defaultState) {
			this.preference = preference;
			this.trueDesc = trueDescription;
			this.falseDesc = falseDescription;
			this.def = defaultState;
		}
		
		@Override
		public Object getSelectedItem() {
			if (Prefs.NODE.getBoolean(preference, def)) {
				return trueDesc;
			} else {
				return falseDesc;
			}
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument "+item);
			}
			
			if (trueDesc.equals(item)) {
				Prefs.NODE.putBoolean(preference, true);
			} else if (falseDesc.equals(item)) {
				Prefs.NODE.putBoolean(preference, false);
			} else {
				throw new IllegalArgumentException("Illegal argument "+item);
			}
		}
		
		@Override
		public Object getElementAt(int index) {
			switch (index) {
			case 0:
				return def ? trueDesc : falseDesc;

			case 1:
				return def ? falseDesc: trueDesc;
				
			default:
				throw new IndexOutOfBoundsException("Boolean asked for index="+index);
			}
		}
		@Override
		public int getSize() {
			return 2;
		}
	}
	
	
	
	////////  Singleton implementation  ////////
	
	private static PreferencesDialog dialog = null;
	
	public static void showPreferences() {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog();
		dialog.setVisible(true);
	}
	
	
}
