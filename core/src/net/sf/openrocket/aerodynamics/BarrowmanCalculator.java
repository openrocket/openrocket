package net.sf.openrocket.aerodynamics;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.aerodynamics.barrowman.RocketComponentCalc;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
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
	public Coordinate getCP(Configuration configuration, FlightConditions conditions,
			WarningSet warnings) {
		checkCache(configuration);
		AerodynamicForces forces = calculateNonAxialForces(configuration, conditions, null, warnings);
		return forces.getCP();
	}
	
	@Override
	public Map<RocketComponent, AerodynamicForces> getForceAnalysis(Configuration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);
		
		Map<RocketComponent, AerodynamicForces> map = getComponentsMap(configuration);
		
		// Calculate non-axial force data
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, map, warnings);
		
		calculateFrictionData(total, configuration, conditions, warnings);
		
		total.setComponent(configuration.getRocket());
		
		map.put(total.getComponent(), total);
		checkCDAndApplyFriction(map, conditions);
		return map;
	}
	
	/**
	 * get a map of rocket components with their own Aerodynamic forces bean
	 * TODO: LOW: maybe transfer the function to Configuration class?
	 * @param configuration 	The rocket configuration
	 * @return					the map of rocket configuration with it's 
	 * 							correspondent aerodynamic forces bean
	 */
	private Map<RocketComponent, AerodynamicForces> getComponentsMap(Configuration configuration) {
		Map<RocketComponent, AerodynamicForces> map = new LinkedHashMap<RocketComponent, AerodynamicForces>();
		// Add all components to the map
		for (RocketComponent c : configuration) {
			AerodynamicForces f = new AerodynamicForces();
			f.setComponent(c);
			map.put(c, f);
		}
		return map;
	}
	
	/**
	 * check an analysis to fix possible invalid CDs and apply the actual friction
	 * 
	 * @param forceAnalysis
	 * @param conditions
	 */
	private void checkCDAndApplyFriction(Map<RocketComponent, AerodynamicForces> forceAnalysis, FlightConditions conditions) {
		for (RocketComponent c : forceAnalysis.keySet()) {
			checkCDConsistency(forceAnalysis.get(c));
			applyFriction(forceAnalysis.get(c), conditions);
		}
	}
	
	/**
	 * fixes possibles NaN in previous calculation of CDs
	 * 
	 * @param f
	 * @param conditions
	 */
	private void checkCDConsistency(AerodynamicForces f) {
		if (Double.isNaN(f.getBaseCD()) && Double.isNaN(f.getPressureCD()) &&
				Double.isNaN(f.getFrictionCD()))
			return;
		if (Double.isNaN(f.getBaseCD()))
			f.setBaseCD(0);
		if (Double.isNaN(f.getPressureCD()))
			f.setPressureCD(0);
		if (Double.isNaN(f.getFrictionCD()))
			f.setFrictionCD(0);
	}
	
	
	
	@Override
	public AerodynamicForces getAerodynamicForces(Configuration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);
		
		if (warnings == null)
			warnings = ignoreWarningSet;
		
		// Calculate non-axial force data
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, null, warnings);
		
		// Calculate friction data
		calculateFrictionData(total, configuration, conditions, warnings);
		applyFriction(total, conditions);
		
		// Calculate pitch and yaw damping moments
		calculateDampingMoments(configuration, conditions, total);
		applyDampingMoments(total);
		return total;
	}
	
	/**
	 * Applies the actual influence of friction in an AerodynamicForces set
	 * 
	 * @param force			the Aerodynamic forces to be applied with friction
	 * @param conditions	the flight conditions in consideration
	 */
	private void applyFriction(AerodynamicForces force, FlightConditions conditions) {
		force.setCD(force.getFrictionCD() + force.getPressureCD() + force.getBaseCD());
		force.setCaxial(calculateAxialDrag(conditions, force.getCD()));
	}
	
	/**
	 * does the actual action of damping into an AerodynamicForces set
	 * 
	 * @param total 	the AerodynamicForces object to be applied with the damping
	 */
	private void applyDampingMoments(AerodynamicForces total) {
		total.setCm(total.getCm() - total.getPitchDampingMoment());
		total.setCyaw(total.getCyaw() - total.getYawDampingMoment());
	}
	
	/**
	 * Will calculate all basic CD from an AerodynamicForces set
	 * @param total				The AerodynamicForces that will be calculated
	 * @param configuration 	the Rocket configutarion
	 * @param conditions		Flight conditions in the simulation
	 * @param warnings			Warning set to handle special events
	 */
	private void calculateFrictionData(AerodynamicForces total, Configuration configuration, FlightConditions conditions, WarningSet warnings) {
		total.setFrictionCD(calculateFrictionDrag(configuration, conditions, null, warnings));
		total.setPressureCD(calculatePressureDrag(configuration, conditions, null, warnings));
		total.setBaseCD(calculateBaseDrag(configuration, conditions, null, warnings));
	}
	
	
	
	
	/**
	 * Perform the actual CP calculation.
	 */
	private AerodynamicForces calculateNonAxialForces(Configuration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		checkCache(configuration);
		
		AerodynamicForces total = new AerodynamicForces(true);
		
		double radius = 0; // aft radius of previous component
		double componentX = 0; // aft coordinate of previous component
		AerodynamicForces forces = new AerodynamicForces();
		
		if (warnings == null)
			warnings = ignoreWarningSet;
		
		if (conditions.getAOA() > 17.5 * Math.PI / 180)
			warnings.add(new Warning.LargeAOA(conditions.getAOA()));
		
		
		checkCalcMap(configuration);
		
		for (RocketComponent component : configuration) {
			
			// Skip non-aerodynamic components
			if (!component.isAerodynamic())
				continue;
			
			// Check for discontinuities
			if (component instanceof SymmetricComponent) {
				SymmetricComponent sym = (SymmetricComponent) component;
				// TODO:LOW: Ignores other cluster components (not clusterable)
				double x = component.toAbsolute(Coordinate.NUL)[0].x;
				
				// Check for lengthwise discontinuity
				if (x > componentX + 0.0001) {
					if (!MathUtil.equals(radius, 0)) {
						warnings.add(Warning.DISCONTINUITY);
						radius = 0;
					}
				}
				componentX = component.toAbsolute(new Coordinate(component.getLength()))[0].x;
				
				// Check for radius discontinuity
				if (!MathUtil.equals(sym.getForeRadius(), radius)) {
					warnings.add(Warning.DISCONTINUITY);
					// TODO: MEDIUM: Apply correction to values to cp and to map
				}
				radius = sym.getAftRadius();
			}
			
			// Call calculation method
			forces.zero();
			calcMap.get(component).calculateNonaxialForces(conditions, forces, warnings);
			forces.setCP(component.toAbsolute(forces.getCP())[0]);
			forces.setCm(forces.getCN() * forces.getCP().x / conditions.getRefLength());
			
			//TODO: LOW: Why is it here? was this the todo from above? Vicilu
			if (map != null) {
				AerodynamicForces f = map.get(component);
				
				f.setCP(forces.getCP());
				f.setCNa(forces.getCNa());
				f.setCN(forces.getCN());
				f.setCm(forces.getCm());
				f.setCside(forces.getCside());
				f.setCyaw(forces.getCyaw());
				f.setCroll(forces.getCroll());
				f.setCrollDamp(forces.getCrollDamp());
				f.setCrollForce(forces.getCrollForce());
				map.put(component, f);
			}
			
			total.setCP(total.getCP().average(forces.getCP()));
			total.setCNa(total.getCNa() + forces.getCNa());
			total.setCN(total.getCN() + forces.getCN());
			total.setCm(total.getCm() + forces.getCm());
			total.setCside(total.getCside() + forces.getCside());
			total.setCyaw(total.getCyaw() + forces.getCyaw());
			total.setCroll(total.getCroll() + forces.getCroll());
			total.setCrollDamp(total.getCrollDamp() + forces.getCrollDamp());
			total.setCrollForce(total.getCrollForce() + forces.getCrollForce());
		}
		
		return total;
	}
	
	
	
	
	////////////////  DRAG CALCULATIONS  ////////////////
	//TODO: LOW: clarify what map is doing here, or use it
	/**
	 * Calculation of drag coefficient due to air friction
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param set				Set to handle 
	 * @return
	 */
	private double calculateFrictionDrag(Configuration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet set) {
		double c1 = 1.0, c2 = 1.0;
		
		double mach = conditions.getMach();
		double Re;
		double Cf;
		
		checkCalcMap(configuration);
		
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
		
		for (RocketComponent c : configuration) {
			
			// Consider only SymmetricComponents and FinSets:
			if (!(c instanceof SymmetricComponent) &&
					!(c instanceof FinSet))
				continue;
			
			// Calculate the roughness-limited friction coefficient
			Finish finish = ((ExternalComponent) c).getFinish();
			if (Double.isNaN(roughnessLimited[finish.ordinal()])) {
				roughnessLimited[finish.ordinal()] = 0.032 * Math.pow(finish.getRoughnessSize() / configuration.getLength(), 0.2) *
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
						2 * f.getFinCount() * f.getFinArea();
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
	 * method to avoid repetition, create the calcMap if null
	 * @param configuration the rocket configuration
	 */
	private void checkCalcMap(Configuration configuration) {
		if (calcMap == null)
			buildCalcMap(configuration);
	}
	
	//TODO: LOW: clarify what map is doing here, or use it
	/**
	 * Calculation of drag coefficient due to pressure
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param set				Set to handle 
	 * @return
	 */
	private double calculatePressureDrag(Configuration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		double stagnation, base, total;
		double radius = 0;
		
		checkCalcMap(configuration);
		
		stagnation = calculateStagnationCD(conditions.getMach());
		base = calculateBaseCD(conditions.getMach());
		
		total = 0;
		for (RocketComponent c : configuration) {
			if (!c.isAerodynamic())
				continue;
			
			// Pressure fore drag
			double cd = calcMap.get(c).calculatePressureDragForce(conditions, stagnation, base,
					warnings);
			total += cd;
			
			if (map != null) {
				map.get(c).setPressureCD(cd);
			}
			
			
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
	
	//TODO: LOW: clarify what map is doing here, or use it
	/**
	 * Calculation of drag coefficient due to base
	 * 
	 * @param configuration		Rocket configuration
	 * @param conditions		Flight conditions taken into account
	 * @param map				?
	 * @param set				Set to handle 
	 * @return
	 */
	private double calculateBaseDrag(Configuration configuration, FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> map, WarningSet warnings) {
		
		double base, total;
		double radius = 0;
		RocketComponent prevComponent = null;
		
		checkCalcMap(configuration);
		
		base = calculateBaseCD(conditions.getMach());
		total = 0;
		
		for (RocketComponent c : configuration) {
			if (!(c instanceof SymmetricComponent))
				continue;
			
			SymmetricComponent s = (SymmetricComponent) c;
			
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
				new double[] { 0, 17 * Math.PI / 180 });
		axialDragPoly1 = interpolator.interpolator(1, 1.3, 0, 0);
		
		interpolator = new PolyInterpolator(
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { Math.PI / 2 });
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
	private void calculateDampingMoments(Configuration configuration, FlightConditions conditions,
			AerodynamicForces total) {
		
		// Calculate pitch and yaw damping moments
		double mul = getDampingMultiplier(configuration, conditions,
				conditions.getPitchCenter().x);
		double pitch = conditions.getPitchRate();
		double yaw = conditions.getYawRate();
		double vel = conditions.getVelocity();
		
		vel = MathUtil.max(vel, 1);
		
		mul *= 3; // TODO: Higher damping yields much more realistic apogee turn
		
		total.setPitchDampingMoment(mul * MathUtil.sign(pitch) * pow2(pitch / vel));
		total.setYawDampingMoment(mul * MathUtil.sign(yaw) * pow2(yaw / vel));
	}
	
	// TODO: MEDIUM: Are the rotation etc. being added correctly?  sin/cos theta?
	
	
	private double getDampingMultiplier(Configuration configuration, FlightConditions conditions,
			double cgx) {
		if (cacheDiameter < 0) {
			double area = 0;
			cacheLength = 0;
			cacheDiameter = 0;
			
			for (RocketComponent c : configuration) {
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
		for (RocketComponent c : configuration) {
			if (c instanceof FinSet) {
				FinSet f = (FinSet) c;
				mul += 0.6 * Math.min(f.getFinCount(), 4) * f.getFinArea() *
						MathUtil.pow3(Math.abs(f.toAbsolute(new Coordinate(
								((FinSetCalc) calcMap.get(f)).getMidchordPos()))[0].x
								- cgx))
						/
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
	
	/**
	 * caches the map for aerodynamics calculations
	 * @param configuration		the rocket configuration
	 */
	private void buildCalcMap(Configuration configuration) {
		Iterator<RocketComponent> iterator;
		
		calcMap = new HashMap<RocketComponent, RocketComponentCalc>();
		
		iterator = configuration.getRocket().iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			
			if (!c.isAerodynamic())
				continue;
			
			calcMap.put(c, (RocketComponentCalc) Reflection.construct(BARROWMAN_PACKAGE,
					c, BARROWMAN_SUFFIX, c));
		}
	}
	
	
	@Override
	public int getModID() {
		// Only cached data is stored, return constant mod ID
		return 0;
	}
	
	
}
