package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;
import java.util.Locale;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Reflection;

////MaterialSetter  -  sets a Material value
class MaterialSetter implements Setter {
	private final Reflection.Method setMethod;
	private final Material.Type type;
	
	public MaterialSetter(Reflection.Method set, Material.Type type) {
		this.setMethod = set;
		this.type = type;
	}
	
	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		Material mat;
		
		// Check name != ""
		name = name.trim();
		if (name.equals("")) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}
		
		// Parse density
		double density;
		String str;
		str = attributes.remove("density");
		if (str == null) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}
		try {
			density = Double.parseDouble(str);
		} catch (NumberFormatException e) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}
		
		// Parse thickness
		//		double thickness = 0;
		//		str = attributes.remove("thickness");
		//		try {
		//			if (str != null)
		//				thickness = Double.parseDouble(str);
		//		} catch (NumberFormatException e){
		//			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
		//			return;
		//		}
		
		// Check type if specified
		str = attributes.remove("type");
		if (str != null && !type.name().toLowerCase(Locale.ENGLISH).equals(str)) {
			warnings.add(Warning.fromString("Illegal material type specified, ignoring."));
			return;
		}
		
		mat = Databases.findMaterial(type, name, density);
		
		setMethod.invoke(c, mat);
	}
}