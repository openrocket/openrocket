package info.openrocket.core.file.openrocket.savers;

import java.util.List;

public class BodyComponentSaver extends ExternalComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		// Body components have a natural length, store it now
		elements.add("<length>" + ((info.openrocket.core.rocketcomponent.BodyComponent) c).getLength() + "</length>");
	}

}
