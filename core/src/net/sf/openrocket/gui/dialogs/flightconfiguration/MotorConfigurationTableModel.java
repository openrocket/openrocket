package net.sf.openrocket.gui.dialogs.flightconfiguration;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Coordinate;

/**
 * The table model for selecting and editing the motor configurations.
 */
class MotorConfigurationTableModel extends AbstractTableModel {
	
	private Translator trans = Application.getTranslator();
	
	private final static String NONE = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.None");
	private final static String MOTOR_MOUNT = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Mountheader");
	private final static String MOTOR = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Motorheader");
	private final static String IGNITION = FlightConfigurationDialog.trans.get("edtmotorconfdlg.tbl.Ignitionheader");
	
	private final Rocket rocket;
	
	
	public MotorConfigurationTableModel(Rocket rocket) {
		this.rocket = rocket;
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public int getRowCount() {
		int count = 0;
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount && ((MotorMount) c).isMotorMount()) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0: {
			MotorMount mount = findMount(row);
			String name = mount.toString();
			int count = getMountMultiplicity(mount);
			if (count > 1) {
				name = name + " (" + Chars.TIMES + count + ")";
			}
			return name;
		}
		case 1: {
			MotorMount mount = findMount(row);
			String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
			Motor motor = mount.getMotor(currentID);
			
			if (motor == null)
				return NONE;
			
			String str = motor.getDesignation(mount.getMotorDelay(currentID));
			int count = getMountMultiplicity(mount);
			if (count > 1) {
				str = "" + count + Chars.TIMES + " " + str;
			}
			return str;
		}
		case 2: {
			return getIgnitionEventString(row);
			
		}
		default:
			throw new IndexOutOfBoundsException("column=" + column);
		}
	}
	
	
	
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return MOTOR_MOUNT;
		case 1:
			return MOTOR;
		case 2:
			return IGNITION;
		default:
			throw new IndexOutOfBoundsException("column=" + column);
		}
	}
	
	
	private MotorMount findMount(int row) {
		int count = row;
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount && ((MotorMount) c).isMotorMount()) {
				count--;
				if (count < 0) {
					return (MotorMount) c;
				}
			}
		}
		throw new IndexOutOfBoundsException("Requesting row=" + row + " but only " + getRowCount() + " rows exist");
	}
	
	
	private int getMountMultiplicity(MotorMount mount) {
		RocketComponent c = (RocketComponent) mount;
		return c.toAbsolute(Coordinate.NUL).length;
	}
	
	
	
	private String getIgnitionEventString(int row) {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = findMount(row);
		MotorConfiguration motorConfig = mount.getFlightConfiguration(currentID);
		if (motorConfig == null) {
			return NONE;
		}
		
		MotorConfiguration.IgnitionEvent ignition = motorConfig.getIgnitionEvent();
		Double ignitionDelay = motorConfig.getIgnitionDelay();
		boolean isDefault = (ignition == null);
		
		if (ignition == null) {
			ignition = mount.getDefaultIgnitionEvent();
		}
		if (ignitionDelay == null) {
			ignitionDelay = mount.getDefaultIgnitionDelay();
		}
		
		String str = trans.get("MotorMount.IgnitionEvent.short." + ignition.name());
		if (ignitionDelay > 0) {
			str = str + " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(ignitionDelay);
		}
		if (isDefault) {
			String def = trans.get("table.ignition.default");
			str = def.replace("{0}", str);
		}
		return str;
	}
}