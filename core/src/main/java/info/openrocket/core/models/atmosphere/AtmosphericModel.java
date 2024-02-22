package info.openrocket.core.models.atmosphere;

import info.openrocket.core.util.Monitorable;

public interface AtmosphericModel extends Monitorable {

	public AtmosphericConditions getConditions(double altitude);

}
