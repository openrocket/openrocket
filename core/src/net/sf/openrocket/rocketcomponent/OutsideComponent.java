package net.sf.openrocket.rocketcomponent;

public interface OutsideComponent {
	
	
	/**
	 * Indicates whether this component is located inside or outside of the rest of the rocket. (Specifically, inside or outside its parent.)
	 * 
	 * @return      <code> False </code> This component is aligned with its parent
	 *              <code> True </code> This component is offset from its parent -- like an external pod, or strap-on stage
	 */
	public boolean getOutside();
	
	/**
	 * Change whether this component is located inside or outside of the rest of the rocket. (Specifically, inside or outside its parent.)
	 * 
	 * @param inline False indicates that this component axially aligned with its parent.  True indicates an off-center component.
	 */
	public void setOutside(final boolean inline);
	
	/**
	 * Get the position of this component in polar coordinates 
	 * 
	 * @return              Angular position in radians.
	 */
	public double getAngularOffset();
	
	/** 
	 * Set the position of this component in polar coordinates 
	 * 
	 * @param phi Angular position in radians
	 */
	public void setAngularOffset(final double phi);
	
	/**
	 * Number of instances this stage represents 
	 * 
	 * @return  number of instances this stage currently represents
	 */
	public int getInstanceCount();
	
	/** 
	 * Set the multiplicity of this component 
	 * 
	 * @param number of instances this component should represent
	 */
	public void setInstanceCount(final int phi);
	
	
	/**
	 * Get the position of this component in polar coordinates 
	 * 
	 * @return Radial position in radians (m)
	 */
	public double getRadialOffset();
	
	/**
	 * Get the position of this component in polar coordinates 
	 * 
	 * @param radius Radial distance in standard units. (m)
	 */
	public void setRadialOffset(final double radius);
	
}
