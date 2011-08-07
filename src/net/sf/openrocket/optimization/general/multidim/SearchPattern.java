package net.sf.openrocket.optimization.general.multidim;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.util.MathUtil;

/**
 * A helper class to create search patterns.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SearchPattern {
	
	/**
	 * Create a square search pattern with the specified dimensionality.
	 * 
	 * @param dimensionality	the dimensionality
	 */
	public static List<Point> square(int dimensionality) {
		List<Point> pattern = new ArrayList<Point>(dimensionality);
		
		for (int i = 0; i < dimensionality; i++) {
			double[] p = new double[dimensionality];
			p[i] = 1.0;
			pattern.add(new Point(p));
		}
		return pattern;
	}
	
	

	/**
	 * Create a regular simplex search pattern with the specified dimensionality.
	 * 
	 * @param dimensionality	the dimensionality
	 */
	public static List<Point> regularSimplex(int dimensionality) {
		if (dimensionality <= 0) {
			throw new IllegalArgumentException("Illegal dimensionality " + dimensionality);
		}
		
		List<Point> pattern = new ArrayList<Point>(dimensionality);
		
		double[] coordinates = new double[dimensionality];
		double dot = -1.0 / dimensionality;
		
		/*
		 * First construct an origin-centered regular simplex.
		 * http://en.wikipedia.org/wiki/Simplex#Cartesian_coordinates_for_regular_n-dimensional_simplex_in_Rn
		 */

		for (int i = 0; i < dimensionality; i++) {
			// Compute the next point coordinate
			double value = 1;
			
			for (int j = 0; j < i; j++) {
				value -= MathUtil.pow2(coordinates[j]);
			}
			value = MathUtil.safeSqrt(value);
			
			coordinates[i] = value;
			pattern.add(new Point(coordinates));
			
			// Compute the i-coordinate for all next points
			value = dot;
			for (int j = 0; j < i; j++) {
				value -= MathUtil.pow2(coordinates[j]);
			}
			value = value / coordinates[i];
			
			coordinates[i] = value;
		}
		
		// Minimum point
		Point min = pattern.get(dimensionality - 1);
		min = min.set(dimensionality - 1, -min.get(dimensionality - 1));
		

		/*
		 * Shift simplex to have a corner at the origin and scale to unit length.
		 */
		if (dimensionality > 1) {
			double scale = 1.0 / (pattern.get(1).sub(pattern.get(0)).length());
			for (int i = 0; i < dimensionality; i++) {
				Point p = pattern.get(i);
				p = p.sub(min).mul(scale);
				pattern.set(i, p);
			}
		}
		
		return pattern;
	}
}
