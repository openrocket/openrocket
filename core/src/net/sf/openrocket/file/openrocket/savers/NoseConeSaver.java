package net.sf.openrocket.file.openrocket.savers;

import net.sf.openrocket.rocketcomponent.NoseCone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
		NoseCone noseCone = (NoseCone) c;
		super.addParams(c, elements);

		if (noseCone.isBaseRadiusAutomatic())
			elements.add("<aftradius>auto " + noseCone.getBaseRadiusNoAutomatic() + "</aftradius>");
		else
			elements.add("<aftradius>" + noseCone.getBaseRadius() + "</aftradius>");

		elements.add("<aftshoulderradius>" + noseCone.getShoulderRadius()
				+ "</aftshoulderradius>");
		elements.add("<aftshoulderlength>" + noseCone.getShoulderLength()
				+ "</aftshoulderlength>");
		elements.add("<aftshoulderthickness>" + noseCone.getShoulderThickness()
				+ "</aftshoulderthickness>");
		elements.add("<aftshouldercapped>" + noseCone.isShoulderCapped()
				+ "</aftshouldercapped>");

		elements.add("<isflipped>" + noseCone.isFlipped() + "</isflipped>");
	}
}
