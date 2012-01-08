package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.StructuralComponent;


public class StructuralComponentSaver extends InternalComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		StructuralComponent comp = (StructuralComponent)c;
		elements.add(materialParam(comp.getMaterial()));
	}
		
}
