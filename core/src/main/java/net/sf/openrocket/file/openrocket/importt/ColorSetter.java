package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Reflection;

////  ColorSetter  -  sets a Color value
class ColorSetter implements Setter {
	private final Reflection.Method setMethod;
	
	public ColorSetter(Reflection.Method set) {
		setMethod = set;
	}
	
	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		String red = attributes.get("red");
		String green = attributes.get("green");
		String blue = attributes.get("blue");
		
		if (red == null || green == null || blue == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		int r, g, b;
		try {
			r = Integer.parseInt(red);
			g = Integer.parseInt(green);
			b = Integer.parseInt(blue);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		if (r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}
		
		Color color = new Color(r, g, b);
		setMethod.invoke(c, color);
		
		if (!s.trim().equals("")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}