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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.CheckList;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.startup.Application;

import com.itextpdf.text.Font;

public abstract class MotorFilterPopupMenu extends JPopupMenu {

	private static final Translator trans = Application.getTranslator();

	private final CheckList<Manufacturer> manufacturerCheckList;

	private final CheckList<ImpulseClass> impulseCheckList;

	private final MotorRowFilter filter;

	private int showMode = SHOW_ALL;

	private static final int SHOW_ALL = 0;
	private static final int SHOW_SMALLER = 1;
	private static final int SHOW_EXACT = 2;
	private static final int SHOW_MAX = 2;


	public MotorFilterPopupMenu(Collection<Manufacturer> allManufacturers, MotorRowFilter filter ) {

		this.filter = filter; 

		showMode = Application.getPreferences().getChoice(net.sf.openrocket.startup.Preferences.MOTOR_DIAMETER_FILTER, MotorFilterPopupMenu.SHOW_MAX, MotorFilterPopupMenu.SHOW_EXACT);
		List<Manufacturer> unselectedManusFromPreferences = ((SwingPreferences) Application.getPreferences()).getExcludedMotorManufacturers();
		
		// Manufacturer selection
		JPanel sub = new JPanel(new MigLayout("fill"));
		TitledBorder border = BorderFactory.createTitledBorder("Manufacturer");
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);

		JPanel root = new JPanel(new MigLayout("fill", "[grow]"));
		root.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

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
		filter.setExcludedManufacturers(unselectedManusFromPreferences);
		manufacturerCheckList.getModel().addListDataListener( new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				MotorFilterPopupMenu.this.filter.setExcludedManufacturers( manufacturerCheckList.getUncheckedItems() );
				onSelectionChanged();
			}
		});

		sub.add(new JScrollPane(manufacturerCheckList.getList()), "grow,wrap");

		JButton clearMotors = new JButton("clear");
		clearMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPopupMenu.this.manufacturerCheckList.clearAll();

			}
		});

		sub.add(clearMotors,"split 2");

		JButton selectMotors = new JButton("all");
		selectMotors.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPopupMenu.this.manufacturerCheckList.checkAll();

			}
		});

		sub.add(selectMotors,"wrap");

		root.add(sub,"grow, wrap");

		// Impulse selection
		sub = new JPanel(new MigLayout("fill"));
		border = BorderFactory.createTitledBorder("Impulse");
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
				MotorFilterPopupMenu.this.filter.setExcludedImpulseClasses( impulseCheckList.getUncheckedItems() );
				onSelectionChanged();
			}
			
		});

		sub.add(new JScrollPane(impulseCheckList.getList()), "grow,wrap");

		JButton clearImpulse = new JButton("clear");
		clearImpulse.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPopupMenu.this.impulseCheckList.clearAll();

			}
		});
		sub.add(clearImpulse,"split 2");

		JButton selectImpulse = new JButton("all");
		selectImpulse.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPopupMenu.this.impulseCheckList.checkAll();

			}
		});
		sub.add(selectImpulse,"wrap");

		root.add(sub,"grow, wrap");

		// Diameter selection

		sub = new JPanel(new MigLayout("fill"));
		border = BorderFactory.createTitledBorder("Diameter");
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);

		JRadioButton showAllDiametersButton = new JRadioButton( trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc1") );
		showAllDiametersButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMode = SHOW_ALL;
				MotorFilterPopupMenu.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.ALL);
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
				MotorFilterPopupMenu.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.SMALLER);
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
				MotorFilterPopupMenu.this.filter.setDiameterControl(MotorRowFilter.DiameterFilterControl.EXACT);
				onSelectionChanged();
			}
		});
		showExactDiametersButton.setSelected( showMode == SHOW_EXACT );
		sub.add(showExactDiametersButton, "growx,wrap");

		root.add(sub, "grow,wrap");
		ButtonGroup comboGroup = new ButtonGroup();
		comboGroup.add( showAllDiametersButton );
		comboGroup.add( showSmallerDiametersButton );
		comboGroup.add( showExactDiametersButton );


		// Close button
		JButton closeButton = new JButton("close");
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MotorFilterPopupMenu.this.onClose();
			}

		});
		root.add(closeButton, "split 2");

		this.add(root);

	}

	public void onClose() {

		((SwingPreferences) Application.getPreferences()).setExcludedMotorManufacturers(filter.getExcludedManufacturers());

		Application.getPreferences().putChoice("MotorDiameterMatch", showMode );

		setVisible(false);
	}

	public abstract void onSelectionChanged();

}
