package info.openrocket.core.file.openrocket.savers;

import java.util.List;

public class SymmetricComponentSaver extends BodyComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		info.openrocket.core.rocketcomponent.SymmetricComponent comp = (info.openrocket.core.rocketcomponent.SymmetricComponent) c;
		if (comp.isFilled())
			elements.add("<thickness>filled</thickness>");
		else
			elements.add("<thickness>" + comp.getThickness() + "</thickness>");
	}

}
