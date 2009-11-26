package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.Parachute;


public class ParachuteSaver extends RecoveryDeviceSaver {

	private static final ParachuteSaver instance = new ParachuteSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<parachute>");
		instance.addParams(c, list);
		list.add("</parachute>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		Parachute para = (Parachute) c;

		elements.add("<diameter>" + para.getDiameter() + "</diameter>");
		elements.add("<linecount>" + para.getLineCount() + "</linecount>");
		elements.add("<linelength>" + para.getLineLength() + "</linelength>");
		elements.add(materialParam("linematerial", para.getLineMaterial()));
	}


}
