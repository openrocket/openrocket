package net.sf.openrocket.optimization;

import java.util.concurrent.Callable;

/**
 * A Callable that computes the value of a function at a specific point.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FunctionCallable implements Callable<Double> {
	
	private final Function function;
	private final Point point;
	
	/**
	 * Sole constructor.
	 * 
	 * @param function	the function to evaluate
	 * @param point		the point at which to evaluate the function
	 */
	public FunctionCallable(Function function, Point point) {
		this.function = function;
		this.point = point;
	}
	
	/**
	 * Evaluate the function and return the result.
	 */
	@Override
	public Double call() throws InterruptedException {
		return function.evaluate(point);
	}
	
}
