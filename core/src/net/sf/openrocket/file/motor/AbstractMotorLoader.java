package net.sf.openrocket.file.motor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.util.MathUtil;

public abstract class AbstractMotorLoader implements MotorLoader {
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This method delegates the reading to the loaded from the Reader using the charset
	 * returned by {@link #getDefaultCharset()}.
	 */
	@Override
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		return load(new InputStreamReader(stream, getDefaultCharset()), filename);
	}
	
	
	/**
	 * Load motors from the specified <code>Reader</code>.
	 * 
	 * @param reader		the source of the motor definitions.
	 * @param filename		the file name of the file, may be <code>null</code> if not 
	 * 						applicable.
	 * @return				a list of motors contained in the file.
	 * @throws IOException	if an I/O exception occurs of the file format is invalid.
	 */
	protected abstract List<Motor> load(Reader reader, String filename) throws IOException;
	
	
	/**
	 * Return the default charset to use when loading rocket files of this type.
	 * <p>
	 * If the method {@link #load(InputStream, String)} is overridden as well, this
	 * method may return <code>null</code>.
	 * 
	 * @return	the charset to use when loading the rocket file.
	 */
	protected abstract Charset getDefaultCharset();
	
	
	
	
	//////////  Helper methods  //////////
	
	
	/**
	 * Calculate the mass of a motor at distinct points in time based on the
	 * initial total mass, propellant weight and thrust.
	 * <p>
	 * This calculation assumes that the velocity of the exhaust remains constant
	 * during the burning.  This derives from the mass-flow and thrust relation
	 * <pre>F = m' * v</pre>
	 *  
	 * @param time    list of time points
	 * @param thrust  thrust at the discrete times
	 * @param total   total weight of the motor
	 * @param prop    propellant amount consumed during burning
	 * @return		  a list of the mass at the specified time points
	 */
	protected static List<Double> calculateMass(List<Double> time, List<Double> thrust,
			double total, double prop) {
		List<Double> mass = new ArrayList<Double>();
		List<Double> deltam = new ArrayList<Double>();
		
		double t0, f0;
		double totalMassChange = 0;
		double scale;
		
		// First calculate mass change between points
		t0 = time.get(0);
		f0 = thrust.get(0);
		for (int i = 1; i < time.size(); i++) {
			double t1 = time.get(i);
			double f1 = thrust.get(i);
			
			double dm = 0.5 * (f0 + f1) * (t1 - t0);
			
			deltam.add(dm);
			totalMassChange += dm;
			t0 = t1;
			f0 = f1;
		}
		
		// Scale mass change and calculate mass
		mass.add(total);
		scale = prop / totalMassChange;
		for (double dm : deltam) {
			total -= dm * scale;
			// to correct negative mass error condition: (caused by rounding errors in the above loop)
			if (total < 0) {
				total = 0;
			}
			mass.add(total);
			
		}
		return mass;
	}
	
	/**
	 * Helper method to remove a delay (or plugged) from the end of a motor designation,
	 * if present.
	 * 
	 * @param designation	the motor designation.
	 * @return				the designation with a possible delay removed.
	 */
	protected static String removeDelay(String designation) {
		if (designation.matches(".*-([0-9]+|[pP])$")) {
			designation = designation.substring(0, designation.lastIndexOf('-'));
		}
		return designation;
	}
	
	
	/**
	 * Helper method to tokenize a string using whitespace as the delimiter.
	 */
	protected static String[] split(String str) {
		return split(str, "\\s+");
	}
	
	
	/**
	 * Helper method to tokenize a string using the given delimiter.
	 */
	protected static String[] split(String str, String delim) {
		String[] pieces = str.split(delim);
		if (pieces.length == 0 || !pieces[0].equals(""))
			return pieces;
		return ArrayUtils.copyOfRange(pieces, 1, pieces.length);
	}
	
	
	/**
	 * Sort the primary list and other lists in that order.
	 * 
	 * @param primary	the list to order.
	 * @param lists		lists to order in the same permutation.
	 */
	protected static void sortLists(List<Double> primary, List<?>... lists) {
		
		// TODO: LOW: Very idiotic sort algorithm, but should be fast enough
		// since the time should be sorted already
		
		int index;
		
		do {
			for (index = 0; index < primary.size() - 1; index++) {
				if (primary.get(index + 1) < primary.get(index)) {
					Collections.swap(primary, index, index + 1);
					for (List<?> l : lists) {
						Collections.swap(l, index, index + 1);
					}
					break;
				}
			}
		} while (index < primary.size() - 1);
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected static void finalizeThrustCurve(List<Double> time, List<Double> thrust,
			List... lists) {
		
		if (time.size() == 0)
			return;
		
		// Start
		if (!MathUtil.equals(time.get(0), 0) || !MathUtil.equals(thrust.get(0), 0)) {
			time.add(0, 0.0);
			thrust.add(0, 0.0);
			for (List l : lists) {
				Object o = l.get(0);
				l.add(0, o);
			}
		}
		
		// End
		int n = time.size() - 1;
		if (!MathUtil.equals(thrust.get(n), 0)) {
			time.add(time.get(n));
			thrust.add(0.0);
			for (List l : lists) {
				Object o = l.get(n);
				l.add(o);
			}
		}
	}
	
}
