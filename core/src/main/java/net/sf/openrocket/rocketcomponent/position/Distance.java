package net.sf.openrocket.rocketcomponent.position;

public class Distance {
    
	public DistanceMethod method;
	public double distance;
	
	// just for convenience
	public Distance( final DistanceMethod initialMethod, final double initialMagnitude ) {
		this.method = initialMethod;
		this.distance = initialMagnitude;
	}	
	
}
