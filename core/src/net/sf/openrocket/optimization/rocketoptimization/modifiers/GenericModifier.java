package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Reflection.Method;

/**
 * A generic SimulationModifier that uses reflection to get and set a double value.
 * Implementations need to implement the {@link #getModifiedObject(Simulation)} method
 * to return which object is modified.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class GenericModifier<T> extends AbstractSimulationModifier {
	private static final Logger log = LoggerFactory.getLogger(GenericModifier.class);
	
	private final double multiplier;
	
	private final Class<? extends T> modifiedClass;
	private final String methodName;
	
	private final Method getter;
	private final Method setter;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param modifierName		the name of this modifier (returned by {@link #getName()})
	 * @param modifierDescription	the description of this modifier (returned by {@link #getDescription()})
	 * @param relatedObject		the related object (returned by {@link #getRelatedObject()})
	 * @param unitGroup			the unit group (returned by {@link #getUnitGroup()})
	 * @param multiplier		the multiplier by which the value returned by the getter is multiplied
	 * 							to obtain the desired value
	 * @param modifiedClass		the class type that {@link #getModifiedObject(Simulation)} returns
	 * @param methodName		the base name of the getter/setter methods (without "get"/"set")
	 */
	public GenericModifier(String modifierName, String modifierDescription, Object relatedObject, UnitGroup unitGroup,
			double multiplier, Class<? extends T> modifiedClass, String methodName) {
		super(modifierName, modifierDescription, relatedObject, unitGroup);
		this.multiplier = multiplier;
		
		this.modifiedClass = modifiedClass;
		this.methodName = methodName;
		
		if (MathUtil.equals(multiplier, 0)) {
			throw new IllegalArgumentException("multiplier is zero");
		}
		
		try {
			methodName = methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1);
			getter = new Method(modifiedClass.getMethod("get" + methodName));
			setter = new Method(modifiedClass.getMethod("set" + methodName, double.class));
		} catch (SecurityException e) {
			throw new BugException("Trying to find method get/set" + methodName + " in class " + modifiedClass, e);
		} catch (NoSuchMethodException e) {
			throw new BugException("Trying to find method get/set" + methodName + " in class " + modifiedClass, e);
		}
	}
	
	
	
	@Override
	public double getCurrentSIValue(Simulation simulation) throws OptimizationException {
		T modifiable = getModifiedObject(simulation);
		if (modifiable == null) {
			throw new OptimizationException("BUG: getModifiedObject() returned null");
		}
		return ((Double) getter.invoke(modifiable)) * multiplier;
	}
	
	
	@Override
	public void modify(Simulation simulation, double scaledValue) throws OptimizationException {
		T modifiable = getModifiedObject(simulation);
		if (modifiable == null) {
			throw new OptimizationException("BUG: getModifiedObject() returned null");
		}
		double siValue = toBaseValue(scaledValue) / multiplier;
		log.trace("Setting setter=" + setter + " modifiable=" + modifiable + " siValue=" + siValue + "scaledValue=" + scaledValue);
		setter.invoke(modifiable, siValue);
	}
	
	
	/**
	 * Return the object from the simulation that will be modified.
	 * @param simulation	the simulation
	 * @return				the object to modify
	 * 
	 * @throws OptimizationException 	if the object cannot be found
	 */
	protected abstract T getModifiedObject(Simulation simulation) throws OptimizationException;
	
	
	
	@Override
	public String toString() {
		return "GenericModifier[modifiedClass=" + modifiedClass.getCanonicalName() + ", methodName=" + methodName + ", multiplier=" + multiplier + "]";
	}
	
}
