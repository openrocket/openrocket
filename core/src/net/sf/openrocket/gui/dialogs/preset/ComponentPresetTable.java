package net.sf.openrocket.gui.dialogs.preset;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
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

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.AlphanumComparator;

public class ComponentPresetTable extends JTable {

	private static final Translator trans = Application.getTranslator();

	private final TableRowSorter<TableModel> sorter;
	private List<ComponentPreset> presets;
	private final ComponentPreset.Type presetType;
	private Set<String> favorites;
	private final AbstractTableModel tableModel;
	private final XTableColumnModel tableColumnModel;
	private final ComponentPresetTableColumn[] columns;

	public ComponentPresetTable(final ComponentPreset.Type presetType, List<ComponentPreset> presets, List<TypedKey<?>> visibleColumnKeys) {
		super();
		this.presets = presets;
		this.presetType = presetType;
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.columns = new ComponentPresetTableColumn[ComponentPreset.ORDERED_KEY_LIST.size()+1];


		tableModel = new AbstractTableModel() {
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
				return myColumns[columnIndex].getValueFromPreset(favorites,ComponentPresetTable.this.presets.get(rowIndex));
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				// Only support favorite
				if ( columnIndex != 0 ) {
					return;
				}
				ComponentPreset preset = ComponentPresetTable.this.presets.get(rowIndex);
				Application.getComponentPresetDao().setFavorite(preset, presetType, (Boolean) aValue);
				ComponentPresetTable.this.updateFavorites();
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


		sorter = new TableRowSorter<TableModel>(tableModel);

		tableColumnModel = new XTableColumnModel();

		/*
		 * Set up the Column Table model, and customize the sorting.
		 */
		columns[0] = new ComponentPresetTableColumn.Favorite(0);
		tableColumnModel.addColumn(columns[0]);

		List<TableColumn> hiddenColumns = new ArrayList<TableColumn>();
		{
			int index = 1;
			for (final TypedKey<?> key: ComponentPreset.ORDERED_KEY_LIST ) {
				if ( key.getType() == Double.class && key.getUnitGroup() != null ) {
					columns[index] = new ComponentPresetTableColumn.DoubleWithUnit((TypedKey<Double>)key,index);
				} else {
					columns[index] = new ComponentPresetTableColumn.Parameter(key,index);
				}
				tableColumnModel.addColumn(columns[index]);
				if ( key == ComponentPreset.PARTNO ) {
					sorter.setComparator(index, new AlphanumComparator());
				} else if ( key.getType() == Double.class ) {
					sorter.setComparator(index,  new Comparator<Value>() {

						@Override
						public int compare(Value o1, Value o2) {
							return Double.compare(o1.getValue(), o2.getValue());
						}
						
					});
				}
				if ( visibleColumnKeys.indexOf(key) < 0 ) {
					hiddenColumns.add(columns[index]);
				}
				index ++;
			}
		}

		this.setAutoCreateColumnsFromModel(false);
		this.setColumnModel( tableColumnModel );
		this.setModel(tableModel);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setRowSorter(sorter);

		for ( TableColumn hiddenColumn : hiddenColumns ) {
			tableColumnModel.setColumnVisible(hiddenColumn, false);
		}

		JTableHeader header = this.getTableHeader();
		
		header.setReorderingAllowed(true);

		header.addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if ( e.isPopupTrigger() ) {
					doPopup(e);
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if ( e.isPopupTrigger() ) {
					doPopup(e);
				}
			}
		});
	}

	public void setRowFilter( RowFilter<? super TableModel ,? super Integer> filter ) {
		sorter.setRowFilter( filter );
	}

	public void updateData( List<ComponentPreset> myPresets ) {
		this.presets = myPresets;
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.tableModel.fireTableDataChanged();
	}
	
	public void updateFavorites() {
		this.favorites = Application.getPreferences().getComponentFavorites(presetType);
		this.tableModel.fireTableDataChanged();
	}

	private void doPopup(MouseEvent evt ) {
		
		// Figure out what column header was clicked on.
		int colIndex = tableColumnModel.getColumnIndexAtX( evt.getX() );
		ComponentPresetTableColumn colClicked = null;
		if ( colIndex >=0 ) {
			colClicked = (ComponentPresetTableColumn) tableColumnModel.getColumn(colIndex);
		}
		
		JPopupMenu columnMenu = new ColumnPopupMenu(colClicked, colIndex);
		columnMenu.show(evt.getComponent(),evt.getX(),evt.getY());
	}

	private class ColumnPopupMenu extends JPopupMenu {

		ColumnPopupMenu(ComponentPresetTableColumn colClicked, int colClickedIndex) {
			if ( colClickedIndex >= 0 ) {
				JCheckBoxMenuItem item = new SortAscColumnMenuItem(colClickedIndex);
				this.add(item);
				item = new SortDescColumnMenuItem(colClickedIndex);
				this.add(item);
				this.addSeparator();
				if ( colClicked instanceof ComponentPresetTableColumn.DoubleWithUnit ) {
					this.add( new UnitSelectorMenuItem( (ComponentPresetTableColumn.DoubleWithUnit) colClicked ));
					this.addSeparator();
				}
			}
			for( TableColumn c: columns ) {
				JCheckBoxMenuItem item = new ToggleColumnMenuItem(c);
				this.add(item);
			}
		}


		private class SortAscColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			private int columnClicked;
			SortAscColumnMenuItem(int columnClicked) {
				super( trans.get("ComponentPresetChooserDialog.menu.sortAsc") );
				this.addItemListener(this);
				this.columnClicked = columnClicked;
			}
			@Override
			public void itemStateChanged(ItemEvent e) {
				sorter.setSortKeys( Collections.singletonList( new SortKey(columnClicked, SortOrder.ASCENDING)));
			}
		}
		
		private class SortDescColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			private int columnClicked;
			SortDescColumnMenuItem(int columnClicked) {
				super( trans.get("ComponentPresetChooserDialog.menu.sortDesc") );
				this.addItemListener(this);
				this.columnClicked = columnClicked;
			}
			@Override
			public void itemStateChanged(ItemEvent e) {
				sorter.setSortKeys( Collections.singletonList( new SortKey(columnClicked, SortOrder.DESCENDING)));
			}
		}
		
		private class ToggleColumnMenuItem extends JCheckBoxMenuItem implements ItemListener {
			TableColumn col;
			ToggleColumnMenuItem( TableColumn col ) {
				super( String.valueOf(col.getHeaderValue()), tableColumnModel.isColumnVisible(col));
				this.addItemListener(this);
				this.col = col;
			}
			@Override
			public void itemStateChanged(ItemEvent e) {
				tableColumnModel.setColumnVisible(col, !tableColumnModel.isColumnVisible(col));
			}
		}
		
		private class UnitSelectorMenuItem extends JMenu implements ItemListener {
			ComponentPresetTableColumn.DoubleWithUnit col;
			UnitSelectorMenuItem( ComponentPresetTableColumn.DoubleWithUnit col ) {
				super(trans.get("ComponentPresetChooserDialog.menu.units"));
				this.col = col;
				UnitGroup group = col.unitGroup;
				Unit selectedUnit = col.selectedUnit;
				for( Unit u : group.getUnits() ) {
					JCheckBoxMenuItem item = new JCheckBoxMenuItem( u.toString() );
					if ( u == selectedUnit ) {
						item.setSelected(true);
					}
					item.addItemListener(this);
					this.add(item);
				}
				
			}
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getItem();
				String val = item.getText();
				col.selectedUnit = col.unitGroup.findApproximate(val);
				ComponentPresetTable.this.tableModel.fireTableDataChanged();
				return;
			}

		}
	}
}
