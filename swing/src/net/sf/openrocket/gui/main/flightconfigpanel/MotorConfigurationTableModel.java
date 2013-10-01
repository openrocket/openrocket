package net.sf.openrocket.gui.main.flightconfigpanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;

/**
 * The table model for selecting and editing the motor configurations.
 */
class MotorConfigurationTableModel extends AbstractTableModel implements ComponentChangeListener {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final String CONFIGURATION = trans.get("edtmotorconfdlg.col.configuration");
	
	private final Rocket rocket;
	
	private List<MotorMount> motorMounts = new ArrayList<MotorMount>();
	
	
	public MotorConfigurationTableModel(Rocket rocket) {
		this.rocket = rocket;
		this.rocket.addComponentChangeListener(this);
		initializeMotorMounts();
		
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if ( e.isMotorChange() || e.isTreeChange() ) {
			initializeMotorMounts();
			fireTableStructureChanged();
		}
	}

	private void initializeMotorMounts() {
		motorMounts.clear();
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				if (((MotorMount) c).isMotorMount()) {
					motorMounts.add((MotorMount) c);
				}
			}
		}
		
	}
	
	@Override
	public int getColumnCount() {
		return motorMounts.size() + 1;
	}
	
	@Override
	public int getRowCount() {
		return rocket.getFlightConfigurationIDs().length - 1;
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		String id = getConfiguration(row);
		switch (column) {
		case 0: {
			return id;
		}
		default: {
			int mountIndex = column - 1;
			MotorMount mount = motorMounts.get(mountIndex);
			return new Pair<String, MotorMount>(id, mount);
		}
		}
	}
	
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: {
			return CONFIGURATION;
		}
		default: {
			int mountIndex = column - 1;
			MotorMount mount = motorMounts.get(mountIndex);
			return mount.toString();
		}
		}
	}
	
	private String getConfiguration(int row) {
		String id = rocket.getFlightConfigurationIDs()[row + 1];
		return id;
	}
	
}