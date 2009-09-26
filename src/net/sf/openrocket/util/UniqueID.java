package net.sf.openrocket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.openrocket.gui.main.ExceptionHandler;

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
	
	
	/**
	 * Return a hashed unique ID that contains no information whatsoever of the
	 * originating computer.
	 * 
	 * @return	a unique identifier string that contains no information about the computer.
	 */
	public static String generateHashedID() {
		String id = UUID.randomUUID().toString();
		
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(id.getBytes());
			byte[] digest = algorithm.digest();
			
			StringBuilder sb = new StringBuilder();
			for (byte b: digest) {
				sb.append(String.format("%02X", 0xFF & b));
			}
			id = sb.toString();
			
		} catch (NoSuchAlgorithmException e) {
			ExceptionHandler.handleErrorCondition(e);
			id = "" + id.hashCode();
		}
		
		return id;
	}
	
}
