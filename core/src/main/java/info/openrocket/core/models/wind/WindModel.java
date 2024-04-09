package info.openrocket.core.models.wind;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Monitorable;

public interface WindModel extends Monitorable {

	public Coordinate getWindVelocity(double time, double altitude);

}
