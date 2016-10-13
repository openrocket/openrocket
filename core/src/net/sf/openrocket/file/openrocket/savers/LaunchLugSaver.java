package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.LaunchLug;


public class LaunchLugSaver extends ExternalComponentSaver {

	private static final LaunchLugSaver instance = new LaunchLugSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<launchlug>");
		instance.addParams(c, list);
		list.add("</launchlug>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		LaunchLug lug = (LaunchLug) c;

		elements.add("<radius>" + lug.getOuterRadius() + "</radius>");
		elements.add("<length>" + lug.getLength() + "</length>");
		elements.add("<thickness>" + lug.getThickness() + "</thickness>");
		elements.add("<radialdirection>" + (lug.getAngularOffset()*180.0/Math.PI)+ "</radialdirection>");
	}


}
