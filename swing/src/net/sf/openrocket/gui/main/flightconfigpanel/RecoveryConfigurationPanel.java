package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.dialogs.flightconfiguration.DeploymentSelectionDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Pair;

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
		selectDeploymentButton = new JButton(trans.get("edtmotorconfdlg.but.Selectdeployment"));
		selectDeploymentButton.setEnabled(false);
		selectDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDeployment();
			}
		});
		this.add(selectDeploymentButton, "split, align right, sizegroup button");

		//// Reset deployment
		resetDeploymentButton = new JButton(trans.get("edtmotorconfdlg.but.Resetdeployment"));
		resetDeploymentButton.setEnabled(false);
		resetDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetDeployment();
			}
		});
		this.add(resetDeploymentButton, "sizegroup button, wrap");
	}

	@Override
	protected JTable initializeTable() {
		//// Recovery selection 
		recoveryTableModel = new FlightConfigurableTableModel<RecoveryDevice>(RecoveryDevice.class, rocket);
		JTable recoveryTable = new JTable(recoveryTableModel);
		recoveryTable.setCellSelectionEnabled(true);
		recoveryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		recoveryTable.setDefaultRenderer(Object.class, new RecoveryTableCellRenderer());

		return recoveryTable;
	}

	public void fireTableDataChanged() {
		int selected = table.getSelectedRow();
		recoveryTableModel.fireTableDataChanged();
		if (selected >= 0) {
			selected = Math.min(selected, table.getRowCount() - 1);
			table.getSelectionModel().setSelectionInterval(selected, selected);
		}
		updateButtonState();
	}

	private void selectDeployment() {
		RecoveryDevice c = getSelectedComponent();
		if (c == null) {
			return;
		}
		JDialog d = new DeploymentSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, c);
		d.setVisible(true);
		fireTableDataChanged();
	}

	private void resetDeployment() {
		RecoveryDevice c = getSelectedComponent();
		if (c == null) {
			return;
		}
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		c.getDeploymentConfiguration().resetDefault(id);
		fireTableDataChanged();
	}

	public void updateButtonState() {
		boolean componentSelected = getSelectedComponent() != null;
		selectDeploymentButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected);
	}

	class RecoveryTableCellRenderer extends FlightConfigurablePanel<RecoveryDevice>.FlightConfigurableCellRenderer {

		@Override
		protected void format(RecoveryDevice recovery, String configId, JLabel label) {
			DeploymentConfiguration deployConfig = recovery.getDeploymentConfiguration().get(configId);
			String spec = getDeploymentSpecification(deployConfig);
			label.setText(spec);
			if (recovery.getDeploymentConfiguration().isDefault(configId)) {
				shaded(label);
			} else {
				regular(label);
			}
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
