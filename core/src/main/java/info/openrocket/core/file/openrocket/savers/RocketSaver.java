package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.ReferenceType;
import info.openrocket.core.rocketcomponent.Rocket;

public class RocketSaver extends RocketComponentSaver {

	private static final RocketSaver instance = new RocketSaver();

	public static ArrayList<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();

		list.add("<rocket>");
		instance.addParams(c, list);
		list.add("</rocket>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		Rocket rocket = (Rocket) c;

		if (rocket.getDesigner().length() > 0) {
			elements.add("<designer>"
					+ info.openrocket.core.util.TextUtil.escapeXML(rocket.getDesigner())
					+ "</designer>");
		}
		if (rocket.getRevision().length() > 0) {
			elements.add("<revision>"
					+ info.openrocket.core.util.TextUtil.escapeXML(rocket.getRevision())
					+ "</revision>");
		}

		// Flight configurations
		for (FlightConfigurationId fcid : rocket.getIds()) {
			FlightConfiguration flightConfig = rocket.getFlightConfiguration(fcid);
			if (fcid == null)
				continue;

			// these are actually FlightConfigurationIds, but the old tag name is preserved
			// for backwards-compatibility.
			String str = "<motorconfiguration configid=\"" + fcid.key + "\"";
			// if the configuration is the default, add the tag
			if (rocket.getSelectedConfiguration().equals(flightConfig)) {
				str += " default=\"true\"";
			}

			// close motorconfiguration opening tag
			str += ">";
			elements.add(str);

			// flight configuration name
			if (flightConfig.isNameOverridden()) {
				elements.add(OpenRocketSaver.INDENT + "<name>"
						+ info.openrocket.core.util.TextUtil.escapeXML(flightConfig.getNameRaw())
						+ "</name>");
			}
			// stage activeness
			for (AxialStage stage : rocket.getStageList()) {
				elements.add(OpenRocketSaver.INDENT + "<stage number=\"" + stage.getStageNumber() + "\"" +
						" active=\"" + flightConfig.isStageActive(stage.getStageNumber()) + "\"/>");
			}

			elements.add("</motorconfiguration>");
		}

		// Reference diameter
		elements.add("<referencetype>" + rocket.getReferenceType().name().toLowerCase(Locale.ENGLISH)
				+ "</referencetype>");
		if (rocket.getReferenceType() == ReferenceType.CUSTOM) {
			elements.add("<customreference>" + rocket.getCustomReferenceLength()
					+ "</customreference>");
		}

	}

}
