package net.sf.openrocket.rocketcomponent;

public interface RadialParent {

	/**
	 * Return the outer radius of the component at local coordinate <code>x</code>.
	 * Values for <code>x < 0</code> and <code>x > getLength()</code> are undefined.
	 * 
	 * @param x		the lengthwise position in the coordinates of this component.
	 * @return		the outer radius of the component at that position.
	 */
	public double getOuterRadius(double x);

	/**
	 * Return the inner radius of the component at local coordinate <code>x</code>.
	 * Values for <code>x < 0</code> and <code>x > getLength()</code> are undefined.
	 * 
	 * @param x		the lengthwise position in the coordinates of this component.
	 * @return		the inner radius of the component at that position.
	 */
	public double getInnerRadius(double x);
	
	
	/**
	 * Return the length of this component.
	 * 
	 * @return		the length of this component.
	 */
	public double getLength();
		
}
