package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class TrapezoidFinSetSaver extends FinSetSaver {

	private static final TrapezoidFinSetSaver instance = new TrapezoidFinSetSaver();

	public static ArrayList<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();

		list.add("<trapezoidfinset>");
		instance.addParams(c, list);
		list.add("</trapezoidfinset>");

		return list;
	}

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		info.openrocket.core.rocketcomponent.TrapezoidFinSet fins = (info.openrocket.core.rocketcomponent.TrapezoidFinSet) c;
		elements.add("<rootchord>" + fins.getRootChord() + "</rootchord>");
		elements.add("<tipchord>" + fins.getTipChord() + "</tipchord>");
		elements.add("<sweeplength>" + fins.getSweep() + "</sweeplength>");
		elements.add("<height>" + fins.getHeight() + "</height>");
	}

}
