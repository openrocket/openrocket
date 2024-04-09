package info.openrocket.core.file.openrocket.savers;

import java.util.List;

public class InternalComponentSaver extends RocketComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		// Nothing to save
	}

}
