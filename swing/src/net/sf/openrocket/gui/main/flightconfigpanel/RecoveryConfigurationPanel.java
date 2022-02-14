package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.dialogs.flightconfiguration.DeploymentSelectionDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.gui.widgets.SelectColorButton;

public class RecoveryConfigurationPanel extends FlightConfigurablePanel<RecoveryDevice> {

	Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private FlightConfigurableTableModel<RecoveryDevice> recoveryTableModel;
	private final JButton selectDeploymentButton;
	private final JButton resetDeploymentButton;


	RecoveryConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel,rocket);

		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "span, grow, wrap");

		//// Select deployment
		selectDeploymentButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Selectdeployment"));
		selectDeploymentButton.setEnabled(false);
		selectDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDeployment();
			}
		});
		this.add(selectDeploymentButton, "split, align right, sizegroup button");

		//// Reset deployment
		resetDeploymentButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Resetdeployment"));
		resetDeploymentButton.setEnabled(false);
		resetDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetDeployment();
			}
		});
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
		recoveryTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonState();

				if (e.getClickCount() == 2) {
					// Double-click edits 
					selectDeployment();
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

		boolean update = false;
		RecoveryDevice initDevice = devices.get(0);
		FlightConfigurationId initFcId = fcIds.get(0);

		DeploymentConfiguration initialConfig = initDevice.getDeploymentConfigurations().get(initFcId).copy(initFcId);
		JDialog d = new DeploymentSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, initDevice);
		d.setVisible(true);

		if (!initialConfig.equals(initDevice.getDeploymentConfigurations().get(initFcId))) {
			update = true;
		}

		double deployDelay = initDevice.getDeploymentConfigurations().get(initFcId).getDeployDelay();
		double deployAltitude = initDevice.getDeploymentConfigurations().get(initFcId).getDeployAltitude();
		DeployEvent deployEvent = initDevice.getDeploymentConfigurations().get(initFcId).getDeployEvent();

		for (int i = 0; i < devices.size(); i++) {
			for (int j = 0; j < fcIds.size(); j++) {
				if ((i == 0) && (j == 0)) break;

				final RecoveryDevice device = devices.get(i);
				final FlightConfigurationId fcId = fcIds.get(j);
				DeploymentConfiguration config = device.getDeploymentConfigurations().get(fcId).copy(fcId);
				initialConfig = config.copy(fcId);

				config.setDeployDelay(deployDelay);
				config.setDeployAltitude(deployAltitude);
				config.setDeployEvent(deployEvent);

				device.getDeploymentConfigurations().set(fcId, config);

				if (!initialConfig.equals(device.getDeploymentConfigurations().get(fcId))) {
					update = true;
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AERODYNAMIC_CHANGE);
		}

	}

	private void resetDeployment() {
		List<RecoveryDevice> devices = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((devices == null) || (fcIds == null) || (devices.size() == 0) || fcIds.size() == 0) {
			return;
		}

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
			fireTableDataChanged(ComponentChangeEvent.AERODYNAMIC_CHANGE);
		}
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

}
