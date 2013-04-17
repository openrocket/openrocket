package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.StateChangeListener;

public class DeploymentConfiguration implements FlightConfigurableParameter<DeploymentConfiguration> {
	
	
	public static enum DeployEvent {
		LAUNCH(trans.get("RecoveryDevice.DeployEvent.LAUNCH")) {
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				return e.getType() == FlightEvent.Type.LAUNCH;
			}
		},
		EJECTION(trans.get("RecoveryDevice.DeployEvent.EJECTION")) {
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				RocketComponent charge = e.getSource();
				return charge.getStageNumber() == source.getStageNumber();
			}
		},
		APOGEE(trans.get("RecoveryDevice.DeployEvent.APOGEE")) {
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				return e.getType() == FlightEvent.Type.APOGEE;
			}
		},
		ALTITUDE(trans.get("RecoveryDevice.DeployEvent.ALTITUDE")) {
			@SuppressWarnings("unchecked")
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.ALTITUDE)
					return false;
				
				double alt = config.deployAltitude;
				Pair<Double, Double> altitude = (Pair<Double, Double>) e.getData();
				
				return (altitude.getU() >= alt) && (altitude.getV() <= alt);
			}
		},
		LOWER_STAGE_SEPARATION(trans.get("RecoveryDevice.DeployEvent.LOWER_STAGE_SEPARATION")) {
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.STAGE_SEPARATION)
					return false;
				
				int separation = e.getSource().getStageNumber();
				int current = source.getStageNumber();
				return (current + 1 == separation);
			}
		},
		NEVER(trans.get("RecoveryDevice.DeployEvent.NEVER")) {
			@Override
			public boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source) {
				return false;
			}
		};
		
		private final String description;
		
		DeployEvent(String description) {
			this.description = description;
		}
		
		public abstract boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e, RocketComponent source);
		
		@Override
		public String toString() {
			return description;
		}
		
	}
	
	
	private static final Translator trans = Application.getTranslator();
	
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	private DeployEvent deployEvent = DeployEvent.EJECTION;
	private double deployAltitude = 200;
	private double deployDelay = 0;
	
	public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
		return deployEvent.isActivationEvent(this, e, source);
	}
	
	public DeployEvent getDeployEvent() {
		return deployEvent;
	}
	
	public void setDeployEvent(DeployEvent deployEvent) {
		if (this.deployEvent == deployEvent) {
			return;
		}
		if (deployEvent == null) {
			throw new NullPointerException("deployEvent is null");
		}
		this.deployEvent = deployEvent;
		fireChangeEvent();
	}
	
	public double getDeployAltitude() {
		return deployAltitude;
	}
	
	public void setDeployAltitude(double deployAltitude) {
		if (MathUtil.equals(this.deployAltitude, deployAltitude)) {
			return;
		}
		this.deployAltitude = deployAltitude;
		fireChangeEvent();
	}
	
	public double getDeployDelay() {
		return deployDelay;
	}
	
	public void setDeployDelay(double deployDelay) {
		if (MathUtil.equals(this.deployDelay, deployDelay)) {
			return;
		}
		this.deployDelay = deployDelay;
		fireChangeEvent();
	}
	
	@Override
	public String toString() {
		String description = deployEvent.toString();
		if (deployDelay > 0) {
			description += " + " + deployDelay + "s";
		}
		if (deployEvent == DeployEvent.ALTITUDE && deployAltitude != 0) {
			description += " " + UnitGroup.UNITS_DISTANCE.toString(deployAltitude);
		}
		return description;
	}
	
	
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	
	
	private void fireChangeEvent() {
		EventObject event = new EventObject(this);
		Object[] list = listeners.toArray();
		for (Object l : list) {
			((StateChangeListener) l).stateChanged(event);
		}
	}
	
	
	@Override
	public DeploymentConfiguration clone() {
		DeploymentConfiguration that = new DeploymentConfiguration();
		that.deployAltitude = this.deployAltitude;
		that.deployDelay = this.deployDelay;
		that.deployEvent = this.deployEvent;
		return that;
	}
	
	
}