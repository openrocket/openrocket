package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.util.GeodeticComputationStrategy;

class SimulationConditionsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	public FlightConfigurationId idToSet = FlightConfigurationId.ERROR_FCID;
	private SimulationOptions options;
	private AtmosphereHandler atmosphereHandler;
	
	public SimulationConditionsHandler(Rocket rocket, DocumentLoadingContext context) {
		this.context = context;
		options = new SimulationOptions();
		// Set up default loading settings (which may differ from the new defaults)
		options.setGeodeticComputation(GeodeticComputationStrategy.FLAT);
	}
	
	public SimulationOptions getConditions() {
		return options;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		if (element.equals("atmosphere")) {
			atmosphereHandler = new AtmosphereHandler(attributes.get("model"), context);
			return atmosphereHandler;
		}
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		double d = Double.NaN;
		try {
			d = Double.parseDouble(content);
		} catch (NumberFormatException ignore) {
		}
		
		
		if (element.equals("configid")) {
			this.idToSet= new FlightConfigurationId(content);
		} else if (element.equals("launchrodlength")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod length defined, ignoring.");
			} else {
				options.setLaunchRodLength(d);
			}
		} else if (element.equals("launchrodangle")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod angle defined, ignoring.");
			} else {
				options.setLaunchRodAngle(d * Math.PI / 180);
			}
		} else if (element.equals("launchroddirection")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod direction defined, ignoring.");
			} else {
				options.setLaunchRodDirection(d * 2.0 * Math.PI / 360);
			}
		} else if (element.equals("windaverage")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal average windspeed defined, ignoring.");
			} else {
				options.setWindSpeedAverage(d);
			}
		} else if (element.equals("windturbulence")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal wind turbulence intensity defined, ignoring.");
			} else {
				options.setWindTurbulenceIntensity(d);
			}
		} else if (element.equals("launchaltitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch altitude defined, ignoring.");
			} else {
				options.setLaunchAltitude(d);
			}
		} else if (element.equals("launchlatitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch latitude defined, ignoring.");
			} else {
				options.setLaunchLatitude(d);
			}
		} else if (element.equals("launchlongitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch longitude.");
			} else {
				options.setLaunchLongitude(d);
			}
		} else if (element.equals("geodeticmethod")) {
			GeodeticComputationStrategy gcs =
					(GeodeticComputationStrategy) DocumentConfig.findEnum(content, GeodeticComputationStrategy.class);
			if (gcs != null) {
				options.setGeodeticComputation(gcs);
			} else {
				warnings.add("Unknown geodetic computation method '" + content + "'");
			}
		} else if (element.equals("atmosphere")) {
			atmosphereHandler.storeSettings(options, warnings);
		} else if (element.equals("timestep")) {
			if (Double.isNaN(d) || d <= 0) {
				warnings.add("Illegal time step defined, ignoring.");
			} else {
				options.setTimeStep(d);
			}
		}
	}
}