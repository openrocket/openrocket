package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.util.PolyInterpolator;

public class ConeDragTest {

	private static final double DELTA = 0.01;
	private static final double SUBSONIC = 0.0;
	private static final double SUPERSONIC = 1.3;
	
	
	private static final PolyInterpolator polyInt2 = new PolyInterpolator(
			new double[] {1.0, SUPERSONIC}
			,new double[] {1.0, SUPERSONIC}
			,new double[] {SUPERSONIC}
	);
	
	private final double angle;
	private final double sin;
	private final double ratio;
	
	private final double[] int2;

	// Coefficients for subsonic interpolation  a * M^b + c
	private final double a, b, c;
	
	
	
	public ConeDragTest(double angle) {
		this.angle = angle;
		this.sin = Math.sin(angle);
		this.ratio = 1.0 / (2*Math.tan(angle));
		
		double dsuper = (supersonic(SUPERSONIC+DELTA) - supersonic(SUPERSONIC))/DELTA;
		

		c = subsonic(0);
		a = sonic() - c;
		b = sonicDerivative() / a;
		
		
		int2 = polyInt2.interpolator(
				sonic(), supersonic(SUPERSONIC)
				, sonicDerivative(), dsuper
				,0
		);
		
		System.err.println("At mach1: CD="+sin+" dCD/dM="+(4.0/2.4*(1-0.5*sin)));
		
	}
	
	private double subsonic(double m) {
		return 0.8*sin*sin/Math.sqrt(1-m*m);
	}
	
	private double sonic() {
		return sin;
	}
	
	private double sonicDerivative() {
		return 4.0/2.4*(1-0.5*sin);
	}
	
	private double supersonic(double m) {
		return 2.1 * sin*sin + 0.5*sin/Math.sqrt(m*m-1);
	}
	
	
	
	
	public double getCD(double m) {
		if (m >= SUPERSONIC)
			return supersonic(m);

		if (m <= 1.0)
			return a * Math.pow(m,b) + c;
		return PolyInterpolator.eval(m, int2);
			
//		return PolyInterpolator.eval(m, interpolator);
	}
	
	
	
	public static void main(String[] arg) {
		
		ConeDragTest cone10 = new ConeDragTest(10.0*Math.PI/180);
		ConeDragTest cone20 = new ConeDragTest(20.0*Math.PI/180);
		ConeDragTest cone30 = new ConeDragTest(30.0*Math.PI/180);
		
		ConeDragTest coneX = new ConeDragTest(5.0*Math.PI/180);

		for (double m=0; m < 4.0001; m+=0.02) {
			System.out.println(m + " " 
					+ cone10.getCD(m) + " "
					+ cone20.getCD(m) + " "
					+ cone30.getCD(m) + " "
//					+ coneX.getCD(m)
			);
		}
		
	}
	
}
