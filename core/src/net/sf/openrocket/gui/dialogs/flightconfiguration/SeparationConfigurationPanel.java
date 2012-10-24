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
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;

public class SeparationConfigurationPanel extends JPanel {

	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	private final Stage[] stages;

	private final SeparationTableModel separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;

	private Stage selectedComponent;

	SeparationConfigurationPanel( FlightConfigurationDialog flightConfigurationDialog, Rocket rocket ) {
		super( new MigLayout("fill") );
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;

		int stageCount = rocket.getStageCount() -1;
		stages = new Stage[stageCount];
		Iterator<RocketComponent> it = rocket.iterator();
		{
			int stageIndex = -1;
			while( it.hasNext() ) {
				RocketComponent c = it.next();
				if ( c instanceof Stage ) {
					if ( stageIndex >= 0 ){
						stages[stageIndex] = (Stage) c;
					}
					stageIndex++;
				}
			}
		}

		//// Recovery selection 
		separationTableModel = new SeparationTableModel();
		JTable table = new JTable( separationTableModel );
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable table = (JTable) e.getComponent();
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();

				if ( row >= 0 ) {
					selectedComponent = stages[row];
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
		selectSeparationButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Selectseparation"));
		selectSeparationButton.setEnabled(false);
		selectSeparationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDeployment();
			}
		});
		this.add(selectSeparationButton, "skip, split, sizegroup button");

		//// Reset deployment
		resetDeploymentButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Resetseparation"));
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
		separationTableModel.fireTableDataChanged();
		updateButtonState();
	}

	private void selectDeployment() {
		JDialog d = new SelectSeparationConfigDialog( flightConfigurationDialog, rocket, selectedComponent );
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
		selectSeparationButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected & ! isDefaulted);
	}

	private class SeparationTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return stages.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Stage d = SeparationConfigurationPanel.this.stages[rowIndex];
			switch ( columnIndex ) {
			case 0:
				return d.getName();
			case 1:
				StageSeparationConfiguration separationConfig = d.getFlightConfiguration(rocket.getDefaultConfiguration().getFlightConfigurationID());
				if ( separationConfig == null ) {
					return "[" + d.getDefaultFlightConfiguration().toString() + "]";
				} else {
					return separationConfig.toString();
				}
			}

			return null;
		}

		@Override
		public String getColumnName(int column) {
			switch ( column ) {
			case 0:
				return FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Stageheader");
			case 1:
				return FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Separationheader");
			default:
				return "";
			}
		}

	}

}
