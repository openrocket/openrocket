package net.sf.openrocket.rocketcomponent.position;

public interface RadiusPositionable {

    public double getBoundingRadius();

	public double getRadiusOffset();
	public void setRadiusOffset(final double radius);
	
	public RadiusMethod getRadiusMethod();
	public void setRadiusMethod( final RadiusMethod method );
	
	/**
	 * Equivalent to:
	 * `instance.setRadiusMethod(); instance.setRadiusOffset()`
	 * 
	 * @param radius
	 * @param method
	 */
	public void setRadius( final RadiusMethod method, final double radius );
	
}
