package net.sf.openrocket.gui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;


/**
 * A cell editor that returns a combo box containing a selection of units.
 * Using classes must implement the {@link #getUnitGroup(Unit, int, int)} method
 * to return the appropriate unit group for the selection.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class UnitCellEditor extends AbstractCellEditor
		implements TableCellEditor, ActionListener {
	
	private final JComboBox editor;
	
	public UnitCellEditor() {
		editor = new JComboBox();
		editor.setEditable(false);
		editor.addActionListener(this);
	}
	
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		Unit unit = (Unit) value;
		UnitGroup group = getUnitGroup(unit, row, column);
		
		editor.removeAllItems();
		for (Unit u : group.getUnits()) {
			editor.addItem(u);
		}
		
		editor.setSelectedItem(unit);
		
		return editor;
	}
	
	
	@Override
	public Object getCellEditorValue() {
		return editor.getSelectedItem();
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		// End editing when a value has been selected
		this.fireEditingStopped();
	}
	
	
	/**
	 * Return the unit group corresponding to the specified cell.
	 * 
	 * @param value		the cell's value.
	 * @param row		the cell's row.
	 * @param column	the cell's column.
	 * @return			the unit group of this cell.
	 */
	protected abstract UnitGroup getUnitGroup(Unit value, int row, int column);
	
}
