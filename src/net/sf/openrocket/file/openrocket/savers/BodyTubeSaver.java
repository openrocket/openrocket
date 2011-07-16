package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class BodyTubeSaver extends SymmetricComponentSaver {

	private static final BodyTubeSaver instance = new BodyTubeSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<bodytube>");
		instance.addParams(c, list);
		list.add("</bodytube>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		net.sf.openrocket.rocketcomponent.BodyTube tube = (net.sf.openrocket.rocketcomponent.BodyTube) c;

		if (tube.isOuterRadiusAutomatic())
			elements.add("<radius>auto</radius>");
		else
			elements.add("<radius>" + tube.getOuterRadius() + "</radius>");

		if (tube.isMotorMount()) {
			elements.addAll(motorMountParams(tube));
		}
	}


}
