package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.ShockCord;


public class ShockCordSaver extends MassObjectSaver {

	private static final ShockCordSaver instance = new ShockCordSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<shockcord>");
		instance.addParams(c, list);
		list.add("</shockcord>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		ShockCord mass = (ShockCord) c;

		elements.add("<cordlength>" + mass.getCordLength() + "</cordlength>");
		elements.add(materialParam(mass.getMaterial()));
	}

}
