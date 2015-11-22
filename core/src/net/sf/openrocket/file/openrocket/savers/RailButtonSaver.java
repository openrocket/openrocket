package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.RailButton;


public class RailButtonSaver extends ExternalComponentSaver {

	private static final RailButtonSaver instance = new RailButtonSaver();

	public static List<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		List<String> list = new ArrayList<String>();

		list.add("<railbutton>");
		instance.addParams(c, list);
		list.add("</railbutton>");

		return list;
	}

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		RailButton rb = (RailButton) c;

		addElement( elements, "outerradius", rb.getOuterRadius());
		addElement( elements, "height", rb.getTotalHeight());
		addElement( elements, "radialDirection", rb.getRadialDirection()); 
	}

    protected static void addElement( final List<String> elements, final String enclosingTag, final double value){
    	addElement( elements, enclosingTag, Double.toString( value ));
    }

    protected static void addElement( final List<String> elements, final String enclosingTag, final String value){
    	elements.add("<"+enclosingTag+">" + value + "</"+enclosingTag+">");
    }

	
}
