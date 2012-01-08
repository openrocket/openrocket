package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

public class EllipticalFinSetSaver extends FinSetSaver {

	private static final EllipticalFinSetSaver instance = new EllipticalFinSetSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<ellipticalfinset>");
		instance.addParams(c,list);
		list.add("</ellipticalfinset>");
		
		return list;
	}
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		net.sf.openrocket.rocketcomponent.EllipticalFinSet fins = (net.sf.openrocket.rocketcomponent.EllipticalFinSet)c;
		elements.add("<rootchord>"+fins.getLength()+"</rootchord>");
		elements.add("<height>"+fins.getHeight()+"</height>");
	}
	
}
