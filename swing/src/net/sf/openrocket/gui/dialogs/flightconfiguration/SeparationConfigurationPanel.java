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
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

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
				updateButtonState();
				if (e.getClickCount() == 2) {
					// Double-click edits 
					selectDeployment();
				}
			}
		});
		separationTable.setDefaultRenderer(Object.class, new SeparationTableCellRenderer());
		
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
		int selected = separationTable.getSelectedRow();
		separationTableModel.fireTableDataChanged();
		if (selected >= 0) {
			selected = Math.min(selected, separationTable.getRowCount() - 1);
			separationTable.getSelectionModel().setSelectionInterval(selected, selected);
		}
		updateButtonState();
	}
	
	private Stage getSelectedStage() {
		int row = separationTable.getSelectedRow();
		return getStage(row);
	}
	
	private Stage getStage(int row) {
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
				StageSeparationConfiguration config = d.getStageSeparationConfiguration().get(id);
				
				String str;
				
				str = config.getSeparationEvent().toString();
				if (config.getSeparationDelay() > 0.001) {
					str += " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(config.getSeparationDelay());
				}
				
				if (d.getStageSeparationConfiguration().isDefault(id)) {
					String def = trans.get("SeparationConfigurationPanel.table.separation.default");
					str = def.replace("{0}", str);
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
	
	
	private class SeparationTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!(c instanceof JLabel)) {
				return c;
			}
			JLabel label = (JLabel) c;
			
			Stage stage = getStage(row);
			String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
			
			switch (column) {
			case 0:
				regular(label);
				break;
			
			case 1:
				if (stage.getStageSeparationConfiguration().isDefault(id)) {
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
