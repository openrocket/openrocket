package info.openrocket.core.aerodynamics;

import static info.openrocket.core.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import info.openrocket.core.aerodynamics.barrowman.RocketComponentCalc;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.ExternalComponent.Finish;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.PolyInterpolator;
import info.openrocket.core.util.Reflection;

/**
 * Drag portion of the extended Barrowman aerodynamic calculator.
 */
public class BarrowmanDragCalculator implements DragCalculator {

	private static final String BARROWMAN_PACKAGE = "info.openrocket.core.aerodynamics.barrowman";
	private static final String BARROWMAN_SUFFIX = "Calc";

	private final WarningSet ignoreWarningSet = new WarningSet();

	private Map<RocketComponent, RocketComponentCalc> calcMap = null;

	private static final double[] axialDragPoly1;
	private static final double[] axialDragPoly2;

	static {
		PolyInterpolator interpolator;
		interpolator = new PolyInterpolator(
				new double[] { 0, 17 * Math.PI / 180 },
				new double[] { 0, 17 * Math.PI / 180 });
		axialDragPoly1 = interpolator.interpolator(1, 1.3, 0, 0);

		interpolator = new PolyInterpolator(
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { Math.PI / 2 });
		axialDragPoly2 = interpolator.interpolator(1.3, 0, 0, 0, 0);
	}

	@Override
	public DragCalculator newInstance() {
		return new BarrowmanDragCalculator();
	}

	@Override
	public void calculateDrag(FlightConfiguration configuration,
			FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> componentForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces,
			AerodynamicForces totalForces,
			WarningSet warnings) {
		ensureCalcMap(configuration);
		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;

		double frictionCD = calculateFrictionCD(configuration, conditions, componentForces, actualWarnings);
		double pressureCD = calculatePressureCD(configuration, conditions, componentForces, actualWarnings);
		double baseCD = calculateBaseCD(configuration, conditions, componentForces, actualWarnings);
		double overrideCD = calculateOverrideCD(configuration, componentForces, assemblyForces);

		totalForces.setFrictionCD(frictionCD);
		totalForces.setPressureCD(pressureCD);
		totalForces.setBaseCD(baseCD);
		totalForces.setOverrideCD(overrideCD);
		totalForces.setCD(frictionCD + pressureCD + baseCD + overrideCD);
		totalForces.setCDaxial(calculateAxialCD(conditions, totalForces.getCD()));
	}

	@Override
	public double toAxialDrag(FlightConditions conditions, double cd) {
		return calculateAxialCD(conditions, cd);
	}

	@Override
	public void voidAerodynamicCache() {
		calcMap = null;
	}

	private double calculateFrictionCD(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {
		double mach = conditions.getMach();
		double Re = calculateReynoldsNumber(configuration, conditions);
		double Cf = calculateFrictionCoefficient(configuration, mach, Re);
		double roughnessCorrection = calculateRoughnessCorrection(mach);

		ensureCalcMap(configuration);

		double otherFrictionCD = 0;
		double bodyFrictionCD = 0;
		double maxR = 0;
		double minX = Double.MAX_VALUE;
		double maxX = 0;

		double[] roughnessLimited = new double[Finish.values().length];
		Arrays.fill(roughnessLimited, Double.NaN);

		InstanceMap imap = configuration.getActiveInstances();
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			RocketComponent c = entry.getKey();

			if (!c.isAerodynamic()) {
				continue;
			}

			if (c.isCDOverridden() || c.isCDOverriddenByAncestor()) {
				continue;
			}

			Finish finish = ((ExternalComponent) c).getFinish();
			if (Double.isNaN(roughnessLimited[finish.ordinal()])) {
				roughnessLimited[finish.ordinal()] = 0.032
						* Math.pow(finish.getRoughnessSize() / configuration.getLengthAerodynamic(), 0.2)
						* roughnessCorrection;
			}

			double componentCf;
			if (configuration.getRocket().isPerfectFinish()) {
				if ((Re > 1.0e6) && (roughnessLimited[finish.ordinal()] > Cf)) {
					componentCf = roughnessLimited[finish.ordinal()];
				} else {
					componentCf = Cf;
				}
			} else {
				componentCf = Math.max(Cf, roughnessLimited[finish.ordinal()]);
			}

			double componentFrictionCD = calcMap.get(c).calculateFrictionCD(conditions, componentCf, warningSet);
			int instanceCount = entry.getValue().size();

			if (c instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) c;

				bodyFrictionCD += instanceCount * componentFrictionCD;

				double componentMinX = c.getAxialOffset(AxialMethod.ABSOLUTE);
				minX = Math.min(minX, componentMinX);

				double componentMaxX = componentMinX + c.getLength();
				maxX = Math.max(maxX, componentMaxX);

				double componentMaxR = Math.max(s.getForeRadius(), s.getAftRadius());
				maxR = Math.max(maxR, componentMaxR);

			} else {
				otherFrictionCD += instanceCount * componentFrictionCD;
			}

			if (forceMap != null && forceMap.get(c) != null) {
				forceMap.get(c).setFrictionCD(componentFrictionCD);
			}
		}

		double fB = (maxX - minX + 0.0001) / maxR;
		double correction = (1 + 1.0 / (2 * fB));

		if (forceMap != null) {
			for (Map.Entry<RocketComponent, AerodynamicForces> entry : forceMap.entrySet()) {
				if (entry.getKey() instanceof SymmetricComponent) {
					entry.getValue().setFrictionCD(entry.getValue().getFrictionCD() * correction);
				}
			}
		}

		return otherFrictionCD + correction * bodyFrictionCD;
	}

	private double calculateReynoldsNumber(FlightConfiguration configuration, FlightConditions conditions) {
		return conditions.getVelocity() * configuration.getLengthAerodynamic() /
				conditions.getAtmosphericConditions().getKinematicViscosity();
	}

	private double calculateFrictionCoefficient(FlightConfiguration configuration, double mach, double Re) {
		double Cf;
		double c1 = 1.0;
		double c2 = 1.0;

		if (configuration.getRocket().isPerfectFinish()) {
			if (Re < 1.0e4) {
				Cf = 1.33e-2;
			} else if (Re < 5.39e5) {
				Cf = 1.328 / MathUtil.safeSqrt(Re);
			} else {
				Cf = 1.0 / pow2(1.50 * Math.log(Re) - 5.6) - 1700 / Re;
			}

			if (mach < 1.1) {
				if (Re > 1.0e6) {
					if (Re < 3.0e6) {
						c1 = 1 - 0.1 * pow2(mach) * (Re - 1.0e6) / 2.0e6;
					} else {
						c1 = 1 - 0.1 * pow2(mach);
					}
				}
			}
			if (mach > 0.9) {
				if (Re > 1.0e6) {
					if (Re < 3.0e6) {
						c2 = 1 + (1.0 / Math.pow(1 + 0.045 * pow2(mach), 0.25) - 1) * (Re - 1.0e6) / 2.0e6;
					} else {
						c2 = 1.0 / Math.pow(1 + 0.045 * pow2(mach), 0.25);
					}
				}
			}

			if (mach < 0.9) {
				Cf *= c1;
			} else if (mach < 1.1) {
				Cf *= (c2 * (mach - 0.9) / 0.2 + c1 * (1.1 - mach) / 0.2);
			} else {
				Cf *= c2;
			}

		} else {
			if (Re < 1.0e4) {
				Cf = 1.48e-2;
			} else {
				Cf = 1.0 / pow2(1.50 * Math.log(Re) - 5.6);
			}

			if (mach < 1.1) {
				c1 = 1 - 0.1 * pow2(mach);
			}
			if (mach > 0.9) {
				c2 = 1 / Math.pow(1 + 0.15 * pow2(mach), 0.58);
			}
			if (mach < 0.9) {
				Cf *= c1;
			} else if (mach < 1.1) {
				Cf *= c2 * (mach - 0.9) / 0.2 + c1 * (1.1 - mach) / 0.2;
			} else {
				Cf *= c2;
			}
		}

		return Cf;
	}

	private double calculateRoughnessCorrection(double mach) {
		double roughnessCorrection;
		if (mach < 0.9) {
			roughnessCorrection = 1 - 0.1 * pow2(mach);
		} else if (mach > 1.1) {
			roughnessCorrection = 1 / (1 + 0.18 * pow2(mach));
		} else {
			double c1 = 1 - 0.1 * pow2(0.9);
			double c2 = 1.0 / (1 + 0.18 * pow2(1.1));
			roughnessCorrection = c2 * (mach - 0.9) / 0.2 + c1 * (1.1 - mach) / 0.2;
		}
		return roughnessCorrection;
	}

	private double calculatePressureCD(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {
		ensureCalcMap(configuration);

		double stagnation = calculateStagnationCD(conditions.getMach());
		double base = calculateBaseCD(conditions.getMach());

		double total = 0;
		InstanceMap imap = configuration.getActiveInstances();
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			RocketComponent c = entry.getKey();

			if (!c.isAerodynamic()) {
				continue;
			}

			if (c.isCDOverridden() || c.isCDOverriddenByAncestor()) {
				continue;
			}

			int instanceCount = entry.getValue().size();

			double cd = calcMap.get(c).calculatePressureCD(conditions, stagnation, base, warningSet);

			if (forceMap != null && forceMap.get(c) != null) {
				forceMap.get(c).setPressureCD(cd);
			}

			total += cd * instanceCount;

			if (c instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) c;
				double foreRadius = s.getForeRadius();
				double aftRadius = s.getAftRadius();
				if (s.getLength() == 0) {
					foreRadius = Math.max(foreRadius, aftRadius);
				}
				double radius = 0;
				SymmetricComponent prevComponent = s.getPreviousSymmetricComponent();
				if (prevComponent != null && configuration.isComponentActive(prevComponent)) {
					radius = prevComponent.getAftRadius();
				}

				if (radius < foreRadius) {
					double area = Math.PI * (pow2(foreRadius) - pow2(radius));
					double diskCd = stagnation * area / conditions.getRefArea();
					total += instanceCount * diskCd;

					if (forceMap != null && forceMap.get(c) != null) {
						forceMap.get(c).setPressureCD(forceMap.get(c).getPressureCD() + diskCd);
					}
				}
			}
		}

		return total;
	}

	private double calculateBaseCD(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {
		ensureCalcMap(configuration);

		double base = calculateBaseCD(conditions.getMach());
		double total = 0;

		InstanceMap imap = configuration.getActiveInstances();
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			RocketComponent c = entry.getKey();

			if (!(c instanceof SymmetricComponent)) {
				continue;
			}

			if (c.isCDOverridden() || c.isCDOverriddenByAncestor()) {
				continue;
			}

			SymmetricComponent s = (SymmetricComponent) c;
			double foreRadius = s.getForeRadius();
			double aftRadius = s.getAftRadius();
			if (s.getLength() == 0) {
				double componentMaxR = Math.max(foreRadius, aftRadius);
				foreRadius = componentMaxR;
				aftRadius = componentMaxR;
			}

			int instanceCount = entry.getValue().size();

			SymmetricComponent nextComponent = s.getNextSymmetricComponent();
			double nextRadius;
			if ((nextComponent != null) && configuration.isComponentActive(nextComponent)) {
				nextRadius = nextComponent.getForeRadius();
			} else {
				nextRadius = 0.0;
			}

			if (nextRadius < aftRadius) {
				double area = Math.PI * (pow2(aftRadius) - pow2(nextRadius));
				double cd = base * area / conditions.getRefArea();
				total += instanceCount * cd;
				if (forceMap != null && forceMap.get(s) != null) {
					forceMap.get(s).setBaseCD(cd);
				}
			}
		}

		return total;
	}

	private double calculateOverrideCD(FlightConfiguration configuration,
			Map<RocketComponent, AerodynamicForces> componentForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces) {
		ensureCalcMap(configuration);

		double total = 0;
		InstanceMap imap = configuration.getActiveInstances();
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			RocketComponent c = entry.getKey();
			int instanceCount = entry.getValue().size();

			if (!c.isAerodynamic() && !(c instanceof ComponentAssembly)) {
				continue;
			}

			if (c.isCDOverridden() && !c.isCDOverriddenByAncestor()) {
				double cd = instanceCount * c.getOverrideCD();
				Map<RocketComponent, AerodynamicForces> targetMap = (c instanceof ComponentAssembly) ? assemblyForces
						: componentForces;
				if (targetMap != null && targetMap.get(c) != null) {
					targetMap.get(c).setOverrideCD(cd);
				}
				total += cd;
			}
		}

		return total;
	}

	private double calculateAxialCD(FlightConditions conditions, double cd) {
		double aoa = MathUtil.clamp(conditions.getAOA(), 0, Math.PI);
		double mul;

		if (aoa > Math.PI / 2) {
			aoa = Math.PI - aoa;
		}
		if (aoa < 17 * Math.PI / 180) {
			mul = PolyInterpolator.eval(aoa, axialDragPoly1);
		} else {
			mul = PolyInterpolator.eval(aoa, axialDragPoly2);
		}

		if (conditions.getAOA() < Math.PI / 2) {
			return mul * cd;
		}
		return -mul * cd;
	}

	public static double calculateStagnationCD(double m) {
		double pressure;
		if (m <= 1) {
			pressure = 1 + pow2(m) / 4 + pow2(pow2(m)) / 40;
		} else {
			pressure = 1.84 - 0.76 / pow2(m) + 0.166 / pow2(pow2(m)) + 0.035 / pow2(m * m * m);
		}
		return 0.85 * pressure;
	}

	public static double calculateBaseCD(double m) {
		if (m <= 1) {
			return 0.12 + 0.13 * m * m;
		}
		return 0.25 / m;
	}

	private void ensureCalcMap(FlightConfiguration configuration) {
		if (calcMap == null) {
			buildCalcMap(configuration);
		}
	}

	private void buildCalcMap(FlightConfiguration configuration) {
		calcMap = new HashMap<>();

		for (RocketComponent comp : configuration.getAllComponents()) {
			if (!comp.isAerodynamic() && !(comp instanceof ComponentAssembly)) {
				continue;
			}

			RocketComponentCalc calcObj = (RocketComponentCalc) Reflection.construct(
					BARROWMAN_PACKAGE,
					comp,
					BARROWMAN_SUFFIX,
					comp);

			calcMap.put(comp, calcObj);
		}
	}
}
