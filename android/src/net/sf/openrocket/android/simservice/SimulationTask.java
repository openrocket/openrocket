package net.sf.openrocket.android.simservice;

import java.io.Serializable;

public class SimulationTask implements Serializable {

	int simulationId;

	public SimulationTask(int simulationId) {
		this.simulationId = simulationId;
	}
	
}
