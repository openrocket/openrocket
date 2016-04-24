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
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameterSet;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A ComboBoxModel that contains a list of flight configurations.  The list can
 * optionally contain a last element that opens up the configuration edit dialog.
 */
public class ParameterSetModel<T extends FlightConfigurableParameter<T>> implements ComboBoxModel<T>, StateChangeListener {
	//private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(ParameterSetModel.class);
	//private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private EventListenerList listenerList = new EventListenerList();
	
	private Object selected;
	private final FlightConfigurableParameterSet<T> sourceSet;
	List<FlightConfigurationId> idList= new Vector<FlightConfigurationId>();
	
	public ParameterSetModel(FlightConfigurableParameterSet<T> set ) {
		this.sourceSet = set;
		this.selected = this.sourceSet.getDefault();
	}	
	
	@Override
	public T getElementAt(int index) {
		if((index < 0)||( index >= this.idList.size())){ 
			return sourceSet.getDefault();
		}
		FlightConfigurationId fcid = this.idList.get(index);
		return this.sourceSet.get( fcid);
	}
	
	@Override
	public int getSize() {
		this.idList = this.sourceSet.getIds();
		return this.idList.size();
	}
	
	@Override
	public Object getSelectedItem() {
		return this.selected;
	}
	
	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}
		
		if( item.getClass().isAssignableFrom(this.selected.getClass())){
			this.selected = item; 
			return;
		}else{
			throw new IllegalArgumentException("attempted to set selected item (oftype "+item.getClass().getSimpleName()
					+") when this generic contains a type: "+this.selected.getClass().getSimpleName()); 
		}
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
		this.idList = this.sourceSet.getIds();		
	}
	
}
