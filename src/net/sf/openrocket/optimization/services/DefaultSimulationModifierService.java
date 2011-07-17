package net.sf.openrocket.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.rocketoptimization.modifiers.GenericComponentModifier;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class DefaultSimulationModifierService implements SimulationModifierService {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final Map<Class<?>, List<ModifierDefinition>> definitions = new HashMap<Class<?>, List<ModifierDefinition>>();
	static {
		//addModifier("optimization.modifier.", unitGroup, multiplier, componentClass, methodName);
		
		/*
		 * Note:  Each component type must contain only mutually exclusive variables.
		 * For example, body tube does not have inner diameter definition because it is
		 * defined by the outer diameter and thickness.
		 */

		addModifier("optimization.modifier.nosecone.length", UnitGroup.UNITS_LENGTH, 1.0, NoseCone.class, "Length");
		addModifier("optimization.modifier.nosecone.diameter", UnitGroup.UNITS_LENGTH, 2.0, NoseCone.class, "AftRadius");
		addModifier("optimization.modifier.nosecone.thickness", UnitGroup.UNITS_LENGTH, 1.0, NoseCone.class, "Thickness");
		
		addModifier("optimization.modifier.transition.length", UnitGroup.UNITS_LENGTH, 1.0, Transition.class, "Length");
		addModifier("optimization.modifier.transition.forediameter", UnitGroup.UNITS_LENGTH, 2.0, Transition.class, "ForeRadius");
		addModifier("optimization.modifier.transition.aftdiameter", UnitGroup.UNITS_LENGTH, 2.0, Transition.class, "AftRadius");
		addModifier("optimization.modifier.transition.thickness", UnitGroup.UNITS_LENGTH, 1.0, Transition.class, "Thickness");
		
		addModifier("optimization.modifier.bodytube.length", UnitGroup.UNITS_LENGTH, 1.0, BodyTube.class, "Length");
		addModifier("optimization.modifier.bodytube.outerDiameter", UnitGroup.UNITS_LENGTH, 2.0, BodyTube.class, "OuterRadius");
		addModifier("optimization.modifier.bodytube.thickness", UnitGroup.UNITS_LENGTH, 1.0, BodyTube.class, "Thickness");
		

	}
	
	private static void addModifier(String modifierNameKey, UnitGroup unitGroup, double multiplier,
				Class<? extends RocketComponent> componentClass, String methodName) {
		
		List<ModifierDefinition> list = definitions.get(componentClass);
		if (list == null) {
			list = new ArrayList<DefaultSimulationModifierService.ModifierDefinition>();
			definitions.put(componentClass, list);
		}
		
		ModifierDefinition definition = new ModifierDefinition(modifierNameKey, unitGroup, multiplier, componentClass, methodName);
		list.add(definition);
	}
	
	


	@Override
	public Collection<SimulationModifier> getModifiers(OpenRocketDocument document) {
		List<SimulationModifier> modifiers = new ArrayList<SimulationModifier>();
		
		Rocket rocket = document.getRocket();
		for (RocketComponent c : rocket) {
			
			// Attribute modifiers
			List<ModifierDefinition> list = definitions.get(c.getClass());
			if (list != null) {
				for (ModifierDefinition def : list) {
					SimulationModifier mod = new GenericComponentModifier(
							trans.get(def.modifierNameKey), c, def.unitGroup, def.multiplier, def.componentClass,
							c.getID(), def.methodName);
					modifiers.add(mod);
				}
			}
			
			// TODO: HIGH: Conditional modifiers (overrides)
			
			// TODO: Transition / Nose cone shape parameter (conditional)
		}
		
		return modifiers;
	}
	
	

	/*
	 * String modifierName, Object relatedObject, UnitGroup unitGroup,
			double multiplier, Class<? extends RocketComponent> componentClass, String componentId, String methodName
	 */

	private static class ModifierDefinition {
		private final String modifierNameKey;
		private final UnitGroup unitGroup;
		private final double multiplier;
		private final Class<? extends RocketComponent> componentClass;
		private final String methodName;
		
		
		public ModifierDefinition(String modifierNameKey, UnitGroup unitGroup, double multiplier,
				Class<? extends RocketComponent> componentClass, String methodName) {
			super();
			this.modifierNameKey = modifierNameKey;
			this.unitGroup = unitGroup;
			this.multiplier = multiplier;
			this.componentClass = componentClass;
			this.methodName = methodName;
		}
		
	}
}
