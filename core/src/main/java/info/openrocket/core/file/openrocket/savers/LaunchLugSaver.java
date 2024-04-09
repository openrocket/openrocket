package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.LaunchLug;

public class LaunchLugSaver extends ExternalComponentSaver {

	private static final LaunchLugSaver instance = new LaunchLugSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<launchlug>");
		instance.addParams(c, list);
		list.add("</launchlug>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		LaunchLug lug = (LaunchLug) c;

		elements.add("<radius>" + lug.getOuterRadius() + "</radius>");
		elements.add("<length>" + lug.getLength() + "</length>");
		elements.add("<thickness>" + lug.getThickness() + "</thickness>");
	}

}
