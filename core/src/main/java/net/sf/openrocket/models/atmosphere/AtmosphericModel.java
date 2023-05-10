package net.sf.openrocket.models.atmosphere;

import net.sf.openrocket.util.Monitorable;

public interface AtmosphericModel extends Monitorable {

	public AtmosphericConditions getConditions(double altitude);
	
}
