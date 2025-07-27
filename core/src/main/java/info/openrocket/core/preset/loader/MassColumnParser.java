package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.StringUtils;

/**
 * Special DoubleUnitColumnParser for Mass column. Here we assume that if a mass
 * of 0 is
 * specified in the csv, then we should not put a mass explicitly in the preset
 * but instead
 * rely on the density to compute a mass value.
 *
 */
public class MassColumnParser extends DoubleUnitColumnParser {

	public MassColumnParser(String columnHeader, String unitHeader) {
		super(columnHeader, unitHeader, ComponentPreset.MASS);
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		if (StringUtils.isEmpty(columnData) || "?".equals(columnData.trim())) {
			return;
		}
		double d = Double.valueOf(columnData);
		if (d == 0.0) {
			return;
		}
		super.doParse(columnData, data, props);
	}

}
