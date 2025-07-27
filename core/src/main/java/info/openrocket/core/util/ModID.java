package info.openrocket.core.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ModID implements Comparable {

	private static final AtomicInteger nextId = new AtomicInteger(1);
	private final int id;
	/**
	 * Return a positive integer ID unique during this program execution.
	 * <p>
	 * The following is guaranteed of the returned ID values:
	 * <ul>
	 * <li>The value is unique during this program execution
	 * <li>The value is positive
	 * <li>The values are monotonically increasing
	 * </ul>
	 * <p>
	 * This method is thread-safe and fast.
	 * 
	 * @return a positive integer ID unique in this program execution.
	 */
	public ModID() {
		id = nextId.getAndIncrement();
	}

	// There are a few places in the code that want a constant or invalid ModID value; see Barrowman.java
	private ModID(int val) {
		id = val;
	}
	public static ModID ZERO = new ModID(0);
	public static ModID INVALID = new ModID(-1);

	public int toInt() {
		return id;
	}
	
	public String toString() {
		return String.valueOf(id);
	}

	public int compareTo(Object o) {
		return id - ((ModID) o).id;
	}
}
