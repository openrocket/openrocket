package info.openrocket.core.util;

import info.openrocket.core.unit.UnitGroup;

public interface UnitValue {
	/**
	 * Returns the unit group that this value belongs to.
	 * @return the unit group
	 */
	UnitGroup getUnitGroup();
}
