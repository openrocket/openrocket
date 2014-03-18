package net.sf.openrocket.gui.main.flightconfigpanel;

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

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.dialogs.flightconfiguration.SeparationSelectionDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class SeparationConfigurationPanel extends FlightConfigurablePanel<Stage> {
	
	static final Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private FlightConfigurableTableModel<Stage> separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;
	
	
	SeparationConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel,rocket);
		
		JScrollPane scroll = new JScrollPane(table);
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
		this.add(selectSeparationButton, "split, align right, sizegroup button");
		
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
		separationTableModel = new FlightConfigurableTableModel<Stage>(Stage.class, rocket) {
			@Override
			protected boolean includeComponent(Stage component) {
				return component.getStageNumber() > 0;
			}

		};
		JTable separationTable = new JTable(separationTableModel);
		separationTable.getTableHeader().setReorderingAllowed(false);
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
	
	private class SeparationTableCellRenderer extends FlightConfigurablePanel<Stage>.FlightConfigurableCellRenderer {
		
		@Override
		protected JLabel format(Stage stage, String configId, JLabel label) {
			StageSeparationConfiguration sepConfig = stage.getStageSeparationConfiguration().get(configId);
			String spec = getSeparationSpecification(sepConfig);
			label.setText(spec);
			if (stage.getStageSeparationConfiguration().isDefault(configId)) {
				shaded(label);
			} else {
				regular(label);
			}
			return label;
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
