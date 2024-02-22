package info.openrocket.swing.gui.dialogs.preset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.unit.Value;
import info.openrocket.core.util.AlphanumComparator;

@SuppressWarnings("serial")
public class ComponentPresetTable extends JTable {

	private static final Translator trans = Application.getTranslator();

	private final TableRowSorter<TableModel> sorter;
	private List<ComponentPreset> presets;
	private final ComponentPreset.Type presetType;
	private Set<String> favorites;
	private final AbstractTableModel tableModel;
	private final XTableColumnModel tableColumnModel;
	private final ComponentPresetTableColumn[] columns;
	private final List<TableColumn> hiddenColumns;

	public ComponentPresetTable(final ComponentPreset.Type presetType, List<ComponentPreset> presets, List<TypedKey<?>> visibleColumnKeys) {
		super();
		this.presets = presets;
		this.presetType = presetType;
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.columns = new ComponentPresetTableColumn[ComponentPreset.ORDERED_KEY_LIST.size() + 1];


		this.tableModel = new AbstractTableModel() {
			final ComponentPresetTableColumn[] myColumns = columns;

			@Override
			public int getRowCount() {
				return ComponentPresetTable.this.presets.size();
			}

			@Override
			public int getColumnCount() {
				return myColumns.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return myColumns[columnIndex].getValueFromPreset(favorites, ComponentPresetTable.this.presets.get(rowIndex));
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				// Only support favorite
				if (columnIndex != 0) {
					return;
				}
				ComponentPreset preset = ComponentPresetTable.this.presets.get(rowIndex);
				Application.getComponentPresetDao().setFavorite(preset, presetType, (Boolean) aValue);
				ComponentPresetTable.this.updateFavorites();
				int viewIndex = ComponentPresetTable.this.convertRowIndexToView(rowIndex);
				if (viewIndex != -1) {
					ComponentPresetTable.this.setRowSelectionInterval(viewIndex, viewIndex);
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnIndex == 0 ? Boolean.class : Object.class;
			}

		};


		this.sorter = new TableRowSorter<TableModel>(tableModel);
		this.tableColumnModel = new XTableColumnModel();

		/*
		 * Set up the Column Table model, and customize the sorting.
		 */
		this.columns[0] = new ComponentPresetTableColumn.Favorite(0);
		this.tableColumnModel.addColumn(columns[0]);

		this.hiddenColumns = new ArrayList<>();
		{
			int index = 1;
			for (final TypedKey<?> key : ComponentPreset.ORDERED_KEY_LIST) {
				if (key.getType() == Double.class && key.getUnitGroup() != null) {
					columns[index] = new ComponentPresetTableColumn.DoubleWithUnit((TypedKey<Double>) key, index);
				} else {
					columns[index] = new ComponentPresetTableColumn.Parameter(key, index);
				}
				tableColumnModel.addColumn(columns[index]);
				if (key == ComponentPreset.PARTNO) {
					sorter.setComparator(index, new AlphanumComparator());
				} else if (key.getType() == Double.class) {
					sorter.setComparator(index, new Comparator<Value>() {

						@Override
						public int compare(Value o1, Value o2) {
							return Double.compare(o1.getValue(), o2.getValue());
						}

					});
				} else if (key.getType() == Boolean.class) {
					sorter.setComparator(index, new Comparator<Boolean>() {

						@Override
						public int compare(Boolean b1, Boolean b2) {
							if (b1 && !b2) {
								return 1;
							} else if (!b1 && b2) {
								return -1;
							} else {
								return 0;
							}
						}
					});
				}

				if (!visibleColumnKeys.contains(key)) {
					hiddenColumns.add(columns[index]);
				}
				index++;
			}
		}

		this.setAutoCreateColumnsFromModel(false);
		this.setColumnModel(tableColumnModel);
		this.setModel(tableModel);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setRowSorter(sorter);
		this.sorter.toggleSortOrder(2);        // Sort by the first column (manufacturer) by default

		for (TableColumn hiddenColumn : this.hiddenColumns) {
			this.tableColumnModel.removeColumn(hiddenColumn);
		}

		JTableHeader header = this.getTableHeader();

		header.setReorderingAllowed(true);

		header.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPopup(e);
				}
			}
		});
	}

	public XTableColumnModel getXColumnModel() {
		return this.tableColumnModel;
	}

	public void setRowFilter(RowFilter<? super TableModel, ? super Integer> filter) {
		sorter.setRowFilter(filter);
	}

	public void updateData(List<ComponentPreset> myPresets) {
		this.presets = myPresets;
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.tableModel.fireTableDataChanged();
	}

	public void updateFavorites() {
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.tableModel.fireTableDataChanged();
	}

	private void doPopup(MouseEvent evt) {

		// Figure out what column header was clicked on.
		int colIndex = tableColumnModel.getColumnIndexAtX(evt.getX());
		ComponentPresetTableColumn colClicked = null;
		if (colIndex >= 0) {
			colClicked = (ComponentPresetTableColumn) tableColumnModel.getColumn(colIndex);
		}

		JPopupMenu columnMenu = new ColumnPopupMenu(colClicked, colIndex);
		columnMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private class ColumnPopupMenu extends JPopupMenu {

		ColumnPopupMenu(ComponentPresetTableColumn colClicked, int colClickedIndex) {
			if (colClickedIndex >= 0) {
				JCheckBoxMenuItem item = new SortAscColumnMenuItem(colClickedIndex);
				this.add(item);
				item = new SortDescColumnMenuItem(colClickedIndex);
				this.add(item);
				this.addSeparator();
				if (colClicked instanceof ComponentPresetTableColumn.DoubleWithUnit) {
					this.add(new UnitSelectorMenuItem((ComponentPresetTableColumn.DoubleWithUnit) colClicked));
					this.addSeparator();
				}
			}
			for (TableColumn c : columns) {
				if (hiddenColumns.contains(c)) {
					continue;
				}
				JCheckBoxMenuItem item = new ToggleColumnMenuItem(c);
				this.add(item);
			}
		}


		private class SortAscColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			private int columnClicked;

			SortAscColumnMenuItem(int columnClicked) {
				super(trans.get("ComponentPresetChooserDialog.menu.sortAsc"));
				this.addItemListener(this);
				this.columnClicked = columnClicked;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				sorter.setSortKeys(Collections.singletonList(new SortKey(columnClicked, SortOrder.ASCENDING)));
			}
		}

		private class SortDescColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			private int columnClicked;

			SortDescColumnMenuItem(int columnClicked) {
				super(trans.get("ComponentPresetChooserDialog.menu.sortDesc"));
				this.addItemListener(this);
				this.columnClicked = columnClicked;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				sorter.setSortKeys(Collections.singletonList(new SortKey(columnClicked, SortOrder.DESCENDING)));
			}
		}

		private class ToggleColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			TableColumn col;

			ToggleColumnMenuItem(TableColumn col) {
				super(String.valueOf(col.getHeaderValue()), tableColumnModel.isColumnVisible(col));
				this.addItemListener(this);
				this.col = col;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				tableColumnModel.setColumnVisible(col, !tableColumnModel.isColumnVisible(col));
			}
		}

		private class UnitSelectorMenuItem extends JMenu {
			ComponentPresetTableColumn.DoubleWithUnit col;
			ButtonGroup buttonGroup; // To group the radio buttons

			UnitSelectorMenuItem(ComponentPresetTableColumn.DoubleWithUnit col) {
				super(trans.get("ComponentPresetChooserDialog.menu.units"));
				this.col = col;

				buttonGroup = new ButtonGroup(); // Create a new ButtonGroup to hold the radio buttons

				UnitGroup group = col.unitGroup;
				Unit selectedUnit = col.selectedUnit;

				for (Unit u : group.getUnits()) {
					JRadioButtonMenuItem item = new JRadioButtonMenuItem(u.toString());

					if (u == selectedUnit) {
						item.setSelected(true);
					}

					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							col.selectedUnit = u;
							ComponentPresetTable.this.tableModel.fireTableDataChanged();
						}
					});

					buttonGroup.add(item); // Add the radio button to the button group
					this.add(item);       // Add the radio button to the menu
				}
			}
		}
	}
}
