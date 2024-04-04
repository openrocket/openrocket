package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.SimulationAbort.Cause;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.FlightEvent.Type;
import info.openrocket.core.simulation.customexpression.CustomExpression;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlightDataBranchHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FlightDataType[] types;
	private final FlightDataBranch branch;
	
	private static final Logger log = LoggerFactory.getLogger(FlightDataBranchHandler.class);
	private final SingleSimulationHandler simHandler;
	private static final Translator trans = Application.getTranslator();

	public FlightDataBranchHandler(String name, String typeList, SingleSimulationHandler simHandler,
			DocumentLoadingContext context) {
		this.simHandler = simHandler;
		this.context = context;
		String[] split = typeList.split(",");
		types = new FlightDataType[split.length];
		for (int i = 0; i < split.length; i++) {
			String typeName = split[i];
			FlightDataType matching = findFlightDataType(typeName);
			types[i] = matching;
			//types[i] = FlightDataType.getShapeType(typeName, matching.getSymbol(), matching.getUnitGroup());
		}
		
		// TODO: LOW: May throw an IllegalArgumentException
		branch = new FlightDataBranch(name, types);
	}
	
	/**
	 * @param timeToOptimumAltitude
	 * @see info.openrocket.core.simulation.FlightDataBranch#setTimeToOptimumAltitude(double)
	 */
	public void setTimeToOptimumAltitude(double timeToOptimumAltitude) {
		branch.setTimeToOptimumAltitude(timeToOptimumAltitude);
	}
	
	/**
	 * @param optimumAltitude
	 * @see info.openrocket.core.simulation.FlightDataBranch#setOptimumAltitude(double)
	 */
	public void setOptimumAltitude(double optimumAltitude) {
		branch.setOptimumAltitude(optimumAltitude);
	}
	
	// Find the full flight data type given name only
	// Note: this way of doing it requires that custom expressions always come before flight data in the file,
	// not the nicest but this is always the case anyway.
	private FlightDataType findFlightDataType(String name) {
		
		// Kevins version with lookup by key. Not using right now
		/*
		if ( key != null ) {
			for (FlightDataType t : FlightDataType.ALL_TYPES){
				if (t.getKey().equals(key) ){
					return t;
				}
			}
		}
		*/
		
		// Look in built in types
		for (FlightDataType t : FlightDataType.ALL_TYPES) {
			if (t.getName().equals(name)) {
				return t;
			}
		}

		// Replace deprecated 'Position upwind' with new 'Position North of launch' option
		if (name.equals(trans.get("FlightDataType.TYPE_UPWIND"))) {
			return FlightDataType.TYPE_POSITION_Y;
		}
		
		// Look in custom expressions
		for (CustomExpression exp : simHandler.getDocument().getCustomExpressions()) {
			if (exp.getName().equals(name)) {
				return exp.getType();
			}
		}
		
		log.warn("Could not find the flight data type '" + name + "' used in the XML file. Substituted type with unknown symbol and units.");
		return FlightDataType.getType(name, "Unknown", UnitGroup.UNITS_NONE);
	}
	
	public FlightDataBranch getBranch() {
		branch.immute();
		return branch;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (element.equals("datapoint"))
			return PlainTextHandler.INSTANCE;
		if (element.equals("event"))
			return PlainTextHandler.INSTANCE;
		
		warnings.add("Unknown element '" + element + "' encountered, ignoring.");
		return null;
	}
	
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		if (element.equals("event")) {
			double time;
			FlightEvent.Type type;
			SimulationAbort abort = null;
			SimulationAbort.Cause cause = null;
			RocketComponent source = null;
			String sourceID;

			try {
				time = DocumentConfig.stringToDouble(attributes.get("time"));
			} catch (NumberFormatException e) {
				warnings.add("Illegal event specification, ignoring.");
				return;
			}
			
			type = (Type) DocumentConfig.findEnum(attributes.get("type"), FlightEvent.Type.class);
			if (type == null) {
				warnings.add("Illegal event specification, ignoring.");
				return;
			}

			// Get the event source
			Rocket rocket = context.getOpenRocketDocument().getRocket();
			sourceID = attributes.get("source");
			if (sourceID != null) {
				source = rocket.findComponent(sourceID);
			}

			// For aborts, get the cause
			cause = (Cause) DocumentConfig.findEnum(attributes.get("cause"), SimulationAbort.Cause.class);
			if (cause != null) {
				abort = new SimulationAbort(cause);
			}

			branch.addEvent(new FlightEvent(type, time, source, abort));
			return;
		}
		
		if (!element.equals("datapoint")) {
			warnings.add("Unknown element '" + element + "' encountered, ignoring.");
			return;
		}
		
		// element == "datapoint"
		
		
		// Check line format
		String[] split = content.split(",");
		if (split.length != types.length) {
			warnings.add("Data point did not contain correct amount of values, ignoring point.");
			return;
		}
		
		// Parse the doubles
		double[] values = new double[split.length];
		for (int i = 0; i < values.length; i++) {
			try {
				values[i] = DocumentConfig.stringToDouble(split[i]);
			} catch (NumberFormatException e) {
				warnings.add("Data point format error, ignoring point.");
				return;
			}
		}
		
		// Add point to branch
		branch.addPoint();
		for (int i = 0; i < types.length; i++) {
			branch.setValue(types[i], values[i]);
		}
	}
}
