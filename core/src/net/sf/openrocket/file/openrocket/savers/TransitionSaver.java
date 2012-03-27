package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Transition;


public class TransitionSaver extends SymmetricComponentSaver {
	
	private static final TransitionSaver instance = new TransitionSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<transition>");
		instance.addParams(c, list);
		list.add("</transition>");
		
		return list;
	}
	
	
	/*
	 * Note:  This method must be capable of handling nose cones as well.
	 */
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		net.sf.openrocket.rocketcomponent.Transition trans = (net.sf.openrocket.rocketcomponent.Transition) c;
		boolean nosecone = (trans instanceof NoseCone);
		
		
		Transition.Shape shape = trans.getType();
		elements.add("<shape>" + shape.name().toLowerCase(Locale.ENGLISH) + "</shape>");
		if (shape.isClippable()) {
			elements.add("<shapeclipped>" + trans.isClipped() + "</shapeclipped>");
		}
		if (shape.usesParameter()) {
			elements.add("<shapeparameter>" + trans.getShapeParameter() + "</shapeparameter>");
		}
		
		
		if (!nosecone) {
			if (trans.isForeRadiusAutomatic())
				elements.add("<foreradius>auto</foreradius>");
			else
				elements.add("<foreradius>" + trans.getForeRadius() + "</foreradius>");
		}
		
		if (trans.isAftRadiusAutomatic())
			elements.add("<aftradius>auto</aftradius>");
		else
			elements.add("<aftradius>" + trans.getAftRadius() + "</aftradius>");
		
		
		if (!nosecone) {
			elements.add("<foreshoulderradius>" + trans.getForeShoulderRadius()
					+ "</foreshoulderradius>");
			elements.add("<foreshoulderlength>" + trans.getForeShoulderLength()
					+ "</foreshoulderlength>");
			elements.add("<foreshoulderthickness>" + trans.getForeShoulderThickness()
					+ "</foreshoulderthickness>");
			elements.add("<foreshouldercapped>" + trans.isForeShoulderCapped()
					+ "</foreshouldercapped>");
		}
		
		elements.add("<aftshoulderradius>" + trans.getAftShoulderRadius()
				+ "</aftshoulderradius>");
		elements.add("<aftshoulderlength>" + trans.getAftShoulderLength()
				+ "</aftshoulderlength>");
		elements.add("<aftshoulderthickness>" + trans.getAftShoulderThickness()
				+ "</aftshoulderthickness>");
		elements.add("<aftshouldercapped>" + trans.isAftShoulderCapped()
				+ "</aftshouldercapped>");
	}
	
}
