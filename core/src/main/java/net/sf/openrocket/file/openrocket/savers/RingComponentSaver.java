package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.RingComponent;


public class RingComponentSaver extends StructuralComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		RingComponent ring = (RingComponent) c;

		elements.add("<length>" + ring.getLength() + "</length>");
		elements.add("<radialposition>" + ring.getRadialPosition() + "</radialposition>");
		elements.add("<radialdirection>" + (ring.getRadialDirection() * 180.0 / Math.PI)
				+ "</radialdirection>");
	}

}
