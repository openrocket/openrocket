package info.openrocket.core.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.Simulation.Status;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.simulation.extension.SimulationExtensionProvider;
import info.openrocket.core.simulation.extension.impl.JavaCode;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StringUtils;

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
		} else if (element.equals("extension") && !StringUtils.isEmpty(attributes.get("extensionid"))) {
			String id = attributes.get("extensionid");
			SimulationExtension extension = null;
			Set<SimulationExtensionProvider> extensionProviders = Application.getInjector()
					.getInstance(new Key<Set<SimulationExtensionProvider>>() {
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

		SimulationOptions options;
		FlightConfigurationId idToSet = FlightConfigurationId.ERROR_FCID;
		if (conditionHandler != null) {
			options = conditionHandler.getConditions();
			idToSet = conditionHandler.idToSet;
		} else {
			warnings.add("Simulation conditions not defined, using defaults.");
			options = new SimulationOptions();
		}

		if (name == null)
			name = "Simulation";

		// If the simulation was saved with flight data (which may just be a summary)
		// mark it as loaded from the file else as not simulated. If outdated data was
		// saved,
		// it'll be marked as outdated (creating a new status for "loaded but outdated"
		// seems
		// excessive, and the fact that it's outdated is the more important)
		FlightData data;
		if (dataHandler == null)
			data = null;
		else
			data = dataHandler.getFlightData();

		if (data == null) {
			status = Status.NOT_SIMULATED;
		} else if (status != Status.OUTDATED) {
			status = Status.LOADED;
		}

		Simulation simulation = new Simulation(doc, doc.getRocket(), status, name,
				options, extensions, data);
		simulation.setFlightConfigurationId(idToSet);

		doc.addSimulation(simulation);
	}

	private SimulationExtension compatibilityExtension(String className) {
		JavaCode extension = Application.getInjector().getInstance(JavaCode.class);
		extension.setClassName(className);
		return extension;
	}

}
