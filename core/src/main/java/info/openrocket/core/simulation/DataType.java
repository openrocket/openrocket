package info.openrocket.core.simulation;

import info.openrocket.core.util.UnitValue;

/**
 * A type of data that can be stored in a {@link DataBranch}.
 */
public interface DataType extends UnitValue {
	String getName();
}
