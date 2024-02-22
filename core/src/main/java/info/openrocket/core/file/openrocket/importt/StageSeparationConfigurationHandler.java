package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;

class StageSeparationConfigurationHandler extends AbstractElementHandler {
	private final AxialStage stage;

	private SeparationEvent event = null;
	private double delay = Double.NaN;

	public StageSeparationConfigurationHandler(AxialStage stage, DocumentLoadingContext context) {
		this.stage = stage;
	}

	public StageSeparationConfiguration getConfiguration(StageSeparationConfiguration def) {
		StageSeparationConfiguration config = def.clone();
		if (event != null) {
			config.setSeparationEvent(event);
		}
		if (!Double.isNaN(delay)) {
			config.setSeparationDelay(delay);
		}
		return config;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content,
			WarningSet warnings) throws SAXException {

		content = content.trim();

		if ("separationevent".equals(element)) {
			event = (SeparationEvent) DocumentConfig.findEnum(content, SeparationEvent.class);
			if (event == null) {
				warnings.add(Warning.FILE_INVALID_PARAMETER);
				return;
			}
			return;
		} else if ("separationdelay".equals(element)) {
			delay = parseDouble(content, warnings, Warning.FILE_INVALID_PARAMETER);
			return;
		}
		super.closeElement(element, attributes, content, warnings);

	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		FlightConfigurationId fcid = new FlightConfigurationId(attributes.get("configid"));
		StageSeparationConfiguration sepConfig = stage.getSeparationConfigurations().get(fcid);

		// copy and update to the file-read values
		stage.getSeparationConfigurations().set(fcid, getConfiguration(sepConfig));
	}

}