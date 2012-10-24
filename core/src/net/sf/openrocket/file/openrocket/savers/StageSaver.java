package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;

public class StageSaver extends ComponentAssemblySaver {
	
	private static final StageSaver instance = new StageSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<stage>");
		instance.addParams(c, list);
		list.add("</stage>");
		
		return list;
	}
	
	@Override
	protected void addParams(RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		Stage stage = (Stage) c;
		
		if (stage.getStageNumber() > 0) {
			elements.add("<separationevent>"
					+ stage.getDefaultSeparationEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "")
					+ "</separationevent>");
			elements.add("<separationdelay>" + stage.getDefaultSeparationDelay() + "</separationdelay>");
		}
	}
}
