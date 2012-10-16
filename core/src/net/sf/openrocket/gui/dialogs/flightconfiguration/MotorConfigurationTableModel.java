package net.sf.openrocket.gui.dialogs.flightconfiguration;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.util.Chars;

/**
 * The table model for selecting and editing the motor configurations.
 */
class MotorConfigurationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private final FlightConfigurationDialog flightConfigurationDialog;

	private final boolean advanced;

	/**
	 * @param flightConfigurationDialog
	 */
	MotorConfigurationTableModel( FlightConfigurationDialog flightConfigurationDialog, boolean advanced) {
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.advanced = advanced;
	}

	@Override
	public int getColumnCount() {
		int count = (advanced)? 3: 2;
		return count;
	}

	@Override
	public int getRowCount() {
		int count = 0;
		for (MotorMount m : this.flightConfigurationDialog.mounts) {
			if (m.isMotorMount())
				count++;
		}
		return count;
	}

	@Override
	public Object getValueAt(int row, int column) {

		String id = this.flightConfigurationDialog.currentID;

		switch( column ) {
		case 0:
		{
			MotorMount mount = this.flightConfigurationDialog.findMount(row);
			String name = mount.toString();
			int count = mount.getMotorCount();
			if (count > 1) {
				name = name + " (" + Chars.TIMES + count + ")";
			}
			return name;
		}
		case 1:
		{
			MotorMount mount = this.flightConfigurationDialog.findMount(row);
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
		case 2:
		{
			MotorMount mount = this.flightConfigurationDialog.findMount(row);
			Motor motor = mount.getMotor(id);
			if (motor == null)
				//// None
				return "None";
			IgnitionEvent ignition = mount.getIgnitionEvent();
			return ignition.toString();
		}
		default:
			return "";
		}
	}


	@Override
	public String getColumnName(int column) {
		switch (column ) {
		case 0:
			return "Motor Mount";
		case 1:
			return "Motor";
		case 2:
			return "Ignition";
		default:
			return "";
		}
	}

}