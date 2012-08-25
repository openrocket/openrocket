package net.sf.openrocket.preset;

import static net.sf.openrocket.preset.ComponentPreset.*;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Transition;

public abstract class ComponentPresetFactory {
	
	public static ComponentPreset create(TypedPropertyMap props) throws InvalidComponentPresetException {
		
		InvalidComponentPresetException exceptions = new InvalidComponentPresetException("Invalid preset specification.");
		
		ComponentPreset preset = new ComponentPreset();
		// First do validation.
		if (!props.containsKey(MANUFACTURER)) {
			exceptions.addInvalidParameter(MANUFACTURER, "No Manufacturer specified");
		}
		if (!props.containsKey(PARTNO)) {
			exceptions.addInvalidParameter(PARTNO, "No PartNo specified");
		}
		if (!props.containsKey(TYPE)) {
			exceptions.addInvalidParameter(TYPE, "No Type specified");
			// We can't do anything else without TYPE so throw immediately.
			throw exceptions;
		}
		
		
		preset.putAll(props);
		
		// Should check for various bits of each of the types.
		Type t = props.get(TYPE);
		switch (t) {
		case BODY_TUBE: {
			makeBodyTube(exceptions, preset);
			break;
		}
		case NOSE_CONE: {
			makeNoseCone(exceptions, preset);
			break;
		}
		case TRANSITION: {
			makeTransition(exceptions, preset);
			break;
		}
		case BULK_HEAD: {
			makeBulkHead(exceptions, preset);
			break;
		}
		case TUBE_COUPLER: {
			// For now TUBE_COUPLER is the same as BODY_TUBE
			makeBodyTube(exceptions, preset);
			break;
		}
		case CENTERING_RING: {
			makeCenteringRing(exceptions, preset);
			break;
		}
		case ENGINE_BLOCK: {
			makeEngineBlock(exceptions, preset);
			break;
		}
		case LAUNCH_LUG: {
			// Same processing as BODY_TUBE
			makeBodyTube(exceptions, preset);
			break;
		}
		case STREAMER: {
			makeStreamer(exceptions, preset);
			break;
		}
		case PARACHUTE: {
			makeParachute(exceptions, preset);
			break;
		}
		}
		
		if (exceptions.hasProblems()) {
			throw exceptions;
		}
		
		preset.computeDigest();
		
		return preset;
		
	}
	
	private static void makeBodyTube(InvalidComponentPresetException exceptions, ComponentPreset preset) throws InvalidComponentPresetException {
		
		checkRequiredFields(exceptions, preset, LENGTH);
		
		checkDiametersAndThickness(exceptions, preset);
		
		double volume = computeVolumeOfTube(preset);
		
		// Need to translate Mass to Density.
		if (preset.has(MASS)) {
			String materialName = "TubeCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, preset.get(MASS) / volume);
			preset.put(MATERIAL, m);
		}
		
		
	}
	
	private static void makeNoseCone(InvalidComponentPresetException exceptions, ComponentPreset preset) {
		
		checkRequiredFields(exceptions, preset, LENGTH, SHAPE, AFT_OUTER_DIAMETER);
		
		if (preset.has(MASS)) {
			// compute a density for this component
			double mass = preset.get(MASS);
			NoseCone nc = new NoseCone();
			nc.loadPreset(preset);
			double density = mass / nc.getComponentVolume();
			
			String materialName = "NoseConeCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, density);
			preset.put(MATERIAL, m);
			
		}
		
	}
	
	private static void makeTransition(InvalidComponentPresetException exceptions, ComponentPreset preset) {
		checkRequiredFields(exceptions, preset, LENGTH, AFT_OUTER_DIAMETER, FORE_OUTER_DIAMETER);
		
		if (preset.has(MASS)) {
			// compute a density for this component
			double mass = preset.get(MASS);
			Transition tr = new Transition();
			tr.loadPreset(preset);
			double density = mass / tr.getComponentVolume();
			
			String materialName = "TransitionCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, density);
			preset.put(MATERIAL, m);
			
		}
		
	}
	
	private static void makeBulkHead(InvalidComponentPresetException exceptions, ComponentPreset preset) {
		checkRequiredFields(exceptions, preset, LENGTH, OUTER_DIAMETER);
		
		if (preset.has(MASS)) {
			// compute a density for this component
			double mass = preset.get(MASS);
			
			double volume = computeVolumeOfTube(preset);
			double density = mass / volume;
			
			String materialName = "BulkHeadCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, density);
			preset.put(MATERIAL, m);
			
		}
		
	}
	
	private static void makeCenteringRing(InvalidComponentPresetException exceptions, ComponentPreset preset) throws InvalidComponentPresetException {
		checkRequiredFields(exceptions, preset, LENGTH);
		
		checkDiametersAndThickness(exceptions, preset);
		
		double volume = computeVolumeOfTube(preset);
		
		// Need to translate Mass to Density.
		if (preset.has(MASS)) {
			String materialName = "CenteringRingCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, preset.get(MASS) / volume);
			preset.put(MATERIAL, m);
		}
		
	}
	
	private static void makeEngineBlock(InvalidComponentPresetException exceptions, ComponentPreset preset) throws InvalidComponentPresetException {
		checkRequiredFields(exceptions, preset, LENGTH);
		
		checkDiametersAndThickness(exceptions, preset);
		
		double volume = computeVolumeOfTube(preset);
		
		// Need to translate Mass to Density.
		if (preset.has(MASS)) {
			String materialName = "EngineBlockCustom";
			if (preset.has(MATERIAL)) {
				materialName = preset.get(MATERIAL).getName();
			}
			Material m = Databases.findMaterial(Material.Type.BULK, materialName, preset.get(MASS) / volume);
			preset.put(MATERIAL, m);
		}
		
	}
	
	private static void makeStreamer(InvalidComponentPresetException exceptions, ComponentPreset preset) {
		checkRequiredFields(exceptions, preset, LENGTH, WIDTH);
	}
	
	private static void makeParachute(InvalidComponentPresetException exceptions, ComponentPreset preset) {
		checkRequiredFields(exceptions, preset, DIAMETER, LINE_COUNT, LINE_LENGTH);
	}
	
	
	private static void checkRequiredFields(InvalidComponentPresetException exceptions, ComponentPreset preset, TypedKey<?>... keys) {
		for (TypedKey<?> key : keys) {
			if (!preset.has(key)) {
				exceptions.addInvalidParameter(key, "No " + key.getName() + " specified");
			}
		}
	}
	
	private static void checkDiametersAndThickness(InvalidComponentPresetException exceptions, ComponentPreset preset) throws InvalidComponentPresetException {
		// Need to verify contains 2 of OD, thickness, ID.  Compute the third.
		boolean hasOd = preset.has(OUTER_DIAMETER);
		boolean hasId = preset.has(INNER_DIAMETER);
		boolean hasThickness = preset.has(THICKNESS);
		
		double outerRadius;
		double innerRadius;
		double thickness;
		
		if (hasOd) {
			outerRadius = preset.get(OUTER_DIAMETER) / 2.0;
			thickness = 0;
			if (hasId) {
				innerRadius = preset.get(INNER_DIAMETER) / 2.0;
				thickness = outerRadius - innerRadius;
			} else if (hasThickness) {
				thickness = preset.get(THICKNESS);
				innerRadius = outerRadius - thickness;
			} else {
				exceptions.addMessage("Preset dimensions underspecified");
				throw exceptions;
			}
		} else {
			if (!hasId || !hasThickness) {
				exceptions.addMessage("Preset dimensions underspecified");
				throw exceptions;
			}
			innerRadius = preset.get(INNER_DIAMETER) / 2.0;
			thickness = preset.get(THICKNESS);
			outerRadius = innerRadius + thickness;
		}
		
		preset.put(OUTER_DIAMETER, outerRadius * 2.0);
		preset.put(INNER_DIAMETER, innerRadius * 2.0);
		preset.put(THICKNESS, thickness);
		
	}
	
	private static double computeVolumeOfTube(ComponentPreset preset) {
		double or = preset.get(OUTER_DIAMETER) / 2.0;
		double ir = preset.has(INNER_DIAMETER) ? preset.get(INNER_DIAMETER) / 2.0 : 0.0;
		double l = preset.get(LENGTH);
		return Math.PI * (or * or - ir * ir) * l;
	}
	
	
}
