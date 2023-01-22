package net.sf.openrocket.aerodynamics;

import static net.sf.openrocket.util.MathUtil.EPSILON;
import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.*;

import net.sf.openrocket.rocketcomponent.AxialStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.aerodynamics.barrowman.RocketComponentCalc;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;
import net.sf.openrocket.util.Reflection;


/**
 * An aerodynamic calculator that uses the extended Barrowman method to 
 * calculate the CP of a rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class BarrowmanCalculator extends AbstractAerodynamicCalculator {
	private static final Logger log = LoggerFactory.getLogger(BarrowmanCalculator.class);
	
	private static final String BARROWMAN_PACKAGE = "net.sf.openrocket.aerodynamics.barrowman";
	private static final String BARROWMAN_SUFFIX = "Calc";
	
	private Map<RocketComponent, RocketComponentCalc> calcMap = null;
	
	private double cacheDiameter = -1;
	private double cacheLength = -1;

	public BarrowmanCalculator() {
		
	}
	
	
	@Override
	public BarrowmanCalculator newInstance() {
		return new BarrowmanCalculator();
	}
	
	
	/**
	 * Calculate the CP according to the extended Barrowman method.
	 */
	@Override
	public Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions,
			WarningSet warnings) {
		checkCache(configuration);
		AerodynamicForces forces = calculateNonAxialForces(configuration, conditions, warnings);
		return forces.getCP();
	}
	
	
	
	@Override
	public Map<RocketComponent, AerodynamicForces> getForceAnalysis(FlightConfiguration configuration,
																	FlightConditions conditions,
																	WarningSet warnings) {
		if (calcMap == null){
			buildCalcMap(configuration);
		}

		InstanceMap instMap = configuration.getActiveInstances();
		Map<RocketComponent, AerodynamicForces> eachMap = new LinkedHashMap<>();
		Map<RocketComponent, AerodynamicForces> assemblyMap = new LinkedHashMap<>();

		// Calculate non-axial force data
		calculateForceAnalysis(configuration, conditions, configuration.getRocket(), instMap, eachMap, assemblyMap, warnings);

		// Calculate drag coefficient data
		AerodynamicForces rocketForces = assemblyMap.get(configuration.getRocket());
		rocketForces.setFrictionCD(calculateFrictionCD(configuration, conditions, eachMap, warnings));
		rocketForces.setPressureCD(calculatePressureCD(configuration, conditions, eachMap, warnings));
		rocketForces.setBaseCD(calculateBaseCD(configuration, conditions, eachMap, warnings));
		rocketForces.setOverrideCD(calculateOverrideCD(configuration, conditions, eachMap, assemblyMap, warnings));

		Map<RocketComponent, AerodynamicForces> finalMap = new LinkedHashMap<>();
		for(final RocketComponent comp : instMap.keySet()){

			AerodynamicForces f;
			if(comp instanceof ComponentAssembly) {
				f = assemblyMap.get(comp);
			} else if(comp.isAerodynamic()){
				f = eachMap.get(comp);
			}else{
				continue;
			}

			if (Double.isNaN(f.getCNa())){
				f.setCNa(0.0);
			}
			if (f.getCP().isNaN()) {
				f.setCP(Coordinate.ZERO);
			}

			if (Double.isNaN(f.getBaseCD()))
				f.setBaseCD(0);

			if (Double.isNaN(f.getPressureCD()))
				f.setPressureCD(0);

			if (Double.isNaN(f.getFrictionCD()))
				f.setFrictionCD(0);

			if (Double.isNaN(f.getOverrideCD()))
				f.setOverrideCD(0);

			f.setCD(f.getBaseCD() + f.getPressureCD() + f.getFrictionCD() + f.getOverrideCD());
			f.setCDaxial(calculateAxialCD(conditions, f.getCD()));

			finalMap.put(comp, f);
		}

		return finalMap;
	}

	private AerodynamicForces calculateForceAnalysis(   FlightConfiguration configuration,
														FlightConditions conds,
														RocketComponent comp,
														InstanceMap instances,
														Map<RocketComponent, AerodynamicForces> eachForces,
														Map<RocketComponent, AerodynamicForces> assemblyForces,
														WarningSet warnings)
	{
		// forces for this component, and all forces below it, in the rocket-tree
		AerodynamicForces aggregateForces = new AerodynamicForces().zero();
		aggregateForces.setComponent(comp);

		// forces for this component, _only_
		if(comp.isAerodynamic() || comp instanceof ComponentAssembly) {
			RocketComponentCalc calcObj = calcMap.get(comp);
			if (null == calcObj) {
				throw new NullPointerException("Could not find a CalculationObject for aerodynamic Component!: " + comp.getComponentName());
			} else {
				List<InstanceContext> contextList = instances.get(comp);
				// across every instance of this component:
				AerodynamicForces compForces = calculateComponentNonAxialForces(conds, comp, calcObj, contextList, warnings);
				eachForces.put(comp, compForces);
				aggregateForces.merge(compForces);
			}
		}

		for( RocketComponent child : comp.getChildren()) {
			// Ignore inactive stages
			if (child instanceof AxialStage && !configuration.isStageActive(child.getStageNumber())) {
				continue;
			}
			
			// forces particular to each component
			AerodynamicForces childForces = calculateForceAnalysis(configuration, conds, child, instances, eachForces, assemblyForces, warnings);

			if(null != childForces) {
				aggregateForces.merge(childForces);
			}
		}

		assemblyForces.put(comp, aggregateForces);

		return assemblyForces.get(comp);
	}

	@Override
	public AerodynamicForces getAerodynamicForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);
		
		if (warnings == null)
			warnings = ignoreWarningSet;
		
		// Calculate non-axial force data
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, warnings);
		
		// Calculate friction data
		total.setFrictionCD(calculateFrictionCD(configuration, conditions, null, warnings));
		total.setPressureCD(calculatePressureCD(configuration, conditions, null, warnings));
		total.setBaseCD(calculateBaseCD(configuration, conditions, null, warnings));
		total.setOverrideCD(calculateOverrideCD(configuration, conditions, null, null, warnings));
		
		total.setCD(total.getFrictionCD() + total.getPressureCD() + total.getBaseCD() + total.getOverrideCD());
		
		total.setCDaxial(calculateAxialCD(conditions, total.getCD()));
		
		// Calculate pitch and yaw damping moments
		calculateDampingMoments(configuration, conditions, total);
		total.setCm(total.getCm() - total.getPitchDampingMoment());
		total.setCyaw(total.getCyaw() - total.getYawDampingMoment());

		return total;
	}


	private AerodynamicForces calculateComponentNonAxialForces( FlightConditions conditions,
																RocketComponent comp,
																RocketComponentCalc calcObj,
																List<InstanceContext> contextList,
																WarningSet warnings)
	{
		// across every instance of this component:
		final AerodynamicForces componentForces = new AerodynamicForces().zero();

		// iterate across component instances
		for(InstanceContext context: contextList ) {
			// specific to this _instance_ of this component:
			AerodynamicForces instanceForces = new AerodynamicForces().zero();
			calcObj.calculateNonaxialForces(conditions, context.transform, instanceForces, warnings);

			Coordinate cp_inst = instanceForces.getCP();
			Coordinate cp_abs = context.transform.transform(cp_inst);
			cp_abs = cp_abs.setY(0.0).setZ(0.0);

			instanceForces.setCP(cp_abs);
			double CN_instanced = instanceForces.getCN();
			instanceForces.setCm(CN_instanced * instanceForces.getCP().x / conditions.getRefLength());

			componentForces.merge(instanceForces);
		}
		componentForces.setComponent(comp);

		return componentForces;
	}

	/**
	 * Perform the actual CP calculation.
	 */
	private AerodynamicForces calculateNonAxialForces(FlightConfiguration configuration, FlightConditions conditions, WarningSet warnings) {

		checkCache(configuration);

		if (warnings == null)
			warnings = ignoreWarningSet;

		if (conditions.getAOA() > 17.5 * Math.PI / 180)
			warnings.add(new Warning.LargeAOA(conditions.getAOA()));

		if (calcMap == null)
			buildCalcMap(configuration);

		checkGeometry(configuration, configuration.getRocket(), warnings);
		
		final InstanceMap imap = configuration.getActiveInstances();

		// across the _entire_ assembly -- like a rocket, or a stage
		final AerodynamicForces assemblyForces= new AerodynamicForces().zero();

		for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> mapEntry: imap.entrySet() ) {
			final RocketComponent comp = mapEntry.getKey();
			final List<InstanceContext> contextList = mapEntry.getValue();

			RocketComponentCalc calcObj = calcMap.get(comp);
			if (null != calcObj) {
				// calculated across all component instances
				final AerodynamicForces componentForces = calculateComponentNonAxialForces(conditions, comp, calcObj, contextList, warnings);

				assemblyForces.merge(componentForces);
			}
		}

		return assemblyForces;
	}

	@Override
	public void checkGeometry(FlightConfiguration configuration, final RocketComponent treeRoot, WarningSet warnings ){
		Queue<RocketComponent> queue = new LinkedList<>();

		for (RocketComponent child : treeRoot.getChildren()) {
			// Ignore inactive stages
			if (child instanceof AxialStage && !configuration.isStageActive(child.getStageNumber())) {
				continue;
			}
			queue.add(child);
		}

		SymmetricComponent prevComp = null;		
		if ((treeRoot instanceof ComponentAssembly) &&
			(!(treeRoot instanceof Rocket)) &&
			(treeRoot.getChildCount() > 0)) {
			prevComp = ((SymmetricComponent) (treeRoot.getChild(0))).getPreviousSymmetricComponent();
		}
		
		while(null != queue.peek()) {
			RocketComponent comp = queue.poll();
			if(( comp instanceof SymmetricComponent ) ||
			   ((comp instanceof AxialStage) &&
				!(comp instanceof ParallelStage))) {
				for (RocketComponent child : comp.getChildren()) {
					// Ignore inactive stages
					if (child instanceof AxialStage && !configuration.isStageActive(child.getStageNumber())) {
						continue;
					}
					queue.add(child);
				}

				if (comp instanceof SymmetricComponent) {
					SymmetricComponent sym = (SymmetricComponent) comp;
					if( null == prevComp){
						if (sym.getForeRadius() - sym.getThickness() > MathUtil.EPSILON) {
							warnings.add(Warning.OPEN_AIRFRAME_FORWARD, sym.toString());
						}
					} else {
						// Check for radius discontinuity
						// We're going to say it's discontinuous if it is presented to the user as having two different
						// string representations.  Hopefully there are enough digits in the string that it will
						// present as different if the discontinuity is big enough to matter.
						if (!UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(2.0*sym.getForeRadius())
							.equals(UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(2.0*prevComp.getAftRadius()))) {
							warnings.add( Warning.DIAMETER_DISCONTINUITY, prevComp + ", " + sym);
						}

						// Check for phantom tube
						if ((sym.getLength() < MathUtil.EPSILON) ||
							(sym.getAftRadius() < MathUtil.EPSILON && sym.getForeRadius() < MathUtil.EPSILON)) {
							warnings.add(Warning.ZERO_VOLUME_BODY, sym.getName());
						}
						
						// check for gap or overlap in airframe.  We'll use a textual comparison to see if there is a
						// gap or overlap, then use arithmetic comparison to see which it is.  This won't be quite as reliable
						// as the case for radius, since we never actually display the absolute X position
						
						double symXfore = sym.toAbsolute(Coordinate.NUL)[0].x;
						double prevXfore = prevComp.toAbsolute(Coordinate.NUL)[0].x;
						
						double symXaft = sym.toAbsolute(new Coordinate(comp.getLength(), 0, 0, 0))[0].x;
						double prevXaft = prevComp.toAbsolute(new Coordinate(prevComp.getLength(), 0, 0, 0))[0].x;
						
						if (!UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(symXfore)
							.equals(UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(prevXaft))) {
							if (symXfore > prevXaft) {
								warnings.add(Warning.AIRFRAME_GAP,  prevComp + ", " + sym);
							} else {
								// If we only have the component with a single forward compartment bring up
								// a body component overlap message								
								if ((symXfore >= prevXfore) &&
									((symXaft >= prevXaft) || (null == sym.getNextSymmetricComponent()))) {
									warnings.add(Warning.AIRFRAME_OVERLAP, prevComp + ", " + sym);
								} else {
									// We have a PodSet that is either overlapping or completely forward of its parent component.
									// We'll find the forward-most and aft-most components and figure out which
									SymmetricComponent firstComp = prevComp;
									SymmetricComponent scout = prevComp;
									while (null != scout) {
										firstComp = scout;
										scout = scout.getPreviousSymmetricComponent();
									}
									double firstCompXfore = firstComp.toAbsolute(Coordinate.NUL)[0].x;
									
									SymmetricComponent lastComp = sym;
									scout = sym;
									while (null != scout) {
										lastComp = scout;
										scout = scout.getNextSymmetricComponent();
									}
									double lastCompXaft = lastComp.toAbsolute(new Coordinate(lastComp.getLength(), 0, 0, 0))[0].x;
									
									// completely forward vs. overlap
									if (lastCompXaft <= firstCompXfore) {
										warnings.add(Warning.PODSET_FORWARD, comp.getParent().toString());
									} else {
										warnings.add(Warning.PODSET_OVERLAP, comp.getParent().toString());
									}
								}
								
							}
						}
					}
					prevComp = sym;
				}
			} else if ((comp instanceof PodSet) ||
					   (comp instanceof ParallelStage)) {
				checkGeometry(configuration, comp, warnings);
			}
		}
	}
		
	
	
	////////////////  DRAG CALCULATIONS  ////////////////
	/**
	 * Calculation of drag coefficient due to air friction
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param warningSet		Set to handle warnings
	 * @return friction drag for entire rocket
	 */
	private double calculateFrictionCD(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {
		
		double mach = conditions.getMach();
		double Re = calculateReynoldsNumber(configuration, conditions);
		double Cf = calculateFrictionCoefficient(configuration, mach, Re);
		double roughnessCorrection = calculateRoughnessCorrection(mach);
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		/*
		 * Calculate the friction drag coefficient.
		 * 
		 * The body wetted area is summed up and finally corrected with the rocket
		 * fineness ratio (calculated in the same iteration).  The fins are corrected
		 * for thickness as we go on.
		 */
		
		double otherFrictionCD = 0;
		double bodyFrictionCD = 0;
		double maxR = 0, minX = Double.MAX_VALUE, maxX = 0;
		
		double[] roughnessLimited = new double[Finish.values().length];
		Arrays.fill(roughnessLimited, Double.NaN);

		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();

			if (!c.isAerodynamic()) {
				continue;
			}
			
			if (c.isCDOverridden() ||
				c.isCDOverriddenByAncestor()) {
				continue;
			}

			// Calculate the roughness-limited friction coefficient
			Finish finish = ((ExternalComponent) c).getFinish();
			if (Double.isNaN(roughnessLimited[finish.ordinal()])) {
				roughnessLimited[finish.ordinal()] =
					0.032 * Math.pow(finish.getRoughnessSize() / configuration.getLengthAerodynamic(), 0.2) *
					roughnessCorrection;
			}
			
			/*
			 * Actual Cf is maximum of Cf and the roughness-limited value.
			 * For perfect finish require additionally that Re > 1e6
			 */
			double componentCf;
			if (configuration.getRocket().isPerfectFinish()) {
				
				// For perfect finish require Re > 1e6
				if ((Re > 1.0e6) && (roughnessLimited[finish.ordinal()] > Cf)) {
					componentCf = roughnessLimited[finish.ordinal()];
				} else {
					componentCf = Cf;
				}
				
			} else {
				
				// For fully turbulent use simple max
				componentCf = Math.max(Cf, roughnessLimited[finish.ordinal()]);
				
			}

			double componentFrictionCD = calcMap.get(c).calculateFrictionCD(conditions, componentCf, warningSet);
			int instanceCount = entry.getValue().size();
			
			if (c instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) c;

				bodyFrictionCD += instanceCount * componentFrictionCD;
				
				final double componentMinX = c.getAxialOffset(AxialMethod.ABSOLUTE);
				minX = Math.min(minX, componentMinX);

				final double componentMaxX = componentMinX + c.getLength();
				maxX = Math.max(maxX, componentMaxX);

				final double componentMaxR = Math.max(s.getForeRadius(), s.getAftRadius());
				maxR = Math.max(maxR, componentMaxR);

			} else {
				otherFrictionCD += instanceCount * componentFrictionCD;
			}

			if (forceMap != null) {
				forceMap.get(c).setFrictionCD(componentFrictionCD);
			}
		}
		
		// fB may be POSITIVE_INFINITY, but that's ok for us
		double fB = (maxX - minX + 0.0001) / maxR;
		double correction = (1 + 1.0 / (2 * fB));
		
		// Correct body data in map
		if (forceMap != null) {
			for (RocketComponent c : forceMap.keySet()) {
				if (c instanceof SymmetricComponent) {
					forceMap.get(c).setFrictionCD(forceMap.get(c).getFrictionCD() * correction);
				}
			}
		}

		return otherFrictionCD + correction * bodyFrictionCD;
	}


	/**
	 * Calculation of Reynolds Number
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @return                  Reynolds Number
	 */
	private double calculateReynoldsNumber(FlightConfiguration configuration, FlightConditions conditions) {
		return conditions.getVelocity() * configuration.getLengthAerodynamic() /
			conditions.getAtmosphericConditions().getKinematicViscosity();
	}
	
	/**
	 * Calculation of skin friction coefficient
	 *
	 *
	 * return skin friction coefficient
	 */
	private double calculateFrictionCoefficient(FlightConfiguration configuration, double mach, double Re) {
		double Cf;
		double c1 = 1.0, c2 = 1.0;
		
		// Calculate the skin friction coefficient (assume non-roughness limited)
		if (configuration.getRocket().isPerfectFinish()) {
			
			// Assume partial laminar layer.  Roughness-limitation is checked later.
			if (Re < 1e4) {
				// Too low, constant
				Cf = 1.33e-2;
			} else if (Re < 5.39e5) {
				// Fully laminar
				Cf = 1.328 / MathUtil.safeSqrt(Re);
			} else {
				// Transitional
				Cf = 1.0 / pow2(1.50 * Math.log(Re) - 5.6) - 1700 / Re;
			}
			
			// Compressibility correction
			
			if (mach < 1.1) {
				// Below Re=1e6 no correction
				if (Re > 1e6) {
					if (Re < 3e6) {
						c1 = 1 - 0.1 * pow2(mach) * (Re - 1e6) / 2e6; // transition to turbulent
					} else {
						c1 = 1 - 0.1 * pow2(mach);
					}
				}
			}
			if (mach > 0.9) {
				if (Re > 1e6) {
					if (Re < 3e6) {
						c2 = 1 + (1.0 / Math.pow(1 + 0.045 * pow2(mach), 0.25) - 1) * (Re - 1e6) / 2e6;
					} else {
						c2 = 1.0 / Math.pow(1 + 0.045 * pow2(mach), 0.25);
					}
				}
			}
			
			// Applying continuously around Mach 1
			if (mach < 0.9) {
				Cf *= c1;
			} else if (mach < 1.1) {
				Cf *= (c2 * (mach - 0.9) / 0.2 + c1 * (1.1 - mach) / 0.2);
			} else {
				Cf *= c2;
			}
			
			
		} else {
			
			// Assume fully turbulent.  Roughness-limitation is checked later.
			if (Re < 1e4) {
				// Too low, constant
				Cf = 1.48e-2;
			} else {
				// Turbulent
				Cf = 1.0 / pow2(1.50 * Math.log(Re) - 5.6);
			}
			
			// Compressibility correction
			
			if (mach < 1.1) {
				c1 = 1 - 0.1 * pow2(mach);
			}
			if (mach > 0.9) {
				c2 = 1 / Math.pow(1 + 0.15 * pow2(mach), 0.58);
			}
			// Applying continuously around Mach 1
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

	/**
	 * Calculation of correction for roughness
	 *
	 * @param  mach
	 * @return roughness correction
	 **/

	private double calculateRoughnessCorrection(double mach) {
		double c1, c2;
		
		// Roughness-limited value correction term
		double roughnessCorrection;
		if (mach < 0.9) {
			roughnessCorrection = 1 - 0.1 * pow2(mach);
		} else if (mach > 1.1) {
			roughnessCorrection = 1 / (1 + 0.18 * pow2(mach));
		} else {
			c1 = 1 - 0.1 * pow2(0.9);
			c2 = 1.0 / (1 + 0.18 * pow2(1.1));
			roughnessCorrection = c2 * (mach - 0.9) / 0.2 + c1 * (1.1 - mach) / 0.2;
		}

		return roughnessCorrection;
	}
	
	/**
	 * Calculation of drag coefficient due to pressure
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param forceMap
	 * @param warningSet			all current warnings
	 * @return
	 */
	private double calculatePressureCD(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {

		double total, stagnation, base;
		if (calcMap == null)
			buildCalcMap(configuration);
		
		stagnation = calculateStagnationCD(conditions.getMach());
		base = calculateBaseCD(conditions.getMach());

		total = 0;
		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();

			if (!c.isAerodynamic()) {
				continue;
			}
				
			if (c.isCDOverridden() ||
				c.isCDOverriddenByAncestor()) {
				continue;
			}
			
			int instanceCount = entry.getValue().size();

			// Pressure drag of this component
			double cd = calcMap.get(c).calculatePressureCD(conditions, stagnation, base,
															   warningSet);

			if (forceMap != null) {
				forceMap.get(c).setPressureCD(cd);
			}
				
			total += cd * instanceCount;
			
			// Stagnation drag caused by difference in radius between this component
			// and previous component (increasing radii.  Decreasing radii handled in
			// base drag calculation
			if (c instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) c;
				double foreRadius = s.getForeRadius();
				double aftRadius = s.getAftRadius();
				// If length is zero, the component is a disk, i.e. a zero-length tube, so match the fore and aft diameter
				if (s.getLength() == 0) {
					foreRadius = Math.max(foreRadius, aftRadius);
				}
				double radius = 0;
				final SymmetricComponent prevComponent = s.getPreviousSymmetricComponent();
				if (prevComponent != null && configuration.isComponentActive(prevComponent))
					radius = prevComponent.getAftRadius();
					
				if (radius < foreRadius) {
					double area = Math.PI * (pow2(foreRadius) - pow2(radius));
					cd = stagnation * area / conditions.getRefArea();
					total += instanceCount * cd;
						
					if (forceMap != null) {
						forceMap.get(c).setPressureCD(forceMap.get(c).getPressureCD() + cd);
					}
				}
			}
		}

		return total;
	}
	

	/**
	 * Calculation of drag coefficient due to base
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param warnings				all current warnings
	 * @return
	 */
	private double calculateBaseCD(FlightConfiguration configuration, FlightConditions conditions,
								   Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warnings) {
		
		double base, total;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		base = calculateBaseCD(conditions.getMach());
		total = 0;
		
		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();
			
			if (!(c instanceof SymmetricComponent)) {
				continue;
			}

			SymmetricComponent s = (SymmetricComponent) c;
			double foreRadius = s.getForeRadius();
			double aftRadius = s.getAftRadius();
			// If length is zero, the component is a disk, i.e. a zero-length tube, so match the fore and aft diameter
			if (s.getLength() == 0) {
				final double componentMaxR = Math.max(foreRadius, aftRadius);
				foreRadius = aftRadius = componentMaxR;
			}

			int instanceCount = entry.getValue().size();
			
			if (c.isCDOverridden() ||
				c.isCDOverriddenByAncestor()) {
				continue;
			}
				
			// if aft radius of previous component is greater than my forward radius, set
			// its aft CD
			double radius = 0;
			final SymmetricComponent prevComponent = s.getPreviousSymmetricComponent();
			if (prevComponent != null && configuration.isComponentActive(prevComponent)) {
				radius = prevComponent.getAftRadius();
			}
			
			if (radius > foreRadius) {
				double area = Math.PI * (pow2(radius) - pow2(foreRadius));
				double cd = base * area / conditions.getRefArea();
				total += instanceCount * cd;
				if ((forceMap != null) && (prevComponent != null)) {
					forceMap.get(prevComponent).setBaseCD(cd);
				}
			}
				
			// if I'm the last component, set my base CD
			// note:  the iterator *should* serve up the next component.... buuuut ....
			//        this code is tested, and there's no compelling reason to change.
			final SymmetricComponent n = s.getNextSymmetricComponent();
			if ((n == null) || !configuration.isStageActive(n.getStageNumber())) {
				double area = Math.PI * pow2(aftRadius);
				double cd = base * area / conditions.getRefArea();
				total += instanceCount * cd;
				if (forceMap != null) {
					forceMap.get(s).setBaseCD(cd);
				}
			}
		}
		
		return total;
	}
	
	/**
	 * gets CD by the speed
	 * @param m		Mach number for calculation
	 * @return		Stagnation CD
	 */
	public static double calculateStagnationCD(double m) {
		double pressure;
		if (m <= 1) {
			pressure = 1 + pow2(m) / 4 + pow2(pow2(m)) / 40;
		} else {
			pressure = 1.84 - 0.76 / pow2(m) + 0.166 / pow2(pow2(m)) + 0.035 / pow2(m * m * m);
		}
		return 0.85 * pressure;
	}
	
	
	/**
	 * Calculates base CD
	 * @param m		Mach number for calculation
	 * @return		Base CD
	 */
	public static double calculateBaseCD(double m) {
		if (m <= 1) {
			return 0.12 + 0.13 * m * m;
		} else {
			return 0.25 / m;
		}
	}
	
	
	
	private static final double[] axialDragPoly1, axialDragPoly2;
	static {
		PolyInterpolator interpolator;
		interpolator = new PolyInterpolator(
				new double[] { 0, 17 * Math.PI / 180 },
				new double[] { 0, 17 * Math.PI / 180 }
				);
		axialDragPoly1 = interpolator.interpolator(1, 1.3, 0, 0);
		
		interpolator = new PolyInterpolator(
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { Math.PI / 2 }
				);
		axialDragPoly2 = interpolator.interpolator(1.3, 0, 0, 0, 0);
	}
	
	
	/**
	 * Calculate the axial drag coefficient from the total drag coefficient.
	 * 
	 * @param conditions
	 * @param cd
	 * @return
	 */
	private double calculateAxialCD(FlightConditions conditions, double cd) {
		double aoa = MathUtil.clamp(conditions.getAOA(), 0, Math.PI);
		double mul;
		
		//		double sinaoa = conditions.getSinAOA();
		//		return cd * (1 + Math.min(sinaoa, 0.25));
		
		
		if (aoa > Math.PI / 2)
			aoa = Math.PI - aoa;
		if (aoa < 17 * Math.PI / 180)
			mul = PolyInterpolator.eval(aoa, axialDragPoly1);
		else
			mul = PolyInterpolator.eval(aoa, axialDragPoly2);
		
		if (conditions.getAOA() < Math.PI / 2)
			return mul * cd;
		else
			return -mul * cd;
	}

	/**
	 * add together CD overrides for active components
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param forceMap
	 * @param warningSet			all current warnings
	 * @return
	 */
	private double calculateOverrideCD(FlightConfiguration configuration, FlightConditions conditions,
									   Map<RocketComponent, AerodynamicForces> eachMap,
									   Map<RocketComponent, AerodynamicForces> assemblyMap,				   
									   WarningSet warningSet) {
		
		if (calcMap == null)
			buildCalcMap(configuration);

		double total = 0;
		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();
			int instanceCount = entry.getValue().size();

			if (!c.isAerodynamic() &&
				!(c instanceof ComponentAssembly)) {
				continue;
			}

			if (c.isCDOverridden() &&
				!c.isCDOverriddenByAncestor()) {
				double cd = instanceCount * c.getOverrideCD();
				Map<RocketComponent, AerodynamicForces> forceMap = (c instanceof ComponentAssembly) ? assemblyMap : eachMap;
				if (forceMap != null) {
					forceMap.get(c).setOverrideCD(cd);
				}
				total += cd;
			}
		}

		return total;
	}
	
    /**
	 * get damping moments from a rocket in a flight
	 * @param configuration		Rocket configuration
	 * @param conditions		flight conditions in consideration
	 * @param total				acting aerodynamic forces
	 */
	private void calculateDampingMoments(FlightConfiguration configuration, FlightConditions conditions,
			AerodynamicForces total) {
		
		// Calculate pitch and yaw damping moments
		double mul = getDampingMultiplier(configuration, conditions,
										  conditions.getPitchCenter().x);
		double pitchRate = conditions.getPitchRate();
		double yawRate = conditions.getYawRate();
		double velocity = conditions.getVelocity();
		
		mul *= 3; // TODO: Higher damping yields much more realistic apogee turn
		
		// find magnitude of damping moments, and clamp so they can't
		// exceed magnitude of pitch and yaw moments
		double pitchDampingMomentMagnitude = MathUtil.min(mul * pow2(pitchRate / velocity), total.getCm());
		double yawDampingMomentMagnitude = MathUtil.min(mul * pow2(yawRate / velocity), total.getCyaw());

		// multiply by sign of pitch and yaw rates
		total.setPitchDampingMoment(MathUtil.sign(pitchRate) * pitchDampingMomentMagnitude);
		total.setYawDampingMoment(MathUtil.sign(yawRate) * yawDampingMomentMagnitude);
	}

	private double getDampingMultiplier(FlightConfiguration configuration, FlightConditions conditions,
			double cgx) {
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
			if (cacheLength > 0)
				cacheDiameter = area / cacheLength;
		}
		
		double mul;
		
		// Body
		mul = 0.275 * cacheDiameter / (conditions.getRefArea() * conditions.getRefLength());
		mul *= (MathUtil.pow4(cgx) + MathUtil.pow4(cacheLength - cgx));
		
		// Fins
		// TODO: LOW: This could be optimized a lot...
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
	
	
	
	////////  The calculator map
	
	@Override
	protected void voidAerodynamicCache() {
		super.voidAerodynamicCache();
		
		calcMap = null;
		cacheDiameter = -1;
		cacheLength = -1;
	}
	
	
	private void buildCalcMap(FlightConfiguration configuration) {
		calcMap = new HashMap<>();

		for (RocketComponent comp: configuration.getAllComponents()) {
			if (!comp.isAerodynamic() && !(comp instanceof ComponentAssembly)) {
				continue;
			}

			RocketComponentCalc calcObj = (RocketComponentCalc) Reflection.construct(BARROWMAN_PACKAGE, comp, BARROWMAN_SUFFIX, comp);

			calcMap.put(comp, calcObj ); 
		}
	}
	
	@Override
	public int getModID() {
		// Only cached data is stored, return constant mod ID
		return 0;
	}
	
}
