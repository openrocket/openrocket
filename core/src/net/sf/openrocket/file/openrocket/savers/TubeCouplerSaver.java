package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class TubeCouplerSaver extends ThicknessRingComponentSaver {

	private static final TubeCouplerSaver instance = new TubeCouplerSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<tubecoupler>");
		instance.addParams(c, list);
		list.add("</tubecoupler>");

		return list;
	}

}
