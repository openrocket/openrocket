package info.openrocket.core.file.openrocket.savers;

import java.util.List;

import info.openrocket.core.rocketcomponent.MassObject;

public class MassObjectSaver extends InternalComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		MassObject mass = (MassObject) c;

		elements.add("<packedlength>" + mass.getLength() + "</packedlength>");
		if (mass.isRadiusAutomatic()) {
			elements.add("<packedradius>auto " + mass.getRadius() + "</packedradius>");
		} else {
			elements.add("<packedradius>" + mass.getRadius() + "</packedradius>");
		}
		elements.add("<radialposition>" + mass.getRadialPosition() + "</radialposition>");
		elements.add("<radialdirection>" + (mass.getRadialDirection() * 180.0 / Math.PI)
				+ "</radialdirection>");
	}

}
