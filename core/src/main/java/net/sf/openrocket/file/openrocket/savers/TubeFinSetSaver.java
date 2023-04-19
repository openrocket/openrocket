package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.TubeFinSet;


public class TubeFinSetSaver extends ExternalComponentSaver {
	
	private static final TubeFinSetSaver instance = new TubeFinSetSaver();
	
	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();
		
		list.add("<tubefinset>");
		instance.addParams(c, list);
		list.add("</tubefinset>");
		
		return list;
	}
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		TubeFinSet fins = (TubeFinSet) c;
		
		elements.add("<fincount>" + fins.getFinCount() + "</fincount>");
		elements.add("<rotation>" + (fins.getBaseRotation() * 180.0 / Math.PI) + "</rotation>");
		if (fins.isOuterRadiusAutomatic())
			elements.add("<radius>auto</radius>");
		else
			elements.add("<radius>" + fins.getOuterRadius() + "</radius>");
		elements.add("<length>" + fins.getLength() + "</length>");
		elements.add("<thickness>" + fins.getThickness() + "</thickness>");
	}
	
}
