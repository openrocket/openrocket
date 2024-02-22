package info.openrocket.swing.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.dialogs.flightconfiguration.SeparationSelectionDialog;
import info.openrocket.swing.gui.main.FlightConfigurationPanel;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class SeparationConfigurationPanel extends FlightConfigurablePanel<AxialStage> {
	private static final long serialVersionUID = -1556652925279847316L;
	static final Translator trans = Application.getTranslator();
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	private FlightConfigurableTableModel<AxialStage> separationTableModel;
	private final JButton selectSeparationButton;
	private final JButton resetDeploymentButton;
	private final JPopupMenu popupMenuFull;		// popup menu containing all the options


	public SeparationConfigurationPanel(FlightConfigurationPanel flightConfigurationPanel, OpenRocketDocument document, Rocket rocket) {
		super(flightConfigurationPanel, document, rocket);
		
		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "span, grow, pushy, wrap");

		// Get all the actions
		AbstractAction selectSeparationAction = new SelectSeparationAction();
		AbstractAction resetSeparationAction = new ResetSeparationAction();
		AbstractAction renameConfigAction = flightConfigurationPanel.getRenameConfigAction();
		AbstractAction deleteConfigAction = flightConfigurationPanel.getDeleteConfigAction();
		AbstractAction duplicateConfigAction = flightConfigurationPanel.getDuplicateConfigAction();

		// Populate the popup menu
		popupMenuFull = new JPopupMenu();
		popupMenuFull.add(selectSeparationAction);
		popupMenuFull.add(resetSeparationAction);
		popupMenuFull.addSeparator();
		popupMenuFull.add(renameConfigAction);
		popupMenuFull.add(deleteConfigAction);
		popupMenuFull.add(duplicateConfigAction);
		
		//// Select separation
		selectSeparationButton = new SelectColorButton(selectSeparationAction);
		selectSeparationButton.setEnabled(false);
		this.add(selectSeparationButton, "split, align right, sizegroup button");
		
		//// Reset separation
		resetDeploymentButton = new SelectColorButton(resetSeparationAction);
		resetDeploymentButton.setEnabled(false);
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

		ListSelectionListener listener = new ListSelectionListener() {
			private int previousRow = -1;
			private int previousColumn = -1;

			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (table != null && (separationTable.getSelectedRow() != previousRow ||
						separationTable.getSelectedColumn() != previousColumn)) {
					updateButtonState();
					previousRow = separationTable.getSelectedRow();
					previousColumn = separationTable.getSelectedColumn();
				}
			}
		};

		separationTable.getSelectionModel().addListSelectionListener(listener);
		separationTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);

		separationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedColumn = table.getSelectedColumn();

				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (selectedColumn > 0) {
						selectSeparation();
					}
				} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					// Get the row and column of the selected cell
					int r = separationTable.rowAtPoint(e.getPoint());
					int c = separationTable.columnAtPoint(e.getPoint());

					// Select new cell
					if (!separationTable.isCellSelected(r, c)) {
						if (r >= 0 && r < separationTable.getRowCount() &&
								c >= 0 && c < separationTable.getColumnCount()) {
							separationTable.setRowSelectionInterval(r, r);
							separationTable.setColumnSelectionInterval(c, c);
						} else {
							separationTable.clearSelection();
							return;
						}
					}

					if (c > 0) {
						doPopupFull(e);
					} else {
						flightConfigurationPanel.doPopupConfig(e);
					}
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

		AxialStage initStage = stages.get(0);			// Arbitrary choice of stage (all stages should have the same settings due to multi-comp editing)
		FlightConfigurationId initFcId = fcIds.get(0);

		// Store the initial configuration so we can check later whether something changed
		StageSeparationConfiguration initialConfig = initStage.getSeparationConfigurations().get(initFcId).copy(initFcId);

		document.addUndoPosition("Select separation");

		// Launch the separation config dialog
		SeparationSelectionDialog d = new SeparationSelectionDialog(SwingUtilities.getWindowAncestor(this), rocket, initStage, initFcId);
		d.setVisible(true);

		final StageSeparationConfiguration modifiedConfig = initStage.getSeparationConfigurations().get(initFcId);
		boolean update = !initialConfig.equals(modifiedConfig);

		double separationDelay = initStage.getSeparationConfigurations().get(initFcId).getSeparationDelay();
		SeparationEvent separationEvent= initStage.getSeparationConfigurations().get(initFcId).getSeparationEvent();
		boolean isOverrideDefault = d.isOverrideDefault();

		// Parse all stages and flight configurations to check whether we need to update
		for (AxialStage stage : stages) {
			for (FlightConfigurationId fcId : fcIds) {
				if ((stage == initStage) && (fcId == initFcId))
					continue;

				// It could be that the current config is the default config, but the user has selected to override it.
				if (isOverrideDefault && !stage.getSeparationConfigurations().containsId(fcId)) {
					stage.getSeparationConfigurations().set(fcId, stage.getSeparationConfigurations().getDefault().clone());
				}

				StageSeparationConfiguration currentConfig = stage.getSeparationConfigurations().get(fcId);

				if (currentConfig.equals(modifiedConfig)) {
					continue;
				}

				update = true;

				currentConfig.setSeparationDelay(separationDelay);
				currentConfig.setSeparationEvent(separationEvent);
				stage.getSeparationConfigurations().set(fcId, currentConfig);
			}
		}

		if (update) {
			fireTableDataChanged(ComponentChangeEvent.AEROMASS_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}
	
	private void resetSeparation() {
		List<AxialStage> stages = getSelectedComponents();
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if ((stages == null) || (fcIds == null) || (stages.size() == 0) || (fcIds.size() == 0)) {
			return;
		}

		document.addUndoPosition("Reset separation");

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
			fireTableDataChanged(ComponentChangeEvent.AEROMASS_CHANGE, fcIds.toArray(new FlightConfigurationId[0]));
		} else {
			table.requestFocusInWindow();
		}
	}

	private void doPopupFull(MouseEvent e) {
		popupMenuFull.show(e.getComponent(), e.getX(), e.getY());
	}

	public void updateRocketViewSelection(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() || getSelectedComponents() == null) {
			return;
		}
		List<RocketComponent> components = new ArrayList<>(getSelectedComponents());
		if (components.size() == 0) return;

		flightConfigurationPanel.setSelectedComponents(components);
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

	private class SelectSeparationAction extends AbstractAction {
		public SelectSeparationAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Selectseparation"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectSeparation();
		}
	}
	private class ResetSeparationAction extends AbstractAction {
		public ResetSeparationAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Resetseparation"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			resetSeparation();
		}
	}
	
}
