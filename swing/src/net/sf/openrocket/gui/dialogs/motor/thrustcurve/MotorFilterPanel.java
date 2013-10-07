package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.CheckList;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.widgets.MultiSlider;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import com.itextpdf.text.Font;

public abstract class MotorFilterPanel extends JPanel {

	private static final Translator trans = Application.getTranslator();

	private static Hashtable diameterLabels = new Hashtable();
	private static double[] diameterValues = new double[] {
		0,
		.013,
		.018,
		.024,
		.029,
		.038,
		.054,
		.075,
		.098,
		1.000
	};
	static {
		for( int i = 0; i< diameterValues.length; i++ ) {
			if( i == diameterValues.length-1) {
				diameterLabels.put( i, new JLabel("+"));
			} else {
				diameterLabels.put( i, new JLabel(UnitGroup.UNITS_MOTOR_DIMENSIONS.toString(diameterValues[i])));
			}
		}
	}

	private static Hashtable impulseLabels = new Hashtable();
	static {
		int i =0;
		for( ImpulseClass impulseClass : ImpulseClass.values() ) {
			impulseLabels.put(i, new JLabel( impulseClass.name() ));
			i++;
		}
	}

	private final CheckList<Manufacturer> manufacturerCheckList;

	private final MotorRowFilter filter;

	// Things we change the label on based on the MotorMount.
	private final JCheckBox maximumLengthCheckBox;
	private final MultiSlider diameterSlider;

	private Double mountLength;

	public MotorFilterPanel(Collection<Manufacturer> allManufacturers, MotorRowFilter filter ) {
		super(new MigLayout("fill", "[grow]"));
		this.filter = filter; 

		List<Manufacturer> unselectedManusFromPreferences = ((SwingPreferences) Application.getPreferences()).getExcludedMotorManufacturers();
		filter.setExcludedManufacturers(unselectedManusFromPreferences);

		// Manufacturer selection
		JPanel sub = new JPanel(new MigLayout("fill"));
		TitledBorder border = BorderFactory.createTitledBorder(trans.get("TCurveMotorCol.MANUFACTURER"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);

		this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		List<Manufacturer> manufacturers = new ArrayList<Manufacturer>();
		for (Manufacturer m : allManufacturers) {
			manufacturers.add(m);
		}

		Collections.sort(manufacturers, new Comparator<Manufacturer>() {
			@Override
			public int compare(Manufacturer o1, Manufacturer o2) {
				return o1.getSimpleName().compareTo( o2.getSimpleName());
			}

		});

		manufacturerCheckList = new CheckList.Builder().<Manufacturer>build();
		manufacturerCheckList.setData(manufacturers);

		manufacturerCheckList.setUncheckedItems(unselectedManusFromPreferences);
		manufacturerCheckList.getModel().addListDataListener( new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				MotorFilterPanel.this.filter.setExcludedManufacturers( manufacturerCheckList.getUncheckedItems() );
				onSelectionChanged();
			}
		});

		sub.add(new JScrollPane(manufacturerCheckList.getList()), "grow,wrap");

		JButton clearMotors = new JButton(trans.get("TCMotorSelPan.btn.checkNone"));
		clearMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.manufacturerCheckList.clearAll();

			}
		});

		sub.add(clearMotors,"split 2");

		JButton selectMotors = new JButton(trans.get("TCMotorSelPan.btn.checkAll"));
		selectMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.manufacturerCheckList.checkAll();

			}
		});

		sub.add(selectMotors,"wrap");

		this.add(sub,"grow, wrap");

		// Impulse selection
		{
			sub = new JPanel(new MigLayout("fill"));
			border = BorderFactory.createTitledBorder(trans.get("TCurveMotorCol.TOTAL_IMPULSE"));
			GUIUtil.changeFontStyle(border, Font.BOLD);
			sub.setBorder(border);

			final MultiSlider impulseSlider = new MultiSlider(MultiSlider.HORIZONTAL,0, ImpulseClass.values().length-1,0, ImpulseClass.values().length-1);
			impulseSlider.setBounded(true); // thumbs cannot cross
			impulseSlider.setMajorTickSpacing(1);
			impulseSlider.setPaintTicks(true);
			impulseSlider.setLabelTable(impulseLabels);
			impulseSlider.setPaintLabels(true);
			impulseSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					int minimpulse = impulseSlider.getValueAt(0);
					MotorFilterPanel.this.filter.setMinimumImpulse(ImpulseClass.values()[minimpulse]);
					int maximpulse = impulseSlider.getValueAt(1);
					MotorFilterPanel.this.filter.setMaximumImpulse(ImpulseClass.values()[maximpulse]);
					onSelectionChanged();
				}
			});
			sub.add( impulseSlider, "growx, wrap");
		}
		this.add(sub,"grow, wrap");
		
		
		// Diameter selection
		sub = new JPanel(new MigLayout("fill"));
		TitledBorder diameterTitleBorder = BorderFactory.createTitledBorder(trans.get("TCMotorSelPan.MotorSize"));
		GUIUtil.changeFontStyle(diameterTitleBorder, Font.BOLD);
		sub.setBorder(diameterTitleBorder);

		{
			sub.add( new JLabel("Minimum diameter"), "split 2, wrap");
			diameterSlider = new MultiSlider(MultiSlider.HORIZONTAL,0, diameterValues.length-1, 0, diameterValues.length-1);
			diameterSlider.setBounded(true); // thumbs cannot cross
			diameterSlider.setMajorTickSpacing(1);
			diameterSlider.setPaintTicks(true);
			diameterSlider.setLabelTable(diameterLabels);
			diameterSlider.setPaintLabels(true);
			diameterSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					int minDiameter = diameterSlider.getValueAt(0);
					MotorFilterPanel.this.filter.setMinimumDiameter(diameterValues[minDiameter]);
					int maxDiameter = diameterSlider.getValueAt(1);
					if( maxDiameter == diameterValues.length-1 ) {
						MotorFilterPanel.this.filter.setMaximumDiameter(null);
					} else {
						MotorFilterPanel.this.filter.setMaximumDiameter(diameterValues[maxDiameter]);
					}
					onSelectionChanged();
				}
			});
			sub.add( diameterSlider, "growx, wrap");
		}

		{
			maximumLengthCheckBox = new JCheckBox(trans.get("TCMotorSelPan.limitByLength"));
			maximumLengthCheckBox.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (maximumLengthCheckBox.isSelected() ) {
						MotorFilterPanel.this.filter.setMaximumLength( mountLength );
					} else {
						MotorFilterPanel.this.filter.setMaximumLength(null);
					}
					onSelectionChanged();
				}

			});
			sub.add(maximumLengthCheckBox);
		}
		this.add(sub, "grow,wrap");

	}

	public void setMotorMount( MotorMount mount ) {
		filter.setMotorMount(mount);
		onSelectionChanged();
		if ( mount == null ) {
			// Disable diameter controls?
			maximumLengthCheckBox.setText("Limit by length");
			mountLength = null;
		} else {
			mountLength = ((RocketComponent)mount).getLength();
			double mountDiameter = mount.getMotorMountDiameter();
			// find the next largest diameter
			int i;
			for( i =0; i< diameterValues.length; i++ ) {
				if ( mountDiameter<= diameterValues[i] ) {
					break;
				}
			}
			if( i >= diameterValues.length-1 ) {
				diameterSlider.setValueAt(1, diameterValues.length-1);
			} else {
				diameterSlider.setValueAt(1, i) ;
			}
			diameterSlider.setValueAt(1, i);
			maximumLengthCheckBox.setText("Limit by length"
					+ " (" + UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(((RocketComponent)mount).getLength()) +")");

		}
	}

	public abstract void onSelectionChanged();

}
