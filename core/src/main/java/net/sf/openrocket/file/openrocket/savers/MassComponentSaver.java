package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassComponent.MassComponentType;


public class MassComponentSaver extends MassObjectSaver {
	
	private static final MassComponentSaver instance = new MassComponentSaver();
	
	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();
		
		list.add("<masscomponent>");
		instance.addParams(c, list);
		list.add("</masscomponent>");
		
		return list;
	}
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		MassComponent mass = (MassComponent) c;
		
		elements.add("<mass>" + mass.getMass() + "</mass>");
		
		MassComponentType type = mass.getMassComponentType();
		elements.add("<masscomponenttype>" + type.name().toLowerCase(Locale.ENGLISH) + "</masscomponenttype>");
		
	}
}
