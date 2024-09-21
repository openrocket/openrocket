package info.openrocket.core.models.wind;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Monitorable;

public interface WindModel extends Monitorable, Cloneable, ChangeSource {
	Coordinate getWindVelocity(double time, double altitude);

	WindModel clone();
}
