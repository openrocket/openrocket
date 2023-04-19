package net.sf.openrocket.optimization.general.multidim;

import java.util.Comparator;

import net.sf.openrocket.optimization.general.FunctionCache;
import net.sf.openrocket.optimization.general.Point;

/**
 * A comparator that orders Points in a function value order, smallest first.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FunctionCacheComparator implements Comparator<Point> {
	
	private final FunctionCache cache;
	
	public FunctionCacheComparator(FunctionCache cache) {
		this.cache = cache;
	}
	
	@Override
	public int compare(Point o1, Point o2) {
		double v1 = cache.getValue(o1);
		double v2 = cache.getValue(o2);
		
		return Double.compare(v1, v2);
	}
	
}
