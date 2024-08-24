package info.openrocket.swing.gui.widgets;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.UnitValue;
import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.components.UnitCellEditor;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class CSVExportPanel<T extends UnitValue> extends JPanel {
	private static final Translator trans = Application.getTranslator();
	protected static final String SPACE = "SPACE";
	protected static final String TAB = "TAB";

	protected final JTable table;
	protected final SelectionTableModel tableModel;
	private final JLabel selectedCountLabel;

	protected final boolean[] selected;
	protected final T[] types;
	protected final Unit[] units;

	protected final CsvOptionPanel csvOptions;

	public CSVExportPanel(T[] types, boolean[] selected, CsvOptionPanel csvOptions, boolean separateRowForTable, Component... extraComponents) {
		super(new MigLayout("fill, flowy"));

		this.types = types;
		this.selected = selected;
		this.csvOptions = csvOptions;

		this.units = new Unit[types.length];
		for (int i = 0; i < types.length; i++) {
			units[i] = types[i].getUnitGroup().getDefaultUnit();
		}

		//// Create the panel
		JPanel panel;
		JButton button;

		// Set up the variable selection table
		tableModel = createTableModel();
		table = new JTable(tableModel);
		initializeTable(types);

		// Add table
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Vartoexport")));

		panel.add(new JScrollPane(table), "wmin 300lp, width 300lp, pushy, grow 100, wrap");

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


		// Exporting xx variables out of yy
		selectedCountLabel = new JLabel();
		updateSelectedCount();
		panel.add(selectedCountLabel);

		if (separateRowForTable) {
			this.add(panel, "spanx, grow 100, wrap");
		} else {
			this.add(panel, "grow 100, wrap");
		}

		// Add CSV options
		if (separateRowForTable) {
			this.add(csvOptions, "grow 1");
		} else {
			this.add(csvOptions, "spany, split, growx 1");
		}

		//// Add extra widgets
		if (extraComponents != null) {
			for (Component c : extraComponents) {
				if (separateRowForTable) {
					this.add(c, "grow 1");
				} else {
					this.add(c, "spany, split, growx 1");
				}
			}
		}

		// Space-filling panel
		if (!separateRowForTable) {
			panel = new JPanel();
			this.add(panel, "width 1, height 1, grow 1");
		}
	}

	public CSVExportPanel(T[] types, boolean[] selected, CsvOptionPanel csvOptions, Component... extraComponents) {
		this(types, selected, csvOptions, false, extraComponents);
	}

	protected SelectionTableModel createTableModel() {
		return new SelectionTableModel();
	}

	protected void initializeTable(T[] types) {
		table.setDefaultRenderer(Object.class,
				new SelectionBackgroundCellRenderer(table.getDefaultRenderer(Object.class)));
		table.setDefaultRenderer(Boolean.class,
				new SelectionBackgroundCellRenderer(table.getDefaultRenderer(Boolean.class)));
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);

		table.setDefaultEditor(Unit.class, new UnitCellEditor() {
			private static final long serialVersionUID = 1088570433902420935L;

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
	}

	public boolean doExport() {
		throw new RuntimeException("Not implemented");
	}

	private void updateSelectedCount() {
		int total = selected.length;
		int n = 0;
		String str;

		for (boolean b : selected) {
			if (b)
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
	 * The table model for the variable selection.
	 */
	protected class SelectionTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 493067422917621072L;
		protected static final int SELECTED = 0;
		protected static final int NAME = 1;
		protected static final int UNIT = 2;

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
			return switch (column) {
				case SELECTED -> "";
				case NAME ->
					//// Variable
						trans.get("SimExpPan.Col.Variable");
				case UNIT ->
					//// Unit
						trans.get("SimExpPan.Col.Unit");
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch (column) {
				case SELECTED -> Boolean.class;
				case NAME -> new TypeToken<T>(){}.getType().getClass();
				case UNIT -> Unit.class;
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			return switch (column) {
				case SELECTED -> selected[row];
				case NAME -> types[row];
				case UNIT -> units[row];
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
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
			return switch (column) {
				case SELECTED -> true;
				case NAME -> false;
				case UNIT -> types[row].getUnitGroup().getUnitCount() > 1;
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
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

	public abstract static class TypeToken<T> {
		private final Type type;

		protected TypeToken(){
			Type superClass = getClass().getGenericSuperclass();
			this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
		}

		public Type getType() {
			return type;
		}
	}
}
