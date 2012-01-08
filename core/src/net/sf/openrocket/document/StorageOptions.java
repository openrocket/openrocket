package net.sf.openrocket.document;

import net.sf.openrocket.util.BugException;

public class StorageOptions implements Cloneable {
	
	public static final double SIMULATION_DATA_NONE = Double.POSITIVE_INFINITY;
	public static final double SIMULATION_DATA_ALL = 0;
	
	private boolean compressionEnabled = true;
	
	private double simulationTimeSkip = SIMULATION_DATA_NONE;

	private boolean explicitlySet = false;
	

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compression) {
		this.compressionEnabled = compression;
	}

	public double getSimulationTimeSkip() {
		return simulationTimeSkip;
	}

	public void setSimulationTimeSkip(double simulationTimeSkip) {
		this.simulationTimeSkip = simulationTimeSkip;
	}
	
	
	
	public boolean isExplicitlySet() {
		return explicitlySet;
	}

	public void setExplicitlySet(boolean explicitlySet) {
		this.explicitlySet = explicitlySet;
	}

	
	
	@Override
	public StorageOptions clone() {
		try {
			return (StorageOptions)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!?", e);
		}
	}
}
