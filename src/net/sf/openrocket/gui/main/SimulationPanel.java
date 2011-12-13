package net.sf.openrocket.gui.main;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Prefs;

public class SimulationPanel extends JPanel {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	
	private static final Color WARNING_COLOR = Color.RED;
	private static final String WARNING_TEXT = "\uFF01"; // Fullwidth exclamation mark
	
	private static final Color OK_COLOR = new Color(60, 150, 0);
	private static final String OK_TEXT = "\u2714"; // Heavy check mark
	


	private final OpenRocketDocument document;
	
	private final ColumnTableModel simulationTableModel;
	private final JTable simulationTable;
	
	
	public SimulationPanel(OpenRocketDocument doc) {
		super(new MigLayout("fill", "[grow][][][][][][grow]"));
		
		JButton button;
		

		this.document = doc;
		


		////////  The simulation action buttons
		
		//// New simulation button
		button = new JButton(trans.get("simpanel.but.newsimulation"));
		//// Add a new simulation
		button.setToolTipText(trans.get("simpanel.but.ttip.newsimulation"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Simulation sim = new Simulation(document.getRocket());
				sim.setName(document.getNextSimulationName());
				
				int n = document.getSimulationCount();
				document.addSimulation(sim);
				simulationTableModel.fireTableDataChanged();
				simulationTable.clearSelection();
				simulationTable.addRowSelectionInterval(n, n);
				
				openDialog(sim, SimulationEditDialog.EDIT);
			}
		});
		this.add(button, "skip 1, gapright para");
		
		//// Edit simulation button
		button = new JButton(trans.get("simpanel.but.editsimulation"));
		//// Edit the selected simulation
		button.setToolTipText(trans.get("simpanel.but.ttip.editsim"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = simulationTable.getSelectedRow();
				if (selected < 0)
					return; // TODO: MEDIUM: "None selected" dialog
					
				selected = simulationTable.convertRowIndexToModel(selected);
				simulationTable.clearSelection();
				simulationTable.addRowSelectionInterval(selected, selected);
				
				openDialog(document.getSimulations().get(selected), SimulationEditDialog.EDIT);
			}
		});
		this.add(button, "gapright para");
		
		//// Run simulations
		button = new JButton(trans.get("simpanel.but.runsimulations"));
		//// Re-run the selected simulations
		button.setToolTipText(trans.get("simpanel.but.ttip.runsimu"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selection = simulationTable.getSelectedRows();
				if (selection.length == 0)
					return; // TODO: LOW: "None selected" dialog
					
				Simulation[] sims = new Simulation[selection.length];
				for (int i = 0; i < selection.length; i++) {
					selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
					sims[i] = document.getSimulation(selection[i]);
				}
				
				long t = System.currentTimeMillis();
				new SimulationRunDialog(SwingUtilities.getWindowAncestor(
							SimulationPanel.this), sims).setVisible(true);
				log.info("Running simulations took " + (System.currentTimeMillis() - t) + " ms");
				fireMaintainSelection();
			}
		});
		this.add(button, "gapright para");
		
		//// Delete simulations button
		button = new JButton(trans.get("simpanel.but.deletesimulations"));
		//// Delete the selected simulations
		button.setToolTipText(trans.get("simpanel.but.ttip.deletesim"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selection = simulationTable.getSelectedRows();
				if (selection.length == 0)
					return; // TODO: LOW: "None selected" dialog
					
				// Verify deletion
				boolean verify = Application.getPreferences().getBoolean(Prefs.CONFIRM_DELETE_SIMULATION, true);
				if (verify) {
					
					JPanel panel = new JPanel(new MigLayout());
					//// Do not ask me again
					JCheckBox dontAsk = new JCheckBox(trans.get("simpanel.checkbox.donotask"));
					panel.add(dontAsk, "wrap");
					//// You can change the default operation in the preferences.
					panel.add(new StyledLabel(trans.get("simpanel.lbl.defpref"), -2));
					
					int ret = JOptionPane.showConfirmDialog(SimulationPanel.this,
								new Object[] {
										//// Delete the selected simulations?
										trans.get("simpanel.dlg.lbl.DeleteSim1"),
										//// <html><i>This operation cannot be undone.</i>
										trans.get("simpanel.dlg.lbl.DeleteSim2"),
										"",
										panel },
								//// Delete simulations
										trans.get("simpanel.dlg.lbl.DeleteSim3"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE);
					if (ret != JOptionPane.OK_OPTION)
						return;
					
					if (dontAsk.isSelected()) {
						Application.getPreferences().putBoolean(Prefs.CONFIRM_DELETE_SIMULATION, false);
					}
				}
				
				// Delete simulations
				for (int i = 0; i < selection.length; i++) {
					selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
				}
				Arrays.sort(selection);
				for (int i = selection.length - 1; i >= 0; i--) {
					document.removeSimulation(selection[i]);
				}
				simulationTableModel.fireTableDataChanged();
			}
		});
		this.add(button, "gapright para");
		
		//// Plot / export button
		button = new JButton(trans.get("simpanel.but.plotexport"));
		//		button = new JButton("Plot flight");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selected = simulationTable.getSelectedRow();
				if (selected < 0)
					return; // TODO: MEDIUM: "None selected" dialog
					
				selected = simulationTable.convertRowIndexToModel(selected);
				simulationTable.clearSelection();
				simulationTable.addRowSelectionInterval(selected, selected);
				
				openDialog(document.getSimulations().get(selected), SimulationEditDialog.PLOT);
			}
		});
		this.add(button, "wrap para");
		



		////////  The simulation table
		
		simulationTableModel = new ColumnTableModel(

		////  Status and warning column
				new Column("") {
					private JLabel label = null;
					
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						// Initialize the label
						if (label == null) {
							label = new StyledLabel(2f);
							label.setIconTextGap(1);
							//							label.setFont(label.getFont().deriveFont(Font.BOLD));
						}
						
						// Set simulation status icon
						Simulation.Status status = document.getSimulation(row).getStatus();
						label.setIcon(Icons.SIMULATION_STATUS_ICON_MAP.get(status));
						

						// Set warning marker
						if (status == Simulation.Status.NOT_SIMULATED ||
								status == Simulation.Status.EXTERNAL) {
							
							label.setText("");
							
						} else {
							
							WarningSet w = document.getSimulation(row).getSimulatedWarnings();
							if (w == null) {
								label.setText("");
							} else if (w.isEmpty()) {
								label.setForeground(OK_COLOR);
								label.setText(OK_TEXT);
							} else {
								label.setForeground(WARNING_COLOR);
								label.setText(WARNING_TEXT);
							}
						}
						
						return label;
					}
					
					@Override
					public int getExactWidth() {
						return 36;
					}
					
					@Override
					public Class<?> getColumnClass() {
						return JLabel.class;
					}
				},

				//// Simulation name
				//// Name
				new Column(trans.get("simpanel.col.Name")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						return document.getSimulation(row).getName();
					}
					
					@Override
					public int getDefaultWidth() {
						return 125;
					}
				},

				//// Simulation motors
				//// Motors
				new Column(trans.get("simpanel.col.Motors")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						return document.getSimulation(row).getConfiguration()
								.getMotorConfigurationDescription();
					}
					
					@Override
					public int getDefaultWidth() {
						return 125;
					}
				},

				//// Apogee
				new Column(trans.get("simpanel.col.Apogee")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_DISTANCE.getDefaultUnit().toStringUnit(
								data.getMaxAltitude());
					}
				},

				//// Maximum velocity
				new Column(trans.get("simpanel.col.Maxvelocity")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getMaxVelocity());
					}
				},

				//// Maximum acceleration
				new Column(trans.get("simpanel.col.Maxacceleration")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_ACCELERATION.getDefaultUnit().toStringUnit(
								data.getMaxAcceleration());
					}
				},

				//// Time to apogee
				new Column(trans.get("simpanel.col.Timetoapogee")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit().toStringUnit(
								data.getTimeToApogee());
					}
				},

				//// Flight time
				new Column(trans.get("simpanel.col.Flighttime")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit().toStringUnit(
								data.getFlightTime());
					}
				},

				//// Ground hit velocity
				new Column(trans.get("simpanel.col.Groundhitvelocity")) {
					@Override
					public Object getValueAt(int row) {
						if (row < 0 || row >= document.getSimulationCount())
							return null;
						
						FlightData data = document.getSimulation(row).getSimulatedData();
						if (data == null)
							return null;
						
						return UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(
								data.getGroundHitVelocity());
					}
				}

		) {
			@Override
			public int getRowCount() {
				return document.getSimulationCount();
			}
		};
		
		// Override processKeyBinding so that the JTable does not catch
		// key bindings used in menu accelerators
		simulationTable = new JTable(simulationTableModel) {
			@Override
			protected boolean processKeyBinding(KeyStroke ks,
					KeyEvent e,
					int condition,
					boolean pressed) {
				return false;
			}
		};
		simulationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		simulationTable.setDefaultRenderer(Object.class, new JLabelRenderer());
		simulationTableModel.setColumnWidths(simulationTable.getColumnModel());
		

		// Mouse listener to act on double-clicks
		simulationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					int selected = simulationTable.getSelectedRow();
					if (selected < 0)
						return;
					
					selected = simulationTable.convertRowIndexToModel(selected);
					simulationTable.clearSelection();
					simulationTable.addRowSelectionInterval(selected, selected);
					
					openDialog(document.getSimulations().get(selected),
							SimulationEditDialog.DEFAULT);
				}
			}
		});
		
		document.addDocumentChangeListener(new DocumentChangeListener() {
			@Override
			public void documentChanged(DocumentChangeEvent event) {
				if (!(event instanceof SimulationChangeEvent))
					return;
				simulationTableModel.fireTableDataChanged();
			}
		});
		



		// Fire table change event when the rocket changes
		document.getRocket().addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				fireMaintainSelection();
			}
		});
		

		JScrollPane scrollpane = new JScrollPane(simulationTable);
		this.add(scrollpane, "spanx, grow, wrap rel");
		

	}
	
	
	public ListSelectionModel getSimulationListSelectionModel() {
		return simulationTable.getSelectionModel();
	}
	
	private void openDialog(final Simulation sim, int position) {
		new SimulationEditDialog(SwingUtilities.getWindowAncestor(this), sim, position)
				.setVisible(true);
		fireMaintainSelection();
	}
	
	private void fireMaintainSelection() {
		int[] selection = simulationTable.getSelectedRows();
		simulationTableModel.fireTableDataChanged();
		for (int row : selection) {
			if (row >= simulationTableModel.getRowCount())
				break;
			simulationTable.addRowSelectionInterval(row, row);
		}
	}
	
	
	private class JLabelRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (row < 0 || row >= document.getSimulationCount())
				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			
			// A JLabel is self-contained and has set its own tool tip
			if (value instanceof JLabel) {
				JLabel label = (JLabel) value;
				if (isSelected)
					label.setBackground(table.getSelectionBackground());
				else
					label.setBackground(table.getBackground());
				label.setOpaque(true);
				
				label.setToolTipText(getSimulationToolTip(document.getSimulation(row)));
				return label;
			}
			
			Component component = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			
			if (component instanceof JComponent) {
				((JComponent) component).setToolTipText(getSimulationToolTip(
						document.getSimulation(row)));
			}
			return component;
		}
		
		private String getSimulationToolTip(Simulation sim) {
			String tip;
			FlightData data = sim.getSimulatedData();
			
			tip = "<html><b>" + sim.getName() + "</b><br>";
			switch (sim.getStatus()) {
			case UPTODATE:
				//// <i>Up to date</i><br>
				tip += "<i>Up to date</i><br>";
				break;
			
			case LOADED:
				//// <i>Data loaded from a file</i><br>
				tip += "<i>Data loaded from a file</i><br>";
				break;
			
			case OUTDATED:
				tip += "<i><font color=\"red\">Data is out of date</font></i><br>";
				tip += "Click <i><b>Run simulations</b></i> to simulate.<br>";
				break;
			
			case EXTERNAL:
				tip += "<i>Imported data</i><br>";
				return tip;
				
			case NOT_SIMULATED:
				tip += "<i>Not simulated yet</i><br>";
				tip += "Click <i><b>Run simulations</b></i> to simulate.";
				return tip;
			}
			
			if (data == null) {
				tip += "No simulation data available.";
				return tip;
			}
			WarningSet warnings = data.getWarningSet();
			
			if (warnings.isEmpty()) {
				tip += "<font color=\"gray\">No warnings.</font>";
				return tip;
			}
			
			tip += "<font color=\"red\">Warnings:</font>";
			for (Warning w : warnings) {
				tip += "<br>" + w.toString();
			}
			
			return tip;
		}
		
	}
}
