package net.sf.openrocket.aerodynamics;

public abstract class AtmosphericModel {
	/** Layer thickness of interpolated altitude. */
	private static final double DELTA = 500;
	
	private AtmosphericConditions[] levels = null;
	
	
	public AtmosphericConditions getConditions(double altitude) {
		if (levels == null)
			computeLayers();
		
		if (altitude <= 0)
			return levels[0];
		if (altitude >= DELTA*(levels.length-1))
			return levels[levels.length-1];
		
		int n = (int)(altitude/DELTA);
		double d = (altitude - n*DELTA)/DELTA;
		AtmosphericConditions c = new AtmosphericConditions();
		c.temperature = levels[n].temperature * (1-d) + levels[n+1].temperature * d;
		c.pressure = levels[n].pressure * (1-d) + levels[n+1].pressure * d;
			
		return c;
	}
	
	
	private void computeLayers() {
		double max = getMaxAltitude();
		int n = (int)(max/DELTA) + 1;
		levels = new AtmosphericConditions[n];
		for (int i=0; i < n; i++) {
			levels[i] = getExactConditions(i*DELTA);
		}
	}

	
	public abstract double getMaxAltitude();
	public abstract AtmosphericConditions getExactConditions(double altitude);
}
