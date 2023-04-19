package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.Streamer;


public class StreamerSaver extends RecoveryDeviceSaver {

	private static final StreamerSaver instance = new StreamerSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<streamer>");
		instance.addParams(c, list);
		list.add("</streamer>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		Streamer st = (Streamer) c;

		elements.add("<striplength>" + st.getStripLength() + "</striplength>");
		elements.add("<stripwidth>" + st.getStripWidth() + "</stripwidth>");
	}


}
