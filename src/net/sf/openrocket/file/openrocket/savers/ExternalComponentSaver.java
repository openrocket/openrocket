package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.ExternalComponent;


public class ExternalComponentSaver extends RocketComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		ExternalComponent ext = (ExternalComponent)c;
		
		// Finish enum names are currently the same except for case
		elements.add("<finish>" + ext.getFinish().name().toLowerCase() + "</finish>");
		
		// Material
		elements.add(materialParam(ext.getMaterial()));
	}
		
}
