package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.dialogs.flightconfiguration.IgnitionSelectionDialog;
import net.sf.openrocket.gui.dialogs.flightconfiguration.MotorMountConfigurationPanel;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.main.FlightConfigurationPanel;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.motor.IgnitionEvent;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Chars;

@SuppressWarnings("serial")
public class MotorConfigurationPanel extends FlightConfigurablePanel<MotorMount> {
	
	private static final String NONE = trans.get("edtmotorconfdlg.tbl.None");

	private final JButton selectMotorButton, deleteMotorButton, selectIgnitionButton, resetIgnitionButton;

	private final JPanel cards;
	private final static String HELP_LABEL = "help";
	private final static String TABLE_LABEL = "table";
	
	private final MotorChooserDialog motorChooserDialog;
	protected FlightConfigurableTableModel<MotorMount> configurationTableModel;

	private final JPopupMenu popupMenuFull;		// popup menu containing all the options


	public MotorConfigurationPanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel, rocket);

		motorChooserDialog = new MotorChooserDialog(SwingUtilities.getWindowAncestor(flightConfigurationPanel));

		{
			//// Select motor mounts
			JPanel subpanel = new JPanel(new MigLayout("inset 0, fill"));
			subpanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					"<html><b>" + trans.get("lbl.motorMounts") + "</b></html>"));

			MotorMountConfigurationPanel mountConfigPanel = new MotorMountConfigurationPanel(this, rocket);
			subpanel.add(mountConfigPanel, "grow, pushy");
			this.add(subpanel, "split, growy, pushy");
		}

		cards = new JPanel(new CardLayout());
		this.add(cards, "pushy");

		// Get all the actions
		AbstractAction selectMotorAction = new SelectMotorAction();
		AbstractAction deleteMotorAction = new DeleteMotorAction();
		AbstractAction selectIgnitionAction = new SelectIgnitionAction();
		AbstractAction resetIgnitionAction = new ResetIgnitionAction();
		AbstractAction renameConfigAction = flightConfigurationPanel.getRenameConfigAction();
		AbstractAction deleteConfigAction = flightConfigurationPanel.getDeleteConfigAction();
		AbstractAction duplicateConfigAction = flightConfigurationPanel.getDuplicateConfigAction();

		// Populate the popup menu
		popupMenuFull = new JPopupMenu();
		popupMenuFull.add(selectMotorAction);
		popupMenuFull.add(deleteMotorAction);
		popupMenuFull.addSeparator();
		popupMenuFull.add(selectIgnitionAction);
		popupMenuFull.add(resetIgnitionAction);
		popupMenuFull.addSeparator();
		popupMenuFull.add(renameConfigAction);
		popupMenuFull.add(deleteConfigAction);
		popupMenuFull.add(duplicateConfigAction);

		JLabel helpText = new JLabel(trans.get("MotorConfigurationPanel.lbl.nomotors"));
		cards.add(helpText, HELP_LABEL );

		JPanel configurationPanel = new JPanel(new MigLayout("fill, insets n n 5px n"));
		configurationPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"<html><b>" + trans.get("MotorConfigurationPanel.lbl.motorConfiguration") + "</b></html>"));
		JScrollPane scroll = new JScrollPane(table);
		configurationPanel.add(scroll, "spanx, grow, pushy, wrap");

		//// Select motor
		selectMotorButton = new SelectColorButton(selectMotorAction);
		configurationPanel.add(selectMotorButton, "split, align right, sizegroup button");

		//// Delete motor button
		deleteMotorButton = new SelectColorButton(deleteMotorAction);
		configurationPanel.add(deleteMotorButton, "sizegroup button");

		//// Select Ignition button
		selectIgnitionButton = new SelectColorButton(selectIgnitionAction);
		configurationPanel.add(selectIgnitionButton, "sizegroup button, gapleft para");

		//// Reset Ignition button
		resetIgnitionButton = new SelectColorButton(resetIgnitionAction);
		configurationPanel.add(resetIgnitionButton, "sizegroup button, wrap");

		cards.add(configurationPanel, TABLE_LABEL );

		this.add(cards, "gapleft para, grow, pushy, wrap");

		// Set 'Enter' key action to open the motor selection dialog
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				selectMotor();
			}
		});

		updateButtonState();

	}

	protected void showEmptyText() {
		((CardLayout)cards.getLayout()).show(cards, HELP_LABEL);
	}

	protected void showContent() {
		((CardLayout)cards.getLayout()).show(cards, TABLE_LABEL);
	}

	@Override
	protected JTable initializeTable() {
		//// Motor selection table.
		configurationTableModel = new FlightConfigurableTableModel<MotorMount>(MotorMount.class,rocket) {
			@Override
			protected boolean includeComponent(MotorMount component) {
				return component.isMotorMount();
			}

			@Override
			public void componentChanged(ComponentChangeEvent cce) {
				super.componentChanged(cce);
				// This will catch a name change to cause a change in the header of the table
				if ((cce.getSource() instanceof BodyTube || cce.getSource() instanceof InnerTube) && cce.isNonFunctionalChange()) {
					fireTableStructureChanged();
				}
			}
		};
		
		// Listen to changes to the table so we can disable the help text when a
		// motor mount is added through the edit body tube dialog.
		configurationTableModel.addTableModelListener( new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent tme) {
				MotorConfigurationPanel.this.updateButtonState();
			}
			
		});
		
		JTable configurationTable = new JTable(configurationTableModel);
		configurationTable.getTableHeader().setReorderingAllowed(false);
		configurationTable.setCellSelectionEnabled(true);
		configurationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		configurationTable.setDefaultRenderer(Object.class, new MotorTableCellRenderer());

		ListSelectionListener listener = new ListSelectionListener() {
			private int previousRow = -1;
			private int previousColumn = -1;

			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (table != null && (configurationTable.getSelectedRow() != previousRow ||
						configurationTable.getSelectedColumn() != previousColumn)) {
					updateButtonState();
					previousRow = configurationTable.getSelectedRow();
					previousColumn = configurationTable.getSelectedColumn();
				}
			}
		};

		configurationTable.getSelectionModel().addListSelectionListener(listener);
		configurationTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);

		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedColumn = table.getSelectedColumn();

				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (selectedColumn > 0) {
						selectMotor();
					}
				} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					// Get the row and column of the selected cell
					int r = configurationTable.rowAtPoint(e.getPoint());
					int c = configurationTable.columnAtPoint(e.getPoint());

					// Select new cell
					if (!configurationTable.isCellSelected(r, c)) {
						if (r >= 0 && r < configurationTable.getRowCount() &&
								c >= 0 && c < configurationTable.getColumnCount()) {
							configurationTable.setRowSelectionInterval(r, r);
							configurationTable.setColumnSelectionInterval(c, c);
						} else {
							configurationTable.clearSelection();
							return;
						}
					}

					if (c > 0) {
						doPopupFull(e);
					} else {
						flightConfigurationPanel.doPopupConfig(e);
					}
				}
			}
		});
		
		return configurationTable;
	}

	private void doPopupFull(MouseEvent e) {
		popupMenuFull.show(e.getComponent(), e.getX(), e.getY());
	}

	public void updateRocketViewSelection(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		List<MotorMount> mounts = getSelectedComponents();
		if (mounts == null || mounts.size() == 0) return;
		List<RocketComponent> components = new ArrayList<>();
		for (MotorMount mount : mounts) {
			if (mount instanceof RocketComponent) {
				components.add((RocketComponent) mount);
			}
		}

		flightConfigurationPanel.setSelectedComponents(components);
	}

	public void updateButtonState() {
		if( configurationTableModel.getColumnCount() > 1 ) {
			showContent();
			
			boolean haveSelection = (null != getSelectedComponent());
			selectMotorButton.setEnabled( haveSelection );
			deleteMotorButton.setEnabled( haveSelection );
			selectIgnitionButton.setEnabled( haveSelection );
			resetIgnitionButton.setEnabled( haveSelection );
		} else {
			showEmptyText();
			selectMotorButton.setEnabled(false);
			deleteMotorButton.setEnabled(false);
			selectIgnitionButton.setEnabled(false);
			resetIgnitionButton.setEnabled(false);
		}
	}

	public void selectMotor() {
		List<MotorMount> mounts = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((mounts == null) || (fcIds == null) || mounts.size() == 0 || fcIds.size() == 0) {
			return;
		}

		boolean update = false;
		MotorMount initMount = mounts.get(0);
		FlightConfigurationId initFcId = fcIds.get(0);

		for (FlightConfigurationId fcId : fcIds) {
			if (fcId.equals( FlightConfigurationId.DEFAULT_VALUE_FCID)) {
				throw new IllegalStateException("Attempting to set a motor on the default FCID.");
			}
		}

        double initDelay = initMount.getMotorConfig(initFcId).getEjectionDelay();

		motorChooserDialog.setMotorMountAndConfig(initFcId, initMount);
		motorChooserDialog.open();

        Motor mtr = motorChooserDialog.getSelectedMotor();
		double d = motorChooserDialog.getSelectedDelay();


		if (mtr != null) {
			for (MotorMount mount : mounts) {
				for (FlightConfigurationId fcId : fcIds) {
					if (mtr != mount.getMotorConfig(fcId).getMotor() || d != initDelay) {
						update = true;

						final MotorConfiguration templateConfig = mount.getMotorConfig(fcId);
						final MotorConfiguration newConfig = new MotorConfiguration(mount, fcId, templateConfig);
						newConfig.setMotor(mtr);
						newConfig.setEjectionDelay(d);
						mount.setMotorConfig(newConfig, fcId);
					}
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.MOTOR_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}

	private void deleteMotor() {
		List<MotorMount> mounts = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((mounts == null) || (fcIds == null) || mounts.size() == 0 || fcIds.size() == 0) {
            return;
        }

		for (MotorMount mount : mounts) {
			for (FlightConfigurationId fcId : fcIds) {
				mount.setMotorConfig(null, fcId);
			}
		}
		
		fireTableDataChanged(ComponentChangeEvent.MOTOR_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
	}

	private void selectIgnition() {
		List<MotorMount> mounts = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((mounts == null) || (fcIds == null) || mounts.size() == 0 || fcIds.size() == 0) {
			return;
		}

		boolean update = false;
		MotorMount initMount = mounts.get(0);
		FlightConfigurationId initFcId = fcIds.get(0);

		MotorConfiguration initConfig = initMount.getMotorConfig(initFcId);
		IgnitionEvent initialIgnitionEvent = initConfig.getIgnitionEvent();
		double initialIgnitionDelay = initConfig.getIgnitionDelay();

		// this call also performs the update changes
		IgnitionSelectionDialog ignitionDialog = new IgnitionSelectionDialog(
				SwingUtilities.getWindowAncestor(this.flightConfigurationPanel),
				initFcId,
				initMount);
		ignitionDialog.setVisible(true);

		if (!initialIgnitionEvent.equals(initConfig.getIgnitionEvent()) || (initialIgnitionDelay != initConfig.getIgnitionDelay())) {
			update = true;
		}

		for (int i = 0; i < mounts.size(); i++) {
			for (int j = 0; j < fcIds.size(); j++) {
				if ((i == 0) && (j == 0)) break;

				MotorConfiguration config = mounts.get(i).getMotorConfig(fcIds.get(j));
				initialIgnitionEvent = config.getIgnitionEvent();
				initialIgnitionDelay = config.getIgnitionDelay();

				config.setIgnitionEvent(initConfig.getIgnitionEvent());
				config.setIgnitionDelay(initConfig.getIgnitionDelay());

				if (!initialIgnitionEvent.equals(config.getIgnitionEvent()) || (initialIgnitionDelay != config.getIgnitionDelay())) {
					update = true;
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.MOTOR_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}


	private void resetIgnition() {
		List<MotorMount> mounts = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((mounts == null) || (fcIds == null) || mounts.size() == 0 || fcIds.size() == 0) {
			return;
		}

		boolean update = false;
		for (MotorMount mount : mounts) {
			for (FlightConfigurationId fcId : fcIds) {
				MotorConfiguration config = mount.getMotorConfig(fcId);
				IgnitionEvent initialIgnitionEvent = config.getIgnitionEvent();
				double initialIgnitionDelay = config.getIgnitionDelay();

				config.useDefaultIgnition();

				if (!initialIgnitionEvent.equals(config.getIgnitionEvent()) || (initialIgnitionDelay != config.getIgnitionDelay())) {
					update = true;
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.MOTOR_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}


	private class SelectMotorAction extends AbstractAction {
		public SelectMotorAction() {
			putValue(NAME, trans.get("MotorConfigurationPanel.btn.selectMotor"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectMotor();
		}
	}

	private class DeleteMotorAction extends AbstractAction {
		public DeleteMotorAction() {
			putValue(NAME, trans.get("MotorConfigurationPanel.btn.deleteMotor"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			deleteMotor();
		}
	}

	private class SelectIgnitionAction extends AbstractAction {
		public SelectIgnitionAction() {
			putValue(NAME, trans.get("MotorConfigurationPanel.btn.selectIgnition"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectIgnition();
		}
	}

	private class ResetIgnitionAction extends AbstractAction {
		public ResetIgnitionAction() {
			putValue(NAME, trans.get("MotorConfigurationPanel.btn.resetIgnition"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			resetIgnition();
		}
	}

	private class MotorTableCellRenderer extends FlightConfigurablePanel<MotorMount>.FlightConfigurableCellRenderer {

		@Override
		protected JLabel format( MotorMount mount, FlightConfigurationId configId, JLabel l ) {
			JLabel label = new JLabel();
			label.setLayout(new BoxLayout(label, BoxLayout.X_AXIS));
			
			MotorConfiguration curMotor = mount.getMotorConfig( configId);
			String motorString = getMotorSpecification( curMotor );
			
			JLabel motorDescriptionLabel = new JLabel(motorString);
			label.add(motorDescriptionLabel);
			label.add( Box.createRigidArea(new Dimension(10,0)));
			JLabel ignitionLabel = getIgnitionEventString(configId, mount);
			label.add(ignitionLabel);
			label.validate();
			return label;
		}

		private String getMotorSpecification(MotorConfiguration curMotorInstance ) {
			if( curMotorInstance.isEmpty()){
				return NONE;
			}

			MotorMount mount = curMotorInstance.getMount();
			Motor motor = curMotorInstance.getMotor();
			if( null == mount){
				throw new NullPointerException("Motor has a null mount... this should never happen: "+curMotorInstance.getID());
			}

			String str = motor.getCommonName(curMotorInstance.getEjectionDelay());
			int count = mount.getInstanceCount();
			if (count > 1) {
				str = "" + count + Chars.TIMES + " " + str;
			}
			return str;
		}

		private JLabel getIgnitionEventString(FlightConfigurationId id, MotorMount mount) {
			MotorConfiguration defInstance = mount.getDefaultMotorConfig();
			MotorConfiguration curInstance = mount.getMotorConfig(id);
			
			IgnitionEvent ignitionEvent = curInstance.getIgnitionEvent();
			Double ignitionDelay = curInstance.getIgnitionDelay();
			boolean useDefault = !curInstance.hasIgnitionOverride();
			
			if ( useDefault ) {
				ignitionEvent = defInstance.getIgnitionEvent();
				ignitionDelay = defInstance.getIgnitionDelay();
			}
			
			JLabel label = new JLabel();
			String str = trans.get("MotorMount.IgnitionEvent.short." + ignitionEvent.name());
			if (ignitionEvent != IgnitionEvent.NEVER && ignitionDelay > 0.001) {
				str = str + " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(ignitionDelay);
			}
			if (useDefault) {
				shaded(label);
				String def = trans.get("MotorConfigurationTableModel.table.ignition.default");
				str = def.replace("{0}", str);
			}
			label.setText(str);
			return label;
		}

	}

}
