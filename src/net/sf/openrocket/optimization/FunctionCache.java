package net.sf.openrocket.optimization;

public interface FunctionCache {
	
	public double getValue(Point point);
	
	public void clearCache();
	
	public Function getFunction();
	
	public void setFunction(Function function);
	
}
