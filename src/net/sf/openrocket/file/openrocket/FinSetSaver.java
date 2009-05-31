package net.sf.openrocket.file.openrocket;

import java.util.List;

public class FinSetSaver extends ExternalComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		net.sf.openrocket.rocketcomponent.FinSet fins = (net.sf.openrocket.rocketcomponent.FinSet) c;
		elements.add("<fincount>" + fins.getFinCount() + "</fincount>");
		elements.add("<rotation>" + (fins.getBaseRotation() * 180.0 / Math.PI) + "</rotation>");
		elements.add("<thickness>" + fins.getThickness() + "</thickness>");
		elements.add("<crosssection>" + fins.getCrossSection().name().toLowerCase()
				+ "</crosssection>");
		elements.add("<cant>" + (fins.getCantAngle() * 180.0 / Math.PI) + "</cant>");
	}

}
