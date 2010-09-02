package net.sf.openrocket.optimization;

public interface FunctionOptimizer {
	
	public void optimize(Point initial, OptimizationController control);
	
	
	public Point getOptimumPoint();
	
	public double getOptimumValue();
	
	
	public FunctionCache getFunctionCache();
	
	public void setFunctionCache(FunctionCache functionCache);
	

}
