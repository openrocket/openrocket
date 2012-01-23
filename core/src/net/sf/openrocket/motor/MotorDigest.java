package net.sf.openrocket.motor;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TextUtil;

/**
 * A class that generated a "digest" of a motor.  A digest is a string value that
 * uniquely identifies a motor (like a hash code or checksum).  Two motors that have
 * the same digest behave similarly with a very high probability.  The digest can
 * therefore be used to identify motors that otherwise have the same specifications.
 * <p>
 * The digest only uses a limited amount of precision, so that rounding errors won't
 * cause differing digest results.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorDigest {
	
	private static final double EPSILON = 0.00000000001;
	
	public enum DataType {
		/** An array of time points at which data is available (in ms) */
		TIME_ARRAY(0, 1000),
		/** Mass data for a few specific points (normally initial and empty mass) (in 0.1g) */
		MASS_SPECIFIC(1, 10000),
		/** Mass per time (in 0.1g) */
		MASS_PER_TIME(2, 10000),
		/** CG position for a few specific points (normally initial and final CG) (in mm) */
		CG_SPECIFIC(3, 1000),
		/** CG position per time (in mm) */
		CG_PER_TIME(4, 1000),
		/** Thrust force per time (in mN) */
		FORCE_PER_TIME(5, 1000);
		
		private final int order;
		private final int multiplier;
		
		DataType(int order, int multiplier) {
			this.order = order;
			this.multiplier = multiplier;
		}
		
		public int getOrder() {
			return order;
		}
		
		public int getMultiplier() {
			return multiplier;
		}
	}
	
	
	private final MessageDigest digest;
	private boolean used = false;
	private int lastOrder = -1;
	
	
	public MotorDigest() {
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 digest not supported by JRE", e);
		}
	}
	
	
	
	public void update(DataType type, double... values) {
		int multiplier = type.getMultiplier();
		
		int[] intValues = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			double v = values[i];
			v = next(v);
			v *= multiplier;
			v = next(v);
			intValues[i] = (int) Math.round(v);
		}
		update(type, intValues);
	}
	
	
	private void update(DataType type, int... values) {
		
		// Check for correct order
		if (lastOrder >= type.getOrder()) {
			throw new IllegalArgumentException("Called with type=" + type + " order=" + type.getOrder() +
					" while lastOrder=" + lastOrder);
		}
		lastOrder = type.getOrder();
		
		// Digest the type
		digest.update(bytes(type.getOrder()));
		
		// Digest the data length
		digest.update(bytes(values.length));
		
		// Digest the values
		for (int v : values) {
			digest.update(bytes(v));
		}
		
	}
	
	
	private static double next(double v) {
		return v + Math.signum(v) * EPSILON;
	}
	
	
	public String getDigest() {
		if (used) {
			throw new IllegalStateException("MotorDigest already used");
		}
		used = true;
		byte[] result = digest.digest();
		return TextUtil.hexString(result);
	}
	
	
	
	private byte[] bytes(int value) {
		return new byte[] {
				(byte) ((value >>> 24) & 0xFF), (byte) ((value >>> 16) & 0xFF),
				(byte) ((value >>> 8) & 0xFF), (byte) (value & 0xFF) };
	}
	
	
	/**
	 * Digest the contents of a thrust curve motor.  The result is a string uniquely
	 * defining the functional aspects of the motor.
	 * 
	 * @param m		the motor to digest
	 * @return		the digest
	 */
	public static String digestMotor(ThrustCurveMotor m) {
		
		// Create the motor digest from data available in RASP files
		MotorDigest motorDigest = new MotorDigest();
		motorDigest.update(DataType.TIME_ARRAY, m.getTimePoints());
		
		Coordinate[] cg = m.getCGPoints();
		double[] cgx = new double[cg.length];
		double[] mass = new double[cg.length];
		for (int i = 0; i < cg.length; i++) {
			cgx[i] = cg[i].x;
			mass[i] = cg[i].weight;
		}
		
		motorDigest.update(DataType.MASS_PER_TIME, mass);
		motorDigest.update(DataType.CG_PER_TIME, cgx);
		motorDigest.update(DataType.FORCE_PER_TIME, m.getThrustPoints());
		return motorDigest.getDigest();
		
	}
	
	public static String digestComment(String comment) {
		comment = comment.replaceAll("\\s+", " ").trim();
		
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 digest not supported by JRE", e);
		}
		
		try {
			digest.update(comment.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not supported by JRE", e);
		}
		
		return TextUtil.hexString(digest.digest());
	}
	
}
