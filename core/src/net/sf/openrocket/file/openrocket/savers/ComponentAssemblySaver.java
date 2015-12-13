package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;

import net.sf.openrocket.rocketcomponent.PodSet;

public class ComponentAssemblySaver extends RocketComponentSaver {
	
	
	private static final ComponentAssemblySaver instance = new ComponentAssemblySaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		if (!c.isAfter()) {
			if (c instanceof PodSet) {
				list.add("<podset>");
				instance.addParams(c, list);
				list.add("</podset>");
			}
//			else if (c instanceof ParallelStage) {
//				list.add("<parallelstage>");
//				instance.addParams(c, list);
//				list.add("</parallelstage>");
//			}
		}
		
		return list;
	}
	
}