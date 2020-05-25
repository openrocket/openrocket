package net.sf.openrocket.aerodynamics;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.*;

import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.aerodynamics.barrowman.RocketComponentCalc;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
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
		calculateForceAnalysis(conditions, configuration.getRocket(), instMap, eachMap, assemblyMap, warnings);

		// Calculate friction data
		AerodynamicForces rocketForces = assemblyMap.get(configuration.getRocket());
		rocketForces.setFrictionCD(calculateFrictionDrag(configuration, conditions, eachMap, warnings));
		rocketForces.setPressureCD(calculatePressureDrag(configuration, conditions, eachMap, warnings));
		rocketForces.setBaseCD(calculateBaseDrag(configuration, conditions, eachMap, warnings));

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

			f.setCD(f.getBaseCD() + f.getPressureCD() + f.getFrictionCD());
			f.setCaxial(calculateAxialDrag(conditions, f.getCD()));

			finalMap.put(comp, f);
		}

		return finalMap;
	}

	private AerodynamicForces calculateForceAnalysis(   FlightConditions conds,
														RocketComponent comp,
														InstanceMap instances,
														Map<RocketComponent, AerodynamicForces> eachForces,
														Map<RocketComponent, AerodynamicForces> assemblyForces,
														WarningSet warnings)
	{
		// forces for this component, and all forces below it, in the rocket-tree
		// => regardless `if(comp isinstance ComponentAssembly)`, or not.
		AerodynamicForces aggregateForces = new AerodynamicForces().zero();
		aggregateForces.setComponent(comp);

		// forces for this component, _only_
		if(comp.isAerodynamic()) {
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
			// forces particular to each component
			AerodynamicForces childForces = calculateForceAnalysis(conds, child, instances, eachForces, assemblyForces, warnings);

			if(null != childForces) {
				aggregateForces.merge(childForces);
			}
		}

		assemblyForces.put(comp, aggregateForces);

		return eachForces.get(comp);
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
		total.setFrictionCD(calculateFrictionDrag(configuration, conditions, null, warnings));
		total.setPressureCD(calculatePressureDrag(configuration, conditions, null, warnings));
		total.setBaseCD(calculateBaseDrag(configuration, conditions, null, warnings));
		
		total.setCD(total.getFrictionCD() + total.getPressureCD() + total.getBaseCD());
		
		total.setCaxial(calculateAxialDrag(conditions, total.getCD()));
		
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
		
		if( ! isContinuous(  configuration.getRocket() ) ){
			warnings.add( Warning.DIAMETER_DISCONTINUITY);
		}
		
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
	public boolean isContinuous( final Rocket rkt){
		return testIsContinuous( rkt);
	}
	
	private boolean testIsContinuous( final RocketComponent treeRoot ){
		Queue<RocketComponent> queue = new LinkedList<>();
		queue.addAll(treeRoot.getChildren());
		
		boolean isContinuous = true;
		SymmetricComponent prevComp = null; 
		while((isContinuous)&&( null != queue.peek())){
			RocketComponent comp = queue.poll();
			if( comp instanceof SymmetricComponent ){
				queue.addAll( comp.getChildren());
				
				SymmetricComponent sym = (SymmetricComponent) comp;
				if( null == prevComp){
					prevComp = sym;
					continue;
				}
				
				// Check for radius discontinuity
				if ( !MathUtil.equals(sym.getForeRadius(), prevComp.getAftRadius())) {
					isContinuous = false;
				}
				
				// double x = component.toAbsolute(Coordinate.NUL)[0].x;
				// // Check for lengthwise discontinuity
				// if (x > componentX + 0.0001) {
				//	if (!MathUtil.equals(radius, 0)) {
				//		warnings.add(Warning.DISCONTINUITY);
				//		radius = 0;
                //}
				//componentX = component.toAbsolute(new Coordinate(component.getLength()))[0].x;
						
				prevComp = sym;
			}else if( comp instanceof ComponentAssembly ){
				isContinuous &= testIsContinuous( comp );
			}
			
		}
		return isContinuous;
	}
		
	
	
	////////////////  DRAG CALCULATIONS  ////////////////
	/**
	 * Calculation of drag coefficient due to air friction
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param set				Set to handle 
	 * @return friction drag for entire rocket
	 */
	private double calculateFrictionDrag(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet set) {
		double c1 = 1.0, c2 = 1.0;
		
		double mach = conditions.getMach();
		double Re;
		double Cf;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		Re = conditions.getVelocity() * configuration.getLength() /
				conditions.getAtmosphericConditions().getKinematicViscosity();
		
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
		
		
		
		/*
		 * Calculate the friction drag coefficient.
		 * 
		 * The body wetted area is summed up and finally corrected with the rocket
		 * fineness ratio (calculated in the same iteration).  The fins are corrected
		 * for thickness as we go on.
		 */
		
		double finFriction = 0;
		double bodyFriction = 0;
		double maxR = 0, minX = Double.MAX_VALUE, maxX = 0;
		
		double[] roughnessLimited = new double[Finish.values().length];
		Arrays.fill(roughnessLimited, Double.NaN);

		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();
			
			// Consider only SymmetricComponents and FinSets:
			if (!(c instanceof SymmetricComponent) &&
					!(c instanceof FinSet))
				continue;

			// iterate across component instances
			final ArrayList<InstanceContext> contextList = entry.getValue();
			for(InstanceContext context: contextList ) {
			
				// Calculate the roughness-limited friction coefficient
				Finish finish = ((ExternalComponent) c).getFinish();
				if (Double.isNaN(roughnessLimited[finish.ordinal()])) {
					roughnessLimited[finish.ordinal()] =
						0.032 * Math.pow(finish.getRoughnessSize() / configuration.getLength(), 0.2) *
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
			
				//Handle Overriden CD for Whole Rocket
				if(c.isCDOverridden()) {
					continue;
				}
			
			
				// Calculate the friction drag:
				if (c instanceof SymmetricComponent) {
				
					SymmetricComponent s = (SymmetricComponent) c;
					
					bodyFriction += componentCf * s.getComponentWetArea();
				
					if (map != null) {
						// Corrected later
						map.get(c).setFrictionCD(componentCf * s.getComponentWetArea()
												 / conditions.getRefArea());
					}

					final double componentMinX = context.getLocation().x;
					minX = Math.min(minX, componentMinX);

					final double componentMaxX = componentMinX + c.getLength();
					maxX = Math.max(maxX, componentMaxX);

					final double componentMaxR = Math.max(s.getForeRadius(), s.getAftRadius());
					maxR = Math.max(maxR, componentMaxR);
					
				} else if (c instanceof FinSet) {
				
					FinSet f = (FinSet) c;
					double mac = ((FinSetCalc) calcMap.get(c)).getMACLength();
					double cd = componentCf * (1 + 2 * f.getThickness() / mac) *
						2 * f.getPlanformArea();
					finFriction += cd;
					
					if (map != null) {
						map.get(c).setFrictionCD(cd / conditions.getRefArea());
					}
					
				}
				
			}
		}
		
		// fB may be POSITIVE_INFINITY, but that's ok for us
		double fB = (maxX - minX + 0.0001) / maxR;
		double correction = (1 + 1.0 / (2 * fB));
		
		// Correct body data in map
		if (map != null) {
			for (RocketComponent c : map.keySet()) {
				if (c instanceof SymmetricComponent) {
					map.get(c).setFrictionCD(map.get(c).getFrictionCD() * correction);
				}
			}
		}
		
		return (finFriction + correction * bodyFriction) / conditions.getRefArea();
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
	private double calculatePressureDrag(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> forceMap, WarningSet warningSet) {
		
		double stagnation, base, total;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		stagnation = calculateStagnationCD(conditions.getMach());
		base = calculateBaseCD(conditions.getMach());
		
		total = 0;
		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();
			if (!c.isAerodynamic())
				continue;

			// iterate across component instances
			final ArrayList<InstanceContext> contextList = entry.getValue();
			for(InstanceContext context: contextList ) {

				// Pressure fore drag
				double cd = calcMap.get(c).calculatePressureDragForce(conditions, stagnation, base,
																	  warningSet);
				total += cd;

				if (forceMap != null) {
					forceMap.get(c).setPressureCD(cd);
				}
				
				if(c.isCDOverridden())
					continue;					
				
				// Stagnation drag
				if (c instanceof SymmetricComponent) {
					SymmetricComponent s = (SymmetricComponent) c;

					double radius = 0;
					final SymmetricComponent prevComponent = s.getPreviousSymmetricComponent();
					if (prevComponent != null)
						radius = prevComponent.getAftRadius();
					
					if (radius < s.getForeRadius()) {
						double area = Math.PI * (pow2(s.getForeRadius()) - pow2(radius));
						cd = stagnation * area / conditions.getRefArea();
						total += cd;
						
						if (forceMap != null) {
							forceMap.get(c).setPressureCD(forceMap.get(c).getPressureCD() + cd);
						}
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
	private double calculateBaseDrag(FlightConfiguration configuration, FlightConditions conditions,
									 Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		double base, total;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		base = calculateBaseCD(conditions.getMach());
		total = 0;
		
		final InstanceMap imap = configuration.getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent c = entry.getKey();
			
			if (!(c instanceof SymmetricComponent))
				continue;
			
			SymmetricComponent s = (SymmetricComponent) c;
			
			// iterate across component instances
			final ArrayList<InstanceContext> contextList = entry.getValue();
			for(InstanceContext context: contextList ) {
				if(c.isCDOverridden()) {
					total += c.getOverrideCD();
					continue;
				}
				
				// if aft radius of previous component is greater than my forward radius, set
				// its aft CD
				double radius = 0;
				final SymmetricComponent prevComponent = s.getPreviousSymmetricComponent();
				if (prevComponent != null) {
					radius = prevComponent.getAftRadius();
				}
				
				if (radius > s.getForeRadius()) {
					double area = Math.PI * (pow2(radius) - pow2(s.getForeRadius()));
					double cd = base * area / conditions.getRefArea();
					total += cd;
					if ((map != null) && (prevComponent != null)) {
						map.get(prevComponent).setBaseCD(cd);
					}
				}
				
				// if I'm the last component, set my base CD
				// note:  the iterator *should* serve up the next component.... buuuut ....
				//        this code has is tested, and there's no compelling reason to change. 
				if (s.getNextSymmetricComponent() == null) {
					double area = Math.PI * pow2(s.getAftRadius());
					double cd = base * area / conditions.getRefArea();
					total += cd;
					if (map != null) {
						map.get(s).setBaseCD(cd);
					}
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
	 * Calculate the axial drag from the total drag coefficient.
	 * 
	 * @param conditions
	 * @param cd
	 * @return
	 */
	private double calculateAxialDrag(FlightConditions conditions, double cd) {
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

		// because this is not a per-instance iteration... this usage of 'getActiveComponents' is probably fine.
		for (RocketComponent comp: configuration.getActiveComponents()) {
			if (!comp.isAerodynamic())
				continue;

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
