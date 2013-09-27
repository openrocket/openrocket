package net.sf.openrocket.gui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.util.SaveCSVWorker;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;

public class SimulationExportPanel extends JPanel {
	
	private static final String SPACE = "SPACE";
	private static final String TAB = "TAB";
	private static final Translator trans = Application.getTranslator();
	
	private static final int OPTION_SIMULATION_COMMENTS = 0;
	private static final int OPTION_FIELD_DESCRIPTIONS = 1;
	private static final int OPTION_FLIGHT_EVENTS = 2;
	
	private final JTable table;
	private final SelectionTableModel tableModel;
	private final JLabel selectedCountLabel;
	
	private final Simulation simulation;
	private final FlightDataBranch branch;
	
	private final boolean[] selected;
	private final FlightDataType[] types;
	private final Unit[] units;
	
	private final CsvOptionPanel csvOptions;
	
	
	public SimulationExportPanel(Simulation sim) {
		super(new MigLayout("fill, flowy"));
		
		JPanel panel;
		JButton button;
		

		this.simulation = sim;
		
		// TODO: MEDIUM: Only exports primary branch
		
		final FlightData data = simulation.getSimulatedData();
		
		// Check that data exists
		if (data == null || data.getBranchCount() == 0 ||
				data.getBranch(0).getTypes().length == 0) {
			throw new IllegalArgumentException("No data for panel");
		}
		

		// Create the data model
		branch = data.getBranch(0);
		
		types = branch.getTypes();
		Arrays.sort(types);
		
		selected = new boolean[types.length];
		units = new Unit[types.length];
		for (int i = 0; i < types.length; i++) {
			selected[i] = ((SwingPreferences) Application.getPreferences()).isExportSelected(types[i]);
			units[i] = types[i].getUnitGroup().getDefaultUnit();
		}
		

		//// Create the panel
		

		// Set up the variable selection table
		tableModel = new SelectionTableModel();
		table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class,
				new SelectionBackgroundCellRenderer(table.getDefaultRenderer(Object.class)));
		table.setDefaultRenderer(Boolean.class,
				new SelectionBackgroundCellRenderer(table.getDefaultRenderer(Boolean.class)));
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		
		table.setDefaultEditor(Unit.class, new UnitCellEditor() {
			@Override
			protected UnitGroup getUnitGroup(Unit value, int row, int column) {
				return types[row].getUnitGroup();
			}
		});
		
		// Set column widths
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn col = columnModel.getColumn(0);
		int w = table.getRowHeight();
		col.setMinWidth(w);
		col.setPreferredWidth(w);
		col.setMaxWidth(w);
		
		col = columnModel.getColumn(1);
		col.setPreferredWidth(200);
		
		col = columnModel.getColumn(2);
		col.setPreferredWidth(100);
		
		table.addMouseListener(new GUIUtil.BooleanTableClickListener(table));
		
		// Add table
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Vartoexport")));
		
		panel.add(new JScrollPane(table), "wmin 300lp, width 300lp, height 1, grow 100, wrap");
		
		// Select all/none buttons
		button = new JButton(trans.get("SimExpPan.but.Selectall"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.selectAll();
			}
		});
		panel.add(button, "split 2, growx 1, sizegroup selectbutton");
		
		button = new JButton(trans.get("SimExpPan.but.Selectnone"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.selectNone();
			}
		});
		panel.add(button, "growx 1, sizegroup selectbutton, wrap");
		

		selectedCountLabel = new JLabel();
		updateSelectedCount();
		panel.add(selectedCountLabel);
		
		this.add(panel, "grow 100, wrap");
		

		// These need to be in the order of the OPTIONS_XXX indices
		csvOptions = new CsvOptionPanel(SimulationExportPanel.class,
				trans.get("SimExpPan.checkbox.Includesimudesc"),
				trans.get("SimExpPan.checkbox.ttip.Includesimudesc"),
				trans.get("SimExpPan.checkbox.Includefielddesc"),
				trans.get("SimExpPan.checkbox.ttip.Includefielddesc"),
				trans.get("SimExpPan.checkbox.Incflightevents"),
				trans.get("SimExpPan.checkbox.ttip.Incflightevents"));
		
		this.add(csvOptions, "spany, split, growx 1");
		

		// Space-filling panel
		panel = new JPanel();
		this.add(panel, "width 1, height 1, grow 1");
		

		// Export button
		button = new JButton(trans.get("SimExpPan.but.Exporttofile"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExport();
			}
		});
		this.add(button, "gapbottom para, gapright para, right");
		
	}
	
	
	private void doExport() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(FileHelper.CSV_FILE_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File file = chooser.getSelectedFile();
		if (file == null)
			return;
		
		file = FileHelper.ensureExtension(file, "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return;
		}
		

		String commentChar = csvOptions.getCommentCharacter();
		String fieldSep = csvOptions.getFieldSeparator();
		boolean simulationComment = csvOptions.getSelectionOption(OPTION_SIMULATION_COMMENTS);
		boolean fieldComment = csvOptions.getSelectionOption(OPTION_FIELD_DESCRIPTIONS);
		boolean eventComment = csvOptions.getSelectionOption(OPTION_FLIGHT_EVENTS);
		csvOptions.storePreferences();
		
		// Store preferences and export
		int n = 0;
		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		for (int i = 0; i < selected.length; i++) {
			((SwingPreferences) Application.getPreferences()).setExportSelected(types[i], selected[i]);
			if (selected[i])
				n++;
		}
		

		FlightDataType[] fieldTypes = new FlightDataType[n];
		Unit[] fieldUnits = new Unit[n];
		int pos = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				fieldTypes[pos] = types[i];
				fieldUnits[pos] = units[i];
				pos++;
			}
		}
		
		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}
		

		SaveCSVWorker.export(file, simulation, branch, fieldTypes, fieldUnits, fieldSep,
				commentChar, simulationComment, fieldComment, eventComment,
				SwingUtilities.getWindowAncestor(this));
	}
	
	
	private void updateSelectedCount() {
		int total = selected.length;
		int n = 0;
		String str;
		
		for (int i = 0; i < selected.length; i++) {
			if (selected[i])
				n++;
		}
		
		if (n == 1) {
			//// Exporting 1 variable out of 
			str = trans.get("SimExpPan.ExportingVar.desc1") + " " + total + ".";
		} else {
			//// Exporting 
			//// variables out of
			str = trans.get("SimExpPan.ExportingVar.desc2") + " " + n + " " +
					trans.get("SimExpPan.ExportingVar.desc3") + " " + total + ".";
		}
		
		selectedCountLabel.setText(str);
	}
	
	

	/**
	 * A table cell renderer that uses another renderer and sets the background and
	 * foreground of the returned component based on the selection of the variable.
	 */
	private class SelectionBackgroundCellRenderer implements TableCellRenderer {
		
		private final TableCellRenderer renderer;
		
		public SelectionBackgroundCellRenderer(TableCellRenderer renderer) {
			this.renderer = renderer;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable myTable, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			
			Component component = renderer.getTableCellRendererComponent(myTable,
					value, isSelected, hasFocus, row, column);
			
			if (selected[row]) {
				component.setBackground(myTable.getSelectionBackground());
				component.setForeground(myTable.getSelectionForeground());
			} else {
				component.setBackground(myTable.getBackground());
				component.setForeground(myTable.getForeground());
			}
			
			return component;
		}
		
	}
	
	
	/**
	 * The table model for the variable selection.
	 */
	private class SelectionTableModel extends AbstractTableModel {
		private static final int SELECTED = 0;
		private static final int NAME = 1;
		private static final int UNIT = 2;
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public int getRowCount() {
			return types.length;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case SELECTED:
				return "";
			case NAME:
				//// Variable
				return trans.get("SimExpPan.Col.Variable");
			case UNIT:
				//// Unit
				return trans.get("SimExpPan.Col.Unit");
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case SELECTED:
				return Boolean.class;
			case NAME:
				return FlightDataType.class;
			case UNIT:
				return Unit.class;
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			
			switch (column) {
			case SELECTED:
				return selected[row];
				
			case NAME:
				return types[row];
				
			case UNIT:
				return units[row];
				
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			
			switch (column) {
			case SELECTED:
				selected[row] = (Boolean) value;
				this.fireTableRowsUpdated(row, row);
				updateSelectedCount();
				break;
			
			case NAME:
				break;
			
			case UNIT:
				units[row] = (Unit) value;
				break;
			
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
			case SELECTED:
				return true;
				
			case NAME:
				return false;
				
			case UNIT:
				return types[row].getUnitGroup().getUnitCount() > 1;
				
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
		}
		
		public void selectAll() {
			Arrays.fill(selected, true);
			updateSelectedCount();
			this.fireTableDataChanged();
		}
		
		public void selectNone() {
			Arrays.fill(selected, false);
			updateSelectedCount();
			this.fireTableDataChanged();
		}
		
	}
	
}
