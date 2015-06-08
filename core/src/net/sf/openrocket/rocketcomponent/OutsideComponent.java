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
	public double getAngularPosition();
	
	/** 
	 * Set the position of this component in polar coordinates 
	 * 
	 * @param phi Angular position in radians
	 */
	public void setAngularPosition(final double phi);
	
	/**
	 * Get the position of this component in polar coordinates 
	 * 
	 * @return              Radial position in radians (m)
	 */
	public double getRadialPosition();
	
	/**
	 * Get the position of this component in polar coordinates 
	 * 
	 * @param radius Radial distance in standard units. (m)
	 */
	public void setRadialPosition(final double radius);
	
	/**
	 *      If component is not symmetric, this is the axial rotation angle (around it's own center). Defaults to 0. 
	 * 
	 * @return              Rotation angle in radians.
	 */
	public double getRotation();
	
	/**
	 *      If component is not symmetric, this is the axial rotation angle (around it's own center). Defaults to 0. 
	 * 
	 * @param rotation Rotation angle in radians.
	 */
	public void setRotation(final double rotation);
	
	
}
