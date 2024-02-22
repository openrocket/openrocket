package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;
import java.util.Locale;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Reflection;

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
		// double thickness = 0;
		// str = attributes.remove("thickness");
		// try {
		// if (str != null)
		// thickness = Double.parseDouble(str);
		// } catch (NumberFormatException e){
		// warnings.add(Warning.fromString("Illegal material specification,
		// ignoring."));
		// return;
		// }

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