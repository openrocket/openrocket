package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.RailButton;


public class RailButtonSaver extends ExternalComponentSaver {

	private static final RailButtonSaver instance = new RailButtonSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<railbutton>");
		instance.addParams(c, list);
		list.add("</railbutton>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		RailButton rb = (RailButton) c;

		emitDouble( elements, "outerdiameter", rb.getOuterDiameter());
		emitDouble( elements, "height", rb.getTotalHeight());
		
	}

	
}
