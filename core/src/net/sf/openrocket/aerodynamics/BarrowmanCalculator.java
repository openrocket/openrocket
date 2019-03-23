package net.sf.openrocket.aerodynamics;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
import net.sf.openrocket.util.Transformation;

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
		AerodynamicForces forces = calculateNonAxialForces(configuration, conditions, null, warnings);
		return forces.getCP();
	}
	
	
	
	@Override
	public Map<RocketComponent, AerodynamicForces> getForceAnalysis(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);
		
		AerodynamicForces f;
		Map<RocketComponent, AerodynamicForces> map =
				new LinkedHashMap<RocketComponent, AerodynamicForces>();
		
		// Add all components to the map
		for (RocketComponent component : configuration.getActiveComponents()) {
			
			// Skip non-aerodynamic components
			if (!component.isAerodynamic())
				continue;
			
			f = new AerodynamicForces();
			f.setComponent(component);
			
			map.put(component, f);
		}
		
		
		// Calculate non-axial force data
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, map, warnings);
		
		
		// Calculate friction data
		total.setFrictionCD(calculateFrictionDrag(configuration, conditions, map, warnings));
		total.setPressureCD(calculatePressureDrag(configuration, conditions, map, warnings));
		total.setBaseCD(calculateBaseDrag(configuration, conditions, map, warnings));
		
		total.setComponent(configuration.getRocket());
		map.put(total.getComponent(), total);
		
		
		for (RocketComponent c : map.keySet()) {
			f = map.get(c);
			if (Double.isNaN(f.getBaseCD()) && Double.isNaN(f.getPressureCD()) &&
					Double.isNaN(f.getFrictionCD()))
				continue;
			if (Double.isNaN(f.getBaseCD()))
				f.setBaseCD(0);
			if (Double.isNaN(f.getPressureCD()))
				f.setPressureCD(0);
			if (Double.isNaN(f.getFrictionCD()))
				f.setFrictionCD(0);
			f.setCD(f.getBaseCD() + f.getPressureCD() + f.getFrictionCD());
			f.setCaxial(calculateAxialDrag(conditions, f.getCD()));
		}
		
		return map;
	}
	
	
	
	@Override
	public AerodynamicForces getAerodynamicForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);
		
		if (warnings == null)
			warnings = ignoreWarningSet;
		
		// Calculate non-axial force data
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, null, warnings);
		
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
	
	

	/**
	 * Perform the actual CP calculation.
	 */
	private AerodynamicForces calculateNonAxialForces(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> calculators, WarningSet warnings) {
		
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

		final AerodynamicForces assemblyForces= new AerodynamicForces().zero();

		// iterate through all components
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent comp = entry.getKey();
			RocketComponentCalc calcObj = calcMap.get(comp);

			// System.err.println("comp=" + comp);
			if (null != calcObj) {
				// iterate across component instances
				final ArrayList<InstanceContext> contextList = entry.getValue();

				for(InstanceContext context: contextList ) {
					AerodynamicForces instanceForces = new AerodynamicForces().zero();
					
					calcObj.calculateNonaxialForces(conditions, context.transform, instanceForces, warnings);
					Coordinate cp_comp = instanceForces.getCP();
					
					Coordinate cp_abs = context.transform.transform(cp_comp);
					if ((comp instanceof FinSet) && (((FinSet)comp).getFinCount() > 2))
						cp_abs = cp_abs.setY(0.0).setZ(0.0);
					
					instanceForces.setCP(cp_abs);
					double CN_instanced = instanceForces.getCN();
					instanceForces.setCm(CN_instanced * instanceForces.getCP().x / conditions.getRefLength());
					// System.err.println("instanceForces=" + instanceForces);
					assemblyForces.merge(instanceForces);
				}
			}
		}

		// System.err.println("assemblyForces=" + assemblyForces);
		return assemblyForces;
	}
	
	@Override
	public boolean isContinuous( final Rocket rkt){
		return testIsContinuous( rkt);
	}
	
	private boolean testIsContinuous( final RocketComponent treeRoot ){
		Queue<RocketComponent> queue = new LinkedList<RocketComponent>();
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
	 * @return
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
		double maxR = 0, len = 0;
		
		double[] roughnessLimited = new double[Finish.values().length];
		Arrays.fill(roughnessLimited, Double.NaN);
		
		for (RocketComponent c : configuration.getActiveComponents()) {
			
			// Consider only SymmetricComponents and FinSets:
			if (!(c instanceof SymmetricComponent) &&
					!(c instanceof FinSet))
				continue;
			
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
				
				double r = Math.max(s.getForeRadius(), s.getAftRadius());
				if (r > maxR)
					maxR = r;
				len += c.getLength();
				
			} else if (c instanceof FinSet) {
				
				FinSet f = (FinSet) c;
				double mac = ((FinSetCalc) calcMap.get(c)).getMACLength();
				double cd = componentCf * (1 + 2 * f.getThickness() / mac) *
						2 * f.getFinCount() * f.getPlanformArea();
				finFriction += cd;
				
				if (map != null) {
					map.get(c).setFrictionCD(cd / conditions.getRefArea());
				}
				
			}

		
			
		}
		// fB may be POSITIVE_INFINITY, but that's ok for us
		double fB = (len + 0.0001) / maxR;
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
	 * @param map				?
	 * @param set				Set to handle 
	 * @return
	 */
	private double calculatePressureDrag(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		double stagnation, base, total;
		double radius = 0;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		stagnation = calculateStagnationCD(conditions.getMach());
		base = calculateBaseCD(conditions.getMach());
		
		total = 0;
		for (RocketComponent c : configuration.getActiveComponents()) {
			if (!c.isAerodynamic())
				continue;
			
			// Pressure fore drag
			double cd = calcMap.get(c).calculatePressureDragForce(conditions, stagnation, base,
					warnings);
			total += cd;
			
			if (map != null) {
				map.get(c).setPressureCD(cd);
			}
			
			if(c.isCDOverridden()) continue;					

	
			// Stagnation drag
			if (c instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) c;
				
				if (radius < s.getForeRadius()) {
					double area = Math.PI * (pow2(s.getForeRadius()) - pow2(radius));
					cd = stagnation * area / conditions.getRefArea();
					total += cd;
					if (map != null) {
						map.get(c).setPressureCD(map.get(c).getPressureCD() + cd);
					}
				}
				
				radius = s.getAftRadius();
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
	 * @param set				Set to handle 
	 * @return
	 */
	private double calculateBaseDrag(FlightConfiguration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		double base, total;
		double radius = 0;
		RocketComponent prevComponent = null;
		
		if (calcMap == null)
			buildCalcMap(configuration);
		
		base = calculateBaseCD(conditions.getMach());
		total = 0;
		
		for (RocketComponent c : configuration.getActiveComponents()) {
			if (!(c instanceof SymmetricComponent))
				continue;
			
			SymmetricComponent s = (SymmetricComponent) c;

			if(c.isCDOverridden()) {
				total += c.getOverrideCD();
				continue;
			}
			
			if (radius > s.getForeRadius()) {
				double area = Math.PI * (pow2(radius) - pow2(s.getForeRadius()));
				double cd = base * area / conditions.getRefArea();
				total += cd;
				if (map != null) {
					map.get(prevComponent).setBaseCD(cd);
				}
			}
			
			radius = s.getAftRadius();
			prevComponent = c;
		}
		
		if (radius > 0) {
			double area = Math.PI * pow2(radius);
			double cd = base * area / conditions.getRefArea();
			total += cd;
			if (map != null) {
				map.get(prevComponent).setBaseCD(cd);
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
	
	// TODO: MEDIUM: Are the rotation etc. being added correctly?  sin/cos theta?
	
	
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
		Iterator<RocketComponent> iterator;
		
		calcMap = new HashMap<RocketComponent, RocketComponentCalc>();

		for (RocketComponent comp: configuration.getActiveComponents()) {
			if (!comp.isAerodynamic())
				continue;
			
			RocketComponentCalc calcObj = (RocketComponentCalc) Reflection.construct(BARROWMAN_PACKAGE, comp, BARROWMAN_SUFFIX, comp);
			//String isNull = (null==calcObj?"null":"valid");
			//System.err.println("    >> At component: "+c.getName() +"=="+c.getID()+". CalcObj is "+isNull);
			
			calcMap.put(comp, calcObj ); 
		}
	}
	
	@Override
	public int getModID() {
		// Only cached data is stored, return constant mod ID
		return 0;
	}
	
}
