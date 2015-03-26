package net.sf.openrocket.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.simulation.extension.SimulationExtensionProvider;
import net.sf.openrocket.simulation.extension.impl.JavaCode;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StringUtil;

import com.google.inject.Key;

class SingleSimulationHandler extends AbstractElementHandler {
	
	private final DocumentLoadingContext context;
	
	private final OpenRocketDocument doc;
	
	private String name;
	
	private SimulationConditionsHandler conditionHandler;
	private ConfigHandler configHandler;
	private FlightDataHandler dataHandler;
	
	private final List<SimulationExtension> extensions = new ArrayList<SimulationExtension>();
	
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
		} else if (element.equals("extension")) {
			configHandler = new ConfigHandler();
			return configHandler;
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
			extensions.add(compatibilityExtension(content.trim()));
		} else if (element.equals("extension") && !StringUtil.isEmpty(attributes.get("extensionid"))) {
			String id = attributes.get("extensionid");
			SimulationExtension extension = null;
			Set<SimulationExtensionProvider> extensionProviders = Application.getInjector().getInstance(new Key<Set<SimulationExtensionProvider>>() {
			});
			for (SimulationExtensionProvider p : extensionProviders) {
				if (p.getIds().contains(id)) {
					extension = p.getInstance(id);
				}
			}
			if (extension != null) {
				extension.setConfig(configHandler.getConfig());
				extensions.add(extension);
			} else {
				warnings.add("Simulation extension with id '" + id + "' not found.");
			}
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
				conditions, extensions, data);
		
		doc.addSimulation(simulation);
	}
	
	
	private SimulationExtension compatibilityExtension(String className) {
		JavaCode extension = Application.getInjector().getInstance(JavaCode.class);
		extension.setClassName(className);
		return extension;
	}
	
}