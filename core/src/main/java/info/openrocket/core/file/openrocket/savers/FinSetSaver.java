package info.openrocket.core.file.openrocket.savers;

import java.util.List;
import java.util.Locale;

import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.MathUtil;

public class FinSetSaver extends ExternalComponentSaver {

	@Override
	protected void addParams(info.openrocket.core.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);

		info.openrocket.core.rocketcomponent.FinSet fins = (info.openrocket.core.rocketcomponent.FinSet) c;

		// // this information is already saved as 'RingInstanceable' in RocktComponent
		// elements.add("<fincount>" + fins.getFinCount() + "</fincount>");
		// elements.add("<rotation>" + (fins.getBaseRotation() * 180.0 / Math.PI) +
		// "</rotation>");

		elements.add("<thickness>" + fins.getThickness() + "</thickness>");
		elements.add("<crosssection>" + fins.getCrossSection().name().toLowerCase(Locale.ENGLISH)
				+ "</crosssection>");
		elements.add("<cant>" + Math.toDegrees(fins.getCantAngle()) + "</cant>");

		// Save fin tabs only if they exist (compatibility with file version < 1.1)
		if (!MathUtil.equals(fins.getTabHeight(), 0) &&
				!MathUtil.equals(fins.getTabLength(), 0)) {

			elements.add("<tabheight>" + fins.getTabHeight() + "</tabheight>");
			elements.add("<tablength>" + fins.getTabLength() + "</tablength>");
			// TODO: delete this when no backward compatibility with OR 15.03 is needed
			// anymore
			String offset = "center";
			double offsetVal = fins.getTabOffset();
			switch (fins.getTabOffsetMethod()) {
				case TOP:
					offset = "front";
					break;
				case BOTTOM:
					offset = "end";
					break;
				case MIDDLE:
					offset = "center";
					break;
			}
			elements.add("<tabposition relativeto=\"" + offset + "\">" +
					offsetVal + "</tabposition>");
			elements.add("<tabposition relativeto=\"" +
					fins.getTabOffsetMethod().name().toLowerCase(Locale.ENGLISH) + "\">" +
					fins.getTabOffset() + "</tabposition>");

		}

		elements.add("<filletradius>" + fins.getFilletRadius() + "</filletradius>");
		elements.add(materialParam("filletmaterial", fins.getFilletMaterial()));
	}

}
