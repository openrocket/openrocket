package info.openrocket.core.aerodynamics;

import static info.openrocket.core.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import info.openrocket.core.aerodynamics.barrowman.FinSetCalc;
import info.openrocket.core.aerodynamics.barrowman.RocketComponentCalc;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Reflection;

/**
 * Stability portion of the extended Barrowman aerodynamic calculator.
 */
public class BarrowmanStabilityCalculator implements StabilityCalculator {

	private static final double STALL_ANGLE = 17.5 * Math.PI / 180;
	private static final String BARROWMAN_PACKAGE = "info.openrocket.core.aerodynamics.barrowman";
	private static final String BARROWMAN_SUFFIX = "Calc";

	private final WarningSet ignoreWarningSet = new WarningSet();

	private Map<RocketComponent, RocketComponentCalc> calcMap = null;
	private double cacheDiameter = -1;
	private double cacheLength = -1;
	private double stallMargin;

	@Override
	public StabilityCalculator newInstance() {
		return new BarrowmanStabilityCalculator();
	}

	@Override
	public double getStallMargin() {
		return stallMargin;
	}

	@Override
	public Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions, WarningSet warnings) {
		return calculateNonAxialForces(configuration, conditions, warnings).getCP();
	}

	@Override
	public AerodynamicForces calculateNonAxialForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		ensureCalcMap(configuration);

		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;
		checkGeometry(configuration, configuration.getRocket(), actualWarnings);

		InstanceMap imap = configuration.getActiveInstances();
		AerodynamicForces assemblyForces = new AerodynamicForces().zero();

		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			RocketComponent comp = entry.getKey();
			List<InstanceContext> contextList = entry.getValue();

			RocketComponentCalc calcObj = calcMap.get(comp);
			if (calcObj == null) {
				continue;
			}

			AerodynamicForces componentForces = calculateComponentNonAxialForces(conditions, comp, calcObj,
					contextList, actualWarnings);
			assemblyForces.merge(componentForces);
		}

		return assemblyForces;
	}

	@Override
	public StabilityForceBreakdown getForceAnalysis(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		ensureCalcMap(configuration);

		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;
		InstanceMap instances = configuration.getActiveInstances();

		Map<RocketComponent, AerodynamicForces> eachMap = new LinkedHashMap<>();
		Map<RocketComponent, AerodynamicForces> assemblyMap = new LinkedHashMap<>();

		calculateForceAnalysis(configuration, conditions, configuration.getRocket(), instances, eachMap,
				assemblyMap, actualWarnings);

		return new StabilityForceBreakdown(eachMap, assemblyMap);
	}

	@Override
	public void calculateDampingMoments(FlightConfiguration configuration, FlightConditions conditions,
			AerodynamicForces total) {
		ensureCalcMap(configuration);

		double mul = getDampingMultiplier(configuration, conditions, conditions.getPitchCenter().x);
		double pitchRate = conditions.getPitchRate();
		double yawRate = conditions.getYawRate();
		double velocity = conditions.getVelocity();

		mul *= 3; // Higher damping yields much more realistic apogee turn

		double pitchDampingMomentMagnitude = MathUtil.min(mul * pow2(pitchRate / velocity), total.getCm());
		double yawDampingMomentMagnitude = MathUtil.min(mul * pow2(yawRate / velocity), total.getCyaw());

		total.setPitchDampingMoment(MathUtil.sign(pitchRate) * pitchDampingMomentMagnitude);
		total.setYawDampingMoment(MathUtil.sign(yawRate) * yawDampingMomentMagnitude);

		stallMargin = STALL_ANGLE - conditions.getAOA();
	}

	@Override
	public void checkGeometry(FlightConfiguration configuration, RocketComponent component, WarningSet warnings) {
		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;

		Queue<RocketComponent> queue = new LinkedList<>();
		addDirectChildStagesToQueue(configuration, queue, component);

		SymmetricComponent prevComp = null;
		if ((component instanceof ComponentAssembly) &&
				(!(component instanceof Rocket)) &&
				(component.getChildCount() > 0)) {
			prevComp = ((SymmetricComponent) (component.getChild(0))).getPreviousSymmetricComponent();
		}

		while (queue.peek() != null) {
			RocketComponent comp = queue.poll();
			if ((comp instanceof SymmetricComponent) ||
					((comp instanceof AxialStage) &&
							!(comp instanceof ParallelStage))) {
				addDirectChildStagesToQueue(configuration, queue, comp);

				if (comp instanceof SymmetricComponent) {
					SymmetricComponent sym = (SymmetricComponent) comp;
					if (prevComp == null) {
						if (sym.getForeRadius() - sym.getThickness() > MathUtil.EPSILON) {
							actualWarnings.add(Warning.OPEN_AIRFRAME_FORWARD, sym);
						}
					} else {
						if (!UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(2.0 * sym.getForeRadius())
								.equals(UnitGroup.UNITS_LENGTH.getDefaultUnit()
										.toStringUnit(2.0 * prevComp.getAftRadius()))) {
							actualWarnings.add(Warning.DIAMETER_DISCONTINUITY, prevComp, sym);
						}

						if ((sym.getLength() < MathUtil.EPSILON) ||
							(sym.getAftRadius() < MathUtil.EPSILON && sym.getForeRadius() < MathUtil.EPSILON)) {
							actualWarnings.add(Warning.ZERO_VOLUME_BODY, sym);
						}

						double symXfore = sym.toAbsolute(Coordinate.NUL)[0].x;
						double prevXfore = prevComp.toAbsolute(Coordinate.NUL)[0].x;

						double symXaft = sym.toAbsolute(new Coordinate(comp.getLength(), 0, 0, 0))[0].x;
						double prevXaft = prevComp.toAbsolute(new Coordinate(prevComp.getLength(), 0, 0, 0))[0].x;

						if (!UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(symXfore)
								.equals(UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(prevXaft))) {
							if (symXfore > prevXaft) {
								actualWarnings.add(Warning.AIRFRAME_GAP, prevComp, sym);
							} else {
								if ((symXfore >= prevXfore) &&
										((symXaft >= prevXaft) || (sym.getNextSymmetricComponent() == null))) {
									actualWarnings.add(Warning.AIRFRAME_OVERLAP, prevComp, sym);
								} else {
									SymmetricComponent firstComp = prevComp;
									SymmetricComponent scout = prevComp;
									while (scout != null) {
										firstComp = scout;
										scout = scout.getPreviousSymmetricComponent();
									}
									double firstCompXfore = firstComp.toAbsolute(Coordinate.NUL)[0].x;

									SymmetricComponent lastComp = sym;
									scout = sym;
									while (scout != null) {
										lastComp = scout;
										scout = scout.getNextSymmetricComponent();
									}
									double lastCompXaft = lastComp
											.toAbsolute(new Coordinate(lastComp.getLength(), 0, 0, 0))[0].x;

									if (lastCompXaft <= firstCompXfore) {
										actualWarnings.add(Warning.PODSET_FORWARD, comp.getParent());
									} else {
										actualWarnings.add(Warning.PODSET_OVERLAP, comp.getParent());
									}
								}

							}
						} else {
							RocketComponent prevCompParent = prevComp.getParent();
							RocketComponent compParent = comp.getParent();
							int prevCompPos = prevCompParent.getChildPosition(prevComp);
							RocketComponent nextComp = prevCompPos + 1 >= prevCompParent.getChildCount() ? null
									: prevCompParent.getChild(prevCompPos + 1);
							if ((compParent instanceof PodSet || compParent instanceof ParallelStage) &&
									MathUtil.equals(symXfore, prevXaft) && (compParent.getParent() == nextComp)) {
								actualWarnings.add(Warning.PODSET_OVERLAP, comp.getParent());
							}
						}
					}
					prevComp = sym;
				}
			} else if ((comp instanceof PodSet) || (comp instanceof ParallelStage)) {
				checkGeometry(configuration, comp, actualWarnings);
			}
		}
	}

	@Override
	public void voidAerodynamicCache() {
		calcMap = null;
		cacheDiameter = -1;
		cacheLength = -1;
		stallMargin = 0;
	}

	private AerodynamicForces calculateComponentNonAxialForces(FlightConditions conditions, RocketComponent comp,
			RocketComponentCalc calcObj, List<InstanceContext> contextList, WarningSet warnings) {
		AerodynamicForces componentForces = new AerodynamicForces().zero();

		for (InstanceContext context : contextList) {
			AerodynamicForces instanceForces = new AerodynamicForces().zero();
			calcObj.calculateNonaxialForces(conditions, context.transform, instanceForces, warnings);

			Coordinate cpInst = instanceForces.getCP();
			Coordinate cpAbs = context.transform.transform(cpInst);
			cpAbs = cpAbs.setY(0.0).setZ(0.0);

			instanceForces.setCP(cpAbs);
			double cNInst = instanceForces.getCN();
			instanceForces.setCm(cNInst * instanceForces.getCP().x / conditions.getRefLength());

			componentForces.merge(instanceForces);
		}

		componentForces.setComponent(comp);

		return componentForces;
	}

	private AerodynamicForces calculateForceAnalysis(FlightConfiguration configuration, FlightConditions conds,
			RocketComponent comp,
			InstanceMap instances,
			Map<RocketComponent, AerodynamicForces> eachForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces,
			WarningSet warnings) {
		AerodynamicForces aggregateForces = new AerodynamicForces().zero();
		aggregateForces.setComponent(comp);

		if (comp.isAerodynamic() || comp instanceof ComponentAssembly) {
			RocketComponentCalc calcObj = calcMap.get(comp);
			if (calcObj == null) {
				throw new NullPointerException("Could not find a CalculationObject for aerodynamic Component!: "
						+ comp.getComponentName());
			} else {
				List<InstanceContext> contextList = instances.get(comp);
				AerodynamicForces compForces = calculateComponentNonAxialForces(conds, comp, calcObj, contextList,
						warnings);
				eachForces.put(comp, compForces);
				aggregateForces.merge(compForces);
			}
		}

		for (RocketComponent child : comp.getChildren()) {
			if (child instanceof AxialStage && !configuration.isStageActive(child.getStageNumber())) {
				for (AxialStage childStage : child.getTopLevelChildStages()) {
					if (configuration.isStageActive(childStage.getStageNumber())) {
						AerodynamicForces childForces = calculateForceAnalysis(configuration, conds, childStage, instances,
								eachForces, assemblyForces, warnings);
						if (childForces != null) {
							aggregateForces.merge(childForces);
						}
					}
				}
				continue;
			}

			AerodynamicForces childForces = calculateForceAnalysis(configuration, conds, child, instances, eachForces,
					assemblyForces, warnings);

			if (childForces != null) {
				aggregateForces.merge(childForces);
			}
		}

		assemblyForces.put(comp, aggregateForces);

		return assemblyForces.get(comp);
	}

	private void addDirectChildStagesToQueue(FlightConfiguration configuration, Queue<RocketComponent> queue,
			RocketComponent comp) {
		for (RocketComponent child : comp.getChildren()) {
			if (child instanceof AxialStage && !configuration.isStageActive(child.getStageNumber())) {
				for (AxialStage childStage : child.getTopLevelChildStages()) {
					if (configuration.isStageActive(childStage.getStageNumber())) {
						queue.add(childStage);
					}
				}
				continue;
			}
			queue.add(child);
		}
	}

	private double getDampingMultiplier(FlightConfiguration configuration, FlightConditions conditions, double cgx) {
		if (cacheDiameter < 0) {
			double area = 0;
			cacheLength = 0;
			cacheDiameter = 0;

			for (RocketComponent c : configuration.getActiveComponents()) {
				if (c instanceof SymmetricComponent) {
					SymmetricComponent s = (SymmetricComponent) c;
					area += s.getComponentPlanformArea();
					cacheLength += s.getLength();
				}
			}
			if (cacheLength > 0) {
				cacheDiameter = area / cacheLength;
			}
		}

		double mul = 0.275 * cacheDiameter / (conditions.getRefArea() * conditions.getRefLength());
		mul *= (MathUtil.pow4(cgx) + MathUtil.pow4(cacheLength - cgx));

		for (RocketComponent c : configuration.getActiveComponents()) {
			if (c instanceof FinSet) {
				FinSet f = (FinSet) c;
				mul += 0.6 * Math.min(f.getFinCount(), 4) * f.getPlanformArea() *
						MathUtil.pow3(Math.abs(f.toAbsolute(new Coordinate(
								((FinSetCalc) calcMap.get(f)).getMidchordPos()))[0].x
								- cgx)) /
						(conditions.getRefArea() * conditions.getRefLength());
			}
		}

		return mul;
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
