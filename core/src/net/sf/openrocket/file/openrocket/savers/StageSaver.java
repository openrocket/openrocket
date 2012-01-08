package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;

public class StageSaver extends ComponentAssemblySaver {

	private static final StageSaver instance = new StageSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<stage>");
		instance.addParams(c,list);
		list.add("</stage>");
		
		return list;
	}
	
	
}
