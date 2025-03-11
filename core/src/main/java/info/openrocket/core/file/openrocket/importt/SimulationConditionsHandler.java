package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.GeodeticComputationStrategy;

class SimulationConditionsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	public FlightConfigurationId idToSet = FlightConfigurationId.ERROR_FCID;
	private final SimulationOptions options;
	private AtmosphereHandler atmosphereHandler;
	private WindHandler windHandler;

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
		if (element.equals("wind")) {
			windHandler = new WindHandler(attributes.get("model"), options, attributes);
			return windHandler;
		} else if (element.equals("atmosphere")) {
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

		switch (element) {
			case "configid" -> this.idToSet = new FlightConfigurationId(content);
			case "launchrodlength" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch rod length defined, ignoring.");
				} else {
					options.setLaunchRodLength(d);
				}
			}
			case "launchrodangle" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch rod angle defined, ignoring.");
				} else {
					options.setLaunchRodAngle(d * Math.PI / 180);
				}
			}
			case "launchroddirection" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch rod direction defined, ignoring.");
				} else {
					options.setLaunchRodDirection(d * 2.0 * Math.PI / 360);
				}
			}

			// TODO: remove once support for OR 23.09 and prior is dropped
			case "windaverage" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal average windspeed defined, ignoring.");
				} else {
					options.getAverageWindModel().setAverage(d);
				}
			}
			case "windturbulence" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal wind turbulence intensity defined, ignoring.");
				} else {
					options.getAverageWindModel().setTurbulenceIntensity(d);
				}
			}
			case "winddirection" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal wind direction defined, ignoring.");
				} else {
					options.getAverageWindModel().setDirection(d);
				}
			}

			case "wind" -> windHandler.storeSettings(options, warnings);
			case "windmodeltype" -> {
				options.setWindModelType(WindModelType.fromString(content));
			}

			case "launchaltitude" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch altitude defined, ignoring.");
				} else {
					options.setLaunchAltitude(d);
				}
			}
			case "launchlatitude" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch latitude defined, ignoring.");
				} else {
					options.setLaunchLatitude(d);
				}
			}
			case "launchlongitude" -> {
				if (Double.isNaN(d)) {
					warnings.add("Illegal launch longitude.");
				} else {
					options.setLaunchLongitude(d);
				}
			}
			case "geodeticmethod" -> {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) DocumentConfig.findEnum(content,
						GeodeticComputationStrategy.class);
				if (gcs != null) {
					options.setGeodeticComputation(gcs);
				} else {
					warnings.add("Unknown geodetic computation method '" + content + "'");
				}
			}
			case "atmosphere" -> atmosphereHandler.storeSettings(options, warnings);
			case "timestep" -> {
				if (Double.isNaN(d) || d <= 0) {
					warnings.add("Illegal time step defined, ignoring.");
				} else {
					options.setTimeStep(d);
				}
			}
			case "maxtime" -> {
				if (Double.isNaN(d) || d <= 0) {
					warnings.add("Illegal max simulation time defined, ignoring.");
				} else {
					options.setMaxSimulationTime(d);
				}
			}
		}
	}
}