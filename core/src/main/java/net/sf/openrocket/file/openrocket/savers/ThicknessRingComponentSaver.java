package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;


public class ThicknessRingComponentSaver extends RingComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		ThicknessRingComponent comp = (ThicknessRingComponent)c;
		if (comp.isOuterRadiusAutomatic())
			elements.add("<outerradius>auto</outerradius>");
		else
			elements.add("<outerradius>" + comp.getOuterRadius() + "</outerradius>");
		elements.add("<thickness>" + comp.getThickness() + "</thickness>");
	}
		
}
