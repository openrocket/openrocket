package info.openrocket.core.file.openrocket.savers;

import java.util.List;

import info.openrocket.core.rocketcomponent.ThicknessRingComponent;

public class ThicknessRingComponentSaver extends RingComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		ThicknessRingComponent comp = (ThicknessRingComponent) c;
		if (comp.isOuterRadiusAutomatic())
			elements.add("<outerradius>auto</outerradius>");
		else
			elements.add("<outerradius>" + comp.getOuterRadius() + "</outerradius>");
		elements.add("<thickness>" + comp.getThickness() + "</thickness>");
	}

}
