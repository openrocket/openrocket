package net.sf.openrocket.optimization.general;

/**
 * A storage of cached values of a function.  The purpose of this class is to
 * cache function values 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface FunctionCache {
	
	public double getValue(Point point);
	
	public void clearCache();
	
	public Function getFunction();
	
	public void setFunction(Function function);
	
}
