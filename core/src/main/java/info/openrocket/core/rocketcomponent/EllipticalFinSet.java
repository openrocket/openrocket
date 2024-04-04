package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

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
		Coordinate[] finPoints = new Coordinate[POINTS];
		for (int i = 0; i < POINTS; i++) {
			finPoints[i] = new Coordinate(POINT_X[i] * len, POINT_Y[i] * height);
		}

		// Set the start and end fin points the same as the root points (necessary for canted fins)
		final Coordinate[] rootPoints = getRootPoints();
		if (rootPoints.length > 1) {
			finPoints[0] = finPoints[0].setX(rootPoints[0].x).setY(rootPoints[0].y);
			finPoints[finPoints.length - 1] = finPoints[finPoints.length - 1].setX(rootPoints[rootPoints.length - 1].x).setY(rootPoints[rootPoints.length - 1].y);
		}
		return finPoints;
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof EllipticalFinSet) {
				((EllipticalFinSet) listener).setHeight(height);
			}
		}

		if (MathUtil.equals(this.height, height))
			return;
		this.height = height;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public void setLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof EllipticalFinSet) {
				((EllipticalFinSet) listener).setLength(length);
			}
		}

		if (MathUtil.equals(this.length, length))
			return;
		this.length = length;
		validateFinTabLength();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	

}
