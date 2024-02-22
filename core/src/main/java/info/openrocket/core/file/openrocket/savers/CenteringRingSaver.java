package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class CenteringRingSaver extends RadiusRingComponentSaver {

	private static final CenteringRingSaver instance = new CenteringRingSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<centeringring>");
		instance.addParams(c, list);
		list.add("</centeringring>");

		return list;
	}

}
