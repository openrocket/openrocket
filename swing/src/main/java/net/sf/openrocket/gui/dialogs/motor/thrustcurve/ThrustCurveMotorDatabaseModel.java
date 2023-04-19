package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;

class ThrustCurveMotorDatabaseModel extends AbstractTableModel {
	private final List<ThrustCurveMotorSet> database;
	
	public ThrustCurveMotorDatabaseModel(List<ThrustCurveMotorSet> database) {
		this.database = database;
	}
	
	@Override
	public int getColumnCount() {
		return ThrustCurveMotorColumns.values().length;
	}
	
	@Override
	public int getRowCount() {
		return database.size();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ThrustCurveMotorColumns column = getColumn(columnIndex);
		return column.getValue(database.get(rowIndex));
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return getColumn(columnIndex).getTitle();
	}
	
	
	public ThrustCurveMotorSet getMotorSet(int rowIndex) {
		return database.get(rowIndex);
	}
	
	
	public int getIndex(ThrustCurveMotorSet m) {
		return database.indexOf(m);
	}
	
	private ThrustCurveMotorColumns getColumn(int index) {
		return ThrustCurveMotorColumns.values()[index];
	}
	
}
