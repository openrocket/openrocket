package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

public class SymmetricComponentSaver extends BodyComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		net.sf.openrocket.rocketcomponent.SymmetricComponent comp = (net.sf.openrocket.rocketcomponent.SymmetricComponent)c;
		if (comp.isFilled())
			elements.add("<thickness>filled</thickness>");
		else
			elements.add("<thickness>"+comp.getThickness()+"</thickness>");
	}

}
