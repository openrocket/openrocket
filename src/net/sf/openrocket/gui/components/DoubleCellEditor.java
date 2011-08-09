package net.sf.openrocket.gui.components;

import java.awt.Component;
import java.text.ParseException;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;

public class DoubleCellEditor extends AbstractCellEditor implements TableCellEditor {
	
	private final JSpinner editor;
	private final DoubleModel model;
	
	public DoubleCellEditor() {
		model = new DoubleModel(0);
		editor = new JSpinner(model.getSpinnerModel());
		editor.setEditor(new SpinnerEditor(editor));
		//		editor.addChangeListener(this);
	}
	
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		double val = (Double) value;
		model.setValue(val);
		
		return editor;
	}
	
	
	@Override
	public boolean stopCellEditing() {
		try {
			editor.commitEdit();
		} catch (ParseException e) {
			// Ignore
		}
		return super.stopCellEditing();
	}
	
	
	@Override
	public Object getCellEditorValue() {
		return model.getValue();
	}
	
}
