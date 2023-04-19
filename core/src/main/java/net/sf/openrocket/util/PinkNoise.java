package net.sf.openrocket.util;
import java.util.Random;


/**
 * A class that provides a source of pink noise with a power spectrum density 
 * proportional to 1/f^alpha.  The values are computed by applying an IIR filter to
 * generated Gaussian random numbers.  The number of poles used in the filter may be
 * specified.  Values as low as 3 produce good results, but using a larger number of
 * poles allows lower frequencies to be amplified.  Below the cutoff frequency the
 * power spectrum density if constant.
 * <p>
 * The IIR filter use by this class is presented by N. Jeremy Kasdin, Proceedings of 
 * the IEEE, Vol. 83, No. 5, May 1995, p. 822.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PinkNoise {
	private final int poles;
	private final double[] multipliers;
	
	private final double[] values;
	private final Random rnd;

	
	/**
	 * Generate pink noise with alpha=1.0 using a five-pole IIR.
	 */
	public PinkNoise() {
		this(1.0, 5, new Random());
	}
	
	
	/**
	 * Generate a specific pink noise using a five-pole IIR.
	 * 
	 * @param alpha		the exponent of the pink noise, 1/f^alpha.
	 */
	public PinkNoise(double alpha) {
		this(alpha, 5, new Random());
	}
	
	
	/**
	 * Generate pink noise specifying alpha and the number of poles.  The larger the
	 * number of poles, the lower are the lowest frequency components that are amplified.
	 * 
	 * @param alpha		the exponent of the pink noise, 1/f^alpha.
	 * @param poles		the number of poles to use.
	 */
	public PinkNoise(double alpha, int poles) {
		this(alpha, poles, new Random());
	}
	
	
	/**
	 * Generate pink noise specifying alpha, the number of poles and the randomness source.
	 * 
	 * @param alpha    the exponent of the pink noise, 1/f^alpha.
	 * @param poles    the number of poles to use.
	 * @param random   the randomness source.
	 */
	public PinkNoise(double alpha, int poles, Random random) {
		this.rnd = random;
		this.poles = poles;
		this.multipliers = new double[poles];
		this.values = new double[poles];
		
		double a = 1;
		for (int i=0; i < poles; i++) {
			a = (i - alpha/2) * a / (i+1);
			multipliers[i] = a;
		}
		
		// Fill the history with random values
		for (int i=0; i < 5*poles; i++)
			this.nextValue();
	}
	
	
	
	public double nextValue() {
		double x = rnd.nextGaussian();
//		double x = rnd.nextDouble()-0.5;
		
		for (int i=0; i < poles; i++) {
			x -= multipliers[i] * values[i];
		}
		System.arraycopy(values, 0, values, 1, values.length-1);
		values[0] = x;
		
		return x;
	}
	
	// TODO: this method seems incomplete
	public static void main(String[] arg) {
		
		@SuppressWarnings("unused")
		PinkNoise source;
		
		source = new PinkNoise(1.0, 100);
		@SuppressWarnings("unused")
		double std = 0;
		for (int i=0; i < 1000000; i++) {
			
		}
		

//		int n = 5000000;
//		double avgavg=0;
//		double avgstd = 0; 
//		double[] val = new double[n];
//
//		for (int j=0; j < 10; j++) {
//			double avg=0, std=0;
//			source = new PinkNoise(5.0/3.0, 2);
//
//			for (int i=0; i < n; i++) {
//				val[i] = source.nextValue();
//				avg += val[i];
//			}
//			avg /= n;
//			for (int i=0; i < n; i++) {
//				std += (val[i]-avg)*(val[i]-avg);
//			}
//			std /= n;
//			std = Math.sqrt(std);
//			
//			System.out.println("avg:"+avg+" stddev:"+std);
//			avgavg += avg;
//			avgstd += std;
//		}
//		avgavg /= 10;
//		avgstd /= 10;
//		System.out.println("Average avg:"+avgavg+" std:"+avgstd);
//		
		// Two poles:

	}
	
	
}
