package info.openrocket.swing.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.simulation.LandingDispersionAnalysisCache;
import info.openrocket.swing.gui.simulation.LandingDispersionDialog;

import net.miginfocom.swing.MigLayout;

/** Selects the simulations included in the Monte Carlo PDF report. */
final class MonteCarloReportOptionsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();

	private final Window owner;
	private final SimulationTableModel model;
	private final JTable table;
	private final JButton configureButton;

	MonteCarloReportOptionsPanel(Window owner, OpenRocketDocument document) {
		super(new BorderLayout(0, 4));
		this.owner = owner;
		this.model = new SimulationTableModel(document.getSimulations());
		this.table = new JTable(model);
		this.configureButton = new JButton(trans.get("printdlg.monteCarlo.configure"));
		buildPanel();
	}

	private void buildPanel() {
		setBorder(BorderFactory.createTitledBorder(trans.get("printdlg.monteCarlo.title")));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(55);
		table.getColumnModel().getColumn(2).setPreferredWidth(145);
		table.getColumnModel().getColumn(3).setPreferredWidth(60);
		table.getColumnModel().getColumn(4).setPreferredWidth(90);
		table.getSelectionModel().addListSelectionListener(event -> updateConfigureButton());
		add(new JScrollPane(table), BorderLayout.CENTER);

		configureButton.addActionListener(event -> configureSelectedSimulation());
		JPanel controls = new JPanel(new MigLayout("ins 0", "[grow][button]"));
		controls.add(new JPanel(), "growx");
		controls.add(configureButton);
		add(controls, BorderLayout.SOUTH);
		updateConfigureButton();
	}

	List<Simulation> getSelectedSimulations() {
		return model.getSelectedSimulations();
	}

	void refreshStatuses() {
		model.refresh();
		updateConfigureButton();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (table != null) {
			table.setEnabled(enabled);
			updateConfigureButton();
		}
	}

	private void updateConfigureButton() {
		if (configureButton != null) {
			configureButton.setEnabled(isEnabled() && table.getSelectedRow() >= 0);
		}
	}

	private void configureSelectedSimulation() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			return;
		}
		Simulation simulation = model.getSimulation(table.convertRowIndexToModel(selectedRow));
		new LandingDispersionDialog(owner, simulation).setVisible(true);
		model.configurationChanged(simulation);
	}

	private static final class SimulationTableModel extends AbstractTableModel {
		private static final String[] COLUMN_KEYS = { "include", "simulation", "configuration", "runs", "status" };
		private final List<Row> rows;

		private SimulationTableModel(List<Simulation> simulations) {
			rows = new ArrayList<>(simulations.size());
			for (Simulation simulation : simulations) {
				rows.add(new Row(simulation, simulation.getLandingDispersionSettings() != null));
			}
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_KEYS.length;
		}

		@Override
		public String getColumnName(int column) {
			return trans.get("printdlg.monteCarlo.col." + COLUMN_KEYS[column]);
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return column == 0 ? Boolean.class : column == 3 ? Integer.class : String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0 && rows.get(row).simulation.getLandingDispersionSettings() != null;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Row row = rows.get(rowIndex);
			MonteCarloSettings settings = row.simulation.getLandingDispersionSettings();
			return switch (columnIndex) {
				case 0 -> row.selected;
				case 1 -> row.simulation.getName();
				case 2 -> row.simulation.getActiveConfiguration().getName();
				case 3 -> settings == null ? null : settings.getRunCount();
				case 4 -> status(row.simulation, settings);
				default -> throw new IllegalArgumentException("Unsupported column " + columnIndex);
			};
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				rows.get(rowIndex).selected = Boolean.TRUE.equals(value);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

		private List<Simulation> getSelectedSimulations() {
			return rows.stream()
					.filter(row -> row.selected && row.simulation.getLandingDispersionSettings() != null)
					.map(row -> row.simulation)
					.toList();
		}

		private Simulation getSimulation(int row) {
			return rows.get(row).simulation;
		}

		private void refresh() {
			fireTableDataChanged();
		}

		private void configurationChanged(Simulation simulation) {
			for (int index = 0; index < rows.size(); index++) {
				Row row = rows.get(index);
				if (row.simulation == simulation) {
					if (simulation.getLandingDispersionSettings() != null && !row.wasConfigured) {
						row.selected = true;
					}
					row.wasConfigured = simulation.getLandingDispersionSettings() != null;
					fireTableRowsUpdated(index, index);
					return;
				}
			}
		}

		private static String status(Simulation simulation, MonteCarloSettings settings) {
			if (settings == null) {
				return trans.get("printdlg.monteCarlo.status.notConfigured");
			}
			MonteCarloResult result = LandingDispersionAnalysisCache.get(simulation, settings);
			return trans.get(result == null
					? "printdlg.monteCarlo.status.runRequired"
					: "printdlg.monteCarlo.status.cached");
		}
	}

	private static final class Row {
		private final Simulation simulation;
		private boolean selected;
		private boolean wasConfigured;

		private Row(Simulation simulation, boolean configured) {
			this.simulation = simulation;
			this.selected = configured;
			this.wasConfigured = configured;
		}
	}
}
