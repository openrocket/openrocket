package net.sf.openrocket.simulation.extension.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.unit.UnitGroup;

public class DampingMoment extends AbstractSimulationExtension {
	private static final Logger log = LoggerFactory.getLogger(DampingMoment.class);
	
	// Save it as a FlightDataType
	private static final FlightDataType cdm = FlightDataType.getType("Damping moment coefficient", "Cdm", UnitGroup.UNITS_COEFFICIENT);
	private static final ArrayList<FlightDataType> types = new ArrayList<FlightDataType>();

	DampingMoment() {
		types.add(cdm);
	}

	@Override
	public List<FlightDataType> getFlightDataTypes() {
		return types;
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		log.debug("initializing...");
		conditions.getSimulationListenerList().add(new DampingMomentListener());
	}

	@Override
	public String getName() {
		return "Damping Moment Coeficient(Cdm)";
	}
	
	@Override
	public String getDescription() {
		return "Calculate damping moment coefficient after every simulation step and publish to FlightData as Cdm";
	}

	private class DampingMomentListener extends AbstractSimulationListener {
	
		@Override
		public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) throws SimulationException {
			
			//status.getFlightData().setValue(cdm, aerodynamicPart + propulsivePart);
			status.getFlightData().setValue(cdm, calculate(status, flightConditions));
			
			return flightConditions;
		}
		
		private double calculate(SimulationStatus status, FlightConditions flightConditions) {
			
			// Work out the propulsive/jet damping part of the moment.
			
			// dm/dt = (thrust - ma)/v
			FlightDataBranch data = status.getFlightData();
			
			List<Double> mpAll = data.get(FlightDataType.TYPE_MOTOR_MASS);
			List<Double> time = data.get(FlightDataType.TYPE_TIME);
			if (mpAll == null || time == null) {
				return Double.NaN;
			}
			
			int len = mpAll.size();
			
			// This isn't as accurate as I would like
			double mdot = Double.NaN;
			if (len > 2) {
				// Using polynomial interpolator for derivative. Doesn't help much
				//double[] x = { time.get(len-5), time.get(len-4), time.get(len-3), time.get(len-2), time.get(len-1) };
				//double[] y = { mpAll.get(len-5), mpAll.get(len-4), mpAll.get(len-3), mpAll.get(len-2), mpAll.get(len-1) };
				//PolyInterpolator interp = new PolyInterpolator(x);
				//double[] coeff = interp.interpolator(y);
				//double dt = .01;
				//mdot = (interp.eval(x[4], coeff) - interp.eval(x[4]-dt, coeff))/dt; 
				
				mdot = (mpAll.get(len - 1) - mpAll.get(len - 2)) / (time.get(len - 1) - time.get(len - 2));
			}
			
			double cg = data.getLast(FlightDataType.TYPE_CG_LOCATION);
			
			// find the maximum distance from nose to nozzle. 
			double nozzleDistance = 0;
			FlightConfiguration config = status.getConfiguration();
			for (MotorConfiguration inst : config.getActiveMotors()) {
				double x_position= inst.getX();
				double x = x_position + inst.getMotor().getLaunchCGx();
				if (x > nozzleDistance) {
					nozzleDistance = x;
				}
			}
			
			// now can get the propulsive part
			double propulsivePart = mdot * Math.pow(nozzleDistance - cg, 2);
			
			// Work out the aerodynamic part of the moment.
			double aerodynamicPart = 0;
			
			AerodynamicCalculator aerocalc = status.getSimulationConditions().getAerodynamicCalculator();
			
			// Must go through each component ...
			Map<RocketComponent, AerodynamicForces> forces = aerocalc.getForceAnalysis(status.getConfiguration(), flightConditions, status.getWarnings());
			for (Map.Entry<RocketComponent, AerodynamicForces> entry : forces.entrySet()) {
				
				RocketComponent comp = entry.getKey();
				
				if (!comp.isAerodynamic())
					continue;
				
				//System.out.println(comp.toString());
				
				double CNa = entry.getValue().getCNa(); //?
				double Cp = entry.getValue().getCP().length();
				double z = comp.getAxialOffset();
				
				aerodynamicPart += CNa * Math.pow(z - Cp, 2);
			}
			
			double v = flightConditions.getVelocity();
			double rho = flightConditions.getAtmosphericConditions().getDensity();
			double ar = flightConditions.getRefArea();
			
			aerodynamicPart = aerodynamicPart * .5 * rho * v * ar;
			
			return aerodynamicPart + propulsivePart;
		}
	}
	
}
