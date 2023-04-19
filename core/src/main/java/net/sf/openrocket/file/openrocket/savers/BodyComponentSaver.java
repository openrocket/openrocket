package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

public class BodyComponentSaver extends ExternalComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		// Body components have a natural length, store it now
		elements.add("<length>"+((net.sf.openrocket.rocketcomponent.BodyComponent)c).getLength()+"</length>");
	}
	
}
