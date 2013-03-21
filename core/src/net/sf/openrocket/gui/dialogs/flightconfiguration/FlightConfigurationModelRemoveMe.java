package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;

import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;

public class FlightConfigurationModelRemoveMe extends DefaultComboBoxModel {
	
	private final Configuration config;
	private final Rocket rocket;
	
	private Map<String, ID> map = new HashMap<String, ID>();
	
	private final FlightConfigurationDialog flightConfigurationDialog;
	
	public FlightConfigurationModelRemoveMe(FlightConfigurationDialog flightConfigurationDialog, Configuration config) {
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.config = config;
		this.rocket = config.getRocket();
	}
	
	void fireContentsUpdated() {
		fireContentsChanged(this, 0, rocket.getFlightConfigurationIDs().length);
	}
	
	@Override
	public Object getElementAt(int index) {
		String[] ids = rocket.getFlightConfigurationIDs();
		if (index < 0 || index >= ids.length)
			return null;
		
		return get(ids[index]);
	}
	
	@Override
	public int getSize() {
		return rocket.getFlightConfigurationIDs().length;
	}
	
	@Override
	public Object getSelectedItem() {
		return get(config.getFlightConfigurationID());
	}
	
	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}
		if (!(item instanceof ID)) {
			throw new IllegalArgumentException("MotorConfigurationModel item=" + item);
		}
		
		ID idObject = (ID) item;
		//		flightConfigurationDialog.selectConfiguration(idObject.getID());
	}
	
	/*
	 * The ID class is an adapter, that contains the actual configuration ID,
	 * but gives the configuration description as its String representation.
	 * The get(id) method retrieves ID objects and caches them for reuse.
	 */
	
	private ID get(String id) {
		ID idObject = map.get(id);
		if (idObject != null)
			return idObject;
		
		idObject = new ID(id);
		map.put(id, idObject);
		return idObject;
	}
	
	
	private class ID {
		private final String id;
		
		public ID(String id) {
			this.id = id;
		}
		
		public String getID() {
			return id;
		}
		
		@Override
		public String toString() {
			return rocket.getFlightConfigurationNameOrDescription(id);
		}
	}
	
}
