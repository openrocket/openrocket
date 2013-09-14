package net.sf.openrocket.gui.main.flightconfigpanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;

class RecoveryTableModel extends AbstractTableModel {

	private static final Translator trans = Application.getTranslator();

	private final Rocket rocket;

	private final List<RecoveryDevice> recoveryDevices = new ArrayList<RecoveryDevice>();

	private static final String CONFIGURATION = "Configuration";


	/**
	 * @param recoveryConfigurationPanel
	 */
	RecoveryTableModel(Rocket rocket) {
		this.rocket = rocket;

		initialize();
	}

	private void initialize() {
		recoveryDevices.clear();
		Iterator<RocketComponent> it = rocket.iterator();
		while (it.hasNext()) {
			RocketComponent c = it.next();
			if (c instanceof RecoveryDevice) {
				recoveryDevices.add( (RecoveryDevice) c);
			}
		}
	}

	@Override
	public int getRowCount() {
		return rocket.getFlightConfigurationIDs().length - 1;
	}

	@Override
	public int getColumnCount() {
		return recoveryDevices.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int column) {
		String id = getConfiguration(row);
		switch (column) {
		case 0: {
			return id;
		}
		default: {
			int index = column - 1;
			RecoveryDevice d = recoveryDevices.get(index);
			return new Pair<String, RecoveryDevice>(id, d);
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
			int index = column - 1;
			RecoveryDevice d = recoveryDevices.get(index);
			return d.toString();

		}
		}
	}

	private String getConfiguration(int row) {
		String id = rocket.getFlightConfigurationIDs()[row + 1];
		return id;
	}

}