package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class EllipticalFinSetSaver extends FinSetSaver {

	private static final EllipticalFinSetSaver instance = new EllipticalFinSetSaver();

	public static ArrayList<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();

		list.add("<ellipticalfinset>");
		instance.addParams(c, list);
		list.add("</ellipticalfinset>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		info.openrocket.core.rocketcomponent.EllipticalFinSet fins = (info.openrocket.core.rocketcomponent.EllipticalFinSet) c;
		elements.add("<rootchord>" + fins.getLength() + "</rootchord>");
		elements.add("<height>" + fins.getHeight() + "</height>");
	}

}
