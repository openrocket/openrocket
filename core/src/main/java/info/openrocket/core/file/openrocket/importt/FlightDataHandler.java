package info.openrocket.core.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.openrocket.core.logging.MessagePriority;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;

class FlightDataHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	
	private FlightDataBranchHandler dataHandler;
	private final WarningSet warningSet = new WarningSet();
	private final List<FlightDataBranch> branches = new ArrayList<>();
	
	private final SingleSimulationHandler simHandler;
	private FlightData data;
	
	
	public FlightDataHandler(SingleSimulationHandler simHandler, DocumentLoadingContext context) {
		this.context = context;
		this.simHandler = simHandler;
	}
	
	public FlightData getFlightData() {
		return data;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (element.equals("warning")) {
			return new WarningHandler(context.getOpenRocketDocument().getRocket(), warningSet);
		}
		if (element.equals("databranch")) {
			if (attributes.get("name") == null || attributes.get("types") == null) {
				warnings.add("Illegal flight data definition, ignoring.");
				return null;
			}
			dataHandler = new FlightDataBranchHandler(attributes.get("name"),
					attributes.get("types"),
					simHandler, context);
			
			if (attributes.get("optimumAltitude") != null) {
				double optimumAltitude = Double.NaN;
				try {
					optimumAltitude = Double.parseDouble(attributes.get("optimumAltitude"));
				} catch (NumberFormatException ignore) {
				}
				dataHandler.setOptimumAltitude(optimumAltitude);
			}
			if (attributes.get("timeToOptimumAltitude") != null) {
				double timeToOptimumAltitude = Double.NaN;
				try {
					timeToOptimumAltitude = Double.parseDouble(attributes.get("timeToOptimumAltitude"));
				} catch (NumberFormatException ignore) {
				}
				dataHandler.setTimeToOptimumAltitude(timeToOptimumAltitude);
			}
			return dataHandler;
		}
		
		warnings.add("Unknown element '" + element + "' encountered, ignoring.");
		return null;
	}
	
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		if (element.equals("databranch")) {
			FlightDataBranch branch = dataHandler.getBranch();
			if (branch.getLength() > 0) {
				branches.add(branch);
			}
			//		} else if (element.equals("warning")) {
			//			String priorityStr = attributes.get("priority");
			//			MessagePriority priority = MessagePriority.fromExportLabel(priorityStr);
			//			warningSet.add(Warning.fromString(content, priority));
		}
	}
	
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (branches.size() > 0) {
			data = new FlightData(branches.toArray(new FlightDataBranch[0]));
		} else {
			double maxAltitude = Double.NaN;
			double maxVelocity = Double.NaN;
			double maxAcceleration = Double.NaN;
			double maxMach = Double.NaN;
			double timeToApogee = Double.NaN;
			double flightTime = Double.NaN;
			double groundHitVelocity = Double.NaN;
			double launchRodVelocity = Double.NaN;
			double deploymentVelocity = Double.NaN;
			double optimumDelay = Double.NaN;

			try {
				maxAltitude = DocumentConfig.stringToDouble(attributes.get("maxaltitude"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxVelocity = DocumentConfig.stringToDouble(attributes.get("maxvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxAcceleration = DocumentConfig.stringToDouble(attributes.get("maxacceleration"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxMach = DocumentConfig.stringToDouble(attributes.get("maxmach"));
			} catch (NumberFormatException ignore) {
			}
			try {
				timeToApogee = DocumentConfig.stringToDouble(attributes.get("timetoapogee"));
			} catch (NumberFormatException ignore) {
			}
			try {
				flightTime = DocumentConfig.stringToDouble(attributes.get("flighttime"));
			} catch (NumberFormatException ignore) {
			}
			try {
				groundHitVelocity = DocumentConfig.stringToDouble(attributes.get("groundhitvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				launchRodVelocity = DocumentConfig.stringToDouble(attributes.get("launchrodvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				deploymentVelocity = DocumentConfig.stringToDouble(attributes.get("deploymentvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				optimumDelay = DocumentConfig.stringToDouble(attributes.get("optimumdelay"));
			} catch (NumberFormatException ignore) {
			}

			data = new FlightData(maxAltitude, maxVelocity, maxAcceleration, maxMach,
					timeToApogee, flightTime, groundHitVelocity, launchRodVelocity, deploymentVelocity, optimumDelay);
		}
		
		data.getWarningSet().addAll(warningSet);
		data.immute();
	}


	public WarningSet getWarningSet() {
		return warningSet;
	}
}
