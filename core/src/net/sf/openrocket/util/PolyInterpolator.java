package net.sf.openrocket.util;

import java.util.Arrays;

/**
 * A class for polynomial interpolation.  The interpolation constraints can be specified
 * either as function values or values of the n'th derivative of the function.
 * Using an interpolation consists of three steps:
 * <p>
 * 1. constructing a <code>PolyInterpolator</code> using the interpolation x coordinates <br>
 * 2. generating the interpolation polynomial using the function and derivative values <br>
 * 3. evaluating the polynomial at the desired point
 * <p>
 * The constructor takes an array of double arrays.  The first array defines x coordinate
 * values for the function values, the second array x coordinate values for the function's
 * derivatives, the third array for second derivatives and so on.  Constructing the
 * <code>PolyInterpolator</code> is relatively slow, O(n^3) where n is the order of the
 * polynomial.  (It contains calculation of the inverse of an n x n matrix.)
 * <p>
 * Generating the interpolation polynomial is performed by the method 
 * {@link #interpolator(double...)}, which takes as an argument the function and 
 * derivative values.  This operation takes O(n^2) time.
 * <p>
 * Finally, evaluating the polynomial at different positions takes O(n) time.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PolyInterpolator {

	// Order of the polynomial
	private final int count;
	
	private final double[][] interpolationMatrix;
	
	
	/**
	 * Construct a polynomial interpolation generator.  All arguments to the constructor
	 * are the x coordinates of the interpolated function.  The first array correspond to
	 * the function values, the second to function derivatives, the third to second
	 * derivatives and so forth.  The order of the polynomial is automatically calculated
	 * from the total number of constraints.
	 * <p>
	 * The construction takes O(n^3) time.
	 * 
	 * @param points  	an array of constraints, the first corresponding to function value
	 * 					constraints, the second to derivative constraints etc.
	 */
	public PolyInterpolator(double[] ... points) {
		int myCount = 0;
		for (int i=0; i < points.length; i++) {
			myCount += points[i].length;
		}
		if (myCount == 0) {
			throw new IllegalArgumentException("No interpolation points defined.");
		}
		
		this.count = myCount;
		
		int[] mul = new int[myCount];
		Arrays.fill(mul, 1);

		double[][] matrix = new double[myCount][myCount];
		int row = 0;
		for (int j=0; j < points.length; j++) {
			
			for (int i=0; i < points[j].length; i++) {
				double x = 1;
				for (int col = myCount-1-j; col>= 0; col--) {
					matrix[row][col] = x*mul[col];
					x *= points[j][i];
				}
				row++;
			}
			
			for (int i=0; i < myCount; i++) {
				mul[i] *= (myCount-i-j-1);
			}
		}
		assert(row == myCount);
		
		interpolationMatrix = inverse(matrix);
	}


	/**
	 * Generates an interpolation polynomial.  The arguments supplied to this method
	 * are the function values, derivatives, second derivatives etc. in the order
	 * specified in the constructor (i.e. values first, then derivatives etc).
	 * <p>
	 * This method takes O(n^2) time.
	 * 
	 * @param values 	the function values, derivatives etc. at positions defined in the
	 * 					constructor.
	 * @return  		the coefficients of the interpolation polynomial, the highest order
	 * 					term first and the constant last.
	 */
	public double[] interpolator(double... values) {
		if (values.length != count) {
			throw new IllegalArgumentException("Wrong number of arguments "+values.length+
					" expected "+count);
		}
		
		double[] ret = new double[count];
		
		for (int j=0; j < count; j++) {
			for (int i=0; i < count; i++) {
				ret[j] += interpolationMatrix[j][i] * values[i];
			}
		}
		
		return ret;
	}


	/**
	 * Interpolate the given values at the point <code>x</code>.  This is equivalent
	 * to generating an interpolation polynomial and evaluating the polynomial at the
	 * specified point.
	 * 
	 * @param x			point at which to evaluate the interpolation polynomial.
	 * @param values	the function, derivatives etc. at position defined in the
	 * 					constructor.
	 * @return			the value of the interpolation.
	 */
	public double interpolate(double x, double... values) {
		return eval(x, interpolator(values));
	}

	
	/**
	 * Evaluate a polynomial at the specified point <code>x</code>.  The coefficients are
	 * assumed to have the highest order coefficient first and the constant term last.
	 * 
	 * @param x				position at which to evaluate the polynomial.
	 * @param coefficients	polynomial coefficients, highest term first and constant last.
	 * @return				the value of the polynomial.
	 */
	public static double eval(double x, double[] coefficients) {
		double v = 1;
		double result = 0;
		for (int i = coefficients.length-1; i >= 0; i--) {
			result += coefficients[i] * v;
			v *= x;
		}
		return result;
	}
	
	
	
	
	private static double[][] inverse(double[][] matrix) {
		int n = matrix.length;
		
		double x[][] = new double[n][n];
		double b[][] = new double[n][n];
		int index[] = new int[n];
		for (int i=0; i<n; ++i) 
			b[i][i] = 1;

		// Transform the matrix into an upper triangle
		gaussian(matrix, index);

		// Update the matrix b[i][j] with the ratios stored
		for (int i=0; i<n-1; ++i)
			for (int j=i+1; j<n; ++j)
				for (int k=0; k<n; ++k)
					b[index[j]][k] -= matrix[index[j]][i]*b[index[i]][k];

		// Perform backward substitutions
		for (int i=0; i<n; ++i) {
			x[n-1][i] = b[index[n-1]][i]/matrix[index[n-1]][n-1];
			for (int j=n-2; j>=0; --j) {
				x[j][i] = b[index[j]][i];
				for (int k=j+1; k<n; ++k) {
					x[j][i] -= matrix[index[j]][k]*x[k][i];
				}
				x[j][i] /= matrix[index[j]][j];
			}
		}
		return x;
	}

	private static void gaussian(double a[][],
			int index[]) {
		int n = index.length;
		double c[] = new double[n];

		// Initialize the index
		for (int i=0; i<n; ++i) index[i] = i;

		// Find the rescaling factors, one from each row
		for (int i=0; i<n; ++i) {
			double c1 = 0;
			for (int j=0; j<n; ++j) {
				double c0 = Math.abs(a[i][j]);
				if (c0 > c1) c1 = c0;
			}
			c[i] = c1;
		}

		// Search the pivoting element from each column
		int k = 0;
		for (int j=0; j<n-1; ++j) {
			double pi1 = 0;
			for (int i=j; i<n; ++i) {
				double pi0 = Math.abs(a[index[i]][j]);
				pi0 /= c[index[i]];
				if (pi0 > pi1) {
					pi1 = pi0;
					k = i;
				}
			}

			// Interchange rows according to the pivoting order
			int itmp = index[j];
			index[j] = index[k];
			index[k] = itmp;
			for (int i=j+1; i<n; ++i) {
				double pj = a[index[i]][j]/a[index[j]][j];

				// Record pivoting ratios below the diagonal
				a[index[i]][j] = pj;

				// Modify other elements accordingly
				for (int l=j+1; l<n; ++l)
					a[index[i]][l] -= pj*a[index[j]][l];
			}
		}
	}

}
