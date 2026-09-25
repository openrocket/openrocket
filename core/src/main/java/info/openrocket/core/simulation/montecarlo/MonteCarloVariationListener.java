package info.openrocket.core.simulation.montecarlo;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

/**
 * Applies sampled model uncertainties without mutating the rocket document.
 * Multipliers are fixed for one trajectory so the sampled vehicle remains internally
 * consistent throughout that flight.
 */
final class MonteCarloVariationListener extends AbstractSimulationListener {
	private static final double MIN_MULTIPLIER = 0.01;
	private static final double MIN_EVENT_DELAY = 0.001;

	private final double densityMultiplier;
	private final double massMultiplier;
	private final double axialCgOffset;
	private final double dragMultiplier;
	private final double normalForceMultiplier;
	private final double thrustMultiplier;
	private final double ignitionDelayOffset;
	private final double recoveryDragMultiplier;
	private final double deploymentDelayOffset;

	private boolean reschedulingEvent;

	MonteCarloVariationListener(MonteCarloSample sample) {
		this.densityMultiplier = multiplier(sample, MonteCarloParameter.AIR_DENSITY);
		this.massMultiplier = multiplier(sample, MonteCarloParameter.TOTAL_MASS);
		this.axialCgOffset = sample.getVariation(MonteCarloParameter.CG_AXIAL);
		this.dragMultiplier = multiplier(sample, MonteCarloParameter.AXIAL_DRAG);
		this.normalForceMultiplier = multiplier(sample, MonteCarloParameter.NORMAL_FORCE);
		this.thrustMultiplier = multiplier(sample, MonteCarloParameter.THRUST);
		this.ignitionDelayOffset = sample.getVariation(MonteCarloParameter.IGNITION_DELAY);
		this.recoveryDragMultiplier = multiplier(sample, MonteCarloParameter.RECOVERY_DRAG);
		this.deploymentDelayOffset = sample.getVariation(MonteCarloParameter.DEPLOYMENT_DELAY);
	}

	@Override
	public AtmosphericConditions postAtmosphericModel(SimulationStatus status,
			AtmosphericConditions atmosphericConditions) {
		if (densityMultiplier == 1) {
			return null;
		}
		AtmosphericConditions varied = atmosphericConditions.clone();
		// At a fixed temperature, pressure scales density linearly through the ideal gas law.
		varied.setPressure(atmosphericConditions.getPressure() * densityMultiplier);
		return varied;
	}

	@Override
	public RigidBody postMassCalculation(SimulationStatus status, RigidBody massData) {
		if (massMultiplier == 1 && axialCgOffset == 0) {
			return null;
		}

		CoordinateIF center = massData.getCenterOfMass();
		Coordinate variedCenter = new Coordinate(center.getX() + axialCgOffset,
				center.getY(), center.getZ(), center.getWeight() * massMultiplier);
		return new RigidBody(variedCenter, massData.getIxx() * massMultiplier,
				massData.getIyy() * massMultiplier, massData.getIzz() * massMultiplier);
	}

	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) {
		double effectiveDragMultiplier = dragMultiplier;
		if (!status.getDeployedRecoveryDevices().isEmpty()) {
			effectiveDragMultiplier *= recoveryDragMultiplier;
		}

		if (effectiveDragMultiplier == 1 && normalForceMultiplier == 1) {
			return null;
		}

		if (effectiveDragMultiplier != 1) {
			forces.setCDaxial(scale(forces.getCDaxial(), effectiveDragMultiplier));
			forces.setCD(scale(forces.getCD(), effectiveDragMultiplier));
			forces.setPressureCD(scale(forces.getPressureCD(), effectiveDragMultiplier));
			forces.setBaseCD(scale(forces.getBaseCD(), effectiveDragMultiplier));
			forces.setFrictionCD(scale(forces.getFrictionCD(), effectiveDragMultiplier));
			forces.setOverrideCD(scale(forces.getOverrideCD(), effectiveDragMultiplier));
		}

		if (normalForceMultiplier != 1) {
			CoordinateIF centerOfPressure = forces.getCP();
			forces.setCP(new Coordinate(centerOfPressure.getX(), centerOfPressure.getY(), centerOfPressure.getZ(),
					centerOfPressure.getWeight() * normalForceMultiplier));
			forces.setCN(scale(forces.getCN(), normalForceMultiplier));
			forces.setCm(scale(forces.getCm(), normalForceMultiplier));
			forces.setCside(scale(forces.getCside(), normalForceMultiplier));
			forces.setCyaw(scale(forces.getCyaw(), normalForceMultiplier));
			forces.setPitchDampingMoment(scale(forces.getPitchDampingMoment(), normalForceMultiplier));
			forces.setYawDampingMoment(scale(forces.getYawDampingMoment(), normalForceMultiplier));
		}
		return forces;
	}

	@Override
	public double postSimpleThrustCalculation(SimulationStatus status, double thrust) {
		if (thrustMultiplier == 1) {
			return Double.NaN;
		}
		return Math.max(0, thrust * thrustMultiplier);
	}

	@Override
	public boolean addFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		if (reschedulingEvent) {
			return true;
		}

		double offset;
		if (event.getType() == FlightEvent.Type.IGNITION) {
			// Shifting the pad ignition only translates the flight in time, so apply the
			// variation to air-started and upper-stage motors.
			offset = status.isLiftoff() ? ignitionDelayOffset : 0;
		} else if (event.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
			offset = deploymentDelayOffset;
		} else {
			return true;
		}

		if (offset == 0) {
			return true;
		}

		// Re-entering addEvent is intentional. The guard lets the replacement event pass
		// through this listener while retaining the original motor/deployment payload.
		reschedulingEvent = true;
		try {
			// Events cannot be moved before the step that triggered them. This truncates a
			// negative offset when deployment is triggered immediately at apogee.
			double earliestTime = status.getSimulationTime() + MIN_EVENT_DELAY;
			double variedTime = Math.max(earliestTime, event.getTime() + offset);
			status.addEvent(new FlightEvent(event.getType(), variedTime, event.getSource(), event.getData()));
		} finally {
			reschedulingEvent = false;
		}
		return false;
	}

	@Override
	public boolean isSystemListener() {
		return true;
	}

	private static double multiplier(MonteCarloSample sample, MonteCarloParameter parameter) {
		return Math.max(MIN_MULTIPLIER, 1 + sample.getVariation(parameter));
	}

	private static double scale(double value, double multiplier) {
		return Double.isFinite(value) ? value * multiplier : value;
	}
}
