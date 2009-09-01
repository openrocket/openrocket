package net.sf.openrocket.gui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;
import net.sf.openrocket.util.SaveCSVWorker;

public class SimulationExportPanel extends JPanel {

	private static final String SPACE = "SPACE";
	private static final String TAB = "TAB";
	
	private static final FileFilter CSV_FILE_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return "Comma Separated Files (*.csv)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName().toLowerCase();
			return name.endsWith(".csv");
		}
    };

	
	
	private final JTable table;
	private final SelectionTableModel tableModel;
	private final JLabel selectedCountLabel;
	
	private final Simulation simulation;
	private final FlightDataBranch branch;
	
	private final boolean[] selected;
	private final FlightDataBranch.Type[] types;
	private final Unit[] units;
	
	private final JComboBox fieldSeparator;
	private final JCheckBox simulationComments;
	private final JCheckBox fieldNameComments;
	private final JCheckBox eventComments;
	private final JComboBox commentCharacter;
	
	
	public SimulationExportPanel(Simulation sim) {
		super(new MigLayout("fill, flowy"));

		JLabel label;
		JPanel panel;
		JButton button;
		String tip;
		
		
		this.simulation = sim;
		
		// TODO: MEDIUM: Only exports primary branch
		
		final FlightData data = simulation.getSimulatedData();

		// Check that data exists
		if (data == null  || data.getBranchCount() == 0 ||
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
			selected[i] = Prefs.isExportSelected(types[i]);
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
		panel.setBorder(BorderFactory.createTitledBorder("Variables to export"));
		
		panel.add(new JScrollPane(table), "wmin 300lp, width 300lp, height 1, grow 100, wrap");
		
		// Select all/none buttons
		button = new JButton("Select all");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.selectAll();
			}
		});
		panel.add(button, "split 2, growx 1, sizegroup selectbutton");
		
		button = new JButton("Select none");
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
		
		
		
		// Field separator panel
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder("Field separator"));
		
		label = new JLabel("Field separator string:");
		tip = "<html>The string used to separate the fields in the exported file.<br>" +
				"Use ',' for a Comma Separated Values (CSV) file.";
		label.setToolTipText(tip);
		panel.add(label);
		
		fieldSeparator = new JComboBox(new String[] { ",", ";", SPACE, TAB });
		fieldSeparator.setEditable(true);
		fieldSeparator.setSelectedItem(Prefs.getString(Prefs.EXPORT_FIELD_SEPARATOR, 
				","));
		fieldSeparator.setToolTipText(tip);
		panel.add(fieldSeparator);
		
		this.add(panel, "spany, split, growx 1");
		
		
		
		
		// Comments separator panel
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder("Comments"));
		
		simulationComments = new JCheckBox("Include simulation description");
		simulationComments.setToolTipText("Include a comment at the beginning of the file " +
				"describing the simulation.");
		simulationComments.setSelected(Prefs.getBoolean(Prefs.EXPORT_SIMULATION_COMMENT, 
				true));
		panel.add(simulationComments, "wrap");
		
		
		fieldNameComments = new JCheckBox("Include field descriptions");
		fieldNameComments.setToolTipText("Include a comment line with the descriptions of " +
				"the exported variables.");
		fieldNameComments.setSelected(Prefs.getBoolean(Prefs.EXPORT_FIELD_NAME_COMMENT, true));
		panel.add(fieldNameComments, "wrap");
		
		
		eventComments = new JCheckBox("Include flight events");
		eventComments.setToolTipText("Include a comment line for every flight event.");
		eventComments.setSelected(Prefs.getBoolean(Prefs.EXPORT_EVENT_COMMENTS, true));
		panel.add(eventComments, "wrap");
		
		
		label = new JLabel("Comment character:");
		tip = "The character(s) that mark a comment line.";
		label.setToolTipText(tip);
		panel.add(label, "split 2");
		
		commentCharacter = new JComboBox(new String[] { "#", "%", ";" });
		commentCharacter.setEditable(true);
		commentCharacter.setSelectedItem(Prefs.getString(Prefs.EXPORT_COMMENT_CHARACTER, "#"));
		commentCharacter.setToolTipText(tip);
		panel.add(commentCharacter);
		
		this.add(panel, "growx 1");

		
		// Space-filling panel
		panel = new JPanel();
		this.add(panel, "width 1, height 1, grow 1");
		
		
		// Export button
		button = new JButton("Export to file...");
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
		chooser.setFileFilter(CSV_FILE_FILTER);
		chooser.setCurrentDirectory(Prefs.getDefaultDirectory());
		
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File file = chooser.getSelectedFile();
		if (file == null)
			return;
		
		if (file.getName().indexOf('.') < 0) {
			String name = file.getAbsolutePath();
			name = name + ".csv";
			file = new File(name);
		}

		if (file.exists()) {
			int ret = JOptionPane.showConfirmDialog(this, 
					"File \"" + file.getName() + "\" exists.  Overwrite?", 
					"File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ret != JOptionPane.YES_OPTION)
				return;
		}

		String commentChar = commentCharacter.getSelectedItem().toString();
		String fieldSep = fieldSeparator.getSelectedItem().toString();
		boolean simulationComment = simulationComments.isSelected();
		boolean fieldComment = fieldNameComments.isSelected();
		boolean eventComment = eventComments.isSelected();
		
		// Store preferences and export
		int n = 0;
		Prefs.setDefaultDirectory(chooser.getCurrentDirectory());
		for (int i=0; i < selected.length; i++) {
			Prefs.setExportSelected(types[i], selected[i]);
			if (selected[i])
				n++;
		}
		Prefs.putString(Prefs.EXPORT_FIELD_SEPARATOR, fieldSep);
		Prefs.putString(Prefs.EXPORT_COMMENT_CHARACTER, commentChar);
		Prefs.putBoolean(Prefs.EXPORT_EVENT_COMMENTS, eventComment);
		Prefs.putBoolean(Prefs.EXPORT_FIELD_NAME_COMMENT, fieldComment);
		Prefs.putBoolean(Prefs.EXPORT_SIMULATION_COMMENT, simulationComment);
		
		
		FlightDataBranch.Type[] fieldTypes = new FlightDataBranch.Type[n];
		Unit[] fieldUnits = new Unit[n];
		int pos = 0;
		for (int i=0; i < selected.length; i++) {
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
		
		for (int i=0; i < selected.length; i++) {
			if (selected[i])
				n++;
		}
		
		if (n == 1) {
			str = "Exporting 1 variable out of " + total + ".";
		} else {
			str = "Exporting "+n+" variables out of " + total + ".";
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
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			
			Component component = renderer.getTableCellRendererComponent(table, 
					value, isSelected, hasFocus, row, column);
			
			if (selected[row]) {
				component.setBackground(table.getSelectionBackground());
				component.setForeground(table.getSelectionForeground());
			} else {
				component.setBackground(table.getBackground());
				component.setForeground(table.getForeground());
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
				return "Variable";
			case UNIT:
				return "Unit";
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
				return FlightDataBranch.Type.class;
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
				throw new IndexOutOfBoundsException("column="+column);
			}
			
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			
			switch (column) {
			case SELECTED:
				selected[row] = (Boolean)value;
				this.fireTableRowsUpdated(row, row);
				updateSelectedCount();
				break;
				
			case NAME:
				break;
				
			case UNIT:
				units[row] = (Unit)value;
				break;
				
			default:
				throw new IndexOutOfBoundsException("column="+column);
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
				throw new IndexOutOfBoundsException("column="+column);
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
