package info.openrocket.swing.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.*;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.dialogs.flightconfiguration.DeploymentSelectionDialog;
import info.openrocket.swing.gui.main.FlightConfigurationPanel;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class RecoveryConfigurationPanel extends FlightConfigurablePanel<RecoveryDevice> {

	Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private FlightConfigurableTableModel<RecoveryDevice> recoveryTableModel;
	private final JButton selectDeploymentButton;
	private final JButton resetDeploymentButton;
	private final JPopupMenu popupMenuFull;		// popup menu containing all the options


	public RecoveryConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, OpenRocketDocument document, Rocket rocket) {
		super(flightConfigurationPanel, document, rocket);

		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "span, grow, pushy, wrap");

		// Get all the actions
		AbstractAction selectDeploymentAction = new SelectDeploymentAction();
		AbstractAction resetDeploymentAction = new ResetDeploymentAction();
		AbstractAction renameConfigAction = flightConfigurationPanel.getRenameConfigAction();
		AbstractAction removeConfigAction = flightConfigurationPanel.getDeleteConfigAction();
		AbstractAction duplicateConfigAction = flightConfigurationPanel.getDuplicateConfigAction();

		// Populate the popup menu
		popupMenuFull = new JPopupMenu();
		popupMenuFull.add(selectDeploymentAction);
		popupMenuFull.add(resetDeploymentAction);
		popupMenuFull.addSeparator();
		popupMenuFull.add(renameConfigAction);
		popupMenuFull.add(removeConfigAction);
		popupMenuFull.add(duplicateConfigAction);

		//// Select deployment
		selectDeploymentButton = new SelectColorButton(selectDeploymentAction);
		selectDeploymentButton.setEnabled(false);
		this.add(selectDeploymentButton, "split, align right, sizegroup button");

		//// Reset deployment
		resetDeploymentButton = new SelectColorButton(resetDeploymentAction);
		resetDeploymentButton.setEnabled(false);
		this.add(resetDeploymentButton, "sizegroup button, wrap");

		// Set 'Enter' key action to open the recovery selection dialog
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				selectDeployment();
			}
		});
	}

	@Override
	protected JTable initializeTable() {
		//// Recovery selection 
		recoveryTableModel = new FlightConfigurableTableModel<RecoveryDevice>(RecoveryDevice.class, rocket);
		JTable recoveryTable = new JTable(recoveryTableModel);
		recoveryTable.getTableHeader().setReorderingAllowed(false);
		recoveryTable.setCellSelectionEnabled(true);
		recoveryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		ListSelectionListener listener = new ListSelectionListener() {
			private int previousRow = -1;
			private int previousColumn = -1;

			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (table != null && (recoveryTable.getSelectedRow() != previousRow ||
						recoveryTable.getSelectedColumn() != previousColumn)) {
					updateButtonState();
					previousRow = recoveryTable.getSelectedRow();
					previousColumn = recoveryTable.getSelectedColumn();
				}
			}
		};

		recoveryTable.getSelectionModel().addListSelectionListener(listener);
		recoveryTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);

		recoveryTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedColumn = table.getSelectedColumn();

				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (selectedColumn > 0) {
						selectDeployment();
					}
				} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					// Get the row and column of the selected cell
					int r = recoveryTable.rowAtPoint(e.getPoint());
					int c = recoveryTable.columnAtPoint(e.getPoint());

					// Select new cell
					if (!recoveryTable.isCellSelected(r, c)) {
						if (r >= 0 && r < recoveryTable.getRowCount() &&
								c >= 0 && c < recoveryTable.getColumnCount()) {
							recoveryTable.setRowSelectionInterval(r, r);
							recoveryTable.setColumnSelectionInterval(c, c);
						} else {
							recoveryTable.clearSelection();
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

		rocket.addComponentChangeListener(cce -> {
			final RocketComponent source = cce.getSource();
			if(source instanceof FlightConfigurableComponent) {
				final int index = recoveryTableModel.getColumnIndex((FlightConfigurableComponent) source);
				if (index >= 0) {
					recoveryTable.getColumnModel().getColumn(index).setHeaderValue(source.getName());
				}
				// you would think this would be enough by itself, but it requires an nudge from the above lines to
				// actually update.
				recoveryTable.getTableHeader().resizeAndRepaint();
			}
		});
		recoveryTable.setDefaultRenderer(Object.class, new RecoveryTableCellRenderer());

		return recoveryTable;
	}

	public void selectDeployment() {
		List<RecoveryDevice> devices = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((devices == null) || (fcIds == null) || (devices.size() == 0) || fcIds.size() == 0) {
			return;
		}

		// Get the device and fcid that will be used in the config dialog
		RecoveryDevice initDevice = devices.get(0);
		FlightConfigurationId initFcId = fcIds.get(0);

		DeploymentConfiguration initialConfig = initDevice.getDeploymentConfigurations().get(initFcId).copy(initFcId);

		document.addUndoPosition("Select deployment");

		// Open the configuration dialog
		DeploymentSelectionDialog d = new DeploymentSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, initFcId, initDevice);
		d.setVisible(true);

		final DeploymentConfiguration modifiedConfig = initDevice.getDeploymentConfigurations().get(initFcId);
		boolean update = !initialConfig.equals(modifiedConfig);

		double deployDelay = modifiedConfig.getDeployDelay();
		double deployAltitude = modifiedConfig.getDeployAltitude();
		DeployEvent deployEvent = modifiedConfig.getDeployEvent();
		boolean isOverrideDefault = d.isOverrideDefault();

		for (RecoveryDevice device : devices) {
			for (FlightConfigurationId fcId : fcIds) {
				// Skip the config that was used for the config dialog (it has already been modified)
				if ((device == initDevice) && (fcId == initFcId))
					continue;

				// It could be that the current config is the default config, but the user has selected to override it.
				if (isOverrideDefault && !device.getDeploymentConfigurations().containsId(fcId)) {
					device.getDeploymentConfigurations().set(fcId, device.getDeploymentConfigurations().getDefault().clone());
				}

				DeploymentConfiguration currentConfig = device.getDeploymentConfigurations().get(fcId);

				if (currentConfig.equals(modifiedConfig)) {
					continue;
				}

				update = true;

				currentConfig.setDeployDelay(deployDelay);
				currentConfig.setDeployAltitude(deployAltitude);
				currentConfig.setDeployEvent(deployEvent);
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AERODYNAMIC_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}

	}

	private void resetDeployment() {
		List<RecoveryDevice> devices = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((devices == null) || (fcIds == null) || (devices.size() == 0) || fcIds.size() == 0) {
			return;
		}

		document.addUndoPosition("Reset deployment");

		boolean update = false;
		for (RecoveryDevice device : devices) {
			for (FlightConfigurationId fcId : fcIds) {
				DeploymentConfiguration initialConfig = device.getDeploymentConfigurations().get(fcId).copy(fcId);
				device.getDeploymentConfigurations().reset(fcId);

				if (!initialConfig.equals(device.getDeploymentConfigurations().get(fcId))) {
					update = true;
				}
			}
		}
		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AERODYNAMIC_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}

	private void doPopupFull(MouseEvent e) {
		popupMenuFull.show(e.getComponent(), e.getX(), e.getY());
	}

	public void updateRocketViewSelection(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() || getSelectedComponents() == null) {
			return;
		}
		List<RocketComponent> components = new ArrayList<>(getSelectedComponents());
		if (components.size() == 0) return;

		flightConfigurationPanel.setSelectedComponents(components);
	}

	public void updateButtonState() {
		boolean componentSelected = getSelectedComponent() != null;
		selectDeploymentButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected);
	}

	class RecoveryTableCellRenderer extends FlightConfigurablePanel<RecoveryDevice>.FlightConfigurableCellRenderer {

		@Override
		protected JLabel format(RecoveryDevice recovery, FlightConfigurationId configId, JLabel label) {
			DeploymentConfiguration deployConfig = recovery.getDeploymentConfigurations().get(configId);
			String spec = getDeploymentSpecification(deployConfig);
			label.setText(spec);
			if (recovery.getDeploymentConfigurations().isDefault(configId)) {
				shaded(label);
			} else {
				regular(label);
			}
			return label;
		}

		private String getDeploymentSpecification( DeploymentConfiguration config ) {
			String str;

			str = trans.get("RecoveryDevice.DeployEvent.short." + config.getDeployEvent().name());
			if (config.getDeployEvent() == DeployEvent.ALTITUDE) {
				str += " " + UnitGroup.UNITS_DISTANCE.toStringUnit(config.getDeployAltitude());
			}
			if (config.getDeployDelay() > 0.001) {
				str += " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(config.getDeployDelay());
			}

			return str;
		}
	}

	private class SelectDeploymentAction extends AbstractAction {
		public SelectDeploymentAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Selectdeployment"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectDeployment();
		}
	}
	private class ResetDeploymentAction extends AbstractAction {
		public ResetDeploymentAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Resetdeployment"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			resetDeployment();
		}
	}

}
