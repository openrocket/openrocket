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
import net.sf.openrocket.gui.dialogs.flightconfiguration.SeparationSelectionDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Pair;

public class SeparationConfigurationPanel extends FlightConfigurablePanel<Stage> {
	
	static final Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private JTable separationTable;
	private FlightConfigurableTableModel<Stage> separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;
	
	
	SeparationConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel,rocket);
		
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
		this.add(selectSeparationButton, "split, sizegroup button");
		
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
	
	@Override
	protected JTable initializeTable() {
		//// Separation selection 
		separationTableModel = new FlightConfigurableTableModel<Stage>(Stage.class, rocket);
		separationTable = new JTable(separationTableModel);
		separationTable.setCellSelectionEnabled(true);
		separationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		
		return separationTable;
	}

	@Override
	protected JTable getTable() {
		return separationTable;
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
	

	private void selectDeployment() {
		Stage stage = getSelectedComponent();
		if (stage == null) {
			return;
		}
		JDialog d = new SeparationSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, stage);
		d.setVisible(true);
		fireTableDataChanged();
	}
	
	private void resetDeployment() {
		Stage stage = getSelectedComponent();
		if (stage == null) {
			return;
		}
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		stage.getStageSeparationConfiguration().resetDefault(id);
		fireTableDataChanged();
	}
	public void updateButtonState() {
		boolean componentSelected = getSelectedComponent() != null;
		selectSeparationButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected);
	}
	
	private class SeparationTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!(c instanceof JLabel)) {
				return c;
			}
			JLabel label = (JLabel) c;

			switch (column) {
			case 0: {
				label.setText(descriptor.format(rocket, (String) value));
				regular(label);
				return label;
			}
			default: {
				Pair<String, Stage> v = (Pair<String, Stage>) value;
				String id = v.getU();
				Stage stage = v.getV();
				StageSeparationConfiguration sepConfig = stage.getStageSeparationConfiguration().get(id);
				String spec = getSeparationSpecification(sepConfig);
				label.setText(spec);
				if (stage.getStageSeparationConfiguration().isDefault(id)) {
					shaded(label);
				} else {
					regular(label);
				}
				break;
			}
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
		
		private String getSeparationSpecification( StageSeparationConfiguration sepConfig ) {
			String str;
			
			str = sepConfig.getSeparationEvent().toString();
			if (sepConfig.getSeparationDelay() > 0.001) {
				str += " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(sepConfig.getSeparationDelay());
			}
			
			return str;

		}
	}
	
	
}
