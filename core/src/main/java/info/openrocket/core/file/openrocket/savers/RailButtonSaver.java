package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.RailButton;

public class RailButtonSaver extends ExternalComponentSaver {

	private static final RailButtonSaver instance = new RailButtonSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<railbutton>");
		instance.addParams(c, list);
		list.add("</railbutton>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		RailButton rb = (RailButton) c;

		emitDouble(elements, "outerdiameter", rb.getOuterDiameter());
		emitDouble(elements, "innerdiameter", rb.getInnerDiameter());
		emitDouble(elements, "height", rb.getTotalHeight());
		emitDouble(elements, "baseheight", rb.getBaseHeight());
		emitDouble(elements, "flangeheight", rb.getFlangeHeight());
		emitDouble(elements, "screwheight", rb.getScrewHeight());
	}

}
