package net.sf.openrocket.gui.adaptors;


import java.util.EventObject;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.FlightConfigurationSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A ComboBoxModel that contains a list of flight configurations.  The list can
 * optionally contain a last element that opens up the configuration edit dialog.
 */
public class FlightConfigurationModel implements ComboBoxModel<FlightConfigurationID>, StateChangeListener {
	//private static final Translator trans = Application.getTranslator();
	
	//private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private EventListenerList listenerList = new EventListenerList();
	
	private FlightConfiguration config;
	private final Rocket rocket;
	Vector<FlightConfigurationID> ids= new Vector<FlightConfigurationID>();
	
	public FlightConfigurationModel(FlightConfiguration config) {
		this.config = config;
		this.rocket = config.getRocket();
		config.addChangeListener(this);
	}
	
	
	@Override
	public FlightConfigurationID getElementAt(int index) {
		this.ids = rocket.getSortedConfigurationIDs();
		
		if (index < 0){
			return FlightConfigurationID.ERROR_CONFIGURATION_ID;
		}else if ( index >= this.ids.size()){ 
			return FlightConfigurationID.ERROR_CONFIGURATION_ID;
		}
		
		return this.ids.get(index);
	}
	
	@Override
	public int getSize() {
		return this.ids.size();
	}
	
	@Override
	public Object getSelectedItem() {
		return config.getFlightConfigurationID();
	}
	
	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}
		if (!(item instanceof FlightConfigurationID)) {
			throw new IllegalArgumentException("MotorConfigurationModel item=" + item);
		}
		
		FlightConfigurationID fcid= (FlightConfigurationID) item;
		FlightConfigurationSet<FlightConfiguration> configs= rocket.getConfigurationSet();
		
		configs.setDefault( configs.get(fcid));
		this.config = rocket.getDefaultConfiguration();
	}
	
	
	
	////////////////  Event/listener handling  ////////////////
	
	
	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class, l);
	}
	
	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class, l);
	}
	
	protected void fireListDataEvent() {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;
		
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null)
					e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}
	
	
	@Override
	public void stateChanged(EventObject e) {
		if (e instanceof ComponentChangeEvent) {
			// Ignore unnecessary changes
			if (!((ComponentChangeEvent) e).isMotorChange())
				return;
		}
		fireListDataEvent();
	}
	
}
