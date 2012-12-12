package net.sf.openrocket.models.atmosphere;

/**
 * An abstract atmospheric model that pre-computes the conditions on a number of layers
 * and later linearly interpolates the values from between these layers.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class InterpolatingAtmosphericModel implements AtmosphericModel {
	/** Layer thickness of interpolated altitude. */
	private static final double DELTA = 500;
	
	private AtmosphericConditions[] levels = null;
	
	
	@Override
	public AtmosphericConditions getConditions(double altitude) {
		if (levels == null)
			computeLayers();
		
		if (altitude <= 0)
			return levels[0];
		if (altitude >= DELTA * (levels.length - 1))
			return levels[levels.length - 1];
		
		int n = (int) (altitude / DELTA);
		double d = (altitude - n * DELTA) / DELTA;
		AtmosphericConditions c = new AtmosphericConditions();
		c.setTemperature(levels[n].getTemperature() * (1 - d) + levels[n + 1].getTemperature() * d);
		c.setPressure(levels[n].getPressure() * (1 - d) + levels[n + 1].getPressure() * d);
		
		return c;
	}
	
	
	private void computeLayers() {
		double max = getMaxAltitude();
		int n = (int) (max / DELTA) + 1;
		levels = new AtmosphericConditions[n];
		for (int i = 0; i < n; i++) {
			levels[i] = getExactConditions(i * DELTA);
		}
	}
	
	
	protected abstract double getMaxAltitude();
	
	protected abstract AtmosphericConditions getExactConditions(double altitude);
}
