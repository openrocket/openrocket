package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class RecoveryConfigurationPanel extends JPanel {

	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	
	private final RecoveryTableModel recoveryTableModel;
	private final JButton selectDeploymentButton;
	private final JButton resetDeploymentButton;
	
	private RecoveryDevice selectedComponent;

	RecoveryConfigurationPanel( FlightConfigurationDialog flightConfigurationDialog, Rocket rocket ) {
		super( new MigLayout("fill") );
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;

		//// Recovery selection 
		recoveryTableModel = new RecoveryTableModel();
		JTable table = new JTable( recoveryTableModel );
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable table = (JTable) e.getComponent();
				int row = table.getSelectedRow();

				if ( row >= 0 ) {
					selectedComponent = findRecoveryDevice(row);
				} else { 
					selectedComponent = null;
				}

				if (e.getClickCount() == 1) {
					// Single click updates selection
					updateButtonState();
				} else if (e.getClickCount() == 2) {
					// Double-click edits 
					selectDeployment();
				}

			}
		});

		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "span, grow, wrap");

		//// Select deployment
		selectDeploymentButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Selectdeployment"));
		selectDeploymentButton.setEnabled(false);
		selectDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDeployment();
			}
		});
		this.add(selectDeploymentButton, "skip, split, sizegroup button");

		//// Reset deployment
		resetDeploymentButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Resetdeployment"));
		resetDeploymentButton.setEnabled(false);
		resetDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetDeployment();
			}
		});
		this.add(resetDeploymentButton,"sizegroup button, wrap");

	}
	
	public void fireTableDataChanged() {
		selectedComponent = null;
		recoveryTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void selectDeployment() {
		JDialog d = new SelectDeploymentConfigDialog( flightConfigurationDialog, rocket, selectedComponent );
		d.setVisible(true);
		fireTableDataChanged();
	}
	
	private void resetDeployment() {
		selectedComponent.setFlightConfiguration(rocket.getDefaultConfiguration().getFlightConfigurationID(), null);
		fireTableDataChanged();
	}
	
	public void updateButtonState() {
		boolean componentSelected = selectedComponent != null;
		boolean isDefaulted = true;
		if ( componentSelected ) {
			isDefaulted = selectedComponent.getFlightConfiguration(rocket.getDefaultConfiguration().getFlightConfigurationID()) == null;
		}
		selectDeploymentButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected & ! isDefaulted);
	}
	
	private RecoveryDevice findRecoveryDevice( int count ) {
		RecoveryDevice d = null;
		Iterator<RocketComponent> it = rocket.iterator();
		while( it.hasNext() && count >= 0 ) {
			RocketComponent c = it.next();
			if ( c instanceof RecoveryDevice ) {
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
			while( it.hasNext() ) {
				RocketComponent c = it.next();
				if ( c instanceof RecoveryDevice ) {
					count ++;
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
			RecoveryDevice d = RecoveryConfigurationPanel.this.findRecoveryDevice(rowIndex);
			switch ( columnIndex ) {
			case 0:
				return d.getName();
			case 1:
				DeploymentConfiguration deployConfig = d.getFlightConfiguration(rocket.getDefaultConfiguration().getFlightConfigurationID());
				if ( deployConfig == null ) {
					return "[" + d.getDefaultFlightConfiguration().toString() + "]";
				} else {
					return deployConfig.toString();
				}
			}
			
			return null;
		}

		@Override
		public String getColumnName(int column) {
			switch ( column ) {
			case 0:
				return FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Recoveryheader");
			case 1:
				return FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Deploymentheader");
			default:
				return "";
			}
		}

	}

}
