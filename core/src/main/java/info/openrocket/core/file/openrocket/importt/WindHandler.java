package info.openrocket.core.file.openrocket.importt;

import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;

import java.util.HashMap;

public class WindHandler extends AbstractElementHandler {
	private final String model;
	private final SimulationOptions options;

	public WindHandler(String model, SimulationOptions options) {
		this.model = model;
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

		if ("pinknoise".equals(model)) {
			if (element.equals("windaverage")) {
				if (!Double.isNaN(d)) {
					options.getPinkNoiseWindModel().setAverage(d);
				}
			} else if (element.equals("windturbulence")) {
				if (!Double.isNaN(d)) {
					options.getPinkNoiseWindModel().setTurbulenceIntensity(d);
				}
			} else if (element.equals("winddirection")) {
				if (!Double.isNaN(d)) {
					options.getPinkNoiseWindModel().setDirection(d);
				}
			}
		} else if ("multilevel".equals(model)) {
			if (element.equals("windlevel")) {
				double altitude = Double.parseDouble(attributes.get("altitude"));
				double speed = Double.parseDouble(attributes.get("speed"));
				double direction = Double.parseDouble(attributes.get("direction"));
				options.getMultiLevelWindModel().addWindLevel(altitude, speed, direction);
			}
		}
	}

	public void storeSettings(SimulationOptions options, WarningSet warnings) {
		if ("pinknoise".equals(model)) {
			options.setWindModelType(WindModelType.PINK_NOISE);
		} else if ("multilevel".equals(model)) {
			options.setWindModelType(WindModelType.MULTI_LEVEL);
		} else {
			warnings.add("Unknown wind model type '" + model + "', using default.");
		}
	}
}
