package info.openrocket.core.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.optimization.general.OptimizationException;
import info.openrocket.core.optimization.rocketoptimization.SimulationModifier;
import info.openrocket.core.optimization.rocketoptimization.modifiers.FlightConfigurationModifier;
import info.openrocket.core.optimization.rocketoptimization.modifiers.GenericComponentModifier;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InternalComponent;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Reflection;
import info.openrocket.core.util.Reflection.Method;

public class DefaultSimulationModifierService implements SimulationModifierService {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final double DEFAULT_RANGE_MULTIPLIER = 2.0;
	
	
	private static final Map<Class<?>, List<ModifierDefinition>> definitions = new HashMap<Class<?>, List<ModifierDefinition>>();
	static {
		//addModifier("optimization.modifier.", unitGroup, multiplier, componentClass, methodName);
		
		/*
		 * Note:  Each component type must contain only mutually exclusive variables.
		 * For example, body tube does not have inner diameter definition because it is
		 * defined by the outer diameter and thickness.
		 */
		
		addModifier("optimization.modifier.nosecone.length", UnitGroup.UNITS_LENGTH, 1.0, NoseCone.class, "Length");
		addModifier("optimization.modifier.nosecone.diameter", UnitGroup.UNITS_LENGTH, 2.0, NoseCone.class, "BaseRadius", "isBaseRadiusAutomatic");
		addModifier("optimization.modifier.nosecone.thickness", UnitGroup.UNITS_LENGTH, 1.0, NoseCone.class, "Thickness", "isFilled");
		
		addModifier("optimization.modifier.transition.length", UnitGroup.UNITS_LENGTH, 1.0, Transition.class, "Length");
		addModifier("optimization.modifier.transition.forediameter", UnitGroup.UNITS_LENGTH, 2.0, Transition.class, "ForeRadius", "isForeRadiusAutomatic");
		addModifier("optimization.modifier.transition.aftdiameter", UnitGroup.UNITS_LENGTH, 2.0, Transition.class, "AftRadius", "isAftRadiusAutomatic");
		addModifier("optimization.modifier.transition.thickness", UnitGroup.UNITS_LENGTH, 1.0, Transition.class, "Thickness", "isFilled");
		
		addModifier("optimization.modifier.bodytube.length", UnitGroup.UNITS_LENGTH, 1.0, BodyTube.class, "Length");
		addModifier("optimization.modifier.bodytube.outerDiameter", UnitGroup.UNITS_LENGTH, 2.0, BodyTube.class, "OuterRadius", "isOuterRadiusAutomatic");
		addModifier("optimization.modifier.bodytube.thickness", UnitGroup.UNITS_LENGTH, 1.0, BodyTube.class, "Thickness", "isFilled");
		
		addModifier("optimization.modifier.trapezoidfinset.rootChord", UnitGroup.UNITS_LENGTH, 1.0, TrapezoidFinSet.class, "RootChord");
		addModifier("optimization.modifier.trapezoidfinset.tipChord", UnitGroup.UNITS_LENGTH, 1.0, TrapezoidFinSet.class, "TipChord");
		addModifier("optimization.modifier.trapezoidfinset.sweep", UnitGroup.UNITS_LENGTH, 1.0, TrapezoidFinSet.class, "Sweep");
		addModifier("optimization.modifier.trapezoidfinset.height", UnitGroup.UNITS_LENGTH, 1.0, TrapezoidFinSet.class, "Height");
		addModifier("optimization.modifier.finset.cant", UnitGroup.UNITS_ANGLE, 1.0, TrapezoidFinSet.class, "CantAngle");
		
		addModifier("optimization.modifier.ellipticalfinset.length", UnitGroup.UNITS_LENGTH, 1.0, EllipticalFinSet.class, "Length");
		addModifier("optimization.modifier.ellipticalfinset.height", UnitGroup.UNITS_LENGTH, 1.0, EllipticalFinSet.class, "Height");
		addModifier("optimization.modifier.finset.cant", UnitGroup.UNITS_ANGLE, 1.0, EllipticalFinSet.class, "CantAngle");
		
		addModifier("optimization.modifier.finset.cant", UnitGroup.UNITS_ANGLE, 1.0, FreeformFinSet.class, "CantAngle");
		
		addModifier("optimization.modifier.launchlug.length", UnitGroup.UNITS_LENGTH, 1.0, LaunchLug.class, "Length");
		addModifier("optimization.modifier.launchlug.outerDiameter", UnitGroup.UNITS_LENGTH, 2.0, LaunchLug.class, "OuterRadius");
		addModifier("optimization.modifier.launchlug.thickness", UnitGroup.UNITS_LENGTH, 1.0, LaunchLug.class, "Thickness");
		
		
		addModifier("optimization.modifier.masscomponent.mass", UnitGroup.UNITS_MASS, 1.0, MassComponent.class, "ComponentMass");
		
		addModifier("optimization.modifier.parachute.diameter", UnitGroup.UNITS_LENGTH, 1.0, Parachute.class, "Diameter");
		addModifier("optimization.modifier.parachute.coefficient", UnitGroup.UNITS_NONE, 1.0, Parachute.class, "CD");
		
		addModifier("optimization.modifier.streamer.length", UnitGroup.UNITS_LENGTH, 1.0, Streamer.class, "StripLength");
		addModifier("optimization.modifier.streamer.width", UnitGroup.UNITS_LENGTH, 1.0, Streamer.class, "StripWidth");
		addModifier("optimization.modifier.streamer.aspectRatio", UnitGroup.UNITS_NONE, 1.0, Streamer.class, "AspectRatio");
		addModifier("optimization.modifier.streamer.coefficient", UnitGroup.UNITS_NONE, 1.0, Streamer.class, "CD", "isCDAutomatic");
		
	}
	
	private static void addModifier(String modifierNameKey, UnitGroup unitGroup, double multiplier,
			Class<? extends RocketComponent> componentClass, String methodName) {
		addModifier(modifierNameKey, unitGroup, multiplier, componentClass, methodName, null);
	}
	
	private static void addModifier(String modifierNameKey, UnitGroup unitGroup, double multiplier,
			Class<? extends RocketComponent> componentClass, String methodName, String autoMethod) {
		
		String modifierDescriptionKey = modifierNameKey + ".desc";
		
		List<ModifierDefinition> list = definitions.get(componentClass);
		if (list == null) {
			list = new ArrayList<DefaultSimulationModifierService.ModifierDefinition>();
			definitions.put(componentClass, list);
		}
		
		ModifierDefinition definition = new ModifierDefinition(modifierNameKey, modifierDescriptionKey, unitGroup,
				multiplier, componentClass, methodName, autoMethod);
		list.add(definition);
	}
	
	
	
	
	@Override
	public Collection<SimulationModifier> getModifiers(OpenRocketDocument document) {
		List<SimulationModifier> modifiers = new ArrayList<SimulationModifier>();
		
		Rocket rocket = document.getRocket();
		
		// Simulation is used to calculate default min/max values
		Simulation simulation = new Simulation(document, rocket);
		
		for (RocketComponent c : rocket) {
			
			// Attribute modifiers
			List<ModifierDefinition> list = definitions.get(c.getClass());
			if (list != null) {
				for (ModifierDefinition def : list) {
					
					// Ignore modifier if value is set to automatic
					if (def.autoMethod != null) {
						Method m = Reflection.findMethod(c.getClass(), def.autoMethod);
						if ((Boolean) m.invoke(c)) {
							continue;
						}
					}
					
					SimulationModifier mod = new GenericComponentModifier(
							trans.get(def.modifierNameKey), trans.get(def.modifierDescriptionKey), c, def.unitGroup,
							def.multiplier, def.componentClass, c.getID(), def.methodName);
					setDefaultMinMax(mod, simulation);
					modifiers.add(mod);
				}
			}
			
			
			// Add override modifiers if mass/CG is overridden
			if (c.isMassOverridden()) {
				SimulationModifier mod = new GenericComponentModifier(
						trans.get("optimization.modifier.rocketcomponent.overrideMass"),
						trans.get("optimization.modifier.rocketcomponent.overrideMass.desc"),
						c, UnitGroup.UNITS_MASS,
						1.0, c.getClass(), c.getID(), "OverrideMass");
				setDefaultMinMax(mod, simulation);
				modifiers.add(mod);
			}
			if (c.isCGOverridden()) {
				SimulationModifier mod = new GenericComponentModifier(
						trans.get("optimization.modifier.rocketcomponent.overrideCG"),
						trans.get("optimization.modifier.rocketcomponent.overrideCG.desc"),
						c, UnitGroup.UNITS_LENGTH,
						1.0, c.getClass(), c.getID(), "OverrideCGX");
				mod.setMinValue(0);
				mod.setMaxValue(c.getLength());
				modifiers.add(mod);
			}
			
			
			// Conditional motor mount parameters
			if (c instanceof MotorMount) {
				MotorMount mount = (MotorMount) c;
				if (mount.isMotorMount()) {

					// Motor overhang
					SimulationModifier mod = new GenericComponentModifier(
							trans.get("optimization.modifier.motormount.overhang"),
							trans.get("optimization.modifier.motormount.overhang.desc"),
							c, UnitGroup.UNITS_LENGTH,
							1.0, c.getClass(), c.getID(), "MotorOverhang");
					setDefaultMinMax(mod, simulation);
					modifiers.add(mod);

					// Motor ignition delay
					mod = new FlightConfigurationModifier<MotorConfiguration>(
							trans.get("optimization.modifier.motormount.delay"),
							trans.get("optimization.modifier.motormount.delay.desc"),
							c, UnitGroup.UNITS_SHORT_TIME,
							1.0, c.getClass(), c.getID(), "MotorConfigurationSet",
							MotorConfiguration.class,
							"IgnitionDelay");
					mod.setMinValue(0);
					mod.setMaxValue(5);
					modifiers.add(mod);
				}
			}
			
			
			// Inner component positioning
			if (c instanceof InternalComponent) {
				RocketComponent parent = c.getParent();
				SimulationModifier mod = new GenericComponentModifier(
						trans.get("optimization.modifier.internalcomponent.position"),
						trans.get("optimization.modifier.internalcomponent.position.desc"),
						c, UnitGroup.UNITS_LENGTH,
						1.0, c.getClass(), c.getID(), "AxialOffset");
				mod.setMinValue(0);
				mod.setMaxValue(parent.getLength());
				modifiers.add(mod);
			}
			
			
			// Custom min/max for fin set position
			if (c instanceof FinSet) {
				RocketComponent parent = c.getParent();
				SimulationModifier mod = new GenericComponentModifier(
						trans.get("optimization.modifier.finset.position"),
						trans.get("optimization.modifier.finset.position.desc"),
						c, UnitGroup.UNITS_LENGTH,
						1.0, c.getClass(), c.getID(), "AxialOffset");
				mod.setMinValue(0);
				mod.setMaxValue(parent.getLength());
				modifiers.add(mod);
			}
			
			
			// Custom min/max for launch lug position
			if (c instanceof LaunchLug) {
				RocketComponent parent = c.getParent();
				SimulationModifier mod = new GenericComponentModifier(
						trans.get("optimization.modifier.launchlug.position"),
						trans.get("optimization.modifier.launchlug.position.desc"),
						c, UnitGroup.UNITS_LENGTH,
						1.0, c.getClass(), c.getID(), "AxialOffset");
				mod.setMinValue(0);
				mod.setMaxValue(parent.getLength());
				modifiers.add(mod);
			}
			
			
			// Recovery device deployment altitude and delay
			if (c instanceof RecoveryDevice) {
				SimulationModifier mod = new FlightConfigurationModifier<DeploymentConfiguration>(
						trans.get("optimization.modifier.recoverydevice.deployDelay"),
						trans.get("optimization.modifier.recoverydevice.deployDelay.desc"),
						c,
						UnitGroup.UNITS_SHORT_TIME,
						1.0,
						c.getClass(),
						c.getID(),
						"DeploymentConfigurations",
						DeploymentConfiguration.class,
						"DeployDelay");
				
				mod.setMinValue(0);
				mod.setMaxValue(10);
				modifiers.add(mod);
				
				mod = new FlightConfigurationModifier<DeploymentConfiguration>(
						trans.get("optimization.modifier.recoverydevice.deployAltitude"),
						trans.get("optimization.modifier.recoverydevice.deployAltitude.desc"),
						c,
						UnitGroup.UNITS_DISTANCE,
						1.0,
						c.getClass(),
						c.getID(),
						"DeploymentConfigurations",
						DeploymentConfiguration.class,
						"DeployAltitude") {
					
					@Override
					public void initialize(Simulation simulation) throws OptimizationException {
						DeploymentConfiguration config = getModifiedObject(simulation);
						config.setDeployEvent(DeployEvent.APOGEE);
					}
					
				};
				setDefaultMinMax(mod, simulation);
				modifiers.add(mod);
			}
			
			
			// Conditional shape parameter of Transition
			if (c instanceof Transition) {
				Transition transition = (Transition) c;
				Transition.Shape shape = transition.getShapeType();
				if (shape.usesParameter()) {
					SimulationModifier mod = new GenericComponentModifier(
							trans.get("optimization.modifier." + c.getClass().getSimpleName().toLowerCase(Locale.ENGLISH) + ".shapeparameter"),
							trans.get("optimization.modifier." + c.getClass().getSimpleName().toLowerCase(Locale.ENGLISH) + ".shapeparameter.desc"),
							c, UnitGroup.UNITS_NONE,
							1.0, c.getClass(), c.getID(), "ShapeParameter");
					mod.setMinValue(shape.minParameter());
					mod.setMaxValue(shape.maxParameter());
					modifiers.add(mod);
				}
			}
		}
		
		return modifiers;
	}
	
	private void setDefaultMinMax(SimulationModifier mod, Simulation simulation) {
		try {
			double current = mod.getCurrentSIValue(simulation);
			mod.setMinValue(current / DEFAULT_RANGE_MULTIPLIER);
			mod.setMaxValue(current * DEFAULT_RANGE_MULTIPLIER);
		} catch (OptimizationException e) {
			throw new BugException("Simulation modifier threw exception", e);
		}
	}
	
	
	/*
	 * String modifierName, Object relatedObject, UnitGroup unitGroup,
			double multiplier, Class<? extends RocketComponent> componentClass, String componentId, String methodName
	 */
	
	private static class ModifierDefinition {
		private final String modifierNameKey;
		private final String modifierDescriptionKey;
		private final UnitGroup unitGroup;
		private final double multiplier;
		private final Class<? extends RocketComponent> componentClass;
		private final String methodName;
		private final String autoMethod;
		
		
		public ModifierDefinition(String modifierNameKey, String modifierDescriptionKey, UnitGroup unitGroup,
				double multiplier, Class<? extends RocketComponent> componentClass, String methodName, String autoMethod) {
			this.modifierNameKey = modifierNameKey;
			this.modifierDescriptionKey = modifierDescriptionKey;
			this.unitGroup = unitGroup;
			this.multiplier = multiplier;
			this.componentClass = componentClass;
			this.methodName = methodName;
			this.autoMethod = autoMethod;
		}
		
	}
}
