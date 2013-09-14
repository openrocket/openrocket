package net.sf.openrocket.gui.main.flightconfigpanel;

import javax.swing.JPanel;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;

public abstract class FlightConfigurablePanel<T extends FlightConfigurableComponent> extends JPanel {

	protected static final Translator trans = Application.getTranslator();
	protected RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	protected final FlightConfigurationPanel flightConfigurationPanel;
	protected final Rocket rocket;

	public FlightConfigurablePanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationPanel = flightConfigurationPanel;
		this.rocket = rocket;
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

}