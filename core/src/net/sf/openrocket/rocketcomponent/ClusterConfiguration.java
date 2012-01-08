package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class that defines different cluster configurations available for the InnerTube.
 * The class is immutable, and all the constructors are private.  Therefore the only
 * available cluster configurations are those available in the static fields.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ClusterConfiguration {
	// Helper vars
	private static final double R5 = 1.0/(2*Math.sin(2*Math.PI/10));
	private static final double SQRT2 = Math.sqrt(2);
	private static final double SQRT3 = Math.sqrt(3);

	/** A single motor */
	public static final ClusterConfiguration SINGLE = new ClusterConfiguration("single", 0,0);

	/** Definitions of cluster configurations.  Do not modify array. */
	public static final ClusterConfiguration[] CONFIGURATIONS = {
		// Single row
		SINGLE,
		new ClusterConfiguration("double", -0.5,0, 0.5,0),
		new ClusterConfiguration("3-row", -1.0,0, 0.0,0, 1.0,0),
		new ClusterConfiguration("4-row", -1.5,0, -0.5,0, 0.5,0, 1.5,0),
		
		// Ring of tubes
		new ClusterConfiguration("3-ring", -0.5,-1.0/(2*SQRT3),
								  0.5,-1.0/(2*SQRT3),
								    0, 1.0/SQRT3),
		new ClusterConfiguration("4-ring", -0.5,0.5, 0.5,0.5, 0.5,-0.5, -0.5,-0.5),
		new ClusterConfiguration("5-ring", 0,R5,
								 R5*Math.sin(2*Math.PI/5),R5*Math.cos(2*Math.PI/5),
								 R5*Math.sin(2*Math.PI*2/5),R5*Math.cos(2*Math.PI*2/5),
								 R5*Math.sin(2*Math.PI*3/5),R5*Math.cos(2*Math.PI*3/5),
								 R5*Math.sin(2*Math.PI*4/5),R5*Math.cos(2*Math.PI*4/5)),
		new ClusterConfiguration("6-ring", 0,1, SQRT3/2,0.5, SQRT3/2,-0.5,
				 				 0,-1, -SQRT3/2,-0.5, -SQRT3/2,0.5),
		
		// Centered with ring
		new ClusterConfiguration("3-star", 0,0, 0,1, SQRT3/2,-0.5, -SQRT3/2,-0.5),
		new ClusterConfiguration("4-star", 0,0, -1/SQRT2,1/SQRT2, 1/SQRT2,1/SQRT2,
								 1/SQRT2,-1/SQRT2, -1/SQRT2,-1/SQRT2),
		new ClusterConfiguration("5-star", 0,0, 0,1, 
								 Math.sin(2*Math.PI/5),Math.cos(2*Math.PI/5),
								 Math.sin(2*Math.PI*2/5),Math.cos(2*Math.PI*2/5),
								 Math.sin(2*Math.PI*3/5),Math.cos(2*Math.PI*3/5),
								 Math.sin(2*Math.PI*4/5),Math.cos(2*Math.PI*4/5)),
		new ClusterConfiguration("6-star", 0,0, 0,1, SQRT3/2,0.5, SQRT3/2,-0.5,
				 				 0,-1, -SQRT3/2,-0.5, -SQRT3/2,0.5)
	};
	
	
	
	private final List<Double> points;
	private final String xmlName;
	
	
	private ClusterConfiguration(String xmlName, double... points) {
		this.xmlName = xmlName;
		if (points.length == 0 || points.length%2 == 1) {
			throw new IllegalArgumentException("Illegal number of points specified: "+
					points.length);
		}
		List<Double> l = new ArrayList<Double>(points.length);
		for (double d: points)
			l.add(d);
		
		this.points = Collections.unmodifiableList(l);
	}
	
	public String getXMLName() {
		return xmlName;
	}
	
	public int getClusterCount() {
		return points.size()/2;
	}
	
	/**
	 * Returns the relative positions of the cluster components.  The list is of length
	 * <code>2*getClusterCount()</code> with (x,y) value pairs.  The origin is at (0,0)
	 * and the values are positioned so that the closest clusters have distance of 1.
	 * 
	 * @return	a list of (x,y) coordinate pairs.
	 */
	public List<Double> getPoints() {
		return points;  // Unmodifiable
	}
	
	/**
	 * Return the points rotated by <code>rotation</code> radians.
	 * @param rotation  Rotation amount.
	 */
	public List<Double> getPoints(double rotation) {
		double cos = Math.cos(rotation);
		double sin = Math.sin(rotation);
		List<Double> ret = new ArrayList<Double>(points.size());
		for (int i=0; i<points.size()/2; i++) {
			double x = points.get(2*i);
			double y = points.get(2*i+1);
			ret.add( x*cos + y*sin);
			ret.add(-x*sin + y*cos);
		}
		return ret;
	}
	
	
	@Override
	public String toString() {
		return xmlName;
	}
}
