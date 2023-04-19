package net.sf.openrocket.optimization;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.general.multidim.SearchPattern;

import org.junit.Test;

public class TestSearchPattern {
	
	@Test
	public void testRegularSimplex() {
		for (int dim = 1; dim < 20; dim++) {
			List<Point> points = SearchPattern.regularSimplex(dim);
			assertEquals(dim, points.size());
			
			for (int i = 0; i < dim; i++) {
				// Test dot product
				for (int j = i + 1; j < dim; j++) {
					double[] x = points.get(i).asArray();
					double[] y = points.get(j).asArray();
					double dot = 0;
					for (int k = 0; k < dim; k++) {
						dot += x[k] * y[k];
					}
					assertEquals(0.5, dot, 0.000000001);
				}
				
				// Test positive coordinates
				for (int j = 0; j < dim; j++) {
					assertTrue(points.get(i).get(j) >= 0);
				}
				
				// Test length
				assertEquals(1.0, points.get(i).length(), 0.000000001);
			}
		}
	}
	
}
