package net.sf.openrocket.gui.dialogs.flightconfiguration;

import javax.swing.table.AbstractTableModel;

/**
 * The table model for selecting whether components are motor mounts or not.
 */
class MotorMountTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private final MotorConfigurationPanel motorConfigurationPanel;

	/**
	 * @param motorConfigurationPanel
	 */
	MotorMountTableModel(MotorConfigurationPanel motorConfigurationPanel) {
		this.motorConfigurationPanel = motorConfigurationPanel;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return this.motorConfigurationPanel.mounts.length;
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return Boolean.class;
			
		case 1:
			return String.class;
			
		default:
			throw new IndexOutOfBoundsException("column=" + column);
		}
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0:
			return new Boolean(this.motorConfigurationPanel.mounts[row].isMotorMount());
			
		case 1:
			return this.motorConfigurationPanel.mounts[row].toString();
			
		default:
			throw new IndexOutOfBoundsException("column=" + column);
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column != 0 || !(value instanceof Boolean)) {
			throw new IllegalArgumentException("column=" + column + ", value=" + value);
		}
		
		this.motorConfigurationPanel.makeMotorMount( this.motorConfigurationPanel.mounts[row], (Boolean) value);
	}
}