package net.sf.openrocket.file.openrocket.savers;

import java.util.List;
import java.util.Locale;

import net.sf.openrocket.util.MathUtil;

public class FinSetSaver extends ExternalComponentSaver {
	
	@Override
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		
		net.sf.openrocket.rocketcomponent.FinSet fins = (net.sf.openrocket.rocketcomponent.FinSet) c;
		elements.add("<fincount>" + fins.getFinCount() + "</fincount>");
		elements.add("<rotation>" + (fins.getBaseRotation() * 180.0 / Math.PI) + "</rotation>");
		elements.add("<thickness>" + fins.getThickness() + "</thickness>");
		elements.add("<crosssection>" + fins.getCrossSection().name().toLowerCase(Locale.ENGLISH)
				+ "</crosssection>");
		elements.add("<cant>" + (fins.getCantAngle() * 180.0 / Math.PI) + "</cant>");
		
		// Save fin tabs only if they exist (compatibility with file version < 1.1)
		if (!MathUtil.equals(fins.getTabHeight(), 0) &&
				!MathUtil.equals(fins.getTabLength(), 0)) {
			
			elements.add("<tabheight>" + fins.getTabHeight() + "</tabheight>");
			elements.add("<tablength>" + fins.getTabLength() + "</tablength>");
			elements.add("<tabposition relativeto=\"" +
					fins.getTabRelativePosition().name().toLowerCase(Locale.ENGLISH) + "\">" +
					fins.getTabShift() + "</tabposition>");
			
		}
		
		elements.add("<filletradius>" + fins.getFilletRadius() + "</filletradius>");
		elements.add(materialParam("filletmaterial", fins.getFilletMaterial()));
	}
	
}
