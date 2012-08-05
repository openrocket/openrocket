package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.Rocket;

public class RocketSaver extends RocketComponentSaver {
	
	private static final RocketSaver instance = new RocketSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<rocket>");
		instance.addParams(c, list);
		list.add("</rocket>");
		
		return list;
	}
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		Rocket rocket = (Rocket) c;
		
		if (rocket.getDesigner().length() > 0) {
			elements.add("<designer>"
					+ net.sf.openrocket.file.RocketSaver.escapeXML(rocket.getDesigner())
					+ "</designer>");
		}
		if (rocket.getRevision().length() > 0) {
			elements.add("<revision>"
					+ net.sf.openrocket.file.RocketSaver.escapeXML(rocket.getRevision())
					+ "</revision>");
		}
		
		
		// Motor configurations
		String defId = rocket.getDefaultConfiguration().getMotorConfigurationID();
		for (String id : rocket.getMotorConfigurationIDs()) {
			if (id == null)
				continue;
			
			String str = "<motorconfiguration configid=\"" + id + "\"";
			if (id.equals(defId))
				str += " default=\"true\"";
			
			if (rocket.getMotorConfigurationName(id) == "") {
				str += "/>";
			} else {
				str += "><name>" + net.sf.openrocket.file.RocketSaver.escapeXML(rocket.getMotorConfigurationName(id))
						+ "</name></motorconfiguration>";
			}
			elements.add(str);
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
