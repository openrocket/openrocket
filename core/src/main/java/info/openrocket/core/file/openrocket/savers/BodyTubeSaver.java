package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class BodyTubeSaver extends SymmetricComponentSaver {

	private static final BodyTubeSaver instance = new BodyTubeSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<bodytube>");
		instance.addParams(c, list);
		list.add("</bodytube>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		info.openrocket.core.rocketcomponent.BodyTube tube = (info.openrocket.core.rocketcomponent.BodyTube) c;

		if (tube.isOuterRadiusAutomatic()) {
			elements.add("<radius>auto " + tube.getOuterRadius() + "</radius>");
		} else
			elements.add("<radius>" + tube.getOuterRadius() + "</radius>");

		if (tube.isMotorMount()) {
			elements.addAll(motorMountParams(tube));
		}
	}

}
