package net.sf.openrocket.rocketcomponent;

public interface RocketComponentVisitor<R> {
	
	/**
	 * The callback method.  This method is the 2nd leg of the double-dispatch, having been invoked from a 
	 * corresponding <code>accept</code>.
	 *           
	 * @param visitable  the instance of the Visitable (the target of what is being visiting)
	 */
	void visit(RocketComponent visitable);
	
	/**
	 * Return the final result
	 * @return
	 */
	R getResult();
	
}
