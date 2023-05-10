package net.sf.openrocket.gui.components;

import java.util.EventObject;

import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.StateChangeListener;

// combobox for flight configurations
// this is insane -- it appears the only way to reconstruct a
// JComboBox properly after adding a new entry (when added to the
// underlying data structure being displayed, not when added directly
// to the combobox or to its model) is to reconstruct the model.  This
// is done quickly enough I might as well just do it every time the
// combobox is opened, rather than trying to watch and see if it's needed.
public class ConfigurationComboBox extends JComboBox<FlightConfiguration> implements StateChangeListener {
    public class ConfigurationModel implements MutableComboBoxModel<FlightConfiguration> {
		
		private final Rocket rkt;
		private FlightConfiguration selectedConfig;
		private final boolean updateRocketConfig;
		private final ConfigurationModel listener;

		/**
		 * @param _rkt the rocket to get the configurations from and to (optionally) change the rocket's selected configuration
		 * @param _updateRocketConfig whether to update the rocket's selected configuration based on the selected combo box item,
		 *                            or just change the combo box item without altering the rocket's configuration.
		 * @param listener model that should change its selected item to this model's selected item
		 */
		public ConfigurationModel(final Rocket _rkt, boolean _updateRocketConfig, ConfigurationModel listener) {
			this.rkt = _rkt;
			this.updateRocketConfig = _updateRocketConfig;
			this.selectedConfig = this.rkt.getSelectedConfiguration();
			this.listener = listener;
		}

		public ConfigurationModel(final Rocket _rkt, boolean _updateRocketConfig) {
			this(_rkt, _updateRocketConfig, null);
		}
		
		@Override
		public FlightConfiguration getSelectedItem() {
			if (updateRocketConfig) {
				return rkt.getSelectedConfiguration();
			} else {
				return selectedConfig;
			}
		}
		
		@Override
		public void setSelectedItem(Object nextItem) {
			if( nextItem instanceof FlightConfiguration ){
				FlightConfigurationId selectedId = ((FlightConfiguration)nextItem).getId();
				if (updateRocketConfig) {
					rkt.setSelectedConfiguration(selectedId);
				} else {
					selectedConfig = rkt.getFlightConfiguration(selectedId);
				}

				if (listener != null) {
					listener.setSelectedItem(nextItem);
				}
			}
		}
		
		@Override
		public FlightConfiguration getElementAt( final int configIndex) {
			return rkt.getFlightConfigurationByIndex(configIndex, true);
		}
		
		@Override
		public int getSize() {
			// plus the default config
			return rkt.getConfigurationCount()+1;
		}
		
		// ====== MutableComboBoxModel Functions ======
		// these functions don't need to do anything, just being a 'mutable' version of the combo box
		// is enough to allow updating the UI
		
		@Override
		public void addListDataListener(ListDataListener l) {}
		
		@Override
		public void removeListDataListener(ListDataListener l) {}
		
		@Override
		public void addElement(FlightConfiguration arg0) {}
		
		@Override
		public void insertElementAt(FlightConfiguration arg0, int arg1) {}
		
		@Override
		public void removeElement(Object arg0) {}
		
		@Override
		public void removeElementAt(int arg0) {}
		
	}
	
    private final Rocket rkt;

	/**
	 * @param _rkt the rocket to get the configurations from and to (optionally) change the rocket's selected configuration
	 * @param _updateRocketConfig whether to update the rocket's selected configuration based on the selected combo box item,
	 *                            or just change the combo box item without altering the rocket's configuration.
	 */
    public ConfigurationComboBox(Rocket _rkt, boolean _updateRocketConfig) {
		rkt = _rkt;
		final ConfigurationModel model = new ConfigurationModel(rkt, _updateRocketConfig);
		setModel(model);
		rkt.addChangeListener(this);
	
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				final ConfigurationModel model2 = new ConfigurationModel(rkt, _updateRocketConfig, model);
				model2.setSelectedItem(model.getSelectedItem());
			    setModel(model2);
			}
			
		});
	
    }

	public ConfigurationComboBox(Rocket _rkt) {
		this(_rkt, true);
	}
    
    @Override
    public void stateChanged(EventObject e) {
    	this.repaint();
    	this.revalidate();
    }
}
