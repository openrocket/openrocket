package net.sf.openrocket.preset.loader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;


public abstract class BaseUnitColumnParser extends BaseColumnParser {

	protected String unitHeader;
	protected int unitIndex;
	protected boolean unitConfigured;
	
	protected static Map<String,Unit> rocksimUnits;
	
	static {
		rocksimUnits = new HashMap<String,Unit>();
		rocksimUnits.put("0", UnitGroup.UNITS_LENGTH.getUnit("in"));
		rocksimUnits.put("1", UnitGroup.UNITS_LENGTH.getUnit("mm"));
	}

	protected String mungeUnitNameString( String name ) {
		String newString = name.toLowerCase(Locale.US);
		return newString.replace(".", "");
	}
	
	public BaseUnitColumnParser(String columnHeader, String unitHeader) {
		super(columnHeader);
		this.unitHeader = unitHeader.toLowerCase(Locale.US);
	}

	@Override
	public void configure(String[] headers) {
		// super configure will set columnIndex;
		super.configure(headers);
		
		// This indicates the actual dimension column was found.
		if ( isConfigured ) {
			// Look for the unit column proceeding it
			for( int i=columnIndex-1; i>=0; i-- ) {
				if ( unitHeader.equals(headers[i].toLowerCase(Locale.US))) {
					unitConfigured = true;
					unitIndex = i;
					return;
				}
			}
		}
	}

}
