package info.openrocket.core.componentanalysis;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CAParameterSweep {
	private final CAParameters parameters;
	private final AerodynamicCalculator aerodynamicCalculator;
	private final Rocket rocket;

	public CAParameterSweep(CAParameters parameters, AerodynamicCalculator aerodynamicCalculator, Rocket rocket) {
		this.parameters = parameters.clone();
		this.aerodynamicCalculator = aerodynamicCalculator;
		this.rocket = rocket;
	}

	/**
	 * Perform a parameter sweep over the specified parameter type.
	 * @param sweepParameter the parameter to sweep (e.g. MACH)
	 * @param min the minimum value of the parameter
	 * @param max the maximum value of the parameter
	 * @param delta the step size of the parameter
	 * @return a data branch containing the results of the sweep
	 */
	public CADataBranch sweep(CADomainDataType sweepParameter, double min, double max, double delta, double initialValue) {
		List<Double> sweepValues = generateSweepValues(min, max, delta);
		CADataBranch dataBranch = new CADataBranch("Parameter Sweep");
		dataBranch.addType(sweepParameter);

		for (Double value : sweepValues) {
			setParameterValue(sweepParameter, value);
			FlightConditions conditions = createFlightConditions();
			Map<RocketComponent, AerodynamicForces> aeroData = aerodynamicCalculator.getForceAnalysis(rocket.getSelectedConfiguration(), conditions, new WarningSet());

			dataBranch.addPoint();
			addDomainData(dataBranch, sweepParameter, value);

			addComponentData(dataBranch, aeroData, value);
			addStabilityData(dataBranch, aeroData, value);
			addDragData(dataBranch, aeroData, value);
			addRollData(dataBranch, aeroData, value);
		}

		// Reset the parameter to its original value
		setParameterValue(sweepParameter, initialValue);

		return dataBranch;
	}

	private List<Double> generateSweepValues(double min, double max, double delta) {
		List<Double> values = new ArrayList<>();
		int scale = determineScale(delta);
		double multiplier = Math.pow(10, scale);

		for (double value = min; value <= max; value += delta) {
			double roundedValue = Math.round(value * multiplier) / multiplier;
			values.add(roundedValue);
		}
		return values;
	}

	private int determineScale(double delta) {
		String deltaStr = Double.toString(Math.abs(delta));
		int indexOfDecimal = deltaStr.indexOf(".");
		if (indexOfDecimal == -1) {
			return 0;
		}
		return deltaStr.length() - indexOfDecimal - 1;
	}

	private void setParameterValue(CADomainDataType parameterType, double value) {
		if (parameterType.equals(CADomainDataType.MACH)) {
			parameters.setMach(value);
		} else if (parameterType.equals(CADomainDataType.AOA)) {
			parameters.setAOA(value);
		} else if (parameterType.equals(CADomainDataType.ROLL_RATE)) {
			parameters.setRollRate(value);
		} else if (parameterType.equals(CADomainDataType.WIND_DIRECTION)) {
			parameters.setTheta(value);
		}
		// Add more cases here as more parameter types are implemented
		else {
			throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
		}
	}

	private FlightConditions createFlightConditions() {
		FlightConditions conditions = new FlightConditions(rocket.getSelectedConfiguration());
		conditions.setAOA(parameters.getAOA());
		conditions.setTheta(parameters.getTheta());
		conditions.setMach(parameters.getMach());
		conditions.setRollRate(parameters.getRollRate());
		return conditions;
	}

	private static void addDomainData(CADataBranch dataBranch, CADomainDataType sweepParameter, Double value) {
		dataBranch.setDomainValue(sweepParameter, value);
	}

	private void addComponentData(CADataBranch dataBranch, Map<RocketComponent, AerodynamicForces> aeroData, double sweepValue) {
		for (Map.Entry<RocketComponent, AerodynamicForces> entry : aeroData.entrySet()) {
			RocketComponent component = entry.getKey();
			AerodynamicForces forces = entry.getValue();

			if (forces == null) {
				dataBranch.setValue(CADataType.CP_X, component, 0.0);
				dataBranch.setValue(CADataType.CNa, component, 0.0);
				continue;
			}

			if (forces.getCP() != null) {
				double cpx = (component instanceof Rocket && forces.getCP().weight < MathUtil.EPSILON) ?
						Double.NaN : forces.getCP().x;
				dataBranch.setValue(CADataType.CP_X, component, cpx);
				dataBranch.setValue(CADataType.CNa, component, forces.getCP().weight);
			}

			if (!Double.isNaN(forces.getCD())) {
				dataBranch.setValue(CADataType.PRESSURE_CD, component, forces.getPressureCD());
				dataBranch.setValue(CADataType.BASE_CD, component, forces.getBaseCD());
				dataBranch.setValue(CADataType.FRICTION_CD, component, forces.getFrictionCD());
				dataBranch.setValue(CADataType.PER_INSTANCE_CD, component, forces.getCD());
				dataBranch.setValue(CADataType.TOTAL_CD, component, forces.getCDTotal());
			}

			if (component instanceof FinSet) {
				dataBranch.setValue(CADataType.ROLL_FORCING_COEFFICIENT, component, forces.getCrollForce());
				dataBranch.setValue(CADataType.ROLL_DAMPING_COEFFICIENT, component, forces.getCrollDamp());
				dataBranch.setValue(CADataType.TOTAL_ROLL_COEFFICIENT, component, forces.getCrollForce() + forces.getCrollDamp());
			}
		}
	}

	private void addStabilityData(CADataBranch dataBranch, Map<RocketComponent, AerodynamicForces> aeroData, double sweepValue) {
		AerodynamicForces totalForces = aeroData.get(rocket);
		if (totalForces != null && totalForces.getCP() != null) {
			dataBranch.setValue(CADataType.CP_X, rocket, totalForces.getCP().x);
			dataBranch.setValue(CADataType.CNa, rocket, totalForces.getCP().weight);
		}
	}

	private void addDragData(CADataBranch dataBranch, Map<RocketComponent, AerodynamicForces> aeroData, double sweepValue) {
		AerodynamicForces totalForces = aeroData.get(rocket);
		if (totalForces != null) {
			dataBranch.setValue(CADataType.PRESSURE_CD, rocket, totalForces.getPressureCD());
			dataBranch.setValue(CADataType.BASE_CD, rocket, totalForces.getBaseCD());
			dataBranch.setValue(CADataType.FRICTION_CD, rocket, totalForces.getFrictionCD());
			dataBranch.setValue(CADataType.PER_INSTANCE_CD, rocket, totalForces.getCD());
			dataBranch.setValue(CADataType.TOTAL_CD, rocket, totalForces.getCDTotal());
		}
	}

	private void addRollData(CADataBranch dataBranch, Map<RocketComponent, AerodynamicForces> aeroData, double sweepValue) {
		double totalRollForce = 0;
		double totalRollDamping = 0;

		for (Map.Entry<RocketComponent, AerodynamicForces> entry : aeroData.entrySet()) {
			if (entry.getKey() instanceof FinSet) {
				AerodynamicForces forces = entry.getValue();
				totalRollForce += forces.getCrollForce();
				totalRollDamping += forces.getCrollDamp();
			}
		}

		dataBranch.setValue(CADataType.ROLL_FORCING_COEFFICIENT, rocket, totalRollForce);
		dataBranch.setValue(CADataType.ROLL_DAMPING_COEFFICIENT, rocket, totalRollDamping);
		dataBranch.setValue(CADataType.TOTAL_ROLL_COEFFICIENT, rocket, totalRollForce + totalRollDamping);
	}
}
