package net.sf.openrocket.gui.adaptors;


import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A ComboBoxModel that contains a list of flight configurations.  The list can
 * optionally contain a last element that opens up the configuration edit dialog.
 */
public class FlightConfigurationModel implements ComboBoxModel, StateChangeListener {
	private static final Translator trans = Application.getTranslator();
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	private EventListenerList listenerList = new EventListenerList();
	
	private final Configuration config;
	private final Rocket rocket;
	
	private Map<String, ID> map = new HashMap<String, ID>();
	
	
	public FlightConfigurationModel(Configuration config) {
		this.config = config;
		this.rocket = config.getRocket();
		config.addChangeListener(this);
	}
	
	
	@Override
	public Object getElementAt(int index) {
		String[] ids = rocket.getFlightConfigurationIDs();
		
		if (index < 0)
			return null;
		if ( index >= ids.length) 
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
		config.setFlightConfigurationID(idObject.getID());
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
			return descriptor.format(rocket, id);
		}
	}
	
}
