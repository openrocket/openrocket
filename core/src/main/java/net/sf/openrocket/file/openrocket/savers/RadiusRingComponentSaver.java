package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;


public class RadiusRingComponentSaver extends RingComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		RadiusRingComponent comp = (RadiusRingComponent)c;
		if (comp.isOuterRadiusAutomatic())
			elements.add("<outerradius>auto</outerradius>");
		else
			elements.add("<outerradius>" + comp.getOuterRadius() + "</outerradius>");
		if (!(comp instanceof Bulkhead)) {
			if (comp.isInnerRadiusAutomatic())
				elements.add("<innerradius>auto</innerradius>");
			else
				elements.add("<innerradius>" + comp.getInnerRadius() + "</innerradius>");
		}
	}
		
}
