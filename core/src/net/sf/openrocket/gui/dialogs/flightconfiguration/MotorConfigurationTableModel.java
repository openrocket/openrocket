package net.sf.openrocket.gui.dialogs.flightconfiguration;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.Chars;

/**
 * The table model for selecting and editing the motor configurations.
 */
class MotorConfigurationTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private final FlightConfigurationDialog flightConfigurationDialog;


	/**
	 * @param flightConfigurationDialog
	 */
	MotorConfigurationTableModel( FlightConfigurationDialog flightConfigurationDialog) {
		this.flightConfigurationDialog = flightConfigurationDialog;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if ( columnIndex == 0 ) {
			// Set description:
			flightConfigurationDialog.updateConfigurationName( (String) aValue );
		}
	}

	@Override
	public int getColumnCount() {
		int count = 1;
		for (MotorMount m : this.flightConfigurationDialog.mounts) {
			if (m.isMotorMount())
				count++;
		}
		return count;
	}
	
	@Override
	public int getRowCount() {
		return this.flightConfigurationDialog.rocket.getMotorConfigurationIDs().length - 1;
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		String id = this.flightConfigurationDialog.findID(row);
		
		if (column == 0) {
			return this.flightConfigurationDialog.rocket.getMotorConfigurationNameOrDescription(id);
		}
		
		MotorMount mount = this.flightConfigurationDialog.findMount(column);
		Motor motor = mount.getMotor(id);
		if (motor == null)
			//// None
			return "None";
		
		String str = motor.getDesignation(mount.getMotorDelay(id));
		int count = mount.getMotorCount();
		if (count > 1) {
			str = "" + count + Chars.TIMES + " " + str;
		}
		return str;
	}
	
	
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			//// Configuration name
			return FlightConfigurationDialog.trans.get("edtmotorconfdlg.lbl.Configname");
		}
		
		MotorMount mount = this.flightConfigurationDialog.findMount(column);
		String name = mount.toString();
		int count = mount.getMotorCount();
		if (count > 1) {
			name = name + " (" + Chars.TIMES + count + ")";
		}
		return name;
	}
	
}