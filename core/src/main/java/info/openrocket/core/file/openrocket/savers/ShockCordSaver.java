package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.ShockCord;

public class ShockCordSaver extends MassObjectSaver {

	private static final ShockCordSaver instance = new ShockCordSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<shockcord>");
		instance.addParams(c, list);
		list.add("</shockcord>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		ShockCord mass = (ShockCord) c;

		elements.add("<cordlength>" + mass.getCordLength() + "</cordlength>");
		elements.add(materialParam(mass.getMaterial()));
	}

}
