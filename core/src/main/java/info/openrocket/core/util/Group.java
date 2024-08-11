package info.openrocket.core.util;

/**
 * Interface for objects that a <Groupable> object can be grouped into.
 */
public interface Group {
	String getName();
	int getPriority();		// Lower number = higher priority (can be used for sorting)
}
