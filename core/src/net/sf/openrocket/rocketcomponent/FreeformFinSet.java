package net.sf.openrocket.rocketcomponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;


public class FreeformFinSet extends FinSet {
	private static final Logger log = LoggerFactory.getLogger(FreeformFinSet.class);
	private static final Translator trans = Application.getTranslator();
	
	private ArrayList<Coordinate> points = new ArrayList<Coordinate>();
	
	public FreeformFinSet() {
		points.add(Coordinate.NUL);
		points.add(new Coordinate(0.025, 0.05));
		points.add(new Coordinate(0.075, 0.05));
		points.add(new Coordinate(0.05, 0));
		
		this.length = 0.05;
	}
	
	
	public FreeformFinSet(Coordinate[] finpoints) throws IllegalFinPointException {
		setPoints(finpoints);
	}
	
	/*
	public FreeformFinSet(FinSet finset) {
		Coordinate[] finpoints = finset.getFinPoints();
		this.copyFrom(finset);

		points.clear();
		for (Coordinate c: finpoints) {
			points.add(c);
		}
		this.length = points.get(points.size()-1).x - points.get(0).x;
	}
	*/
	
	
	/**
	 * Convert an existing fin set into a freeform fin set.  The specified
	 * fin set is taken out of the rocket tree (if any) and the new component
	 * inserted in its stead.
	 * <p>
	 * The specified fin set should not be used after the call!
	 *
	 * @param finset	the fin set to convert.
	 * @return			the new freeform fin set.
	 */
	public static FreeformFinSet convertFinSet(FinSet finset) {
		log.info("Converting " + finset.getComponentName() + " into freeform fin set");
		final RocketComponent root = finset.getRoot();
		FreeformFinSet freeform;
		List<RocketComponent> toInvalidate = Collections.emptyList();
		
		try {
			if (root instanceof Rocket) {
				((Rocket) root).freeze();
			}
			
			// Get fin set position and remove fin set
			final RocketComponent parent = finset.getParent();
			final int position;
			if (parent != null) {
				position = parent.getChildPosition(finset);
				parent.removeChild(position);
			} else {
				position = -1;
			}
			
			
			// Create the freeform fin set
			Coordinate[] finpoints = finset.getFinPoints();
			try {
				freeform = new FreeformFinSet(finpoints);
			} catch (IllegalFinPointException e) {
				throw new BugException("Illegal fin points when converting existing fin to " +
						"freeform fin, fin=" + finset + " points=" + Arrays.toString(finpoints),
						e);
			}
			
			// Copy component attributes
			toInvalidate = freeform.copyFrom(finset);
			
			// Set name
			final String componentTypeName = finset.getComponentName();
			final String name = freeform.getName();
			
			if (name.startsWith(componentTypeName)) {
				freeform.setName(freeform.getComponentName() +
						name.substring(componentTypeName.length()));
			}
			
			freeform.setAppearance(finset.getAppearance());
			
			// Add freeform fin set to parent
			if (parent != null) {
				parent.addChild(freeform, position);
			}
			
		} finally {
			if (root instanceof Rocket) {
				((Rocket) root).thaw();
			}
			// Invalidate components after events have been fired
			for (RocketComponent c : toInvalidate) {
				c.invalidate();
			}
		}
		return freeform;
	}
	
	
	
	/**
	 * Add a fin point between indices <code>index-1</code> and <code>index</code>.
	 * The point is placed at the midpoint of the current segment.
	 *
	 * @param index   the fin point before which to add the new point.
	 */
	public void addPoint(int index) {
		double x0, y0, x1, y1;
		
		x0 = points.get(index - 1).x;
		y0 = points.get(index - 1).y;
		x1 = points.get(index).x;
		y1 = points.get(index).y;
		
		points.add(index, new Coordinate((x0 + x1) / 2, (y0 + y1) / 2));
		// adding a point within the segment affects neither mass nor aerodynamics
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	/**
	 * Remove the fin point with the given index.  The first and last fin points
	 * cannot be removed, and will cause an <code>IllegalFinPointException</code>
	 * if attempted.
	 *
	 * @param index   the fin point index to remove
	 * @throws IllegalFinPointException if removing would result in invalid fin planform
	 */
	public void removePoint(int index) throws IllegalFinPointException {
		if (index == 0 || index == points.size() - 1) {
			throw new IllegalFinPointException("cannot remove first or last point");
		}
		
		ArrayList<Coordinate> copy = this.points.clone();
		copy.remove(index);
		validate(copy);
		this.points = copy;
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public int getPointCount() {
		return points.size();
	}
	
	public void setPoints(Coordinate[] points) throws IllegalFinPointException {
		setPoints(Arrays.asList(points));
	}
	
	public void setPoints(List<Coordinate> points) throws IllegalFinPointException {
		ArrayList<Coordinate> list = new ArrayList<Coordinate>(points);
		validate(list);
		this.points = list;
		
		this.length = points.get(points.size() - 1).x;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	/**
	 * Set the point at position <code>i</code> to coordinates (x,y).
	 * <p>
	 * Note that this method enforces basic fin shape restrictions (non-negative y,
	 * first and last point locations) silently, but throws an
	 * <code>IllegalFinPointException</code> if the point causes fin segments to
	 * intersect.
	 * <p>
	 * Moving of the first point in the X-axis is allowed, but this actually moves
	 * all of the other points the corresponding distance back.
	 *
	 * @param index	the point index to modify.
	 * @param x		the x-coordinate.
	 * @param y		the y-coordinate.
	 * @throws IllegalFinPointException	if the specified fin point would cause intersecting
	 * 									segments
	 */
	public void setPoint(int index, double x, double y) throws IllegalFinPointException {
		if (y < 0)
			y = 0;
		
		double x0, y0, x1, y1;
		
		if (index == 0) {
			
			// Restrict point
			x = Math.min(x, points.get(points.size() - 1).x);
			y = 0;
			x0 = Double.NaN;
			y0 = Double.NaN;
			x1 = points.get(1).x;
			y1 = points.get(1).y;
			
		} else if (index == points.size() - 1) {
			
			// Restrict point
			x = Math.max(x, 0);
			y = 0;
			x0 = points.get(index - 1).x;
			y0 = points.get(index - 1).y;
			x1 = Double.NaN;
			y1 = Double.NaN;
			
		} else {
			
			x0 = points.get(index - 1).x;
			y0 = points.get(index - 1).y;
			x1 = points.get(index + 1).x;
			y1 = points.get(index + 1).y;
			
		}
		
		
		
		// Check for intersecting
		double px0, py0, px1, py1;
		px0 = 0;
		py0 = 0;
		for (int i = 1; i < points.size(); i++) {
			px1 = points.get(i).x;
			py1 = points.get(i).y;
			
			if (i != index - 1 && i != index && i != index + 1) {
				if (intersects(x0, y0, x, y, px0, py0, px1, py1)) {
					throw new IllegalFinPointException("segments intersect");
				}
			}
			if (i != index && i != index + 1 && i != index + 2) {
				if (intersects(x, y, x1, y1, px0, py0, px1, py1)) {
					throw new IllegalFinPointException("segments intersect");
				}
			}
			
			px0 = px1;
			py0 = py1;
		}
		
		if (index == 0) {
			
			//System.out.println("Set point zero to x:" + x);
			for (int i = 1; i < points.size(); i++) {
				Coordinate c = points.get(i);
				points.set(i, c.setX(c.x - x));
			}
			
		} else {
			
			points.set(index, new Coordinate(x, y));
			
		}
		if (index == 0 || index == points.size() - 1) {
			this.length = points.get(points.size() - 1).x;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	private boolean intersects(double ax0, double ay0, double ax1, double ay1,
			double bx0, double by0, double bx1, double by1) {
		
		double d = ((by1 - by0) * (ax1 - ax0) - (bx1 - bx0) * (ay1 - ay0));
		
		double ua = ((bx1 - bx0) * (ay0 - by0) - (by1 - by0) * (ax0 - bx0)) / d;
		double ub = ((ax1 - ax0) * (ay0 - by0) - (ay1 - ay0) * (ax0 - bx0)) / d;
		
		return (ua >= 0) && (ua <= 1) && (ub >= 0) && (ub <= 1);
	}
	
	
	@Override
	public Coordinate[] getFinPoints() {
		return points.toArray(new Coordinate[0]);
	}
	
	@Override
	public double getSpan() {
		double max = 0;
		for (Coordinate c : points) {
			if (c.y > max)
				max = c.y;
		}
		return max;
	}
	
	@Override
	public String getComponentName() {
		//// Freeform fin set
		return trans.get("FreeformFinSet.FreeformFinSet");
	}
	
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		RocketComponent c = super.copyWithOriginalID();
		((FreeformFinSet) c).points = this.points.clone();
		return c;
	}
	
	private void validate(ArrayList<Coordinate> pts) throws IllegalFinPointException {
		final int n = pts.size();
		if (pts.get(0).x != 0 || pts.get(0).y != 0 ||
				pts.get(n - 1).x < 0 || pts.get(n - 1).y != 0) {
			throw new IllegalFinPointException("Start or end point illegal.");
		}
		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 2; j < n - 1; j++) {
				if (intersects(pts.get(i).x, pts.get(i).y, pts.get(i + 1).x, pts.get(i + 1).y,
						pts.get(j).x, pts.get(j).y, pts.get(j + 1).x, pts.get(j + 1).y)) {
					throw new IllegalFinPointException("segments intersect");
				}
			}
			if (pts.get(i).z != 0) {
				throw new IllegalFinPointException("z-coordinate not zero");
			}
		}
	}
	
}
