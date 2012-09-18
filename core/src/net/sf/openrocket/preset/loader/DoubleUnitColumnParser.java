package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.StringUtil;

public class DoubleUnitColumnParser extends BaseUnitColumnParser {

	private TypedKey<Double> propKey;

	public DoubleUnitColumnParser(String columnHeader, String unitHeader,
			TypedKey<Double> propKey) {
		super(columnHeader, unitHeader);
		this.propKey = propKey;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		try {
			if (StringUtil.isEmpty(columnData)) {
				return;
			}
			double value = Double.valueOf(columnData);

			if ( unitConfigured ) {
				String unitName = data[unitIndex];

				Unit unit = rocksimUnits.get(unitName);
				if ( unit == null ) {
					if ( unitName == null || "" .equals(unitName) || "?".equals(unitName)) {
						// Hmm no data...  Lets assume SI
						if ( propKey.getUnitGroup() == UnitGroup.UNITS_LENGTH ) {
							unit = UnitGroup.UNITS_LENGTH.getUnit("in");
						} else {
							unit= UnitGroup.UNITS_MASS.getUnit("oz");
						}
					} else {
						unitName = super.mungeUnitNameString(unitName);
						UnitGroup group = propKey.getUnitGroup();
						unit = group.getUnit(unitName);
					}
				}

				value = unit.fromUnit(value);
			}

			props.put(propKey, value);
		}
		catch ( NumberFormatException nex) {
		}
        catch ( IllegalArgumentException iae) {
        }
	}


}
