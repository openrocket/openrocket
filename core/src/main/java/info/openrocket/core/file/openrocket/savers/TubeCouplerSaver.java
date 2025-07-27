package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class TubeCouplerSaver extends ThicknessRingComponentSaver {

	private static final TubeCouplerSaver instance = new TubeCouplerSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<>();

		list.add("<tubecoupler>");
		instance.addParams(c, list);
		list.add("</tubecoupler>");

		return list;
	}

}
