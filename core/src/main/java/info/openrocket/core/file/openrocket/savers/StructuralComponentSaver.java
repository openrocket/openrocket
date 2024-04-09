package info.openrocket.core.file.openrocket.savers;

import java.util.List;

import info.openrocket.core.rocketcomponent.StructuralComponent;

public class StructuralComponentSaver extends InternalComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		StructuralComponent comp = (StructuralComponent) c;
		elements.add(materialParam(comp.getMaterial()));
	}

}
