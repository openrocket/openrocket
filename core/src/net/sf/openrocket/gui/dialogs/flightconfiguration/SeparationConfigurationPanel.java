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
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import net.sf.openrocket.startup.Application;

public class SeparationConfigurationPanel extends JPanel {
	
	private static final Translator trans = Application.getTranslator();
	
	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	private final Stage[] stages;
	
	private final JTable separationTable;
	private final SeparationTableModel separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;
	
	
	SeparationConfigurationPanel(FlightConfigurationDialog flightConfigurationDialog, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;
		
		
		int stageCount = rocket.getStageCount() - 1;
		stages = new Stage[stageCount];
		Iterator<RocketComponent> it = rocket.iterator();
		{
			int stageIndex = -1;
			while (it.hasNext()) {
				RocketComponent c = it.next();
				if (c instanceof Stage) {
					if (stageIndex >= 0) {
						stages[stageIndex] = (Stage) c;
					}
					stageIndex++;
				}
			}
		}
		
		//// Recovery selection 
		separationTableModel = new SeparationTableModel();
		separationTable = new JTable(separationTableModel);
		separationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		separationTable.setRowSelectionAllowed(true);
		separationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					// FIXME: Listen to selection change, not clicks
					// Single click updates selection
					updateButtonState();
				} else if (e.getClickCount() == 2) {
					// Double-click edits 
					selectDeployment();
				}
			}
		});
		
		JScrollPane scroll = new JScrollPane(separationTable);
		this.add(scroll, "span, grow, wrap");
		
		//// Select deployment
		selectSeparationButton = new JButton(trans.get("edtmotorconfdlg.but.Selectseparation"));
		selectSeparationButton.setEnabled(false);
		selectSeparationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDeployment();
			}
		});
		this.add(selectSeparationButton, "skip, split, sizegroup button");
		
		//// Reset deployment
		resetDeploymentButton = new JButton(trans.get("edtmotorconfdlg.but.Resetseparation"));
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
		separationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	private Stage getSelectedStage() {
		int row = separationTable.getSelectedRow();
		if (row >= 0 && row < stages.length) {
			return stages[row];
		}
		return null;
	}
	
	private void selectDeployment() {
		Stage stage = getSelectedStage();
		if (stage == null) {
			return;
		}
		JDialog d = new SeparationSelectionDialog(flightConfigurationDialog, rocket, stage);
		d.setVisible(true);
		fireTableDataChanged();
	}
	
	private void resetDeployment() {
		Stage stage = getSelectedStage();
		if (stage == null) {
			return;
		}
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		stage.getStageSeparationConfiguration().resetDefault(id);
		fireTableDataChanged();
	}
	
	public void updateButtonState() {
		boolean componentSelected = getSelectedStage() != null;
		selectSeparationButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected);
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
			switch (columnIndex) {
			case 0:
				return d.getName();
			case 1:
				String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
				StageSeparationConfiguration separationConfig = d.getStageSeparationConfiguration().get(id);
				
				SeparationEvent event = separationConfig.getSeparationEvent();
				String str = event.toString();
				
				if (d.getStageSeparationConfiguration().isDefault(id)) {
					str = trans.get("SeparationConfigurationPanel.table.separation.default");
					str = str.replace("{0}", event.toString());
				} else {
					str = event.toString();
				}
				return str;
				
			default:
				throw new IndexOutOfBoundsException("column=" + columnIndex);
			}
			
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return trans.get("edtmotorconfdlg.tbl.Stageheader");
			case 1:
				return trans.get("edtmotorconfdlg.tbl.Separationheader");
			default:
				return "";
			}
		}
		
	}
	
}
