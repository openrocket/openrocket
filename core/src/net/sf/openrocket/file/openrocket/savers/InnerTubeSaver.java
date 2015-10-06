package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.InnerTube;


public class InnerTubeSaver extends ThicknessRingComponentSaver {

	private static final InnerTubeSaver instance = new InnerTubeSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<innertube>");
		instance.addParams(c, list);
		list.add("</innertube>");

		return list;
	}


	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
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
