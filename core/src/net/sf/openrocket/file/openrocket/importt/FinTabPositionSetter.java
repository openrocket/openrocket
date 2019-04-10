package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.util.Reflection;

class FinTabPositionSetter extends DoubleSetter {
	
	public FinTabPositionSetter() {
		super(Reflection.findMethod(FinSet.class, "setTabOffset", double.class));
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (!(c instanceof FinSet)) {
			throw new IllegalStateException("FinTabPositionSetter called for component " + c);
		}
		
		String relative = attributes.get("relativeto");
		
		if (relative == null) {
			warnings.add("Required attribute 'relativeto' not found for fin tab position.");
		} else {
			// translate from old enum names to current enum names
			if( relative.contains("front")){
				relative = "top";
			}else if( relative.contains("center")){
				relative = "middle";
			}else if( relative.contains("end")){
				relative = "bottom";
			}
			
			AxialMethod position = (AxialMethod) DocumentConfig.findEnum(relative, AxialMethod.class);
			
			if( null == position ){
				warnings.add("Illegal attribute value '" + relative + "' encountered.");
			}else{
				((FinSet) c).setTabOffsetMethod(position);
				super.set(c, s, attributes, warnings);
			}
		
		}
		
	}
	
	
}
