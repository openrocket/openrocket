package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class RecoveryConfigurationPanel extends JPanel {
	
	private Translator trans = Application.getTranslator();
	
	
	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	
	private final RecoveryTableModel recoveryTableModel;
	private final JTable recoveryTable;
	private final JButton selectDeploymentButton;
	private final JButton resetDeploymentButton;
	
	
	RecoveryConfigurationPanel(FlightConfigurationDialog flightConfigurationDialog, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;
		
		//// Recovery selection 
		recoveryTableModel = new RecoveryTableModel();
		recoveryTable = new JTable(recoveryTableModel);
		recoveryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recoveryTable.setRowSelectionAllowed(true);
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
		
		JScrollPane scroll = new JScrollPane(recoveryTable);
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
		this.add(selectDeploymentButton, "skip, split, sizegroup button");
		
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
	
	public void fireTableDataChanged() {
		int selected = recoveryTable.getSelectedRow();
		recoveryTableModel.fireTableDataChanged();
		if (selected >= 0) {
			selected = Math.min(selected, recoveryTable.getRowCount() - 1);
			recoveryTable.getSelectionModel().setSelectionInterval(selected, selected);
		}
		updateButtonState();
	}
	
	private void selectDeployment() {
		RecoveryDevice c = getSelectedComponent();
		if (c == null) {
			return;
		}
		JDialog d = new DeploymentSelectionDialog(flightConfigurationDialog, rocket, c);
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
	
	
	private RecoveryDevice getSelectedComponent() {
		int row = recoveryTable.getSelectedRow();
		return findRecoveryDevice(row);
	}
	
	private RecoveryDevice findRecoveryDevice(int count) {
		RecoveryDevice d = null;
		Iterator<RocketComponent> it = rocket.iterator();
		while (it.hasNext() && count >= 0) {
			RocketComponent c = it.next();
			if (c instanceof RecoveryDevice) {
				d = (RecoveryDevice) c;
				count--;
			}
		}
		return d;
	}
	
	
	
	private class RecoveryTableModel extends AbstractTableModel {
		
		@Override
		public int getRowCount() {
			int count = 0;
			Iterator<RocketComponent> it = rocket.iterator();
			while (it.hasNext()) {
				RocketComponent c = it.next();
				if (c instanceof RecoveryDevice) {
					count++;
				}
			}
			return count;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			RecoveryDevice d = findRecoveryDevice(rowIndex);
			switch (columnIndex) {
			case 0:
				return d.getName();
			case 1:
				String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
				DeploymentConfiguration config = d.getDeploymentConfiguration().get(id);
				boolean isDefault = d.getDeploymentConfiguration().isDefault(id);
				
				String str;
				
				str = trans.get("RecoveryDevice.DeployEvent.short." + config.getDeployEvent().name());
				if (config.getDeployEvent() == DeployEvent.ALTITUDE) {
					str += " " + UnitGroup.UNITS_DISTANCE.toStringUnit(config.getDeployAltitude());
				}
				if (config.getDeployDelay() > 0.001) {
					str += " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(config.getDeployDelay());
				}
				
				
				if (isDefault) {
					String def = trans.get("table.deployment.default");
					str = def.replace("{0}", str);
				}
				return str;
				
			default:
				throw new IndexOutOfBoundsException("columnIndex=" + columnIndex);
			}
			
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return trans.get("edtmotorconfdlg.tbl.Recoveryheader");
			case 1:
				return trans.get("edtmotorconfdlg.tbl.Deploymentheader");
			default:
				return "";
			}
		}
		
	}
	
	
	private class RecoveryTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!(c instanceof JLabel)) {
				return c;
			}
			JLabel label = (JLabel) c;
			
			RecoveryDevice recoveryDevice = findRecoveryDevice(row);
			String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
			
			switch (column) {
			case 0:
				regular(label);
				break;
			
			case 1:
				if (recoveryDevice.getDeploymentConfiguration().isDefault(id)) {
					shaded(label);
				} else {
					regular(label);
				}
				break;
			}
			
			return label;
		}
		
		private void shaded(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.ITALIC);
			label.setForeground(Color.GRAY);
		}
		
		private void regular(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.PLAIN);
			label.setForeground(Color.BLACK);
		}
		
	}
	
}
