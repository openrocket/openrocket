package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.itextpdf.text.Font;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
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
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.gui.widgets.SelectColorButton;

public abstract class MotorFilterPanel extends JPanel {
	private static final long serialVersionUID = -2068101000195158181L;

	private static final Translator trans = Application.getTranslator();

	private static final Hashtable<Integer,JLabel> diameterLabels = new Hashtable<Integer,JLabel>();
	private static final double[] motorDiameters = new double[] {
		0.0,
		0.013,
		0.018,
		0.024,
		0.029,
		0.038,
		0.054,
		0.075,
		0.098,
		1.000
	};

	/**
	 * updates: motorDiameters, diameterLabels
	 */
	private static void scaleDiameterLabels(){
		Unit unit = UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit();
		for( int i = 0; i < motorDiameters.length; i++ ) {
			// Round the labels, because for imperial units, the labels can otherwise overlap
			double diam = unit.toUnit(motorDiameters[i]);
			double diamRounded = unit.round(diam * 10) / 10;	// 10 multiplication for 2-decimal precision
			diam = unit.fromUnit(diamRounded);
			String formatted = unit.toString(diam);
			// Remove the leading zero for numbers between 0 and 1
			if (diamRounded > 0 && diamRounded < 1) {
				formatted = formatted.substring(1);
			}
			diameterLabels.put( i, new JLabel(formatted));
		}
		diameterLabels.get( motorDiameters.length-1).setText("+");
	}

	final private static Hashtable<Integer,JLabel> impulseLabels = new Hashtable<Integer,JLabel>();
	static {
		int i =0;
		for( ImpulseClass impulseClass : ImpulseClass.values() ) {
			impulseLabels.put(i, new JLabel( impulseClass.name() ));
			i++;
		}
	}

	private final CheckList<Manufacturer> manufacturerCheckList;

	private final MotorRowFilter filter;

	private final JCheckBox limitByLengthCheckBox; 
	//private final BooleanModel limitByLengthModel;
	//private boolean limitLength = false;
	private Double mountLength = null;
	
	private final JCheckBox limitDiameterCheckBox;
	final DoubleModel minLengthModel;
	final JSpinner minLengthSpinner;
	final UnitSelector minLengthUnitSelect;
	final DoubleModel maxLengthModel;
	final JSpinner maxLengthSpinner;
	final UnitSelector maxLengthUnitSelect;
	private boolean limitDiameter = false;
	boolean limitByLength = false;
	private Double mountDiameter = null;


	// Things we change the label on based on the MotorMount.
	private final JLabel motorMountDimension;
	private final MultiSlider lengthSlider;
	private final MultiSlider diameterSlider;

	public MotorFilterPanel(Collection<Manufacturer> allManufacturers, MotorRowFilter filter ) {
		super(new MigLayout("fill", "[grow]"));
		this.filter = filter;

		scaleDiameterLabels();

		List<Manufacturer> unselectedManusFromPreferences = ((SwingPreferences) Application.getPreferences()).getExcludedMotorManufacturers();
		filter.setExcludedManufacturers(unselectedManusFromPreferences);

		limitByLength = ((SwingPreferences) Application.getPreferences()).getBoolean("motorFilterLimitLength", false);
		limitDiameter = ((SwingPreferences) Application.getPreferences()).getBoolean("motorFilterLimitDiameter", false);
		
		//// Hide used motor files
		{
			final JCheckBox hideUsedBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideUsed"));
			GUIUtil.changeFontSize(hideUsedBox, -1);
			hideUsedBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MotorFilterPanel.this.filter.setHideUsedMotors(hideUsedBox.isSelected());
					onSelectionChanged();
				}
			});
			this.add(hideUsedBox, "gapleft para, spanx, growx, wrap");
		}

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
				Collection<Manufacturer> uncheckedManufacturers = manufacturerCheckList.getUncheckedItems();
				MotorFilterPanel.this.filter.setExcludedManufacturers( uncheckedManufacturers );
				((SwingPreferences) Application.getPreferences()).setExcludedMotorManufacturers(uncheckedManufacturers);
				onSelectionChanged();
			}
		});

		sub.add(new JScrollPane(manufacturerCheckList.getList()), "grow, pushy, wrap");

		JButton clearMotors = new SelectColorButton(trans.get("TCMotorSelPan.btn.checkNone"));
		clearMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.manufacturerCheckList.clearAll();

			}
		});

		sub.add(clearMotors,"split 2");

		JButton selectMotors = new SelectColorButton(trans.get("TCMotorSelPan.btn.checkAll"));
		selectMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.manufacturerCheckList.checkAll();

			}
		});

		sub.add(selectMotors,"wrap");

		this.add(sub,"grow, wrap");

		// Total Impulse selection
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


		// Motor Dimensions
		sub = new JPanel(new MigLayout("fill"));
		TitledBorder diameterTitleBorder = BorderFactory.createTitledBorder(trans.get("TCMotorSelPan.MotorSize"));
		GUIUtil.changeFontStyle(diameterTitleBorder, Font.BOLD);
		sub.setBorder(diameterTitleBorder);

		motorMountDimension = new JLabel();
		GUIUtil.changeFontSize(motorMountDimension, -1);
		sub.add(motorMountDimension,"growx,wrap");

		// Motor Dimension selection
		{
			sub.add( new JLabel(trans.get("TCMotorSelPan.Diameter")), "split 2, wrap");
			final BooleanModel limitByDiameterModel = new BooleanModel(limitDiameter);
			limitDiameterCheckBox = new JCheckBox(limitByDiameterModel);
			limitDiameterCheckBox.setText(trans.get("TCMotorSelPan.checkbox.limitdiameter"));
			GUIUtil.changeFontSize(limitDiameterCheckBox, -1);
			limitDiameterCheckBox.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					limitDiameter = limitDiameterCheckBox.isSelected();
					MotorFilterPanel.this.setLimitDiameter();
					onSelectionChanged();
				}
			});
			sub.add( limitDiameterCheckBox, "gapleft para, spanx, growx, wrap" );

			diameterSlider = new MultiSlider(MultiSlider.HORIZONTAL,0, diameterLabels.size()-1, 0, diameterLabels.size()-1);
			diameterSlider.setBounded(true); // thumbs cannot cross
			diameterSlider.setMajorTickSpacing(1);
			diameterSlider.setPaintTicks(true);
			diameterSlider.setLabelTable(diameterLabels);
			diameterSlider.setPaintLabels(true);
			diameterSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					final int minDiameter = diameterSlider.getValueAt(0);
					MotorFilterPanel.this.filter.setMinimumDiameter(motorDiameters[minDiameter]);
					int maxDiameter = diameterSlider.getValueAt(1);
					if( maxDiameter == motorDiameters.length-1 ) {
						MotorFilterPanel.this.filter.setMaximumDiameter(null);
					} else {
						MotorFilterPanel.this.filter.setMaximumDiameter(motorDiameters[maxDiameter]);
					}
					onSelectionChanged();
				}
			});
			sub.add( diameterSlider, "growx, wrap");
			limitByDiameterModel.addEnableComponent(diameterSlider, false);
		}

		{ // length selection
			
			sub.add( new JLabel(trans.get("TCMotorSelPan.Length")), "split 2, wrap");
			final BooleanModel limitByLengthModel = new BooleanModel(limitByLength);
			limitByLengthCheckBox = new JCheckBox( limitByLengthModel );
			limitByLengthCheckBox.setText( trans.get("TCMotorSelPan.checkbox.limitlength"));
			GUIUtil.changeFontSize(limitByLengthCheckBox, -1);
			
			limitByLengthCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					limitByLength = limitByLengthCheckBox.isSelected();
					MotorFilterPanel.this.setLimitLength();
					onSelectionChanged();
				}
			});

			sub.add( limitByLengthCheckBox, "gapleft para, spanx, growx, wrap" );
			
			
			minLengthModel = new DoubleModel(filter, "MinimumLength", UnitGroup.UNITS_MOTOR_DIMENSIONS, 0);
			maxLengthModel = new DoubleModel(filter, "MaximumLength", UnitGroup.UNITS_MOTOR_DIMENSIONS, 0);

			minLengthSpinner = new JSpinner(minLengthModel.getSpinnerModel());
			minLengthSpinner.setEditor(new SpinnerEditor(minLengthSpinner));
			minLengthModel.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					lengthSlider.setValueAt(0, (int)(1000* minLengthModel.getValue()));
				}
			});
			sub.add(minLengthSpinner, "split 5, growx");
			limitByLengthModel.addEnableComponent(minLengthSpinner,false);
			minLengthUnitSelect = new UnitSelector(minLengthModel);
			sub.add(minLengthUnitSelect, "");
			
			maxLengthSpinner = new JSpinner(maxLengthModel.getSpinnerModel());
			maxLengthSpinner.setEditor(new SpinnerEditor(maxLengthSpinner));
			maxLengthModel.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					lengthSlider.setValueAt(1, (int) (1000* maxLengthModel.getValue()));
				}
			});
			sub.add(maxLengthSpinner, "growx");
			limitByLengthModel.addEnableComponent(maxLengthSpinner,false);
			maxLengthUnitSelect = new UnitSelector(maxLengthModel);
			sub.add(maxLengthUnitSelect, "wrap");
			
			lengthSlider = new MultiSlider(MultiSlider.HORIZONTAL,0, 1000, 0, 1000);
			lengthSlider.setBounded(true); // thumbs cannot cross
			lengthSlider.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					
					int minLength = lengthSlider.getValueAt(0);
					minLengthModel.setValue(minLength/1000.0);
					int maxLength = lengthSlider.getValueAt(1);
					maxLengthModel.setValue(maxLength/1000.0);
					onSelectionChanged();
				}
			});

			sub.add( lengthSlider, "growx,wrap");
			limitByLengthModel.addEnableComponent(lengthSlider,false);
			
		}
		this.add(sub, "grow,wrap");

	}

	public void setMotorMount( MotorMount mount ) {
		filter.setMotorMount(mount);
		onSelectionChanged();
		if ( mount == null ) {
			// Disable diameter controls?
			mountLength = null;
			mountDiameter = null;
			motorMountDimension.setText("");
		} else {
			mountLength = ((RocketComponent)mount).getLength();
			mountDiameter = mount.getMotorMountDiameter();
			motorMountDimension.setText( trans.get("TCMotorSelPan.MotorMountDimensions") + " " +
					UnitGroup.UNITS_MOTOR_DIMENSIONS.toStringUnit(mountDiameter)+ " x " + UnitGroup.UNITS_MOTOR_DIMENSIONS.toStringUnit(mountLength));
		}
		setLimitLength();
		setLimitDiameter();
	}

	private void setLimitLength( ) {
		minLengthUnitSelect.setSelectedUnit(UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit());
		maxLengthUnitSelect.setSelectedUnit(UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit());

		((SwingPreferences) Application.getPreferences()).putBoolean("motorFilterLimitLength", limitByLength);
		if ( mountLength != null  & limitByLength ) {
			lengthSlider.setValueAt(1, (int) Math.min(1000,Math.round(1000*mountLength)));
		}
	}
	
	private void setLimitDiameter( ) {
		((SwingPreferences) Application.getPreferences()).putBoolean("motorFilterLimitDiameter", limitDiameter);

		//  motorDiameters, diameterLabels =
		scaleDiameterLabels();
		diameterSlider.setLabelTable(diameterLabels);

		if ( limitDiameter && mountDiameter != null) {
			// find the next largest diameter
			int i;
			for( i =0; i < motorDiameters.length; i++ ) {
				if ( mountDiameter < motorDiameters[i] - 0.0005 ) {
					break;
				}
			}
			if (i >= motorDiameters.length ) {
				i--;
			}
			diameterSlider.setValueAt(1, i-1);
		}
	}

	void setHideUnavailable( boolean hideUnavailable ) {
		this.filter.setHideUnavailable(hideUnavailable);
		onSelectionChanged();
	}
	
	public abstract void onSelectionChanged();

}
