package net.sf.openrocket.gui.dialogs.flightconfiguration;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.Chars;

/**
 * The table model for selecting and editing the motor configurations.
 */
class MotorConfigurationTableModel extends AbstractTableModel {

	private final static String NONE = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.None");
	private final static String MOTOR_MOUNT = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Mountheader");
	private final static String MOTOR = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Motorheader");
	private final static String IGNITION = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Ignitionheader");
	
	/**
	 * 
	 */
	private final MotorConfigurationPanel motorConfigurationPanel;

	private final boolean advanced;

	/**
	 * @param motorConfigurationPanel
	 */
	MotorConfigurationTableModel( MotorConfigurationPanel motorConfigurationPanel, boolean advanced) {
		this.motorConfigurationPanel = motorConfigurationPanel;
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
		for (MotorMount m : this.motorConfigurationPanel.mounts) {
			if (m.isMotorMount())
				count++;
		}
		return count;
	}

	@Override
	public Object getValueAt(int row, int column) {

		switch( column ) {
		case 0:
		{
			MotorMount mount = this.motorConfigurationPanel.findMount(row);
			String name = mount.toString();
			int count = mount.getMotorCount();
			if (count > 1) {
				name = name + " (" + Chars.TIMES + count + ")";
			}
			return name;
		}
		case 1:
		{
			String str = this.motorConfigurationPanel.findMotorForDisplay(row);
			if (str == null)
				//// None
				return NONE;

			return str;
		}
		case 2:
		{
			String str = this.motorConfigurationPanel.findIgnitionForDisplay(row);
			if (str == null)
				//// None
				return NONE;

			return str;
		}
		default:
			return "";
		}
	}


	@Override
	public String getColumnName(int column) {
		switch (column ) {
		case 0:
			return MOTOR_MOUNT;
		case 1:
			return MOTOR;
		case 2:
			return IGNITION;
		default:
			return "";
		}
	}

}