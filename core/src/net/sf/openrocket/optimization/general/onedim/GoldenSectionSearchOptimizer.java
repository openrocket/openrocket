package net.sf.openrocket.optimization.general.onedim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.optimization.general.FunctionCache;
import net.sf.openrocket.optimization.general.FunctionOptimizer;
import net.sf.openrocket.optimization.general.OptimizationController;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.ParallelFunctionCache;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Statistics;

/**
 * An implementation of a one-dimensional golden section search method
 * (see e.g. Nonlinear programming, Bazaraa, Sherall, Shetty, 2nd edition, p. 270)
 * <p>
 * This implementation attempts to guess future evaluations and computes them in parallel
 * with the next point.
 * <p>
 * The optimization can be aborted by interrupting the current thread.
 */
public class GoldenSectionSearchOptimizer implements FunctionOptimizer, Statistics {
	private static final Logger log = LoggerFactory.getLogger(GoldenSectionSearchOptimizer.class);
	
	private static final double ALPHA = (Math.sqrt(5) - 1) / 2.0;
	

	private ParallelFunctionCache functionExecutor;
	
	private Point current = null;
	
	private int guessSuccess = 0;
	private int guessFailure = 0;
	
	
	/**
	 * Construct an optimizer with no function executor.
	 */
	public GoldenSectionSearchOptimizer() {
		// No-op
	}
	
	/**
	 * Construct an optimizer.
	 * 
	 * @param functionExecutor	the function executor.
	 */
	public GoldenSectionSearchOptimizer(ParallelFunctionCache functionExecutor) {
		super();
		this.functionExecutor = functionExecutor;
	}
	
	@Override
	public void optimize(Point initial, OptimizationController control) throws OptimizationException {
		
		if (initial.dim() != 1) {
			throw new IllegalArgumentException("Only single-dimensional optimization supported, dim=" +
					initial.dim());
		}
		
		log.info("Starting golden section search for optimization");
		
		Point guessAC = null;
		Point guessBD = null;
		
		try {
			boolean guessedAC;
			
			Point previous = p(0);
			double previousValue = Double.NaN;
			current = previous;
			double currentValue = Double.NaN;
			
			/*
			 * Initialize the points + computation.
			 */
			Point a = p(0);
			Point d = p(1.0);
			Point b = section1(a, d);
			Point c = section2(a, d);
			
			functionExecutor.compute(a);
			functionExecutor.compute(d);
			functionExecutor.compute(b);
			functionExecutor.compute(c);
			
			// Wait for points a and d, which normally are already precomputed
			functionExecutor.waitFor(a);
			functionExecutor.waitFor(d);
			
			boolean continueOptimization = true;
			while (continueOptimization) {
				
				/*
				 * Get values at A & D for guessing.
				 * These are pre-calculated except during the first step.
				 */
				double fa, fd;
				fa = functionExecutor.getValue(a);
				fd = functionExecutor.getValue(d);
				

				/*
				 * Start calculating possible two next points.  The order of evaluation
				 * is selected based on the function values at A and D.
				 */
				guessAC = section1(a, c);
				guessBD = section2(b, d);
				if (Double.isNaN(fd) || fa < fd) {
					guessedAC = true;
					functionExecutor.compute(guessAC);
					functionExecutor.compute(guessBD);
				} else {
					guessedAC = false;
					functionExecutor.compute(guessBD);
					functionExecutor.compute(guessAC);
				}
				

				/*
				 * Get values at B and C.
				 */
				double fb, fc;
				functionExecutor.waitFor(b);
				functionExecutor.waitFor(c);
				fb = functionExecutor.getValue(b);
				fc = functionExecutor.getValue(c);
				
				double min = MathUtil.min(fa, fb, fc, fd);
				if (Double.isNaN(min)) {
					throw new OptimizationException("Unable to compute initial function values");
				}
				

				/*
				 * Update previous and current values for step control.
				 */
				previousValue = currentValue;
				currentValue = min;
				previous = current;
				if (min == fa) {
					current = a;
				} else if (min == fb) {
					current = b;
				} else if (min == fc) {
					current = c;
				} else {
					current = d;
				}
				

				/*
				 * Select next positions.  These are already being calculated in the background
				 * as guessAC and guessBD.
				 */
				if (min == fa || min == fb) {
					d = c;
					c = b;
					b = guessAC;
					functionExecutor.abort(guessBD);
					guessBD = null;
					log.debug("Selecting A-C region, a=" + a.get(0) + " c=" + c.get(0));
					if (guessedAC) {
						guessSuccess++;
					} else {
						guessFailure++;
					}
				} else {
					a = b;
					b = c;
					c = guessBD;
					functionExecutor.abort(guessAC);
					guessAC = null;
					log.debug("Selecting B-D region, b=" + b.get(0) + " d=" + d.get(0));
					if (!guessedAC) {
						guessSuccess++;
					} else {
						guessFailure++;
					}
				}
				

				/*
				 * Check optimization control.
				 */
				continueOptimization = control.stepTaken(previous, previousValue,
						current, currentValue, c.get(0) - a.get(0));
				
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				
			}
			

		} catch (InterruptedException e) {
			log.info("Optimization was interrupted with InterruptedException");
		}
		
		if (guessAC != null) {
			functionExecutor.abort(guessAC);
		}
		if (guessBD != null) {
			functionExecutor.abort(guessBD);
		}
		

		log.info("Finishing optimization at point " + getOptimumPoint() + " value " + getOptimumValue());
		log.info("Optimization statistics: " + getStatistics());
	}
	
	
	private Point p(double v) {
		return new Point(v);
	}
	
	
	private Point section1(Point a, Point b) {
		double va = a.get(0);
		double vb = b.get(0);
		return p(va + (1 - ALPHA) * (vb - va));
	}
	
	private Point section2(Point a, Point b) {
		double va = a.get(0);
		double vb = b.get(0);
		return p(va + ALPHA * (vb - va));
	}
	
	

	@Override
	public Point getOptimumPoint() {
		return current;
	}
	
	
	@Override
	public double getOptimumValue() {
		if (getOptimumPoint() == null) {
			return Double.NaN;
		}
		return functionExecutor.getValue(getOptimumPoint());
	}
	
	@Override
	public FunctionCache getFunctionCache() {
		return functionExecutor;
	}
	
	@Override
	public void setFunctionCache(FunctionCache functionCache) {
		if (!(functionCache instanceof ParallelFunctionCache)) {
			throw new IllegalArgumentException("Function cache needs to be a ParallelFunctionCache: " + functionCache);
		}
		this.functionExecutor = (ParallelFunctionCache) functionCache;
	}
	
	@Override
	public String getStatistics() {
		return String.format("Guess hit rate %d/%d = %.3f", guessSuccess, guessSuccess + guessFailure,
				((double) guessSuccess) / (guessSuccess + guessFailure));
	}
	
	@Override
	public void resetStatistics() {
		guessSuccess = 0;
		guessFailure = 0;
	}
	
}
