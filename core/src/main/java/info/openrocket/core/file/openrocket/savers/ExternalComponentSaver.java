package info.openrocket.core.file.openrocket.savers;

import java.util.List;
import java.util.Locale;

import info.openrocket.core.rocketcomponent.ExternalComponent;

public class ExternalComponentSaver extends RocketComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		ExternalComponent ext = (ExternalComponent) c;

		// Finish enum names are currently the same except for case
		elements.add("<finish>" + ext.getFinish().name().toLowerCase(Locale.ENGLISH) + "</finish>");

		// Material
		elements.add(materialParam(ext.getMaterial()));
	}

}
