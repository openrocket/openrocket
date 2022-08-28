package net.sf.openrocket.file.openrocket.savers;

import java.util.List;

import net.sf.openrocket.rocketcomponent.MassObject;


public class MassObjectSaver extends InternalComponentSaver {

	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		MassObject mass = (MassObject) c;

		elements.add("<packedlength>" + mass.getLengthNoAuto() + "</packedlength>");
		if (mass.isRadiusAutomatic()) {
			elements.add("<packedradius>auto " + mass.getRadiusNoAuto() + "</packedradius>");
		} else {
			elements.add("<packedradius>" + mass.getRadiusNoAuto() + "</packedradius>");
		}
		elements.add("<radialposition>" + mass.getRadialPosition() + "</radialposition>");
		elements.add("<radialdirection>" + (mass.getRadialDirection() * 180.0 / Math.PI)
				+ "</radialdirection>");
	}

}
