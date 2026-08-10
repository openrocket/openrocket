package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;
import java.util.UUID;

import info.openrocket.core.logging.Message;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.SimulationAbort.Cause;
import info.openrocket.core.logging.Warning;
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
	
	// Find the full flight data type given the value stored in the XML file.
	// Note: this way of doing it requires that custom expressions always come before flight data in the file,
	// not the nicest but this is always the case anyway.
	private FlightDataType findFlightDataType(String name) {
		// 1. Try the stable save key (files saved with the current format store e.g. "TYPE_DRAG_COEFF")
		FlightDataType byKey = FlightDataType.getTypeBySaveKey(name);
		if (byKey != null) {
			return byKey;
		}

		// 2. Try matching by display name (files saved before save keys were introduced)
		for (FlightDataType t : FlightDataType.ALL_TYPES) {
			if (t.getName().equals(name)) {
				return t;
			}
		}

		// 3. Replace deprecated 'Position upwind' with new 'Position North of launch' option
		if (name.equals(trans.get("FlightDataType.TYPE_UPWIND"))) {
			return FlightDataType.TYPE_POSITION_Y;
		}

		// 4. Legacy English name mappings: type names were updated to include symbol suffixes
		// (e.g. "Drag coefficient" -> "Drag coefficient (CD)"). Maps names from files saved
		// before that rename so they still load correctly.
		switch (name) {
			case "Drag coefficient":           return FlightDataType.TYPE_DRAG_COEFF;
			case "Axial drag coefficient":     return FlightDataType.TYPE_AXIAL_DRAG_COEFF;
			case "Friction drag coefficient":  return FlightDataType.TYPE_FRICTION_DRAG_COEFF;
			case "Pressure drag coefficient":  return FlightDataType.TYPE_PRESSURE_DRAG_COEFF;
			case "Base drag coefficient":      return FlightDataType.TYPE_BASE_DRAG_COEFF;
			case "Normal force coefficient":   return FlightDataType.TYPE_NORMAL_FORCE_COEFF;
			case "Pitch moment coefficient":   return FlightDataType.TYPE_PITCH_MOMENT_COEFF;
			case "Roll rate":                  return FlightDataType.TYPE_ROLL_RATE;
			case "Pitch rate":                 return FlightDataType.TYPE_PITCH_RATE;
			case "Yaw rate":                   return FlightDataType.TYPE_YAW_RATE;
		}

		// 5. Look in custom expressions
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
			Message data = null;
			RocketComponent source = null;
			String sourceID;
			UUID id = null;
			
			try {
				time = DocumentConfig.stringToDouble(attributes.get("time"));
			} catch (NumberFormatException e) {
				warnings.add("Illegal event time specification, ignoring: " + e.getMessage());
				return;
			}
			
			type = (Type) DocumentConfig.findEnum(attributes.get("type"), FlightEvent.Type.class);
			if (type == null) {
				warnings.add("Illegal event specification, ignoring.");
				return;
			}

			// Get the event ID
			if (null != attributes.get("id")) {
				id = UUID.fromString(attributes.get("id"));
			}

			// Get the event source
			Rocket rocket = context.getOpenRocketDocument().getRocket();
			sourceID = attributes.get("source");
			if (sourceID != null) {
				source = rocket.findComponent(UUID.fromString(sourceID));
			}

			// For warning events, get the warning
			if (type == FlightEvent.Type.SIM_WARN) {
				String warnid = attributes.get("warnid");
				if (null != warnid) {
					data = simHandler.getWarningSet().findById(UUID.fromString(warnid));
				}
			}
			
			// For aborts, get the cause
			Cause cause = (Cause) DocumentConfig.findEnum(attributes.get("cause"), SimulationAbort.Cause.class);
			if (cause != null) {
				data = new SimulationAbort(cause);
			}

			FlightEvent event = null;
			try {
				event = new FlightEvent(type, time, source, data, id);
				branch.addEvent(event);
			} catch (IllegalStateException e) {
				warnings.add("Illegal parameters for FlightEvent: " + e.getMessage());
			}

			// For EventAfterLanding warning events, hook the event up to the warning if possible
			if ((type == FlightEvent.Type.SIM_WARN) &&
				(null != data) &&
				(data instanceof Warning.EventAfterLanding)) {
				if (null != attributes.get("eventid")) {
					UUID eventID = UUID.fromString(attributes.get("eventid"));
					((Warning.EventAfterLanding) data).setEvent(branch.findEvent(eventID));
				}
			}
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
