package net.sf.openrocket.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueID {
	
	private static AtomicInteger nextId = new AtomicInteger(1);
	
	/**
	 * Return a positive integer ID unique during this program execution.  
	 * The values are taken as sequential numbers, and will re-occur in 
	 * later executions of the program.
	 * <p>
	 * This method is thread-safe and fast.
	 * 
	 * @return	a positive integer ID unique in this program execution.
	 */
	public static int next() {
		return nextId.getAndIncrement();
	}

	
	/**
	 * Return a new universally unique ID string.
	 * 
	 * @return	a unique identifier string.
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}
	
}
