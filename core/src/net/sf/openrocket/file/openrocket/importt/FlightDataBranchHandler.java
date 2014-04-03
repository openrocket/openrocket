package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.FlightEvent.Type;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.unit.UnitGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlightDataBranchHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FlightDataType[] types;
	private final FlightDataBranch branch;
	
	private static final Logger log = LoggerFactory.getLogger(FlightDataBranchHandler.class);
	private final SingleSimulationHandler simHandler;
	
	public FlightDataBranchHandler(String name, String typeList, SingleSimulationHandler simHandler, DocumentLoadingContext context) {
		this.simHandler = simHandler;
		this.context = context;
		String[] split = typeList.split(",");
		types = new FlightDataType[split.length];
		for (int i = 0; i < split.length; i++) {
			String typeName = split[i];
			FlightDataType matching = findFlightDataType(typeName);
			types[i] = matching;
			//types[i] = FlightDataType.getType(typeName, matching.getSymbol(), matching.getUnitGroup());
		}
		
		// TODO: LOW: May throw an IllegalArgumentException
		branch = new FlightDataBranch(name, types);
	}
	
	/**
	 * @param timeToOptimumAltitude
	 * @see net.sf.openrocket.simulation.FlightDataBranch#setTimeToOptimumAltitude(double)
	 */
	public void setTimeToOptimumAltitude(double timeToOptimumAltitude) {
		branch.setTimeToOptimumAltitude(timeToOptimumAltitude);
	}
	
	/**
	 * @param optimumAltitude
	 * @see net.sf.openrocket.simulation.FlightDataBranch#setOptimumAltitude(double)
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
			
			branch.addEvent(new FlightEvent(type, time));
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