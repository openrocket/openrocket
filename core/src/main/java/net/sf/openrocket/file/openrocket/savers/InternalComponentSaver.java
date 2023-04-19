package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

public class InternalComponentSaver extends RocketComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		// Nothing to save
	}
		
}
