package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;

class MotorConfigurationHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final Rocket rocket;
	private String name = null;
	private boolean inNameElement = false;
	private HashMap<Integer, Boolean> stageActiveness = new HashMap<>();

	public MotorConfigurationHandler(Rocket rocket, DocumentLoadingContext context) {
		this.rocket = rocket;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if ((inNameElement && element.equals("name")) || !(element.equals("name") || element.equals("stage"))) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return null;
		}
		if (element.equals("name")) {
			inNameElement = true;
		}

		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		if (element.equals("name")) {
			name = content;
		} else if (element.equals("stage")) {
			int stageNr = Integer.parseInt(attributes.get("number"));
			boolean isActive = Boolean.parseBoolean(attributes.get("active"));
			stageActiveness.put(stageNr, isActive);
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		FlightConfigurationId fcid = new FlightConfigurationId(attributes.remove("configid"));
		if (!fcid.isValid()) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		rocket.createFlightConfiguration(fcid);

		if (name != null && name.trim().length() > 0) {
			rocket.getFlightConfiguration(fcid).setName(name);
		}

		for (int stageNr : stageActiveness.keySet()) {
			rocket.getFlightConfiguration(fcid).preloadStageActiveness(stageNr, stageActiveness.get(stageNr));
		}

		if ("true".equals(attributes.remove("default"))) {
			rocket.setSelectedConfiguration(fcid);
		}

		super.closeElement(element, attributes, content, warnings);
	}
}