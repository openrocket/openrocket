package info.openrocket.core.file.openrocket.importt;

import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;

import java.util.HashMap;

public class WindHandler extends AbstractElementHandler {
	private final String model;
	private final SimulationOptions options;

	public WindHandler(String model, SimulationOptions options, HashMap<String, String> attributes) {
		this.model = model;

		if ("multilevel".equals(model)) {
			// For multilevel wind model, clear the levels (clear the initial level) to fill it up with actual data
			options.getMultiLevelWindModel().clearLevels();

			// Set the altitude reference
			String reference = attributes.get("altituderef");
			if (reference != null) {
				WindModel.AltitudeReference altitudeReference =
						(WindModel.AltitudeReference) DocumentConfig.findEnum(reference, WindModel.AltitudeReference.class);
				options.getMultiLevelWindModel().setAltitudeReference(altitudeReference);
			}
		}
		this.options = options;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
									  WarningSet warnings) {
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

		if ("average".equals(model)) {
			switch (element) {
				case "speed" -> {
					if (!Double.isNaN(d)) {
						options.getAverageWindModel().setAverage(d);
					}
				}
				case "direction" -> {
					if (!Double.isNaN(d)) {
						options.getAverageWindModel().setDirection(d);
					}
				}
				case "standarddeviation" -> {
					if (!Double.isNaN(d)) {
						options.getAverageWindModel().setStandardDeviation(d);
					}
				}
			}
		} else if ("multilevel".equals(model)) {
			if (element.equals("windlevel")) {
				double altitude = Double.parseDouble(attributes.get("altitude"));
				double speed = Double.parseDouble(attributes.get("speed"));
				double direction = Double.parseDouble(attributes.get("direction"));
				double standardDeviation = Double.parseDouble(attributes.get("standarddeviation"));
				options.getMultiLevelWindModel().addWindLevel(altitude, speed, direction, standardDeviation);
			}
		}
	}

	public void storeSettings(SimulationOptions options, WarningSet warnings) {
		if ("average".equals(model)) {
			options.setWindModelType(WindModelType.AVERAGE);
		} else if ("multilevel".equals(model)) {
			options.setWindModelType(WindModelType.MULTI_LEVEL);
		} else {
			warnings.add("Unknown wind model type '" + model + "', using default.");
		}
	}
}
