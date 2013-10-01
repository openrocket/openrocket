package net.sf.openrocket.gui.main.flightconfigpanel;

import java.util.EventObject;

import javax.swing.JPanel;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.StateChangeListener;

public abstract class FlightConfigurablePanel<T extends FlightConfigurableComponent> extends JPanel {

	protected static final Translator trans = Application.getTranslator();
	protected RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	protected final FlightConfigurationPanel flightConfigurationPanel;
	protected final Rocket rocket;

	public FlightConfigurablePanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationPanel = flightConfigurationPanel;
		this.rocket = rocket;
		rocket.getDefaultConfiguration().addChangeListener( new StateChangeListener() {

			@Override
			public void stateChanged(EventObject e) {
				String id = FlightConfigurablePanel.this.rocket.getDefaultConfiguration().getFlightConfigurationID();
				
				String selectedId = FlightConfigurablePanel.this.getSelectedConfigurationId();
				if ( id == null && selectedId == null ) {
					// Nothing to do
				} else if ( id == null ) {
					// need to unselect
					FlightConfigurablePanel.this.getTable().clearSelection();
				} else if ( !id.equals(selectedId)){
					// Need to change selection
					
					// We'll select the correct row, in the currently selected column.
					
					JTable table = FlightConfigurablePanel.this.getTable();
					
					int col = table.getSelectedColumn();
					
					for( int row = 0; row < table.getRowCount(); row++ ) {
						String rowId = FlightConfigurablePanel.this.rocket.getFlightConfigurationIDs()[row + 1];
						if ( rowId.equals(id) ) {
							table.changeSelection(row, col, true, false);
						}
					}
				}
				
				
			}
			
		});
	}

	protected abstract JTable getTable();
	
	protected T getSelectedComponent() {
		
		int col = getTable().getSelectedColumn();
		int row = getTable().getSelectedRow();
		if ( row < 0 || col < 0 ) {
			return null;
		}
		Object tableValue = getTable().getModel().getValueAt(row, col);
		if ( tableValue instanceof Pair ) {
			Pair<String,T> selectedComponent = (Pair<String,T>) tableValue;
			return selectedComponent.getV();
		}
		return null;
	}
	
	protected String getSelectedConfigurationId() {
		int col = getTable().getSelectedColumn();
		int row = getTable().getSelectedRow();
		if ( row < 0 || col < 0 ) {
			return null;
		}
		Object tableValue = getTable().getModel().getValueAt(row, col);
		if ( tableValue instanceof Pair ) {
			Pair<String,T> selectedComponent = (Pair<String,T>) tableValue;
			return selectedComponent.getU();
		} else if ( tableValue instanceof String ){
			return (String) tableValue;
		}
		return null;
	}

}