package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class EngineBlockSaver extends ThicknessRingComponentSaver {

	private static final EngineBlockSaver instance = new EngineBlockSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<engineblock>");
		instance.addParams(c, list);
		list.add("</engineblock>");

		return list;
	}

}
