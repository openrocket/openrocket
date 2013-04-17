package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.ArrayList;

/**
 * The table model for selecting whether components are motor mounts or not.
 */
class MotorMountTableModel extends AbstractTableModel {
	
	private final MotorConfigurationPanel motorConfigurationPanel;
	
	private final List<MotorMount> potentialMounts = new ArrayList<MotorMount>();
	
	/**
	 * @param motorConfigurationPanel
	 */
	MotorMountTableModel(MotorConfigurationPanel motorConfigurationPanel, Rocket rocket) {
		this.motorConfigurationPanel = motorConfigurationPanel;
		
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				potentialMounts.add((MotorMount) c);
			}
		}
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return potentialMounts.size();
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
			return new Boolean(potentialMounts.get(row).isMotorMount());
			
		case 1:
			return potentialMounts.get(row).toString();
			
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
		
		MotorMount mount = potentialMounts.get(row);
		mount.setMotorMount((Boolean) value);
		this.motorConfigurationPanel.fireTableDataChanged();
	}
}