package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class BulkheadSaver extends RadiusRingComponentSaver {

	private static final BulkheadSaver instance = new BulkheadSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<bulkhead>");
		instance.addParams(c, list);
		list.add("</bulkhead>");

		return list;
	}

}
