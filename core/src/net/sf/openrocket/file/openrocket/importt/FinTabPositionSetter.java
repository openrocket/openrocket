package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.util.Reflection;

class FinTabPositionSetter extends DoubleSetter {
	
	public FinTabPositionSetter() {
		super(Reflection.findMethod(FinSet.class, "setTabShift", double.class));
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (!(c instanceof FinSet)) {
			throw new IllegalStateException("FinTabPositionSetter called for component " + c);
		}
		
		String relative = attributes.get("relativeto");
		FinSet.TabRelativePosition position =
				(TabRelativePosition) DocumentConfig.findEnum(relative,
						FinSet.TabRelativePosition.class);
		
		if (position != null) {
			
			((FinSet) c).setTabRelativePosition(position);
			
		} else {
			if (relative == null) {
				warnings.add("Required attribute 'relativeto' not found for fin tab position.");
			} else {
				warnings.add("Illegal attribute value '" + relative + "' encountered.");
			}
		}
		
		super.set(c, s, attributes, warnings);
	}
	
	
}