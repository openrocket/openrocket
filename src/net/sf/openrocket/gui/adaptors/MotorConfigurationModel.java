package net.sf.openrocket.gui.adaptors;


import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.openrocket.gui.dialogs.EditMotorConfigurationDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;

public class MotorConfigurationModel implements ComboBoxModel, ChangeListener {

	private static final String EDIT = "Edit configurations";
	
	
	private EventListenerList listenerList = new EventListenerList();
	
	private final Configuration config;
	private final Rocket rocket;
	
	private Map<String, ID> map = new HashMap<String, ID>();
	

	public MotorConfigurationModel(Configuration config) {
		this.config = config;
		this.rocket = config.getRocket();
		config.addChangeListener(this);
	}
	
	
	
	@Override
	public Object getElementAt(int index) {
		String[] ids = rocket.getMotorConfigurationIDs();
		if (index < 0  ||  index > ids.length)
			return null;
		
		if (index == ids.length)
			return EDIT;
		
		return get(ids[index]);
	}

	@Override
	public int getSize() {
		return rocket.getMotorConfigurationIDs().length + 1;
	}

	@Override
	public Object getSelectedItem() {
		return get(config.getMotorConfigurationID());
	}

	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}
		if (item == EDIT) {
			
			// Open edit dialog in the future, after combo box has closed
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new EditMotorConfigurationDialog(rocket, BasicFrame.findFrame(rocket))
						.setVisible(true);
				}
			});

			return;
		}
		if (!(item instanceof ID)) {
			throw new IllegalArgumentException("MotorConfigurationModel item="+item);
		}
		
		ID idObject = (ID) item;
		config.setMotorConfigurationID(idObject.getID());
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

		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null)
					e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
				((ListDataListener) listeners[i+1]).contentsChanged(e);
			}
		}
	}
	 
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e instanceof ComponentChangeEvent) {
			// Ignore unnecessary changes
			if (!((ComponentChangeEvent)e).isMotorChange())
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
			return rocket.getMotorConfigurationNameOrDescription(id);
		}
	}
	
}

