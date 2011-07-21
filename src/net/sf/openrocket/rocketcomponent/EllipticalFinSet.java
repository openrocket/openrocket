package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

public class EllipticalFinSet extends FinSet {
	private static final Translator trans = Application.getTranslator();
	
	private static final int POINTS = 31;
	
	// Static positioning for the fin points
	private static final double[] POINT_X = new double[POINTS];
	private static final double[] POINT_Y = new double[POINTS];
	static {
		for (int i = 0; i < POINTS; i++) {
			double a = Math.PI * (POINTS - 1 - i) / (POINTS - 1);
			POINT_X[i] = (Math.cos(a) + 1) / 2;
			POINT_Y[i] = Math.sin(a);
		}
		POINT_X[0] = 0;
		POINT_Y[0] = 0;
		POINT_X[POINTS - 1] = 1;
		POINT_Y[POINTS - 1] = 0;
	}
	

	private double height = 0.05;
	
	public EllipticalFinSet() {
		this.length = 0.05;
	}
	
	
	@Override
	public Coordinate[] getFinPoints() {
		double len = MathUtil.max(length, 0.0001);
		Coordinate[] points = new Coordinate[POINTS];
		for (int i = 0; i < POINTS; i++) {
			points[i] = new Coordinate(POINT_X[i] * len, POINT_Y[i] * height);
		}
		return points;
	}
	
	@Override
	public double getSpan() {
		return height;
	}
	
	@Override
	public String getComponentName() {
		//// Elliptical fin set
		return trans.get("EllipticalFinSet.Ellipticalfinset");
	}
	
	
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		if (MathUtil.equals(this.height, height))
			return;
		this.height = height;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public void setLength(double length) {
		if (MathUtil.equals(this.length, length))
			return;
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	

}
