package net.sf.openrocket.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class LinearInterpolatorTest {

	@Test
	public void oldMainTest() {
		LinearInterpolator interpolator = new LinearInterpolator(
				new double[] {1, 1.5, 2, 4, 5},
				new double[] {0, 1,   0, 2, 2}
		);
		
		double[] answer = new double[] {
				/* x=0 */ 0.00,	0.00, 0.00,	0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
				/* x=1 */ 0.00, 0.20, 0.40, 0.60, 0.80, 1.00, 0.80, 0.60, 0.40, 0.20,
				/* x=2 */ 0.00, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90,
				/* x=3 */ 1.00, 1.10, 1.20, 1.30, 1.40, 1.50, 1.60, 1.70, 1.80, 1.90,
				/* x=4 */ 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00,
				/* x=5 */ 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00, 2.00,
				/* x=6 */ 2.00
		};
		
		double x = 0;
		for (int i=0; i < answer.length; i++) {
			assertEquals( "Answer wrong for x = " + x , answer[i], interpolator.getValue(x), 0.01 );
			x+= 0.1;
		}

	}
}
