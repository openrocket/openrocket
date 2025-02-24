package info.openrocket.core.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.MessagePriority;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

class WarningHandler extends AbstractElementHandler {
	private Rocket rocket;
	private WarningSet warningSet;
	private Warning warning;
	private UUID id = UUID.randomUUID();
	private MessagePriority priority = MessagePriority.NORMAL;
	private ArrayList<RocketComponent> sources = new ArrayList<>();
	private String warningText;

	private String parameter;

	public WarningHandler(Rocket rocket, WarningSet warningSet) {
		this.rocket = rocket;
		this.warningSet = warningSet;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
									  WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
							 String content, WarningSet warnings) {
		if (element.equals("id")) {
			id = UUID.fromString(content);
		} else if (element.equals("description")) {
			warningText = content.trim();
		} else if (element.equals("priority")) {
			priority = MessagePriority.fromExportLabel(content);
		} else if (element.equals("source")) {
			RocketComponent component = rocket.findComponent(UUID.fromString(content));
			sources.add(component);
		} else if (element.equals("parameter")) {
			parameter = content.trim();
		} else {
			warnings.add("Unknown element '" + element + "', ignoring.");
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
						   String content, WarningSet warnings) {

		String type = attributes.get("type");
		if (null == type) {
			type = "Other";
		}
		if (null == warningText) {
			warningText = content.trim();
		}

		double parameterVal = Double.NaN;
		if (null != parameter) {
			parameterVal = Double.parseDouble(parameter);
		}
		
		if (type.equals("LargeAOA")) {
			warning = new Warning.LargeAOA(parameterVal);
		} else if (type.equals("HighSpeedDeployment")) {
			warning = new Warning.HighSpeedDeployment(parameterVal, (RocketComponent) null);
		} else if (type.equals("EventAfterLanding")) {
			warning = new Warning.EventAfterLanding(null);
		} else {
			warning = Warning.fromString(content.trim());
		}

		if (null != id) {
			warning.setID(id);
		}
		if (null != priority) {
			warning.setPriority(priority);
		}
		if (null != sources) {
			warning.setSources(sources.toArray(new RocketComponent[0]));
		}

		warningSet.add(warning);
	}
}
