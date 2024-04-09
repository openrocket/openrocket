package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.util.Coordinate;

public class FreeformFinSetSaver extends FinSetSaver {

	private static final FreeformFinSetSaver instance = new FreeformFinSetSaver();

	public static ArrayList<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();

		list.add("<freeformfinset>");
		instance.addParams(c, list);
		list.add("</freeformfinset>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		FreeformFinSet fins = (FreeformFinSet) c;
		elements.add("<finpoints>");
		for (Coordinate p : fins.getFinPoints()) {
			elements.add("  <point x=\"" + p.x + "\" y=\"" + p.y + "\"/>");
		}
		elements.add("</finpoints>");
	}

}
