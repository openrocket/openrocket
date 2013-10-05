package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import com.itextpdf.text.Font;

public abstract class MotorFilterPanel extends JPanel {

	private static final Translator trans = Application.getTranslator();

	private final CheckList<Manufacturer> manufacturerCheckList;

	private final CheckList<ImpulseClass> impulseCheckList;

	private final MotorRowFilter filter;
	private final TitledBorder diameterTitleBorder;
	private final DoubleModel mountDiameter = new DoubleModel(1);
	
	private int showMode = SHOW_ALL;

	private static final int SHOW_ALL = 0;
	private static final int SHOW_SMALLER = 1;
	private static final int SHOW_EXACT = 2;
	private static final int SHOW_MAX = 2;

	public MotorFilterPanel(Collection<Manufacturer> allManufacturers, MotorRowFilter filter ) {
		super(new MigLayout("fill", "[grow]"));
		this.filter = filter; 

		showMode = Application.getPreferences().getChoice(net.sf.openrocket.startup.Preferences.MOTOR_DIAMETER_FILTER, MotorFilterPanel.SHOW_MAX, MotorFilterPanel.SHOW_EXACT);
		switch( showMode ) {
		case SHOW_ALL:
			filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.ALL);
			break;
		case SHOW_EXACT:
			filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.EXACT);
			break;
		case SHOW_SMALLER:
			filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.SMALLER);
			break;
		}
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
		sub = new JPanel(new MigLayout("fill"));
		border = BorderFactory.createTitledBorder(trans.get("TCurveMotorCol.TOTAL_IMPULSE"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);

		impulseCheckList = new CheckList.Builder().<ImpulseClass>build();
		impulseCheckList.setData(Arrays.asList(ImpulseClass.values()));
		impulseCheckList.checkAll();
		impulseCheckList.getModel().addListDataListener( new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				MotorFilterPanel.this.filter.setExcludedImpulseClasses( impulseCheckList.getUncheckedItems() );
				onSelectionChanged();
			}

		});

		sub.add(new JScrollPane(impulseCheckList.getList()), "grow,wrap");

		JButton clearImpulse = new JButton(trans.get("TCMotorSelPan.btn.checkNone"));
		clearImpulse.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.impulseCheckList.clearAll();

			}
		});
		sub.add(clearImpulse,"split 2");

		JButton selectImpulse = new JButton(trans.get("TCMotorSelPan.btn.checkAll"));
		selectImpulse.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPanel.this.impulseCheckList.checkAll();

			}
		});
		sub.add(selectImpulse,"wrap");

		this.add(sub,"grow, wrap");

		// Diameter selection

		sub = new JPanel(new MigLayout("fill"));
		diameterTitleBorder = BorderFactory.createTitledBorder(trans.get("TCurveMotorCol.DIAMETER"));
		GUIUtil.changeFontStyle(diameterTitleBorder, Font.BOLD);
		sub.setBorder(diameterTitleBorder);

		JRadioButton showAllDiametersButton = new JRadioButton( trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc1") );
		showAllDiametersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMode = SHOW_ALL;
				MotorFilterPanel.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.ALL);
				saveMotorDiameterMatchPrefence();
				onSelectionChanged();
			}
		});
		showAllDiametersButton.setSelected( showMode == SHOW_ALL);
		sub.add(showAllDiametersButton, "growx,wrap");

		JRadioButton showSmallerDiametersButton = new JRadioButton( trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc2") );
		showSmallerDiametersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMode = SHOW_SMALLER;
				MotorFilterPanel.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.SMALLER);
				saveMotorDiameterMatchPrefence();
				onSelectionChanged();
			}
		});
		showSmallerDiametersButton.setSelected( showMode == SHOW_SMALLER);
		sub.add(showSmallerDiametersButton, "growx,wrap");

		JRadioButton showExactDiametersButton = new JRadioButton( trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc3") );
		showExactDiametersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMode = SHOW_EXACT;
				MotorFilterPanel.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.EXACT);
				saveMotorDiameterMatchPrefence();
				onSelectionChanged();
			}
		});
		showExactDiametersButton.setSelected( showMode == SHOW_EXACT );
		sub.add(showExactDiametersButton, "growx,wrap");
		ButtonGroup comboGroup = new ButtonGroup();
		comboGroup.add( showAllDiametersButton );
		comboGroup.add( showSmallerDiametersButton );
		comboGroup.add( showExactDiametersButton );

		{
			sub.add( new JLabel("Minimum diameter"), "split 4");
			final DoubleModel minDiameter = new DoubleModel(0, UnitGroup.UNITS_MOTOR_DIMENSIONS, 0, .2);
			minDiameter.addChangeListener( new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					MotorFilterPanel.this.filter.setMinimumDiameter(minDiameter.getValue());
					onSelectionChanged();
				}
			});
			JSpinner spin = new JSpinner(minDiameter.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			sub.add(spin, "growx");

			sub.add(new UnitSelector(minDiameter));
			sub.add(new BasicSlider(minDiameter.getSliderModel(0,0.5, mountDiameter)), "w 100lp, wrap");
		}
		this.add(sub, "grow,wrap");

	}

	public void setMotorMount( MotorMount mount ) {
		filter.setMotorMount(mount);
		onSelectionChanged();
		if ( mount == null ) {
			// Disable diameter controls?
			diameterTitleBorder.setTitle(trans.get("TCurveMotorCol.DIAMETER"));
			mountDiameter.setValue(1.0);
		} else {
			mountDiameter.setValue(mount.getMotorMountDiameter());
			diameterTitleBorder.setTitle(trans.get("TCurveMotorCol.DIAMETER") + " "
					+ trans.get("TCMotorSelPan.lbl.Motormountdia") + " " +
					UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(mount.getMotorMountDiameter()));

		}
	}

	private void saveMotorDiameterMatchPrefence() {
		Application.getPreferences().putChoice("MotorDiameterMatch", showMode );
	}

	public abstract void onSelectionChanged();

}
