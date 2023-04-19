package net.sf.openrocket.preset.loader;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedPropertyMap;

public class ManufacturerColumnParser extends BaseColumnParser {

	public ManufacturerColumnParser() {
		super("Mfg.");
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		Manufacturer m = Manufacturer.getManufacturer(columnData);
		props.put(ComponentPreset.MANUFACTURER, m);

	}

}
