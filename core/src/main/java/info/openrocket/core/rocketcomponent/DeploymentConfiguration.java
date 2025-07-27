package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Pair;

import java.util.List;
import java.util.Objects;

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
				if ((e.getType() != FlightEvent.Type.ALTITUDE) || (e.getData() == null))
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

		public abstract boolean isActivationEvent(DeploymentConfiguration config, FlightEvent e,
				RocketComponent source);

		@Override
		public String toString() {
			return description;
		}

	}

	private static final Translator trans = Application.getTranslator();

	private DeployEvent deployEvent = DeployEvent.EJECTION;
	private double deployAltitude = 200;
	private double deployDelay = 0;

	private List<DeploymentConfiguration> configListeners = new ArrayList<>();

	public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
		return deployEvent.isActivationEvent(this, e, source);
	}

	public DeployEvent getDeployEvent() {
		return deployEvent;
	}

	public void setDeployEvent(DeployEvent deployEvent) {
		for (DeploymentConfiguration listener : configListeners) {
			listener.setDeployEvent(deployEvent);
		}

		if (this.deployEvent == deployEvent) {
			return;
		}
		if (deployEvent == null) {
			throw new NullPointerException("deployEvent is null");
		}
		this.deployEvent = deployEvent;
	}

	public double getDeployAltitude() {
		return deployAltitude;
	}

	public void setDeployAltitude(double deployAltitude) {
		for (DeploymentConfiguration listener : configListeners) {
			listener.setDeployAltitude(deployAltitude);
		}

		if (MathUtil.equals(this.deployAltitude, deployAltitude)) {
			return;
		}
		this.deployAltitude = deployAltitude;
	}

	public double getDeployDelay() {
		return deployDelay;
	}

	public void setDeployDelay(double deployDelay) {
		for (DeploymentConfiguration listener : configListeners) {
			listener.setDeployDelay(deployDelay);
		}

		if (MathUtil.equals(this.deployDelay, deployDelay)) {
			return;
		}
		this.deployDelay = deployDelay;
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
	public DeploymentConfiguration clone() {
		return copy(null);
	}

	public DeploymentConfiguration copy(final FlightConfigurationId copyId) {
		DeploymentConfiguration that = new DeploymentConfiguration();
		that.deployAltitude = this.deployAltitude;
		that.deployDelay = this.deployDelay;
		that.deployEvent = this.deployEvent;
		return that;
	}

	@Override
	public void update() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeploymentConfiguration that = (DeploymentConfiguration) o;
		return Double.compare(that.deployAltitude, deployAltitude) == 0
				&& Double.compare(that.deployDelay, deployDelay) == 0 && deployEvent == that.deployEvent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(deployEvent, deployAltitude, deployDelay);
	}

	/**
	 * Add a new config listener that will undergo the same configuration changes as
	 * this configuration.
	 * 
	 * @param listener new config listener
	 * @return true if listener was successfully added, false if not
	 */
	public boolean addConfigListener(DeploymentConfiguration listener) {
		if (listener == null) {
			return false;
		}
		configListeners.add(listener);
		return true;
	}

	public void removeConfigListener(DeploymentConfiguration listener) {
		configListeners.remove(listener);
	}

	public void clearConfigListeners() {
		configListeners.clear();
	}

	public List<DeploymentConfiguration> getConfigListeners() {
		return configListeners;
	}
}
