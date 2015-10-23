package net.sf.openrocket.gui.adaptors;


import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.ParameterSet;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A ComboBoxModel that contains a list of flight configurations.  The list can
 * optionally contain a last element that opens up the configuration edit dialog.
 */
public class ParameterSetModel<T extends FlightConfigurableParameter<T>> implements ComboBoxModel<FlightConfigurationID>, StateChangeListener {
	//private static final Translator trans = Application.getTranslator();
	
	//private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private EventListenerList listenerList = new EventListenerList();
	
	private T selected;
	private final ParameterSet<T> sourceSet;
	List<FlightConfigurationID> idList= new Vector<FlightConfigurationID>();
	
	public ParameterSetModel(ParameterSet<T> set ) {
		this.sourceSet = set;
		this.selected = this.sourceSet.getDefault();
	}	
	
	@Override
	public FlightConfigurationID getElementAt(int index) {
		
		this.idList = this.sourceSet.getSortedConfigurationIDs();

		if (index < 0){
			return FlightConfigurationID.ERROR_CONFIGURATION_FCID;
		}else if ( index >= this.idList.size()){ 
			return FlightConfigurationID.ERROR_CONFIGURATION_FCID;
		}
		
		return this.idList.get(index);
	}
	
	@Override
	public int getSize() {
		this.idList = this.sourceSet.getSortedConfigurationIDs();
		return this.idList.size();
	}
	
	@Override
	public Object getSelectedItem() {
		return selected;
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

		this.selected = sourceSet.get(fcid);
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
