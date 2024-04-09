package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.InnerTube;

public class InnerTubeSaver extends ThicknessRingComponentSaver {

	private static final InnerTubeSaver instance = new InnerTubeSaver();

	public static List<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<innertube>");
		instance.addParams(c, list);
		list.add("</innertube>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		InnerTube tube = (InnerTube) c;

		elements.add("<clusterconfiguration>" + tube.getClusterConfiguration().getXMLName()
				+ "</clusterconfiguration>");
		elements.add("<clusterscale>" + tube.getClusterScale() + "</clusterscale>");
		elements.add("<clusterrotation>" + (tube.getClusterRotation() * 180.0 / Math.PI)
				+ "</clusterrotation>");

		if (tube.isMotorMount()) {
			elements.addAll(motorMountParams(tube));
		}

	}

}
