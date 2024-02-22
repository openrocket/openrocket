package info.openrocket.core.preset.loader;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedPropertyMap;

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
