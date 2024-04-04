package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;
import java.util.List;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Reflection;

////ComponentPresetSetter  -  sets a ComponentPreset value
class ComponentPresetSetter implements Setter {
	private final Reflection.Method setMethod;
	private Object[] extraParameters = null;

	public ComponentPresetSetter(Reflection.Method set) {
		this.setMethod = set;
	}

	public ComponentPresetSetter(Reflection.Method set, Object... parameters) {
		this.setMethod = set;
		this.extraParameters = parameters;
	}

	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {
		String manufacturerName = attributes.get("manufacturer");
		if (manufacturerName == null) {
			warnings.add(Warning.fromString("Invalid ComponentPreset for component " + c.getName() + ", no manufacturer specified.  Ignored"));
			return;
		}
		
		String productNo = attributes.get("partno");
		if (productNo == null) {
			warnings.add(Warning.fromString("Invalid ComponentPreset for component " + c.getName() + ", no partno specified.  Ignored"));
			return;
		}
		
		String digest = attributes.get("digest");
		if (digest == null) {
			warnings.add(Warning.fromString("Invalid ComponentPreset for component " + c.getName() + ", no digest specified."));
		}
		
		String type = attributes.get("type");
		if (type == null) {
			warnings.add(Warning.fromString("Invalid ComponentPreset for component " + c.getName() + ", no type specified."));
		}
		
		List<ComponentPreset> presets = Application.getComponentPresetDao().find(manufacturerName, productNo);
		ComponentPreset matchingPreset = null;
		
		for (ComponentPreset preset : presets) {
			if (digest != null && preset.getDigest().equals(digest)) {
				// Found one with matching digest.  Take it.
				matchingPreset = preset;
				break;
			}

			if (type != null && preset.getType().name().equals(type) && matchingPreset != null) {
				// Found the first one with matching type.
				matchingPreset = preset;
			}
		}
		
		// Was any found?
		if (matchingPreset == null) {
			warnings.add(Warning.fromString("No matching ComponentPreset for component " + c.getName() + " found matching " + manufacturerName + " " + productNo));
			return;
		}
		
		if (digest != null && !matchingPreset.getDigest().equals(digest)) {
			warnings.add(Warning.fromString("ComponentPreset for component " + c.getName() + " has wrong digest"));
		}

		// The preset loader can override the component name, so first store it and then apply it again
		String componentName = c.getName();
		if (extraParameters != null) {
			setMethod.invoke(c, matchingPreset, extraParameters);
		} else {
			setMethod.invoke(c, matchingPreset);
		}
		c.setName(componentName);
	}
}
