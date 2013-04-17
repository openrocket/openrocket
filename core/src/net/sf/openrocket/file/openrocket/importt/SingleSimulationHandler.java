package net.sf.openrocket.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.Simulation.Status;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationOptions;

class SingleSimulationHandler extends AbstractElementHandler {
	
	private final DocumentLoadingContext context;
	
	private final OpenRocketDocument doc;
	
	private String name;
	
	private SimulationConditionsHandler conditionHandler;
	private FlightDataHandler dataHandler;
	
	private final List<String> listeners = new ArrayList<String>();
	
	public SingleSimulationHandler(OpenRocketDocument doc, DocumentLoadingContext context) {
		this.doc = doc;
		this.context = context;
	}
	
	public OpenRocketDocument getDocument() {
		return doc;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (element.equals("name") || element.equals("simulator") ||
				element.equals("calculator") || element.equals("listener")) {
			return PlainTextHandler.INSTANCE;
		} else if (element.equals("conditions")) {
			conditionHandler = new SimulationConditionsHandler(doc.getRocket(), context);
			return conditionHandler;
		} else if (element.equals("flightdata")) {
			dataHandler = new FlightDataHandler(this, context);
			return dataHandler;
		} else {
			warnings.add("Unknown element '" + element + "', ignoring.");
			return null;
		}
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		if (element.equals("name")) {
			name = content;
		} else if (element.equals("simulator")) {
			if (!content.trim().equals("RK4Simulator")) {
				warnings.add("Unknown simulator '" + content.trim() + "' specified, ignoring.");
			}
		} else if (element.equals("calculator")) {
			if (!content.trim().equals("BarrowmanCalculator")) {
				warnings.add("Unknown calculator '" + content.trim() + "' specified, ignoring.");
			}
		} else if (element.equals("listener") && content.trim().length() > 0) {
			listeners.add(content.trim());
		}
		
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		String s = attributes.get("status");
		Simulation.Status status = (Status) DocumentConfig.findEnum(s, Simulation.Status.class);
		if (status == null) {
			warnings.add("Simulation status unknown, assuming outdated.");
			status = Simulation.Status.OUTDATED;
		}
		
		SimulationOptions conditions;
		if (conditionHandler != null) {
			conditions = conditionHandler.getConditions();
		} else {
			warnings.add("Simulation conditions not defined, using defaults.");
			conditions = new SimulationOptions(doc.getRocket());
		}
		
		if (name == null)
			name = "Simulation";
		
		FlightData data;
		if (dataHandler == null)
			data = null;
		else
			data = dataHandler.getFlightData();
		
		Simulation simulation = new Simulation(doc.getRocket(), status, name,
				conditions, listeners, data);
		
		doc.addSimulation(simulation);
	}
}