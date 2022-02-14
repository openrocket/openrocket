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
import net.sf.openrocket.gui.dialogs.flightconfiguration.SeparationSelectionDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.gui.widgets.SelectColorButton;

public class SeparationConfigurationPanel extends FlightConfigurablePanel<AxialStage> {
	private static final long serialVersionUID = -1556652925279847316L;
	static final Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private FlightConfigurableTableModel<AxialStage> separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;
	
	
	SeparationConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel,rocket);
		
		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "span, grow, wrap");
		
		//// Select deployment
		selectSeparationButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Selectseparation"));
		selectSeparationButton.setEnabled(false);
		selectSeparationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectSeparation();
			}
		});
		this.add(selectSeparationButton, "split, align right, sizegroup button");
		
		//// Reset deployment
		resetDeploymentButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Resetseparation"));
		resetDeploymentButton.setEnabled(false);
		resetDeploymentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetSeparation();
			}
		});
		this.add(resetDeploymentButton, "sizegroup button, wrap");

		// Set 'Enter' key action to open the separation selection dialog
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				selectSeparation();
			}
		});
	}
	
	@Override
	protected JTable initializeTable() {
		//// Separation selection 
		separationTableModel = new FlightConfigurableTableModel<AxialStage>(AxialStage.class, rocket) {
			private static final long serialVersionUID = 7979648984099308970L;

			@Override
			protected boolean includeComponent(AxialStage component) {
				return component.getStageNumber() > 0;
			}

			@Override
			public void componentChanged(ComponentChangeEvent cce) {
				super.componentChanged(cce);
				// This will catch a name change of the stage to cause a change in the header of the table
				if (cce.getSource() instanceof AxialStage && cce.isNonFunctionalChange()) {
					fireTableStructureChanged();
				}
			}
		};
		JTable separationTable = new JTable(separationTableModel);
		separationTable.getTableHeader().setReorderingAllowed(false);
		separationTable.setCellSelectionEnabled(true);
		separationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		separationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonState();
				if (e.getClickCount() == 2) {
					// Double-click edits 
					selectSeparation();
				}
			}
		});
		separationTable.setDefaultRenderer(Object.class, new SeparationTableCellRenderer());
		
		return separationTable;
	}

	public void selectSeparation() {
		List<AxialStage> stages = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((stages == null) || (fcIds == null) || (stages.size() == 0) || (fcIds.size() == 0)) {
			return;
		}

		boolean update = false;
		AxialStage initStage = stages.get(0);
		FlightConfigurationId initFcId = fcIds.get(0);

		StageSeparationConfiguration initialConfig = initStage.getSeparationConfigurations().get(initFcId).copy(initFcId);
		JDialog d = new SeparationSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, initStage);
		d.setVisible(true);

		if (!initialConfig.equals(initStage.getSeparationConfigurations().get(initFcId))) {
			update = true;
		}

		double separationDelay = initStage.getSeparationConfigurations().get(initFcId).getSeparationDelay();
		SeparationEvent separationEvent= initStage.getSeparationConfigurations().get(initFcId).getSeparationEvent();

		for (int i = 0; i < stages.size(); i++) {
			for (int j = 0; j < fcIds.size(); j++) {
				if ((i == 0) && (j == 0)) break;

				final AxialStage stage = stages.get(i);
				final FlightConfigurationId fcId = fcIds.get(j);
				StageSeparationConfiguration config = stage.getSeparationConfigurations().get(fcId);
				initialConfig = config.copy(fcId);

				if (stage.getSeparationConfigurations().isDefault(config)) {
					config = config.clone();
				}

				config.setSeparationDelay(separationDelay);
				config.setSeparationEvent(separationEvent);
				stage.getSeparationConfigurations().set(fcId, config);

				if (!initialConfig.equals(config)) {
					update = true;
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AEROMASS_CHANGE);
		}
	}
	
	private void resetSeparation() {
		List<AxialStage> stages = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((stages == null) || (fcIds == null) || (stages.size() == 0) || (fcIds.size() == 0)) {
			return;
		}

		boolean update = false;
		for (AxialStage stage : stages) {
			for (FlightConfigurationId fcId : fcIds) {
				StageSeparationConfiguration initialConfig = stage.getSeparationConfigurations().get(fcId).copy(fcId);
				stage.getSeparationConfigurations().reset(fcId);

				if (!initialConfig.equals(stage.getSeparationConfigurations().get(fcId))) {
					update = true;
				}
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AEROMASS_CHANGE);
		}
	}
	public void updateButtonState() {
		boolean componentSelected = getSelectedComponent() != null;
		selectSeparationButton.setEnabled(componentSelected);
		resetDeploymentButton.setEnabled(componentSelected);
	}
	
	private class SeparationTableCellRenderer extends FlightConfigurablePanel<AxialStage>.FlightConfigurableCellRenderer {
		private static final long serialVersionUID = -7066580803931938686L;

		@Override
		protected JLabel format(AxialStage stage, FlightConfigurationId configId, JLabel label) {
			StageSeparationConfiguration sepConfig = stage.getSeparationConfigurations().get(configId);
			String spec = getSeparationSpecification(sepConfig);
			label.setText(spec);
			if (stage.getSeparationConfigurations().isDefault(configId)) {
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
