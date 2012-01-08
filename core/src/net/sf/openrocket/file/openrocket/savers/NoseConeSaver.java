package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class NoseConeSaver extends TransitionSaver {

	private static final NoseConeSaver instance = new NoseConeSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<nosecone>");
		instance.addParams(c,list);
		list.add("</nosecone>");
		
		return list;
	}
	

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		// Transition handles nose cone saving as well
	}
}
