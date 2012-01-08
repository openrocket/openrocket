package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.util.Coordinate;


public class FreeformFinSetSaver extends FinSetSaver {

	private static final FreeformFinSetSaver instance = new FreeformFinSetSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<freeformfinset>");
		instance.addParams(c,list);
		list.add("</freeformfinset>");
		
		return list;
	}
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		FreeformFinSet fins = (FreeformFinSet)c;
		elements.add("<finpoints>");
		for (Coordinate p: fins.getFinPoints()) {
			elements.add("  <point x=\"" + p.x + "\" y=\"" + p.y + "\"/>");
		}
		elements.add("</finpoints>");
	}
	
}
