package net.sf.openrocket.gui.components;

import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.util.StateChangeListener;

import javax.swing.*;
import javax.swing.event.ListDataListener;

import java.util.EventObject;

public class ConfigurationModel implements MutableComboBoxModel<FlightConfiguration>, StateChangeListener {

    private final Rocket rkt;
    private final JComboBox<FlightConfiguration> combo;
    
    public ConfigurationModel( final Rocket _rkt, final JComboBox<FlightConfiguration> _combo) {
        this.rkt = _rkt;
        this.combo = _combo;
    }

	@Override
	public void stateChanged(EventObject eo) {
		combo.revalidate();
		combo.repaint();
    }


	@Override
	public Object getSelectedItem() {
		return rkt.getSelectedConfiguration();
	}


	@Override
	public void setSelectedItem(Object nextItem) {
		if( nextItem instanceof FlightConfiguration ){
			FlightConfigurationId selectedId = ((FlightConfiguration)nextItem).getId();
			rkt.setSelectedConfiguration(selectedId);
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// let the rocket send events, if necessary
		// ignore any listen requests here...
	}


	public FlightConfiguration getElementAt( final int configIndex) {
		return rkt.getFlightConfigurationByIndex(configIndex, true);
	}


	@Override
	public int getSize() {
		// plus the default config
		return rkt.getConfigurationCount()+1;
	}


	@Override
	public void removeListDataListener(ListDataListener l) {
		// delegate event handling to the rocket
		// ignore any listen requests here...
	}

	// ====== MutableComboBoxModel Functions ======
	// these functions don't need to do anything, just being a 'mutable' version of the combo box
	// is enough to allow updating the UI
	
	@Override
	public void addElement(FlightConfiguration arg0) {}

	@Override
	public void insertElementAt(FlightConfiguration arg0, int arg1) {}

	@Override
	public void removeElement(Object arg0) {}

	@Override
	public void removeElementAt(int arg0) {}


}
