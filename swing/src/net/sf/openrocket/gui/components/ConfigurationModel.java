package net.sf.openrocket.gui.components;

import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.util.StateChangeListener;

import javax.swing.*;
import javax.swing.event.ListDataListener;

import java.util.EventObject;

public class ConfigurationModel implements ComboBoxModel<FlightConfiguration>, StateChangeListener {

    private final Rocket rkt;
    
    //private FlightConfigurationSelector(){}

    public ConfigurationModel( final Rocket _rkt) {
        rkt = _rkt;
    }


	@Override
	public void stateChanged(EventObject e) {
//		FlightConfiguration newConfig = (FlightConfiguration)this.getSelectedItem();
//        rkt.setSelectedConfiguration( newConfig.getId());
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


}
