package net.sf.openrocket.preset;

import static net.sf.openrocket.preset.ComponentPreset.FORE_OUTER_DIAMETER;
import static net.sf.openrocket.preset.ComponentPreset.INNER_DIAMETER;
import static net.sf.openrocket.preset.ComponentPreset.LENGTH;
import static net.sf.openrocket.preset.ComponentPreset.MANUFACTURER;
import static net.sf.openrocket.preset.ComponentPreset.MASS;
import static net.sf.openrocket.preset.ComponentPreset.MATERIAL;
import static net.sf.openrocket.preset.ComponentPreset.OUTER_DIAMETER;
import static net.sf.openrocket.preset.ComponentPreset.PARTNO;
import static net.sf.openrocket.preset.ComponentPreset.SHAPE;
import static net.sf.openrocket.preset.ComponentPreset.THICKNESS;
import static net.sf.openrocket.preset.ComponentPreset.TYPE;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Transition;

public abstract class ComponentPresetFactory {

	public static ComponentPreset create( TypedPropertyMap props ) throws InvalidComponentPresetException {

		ComponentPreset preset = new ComponentPreset();
		// First do validation.
		if ( !props.containsKey(TYPE)) {
			throw new InvalidComponentPresetException("No Type specified " + props.toString() );
		}

		if (!props.containsKey(MANUFACTURER)) {
			throw new InvalidComponentPresetException("No Manufacturer specified " + props.toString() );
		}

		if (!props.containsKey(PARTNO)) {
			throw new InvalidComponentPresetException("No PartNo specified " + props.toString() );
		}

		preset.putAll(props);

		// Should check for various bits of each of the types.
		Type t = props.get(TYPE);
		switch ( t ) {
		case BODY_TUBE: {
			makeBodyTube(preset);
			break;
		}
		case NOSE_CONE: {
			makeNoseCone(preset);
			break;
		}
		case TRANSITION: {
			makeTransition(preset);
			break;
		}
		case BULK_HEAD: {
			makeBulkHead(preset);
			break;
		}
		}

		preset.computeDigest();

		return preset;

	}

	private static void makeBodyTube( ComponentPreset preset ) throws InvalidComponentPresetException {
		
		checkRequiredFields( preset, LENGTH );

		BodyTube bt = new BodyTube();

		bt.setLength(preset.get(LENGTH));

		// Need to verify contains 2 of OD, thickness, ID.  Compute the third.
		boolean hasOd = preset.has(OUTER_DIAMETER);
		boolean hasId = preset.has(INNER_DIAMETER);
		boolean hasThickness = preset.has(THICKNESS);

		if ( hasOd ) {
			double outerRadius = preset.get(OUTER_DIAMETER)/2.0;
			double thickness = 0;
			bt.setOuterRadius( outerRadius );
			if ( hasId ) {
				thickness = outerRadius - preset.get(INNER_DIAMETER)/2.0;
			} else if ( hasThickness ) {
				thickness = preset.get(THICKNESS);
			} else {
				throw new InvalidComponentPresetException("Body tube preset underspecified " + preset.toString());
			}
			bt.setThickness( thickness );
		} else {
			if ( ! hasId && ! hasThickness ) {
				throw new InvalidComponentPresetException("Body tube preset underspecified " + preset.toString());
			}
			double innerRadius = preset.get(INNER_DIAMETER)/2.0;
			double thickness = preset.get(THICKNESS);
			bt.setOuterRadius(innerRadius + thickness);
			bt.setThickness(thickness);
		}

		preset.put(OUTER_DIAMETER, bt.getOuterRadius() *2.0);
		preset.put(INNER_DIAMETER, bt.getInnerRadius() *2.0);
		preset.put(THICKNESS, bt.getThickness());

		// Need to translate Mass to Density.
		if ( preset.has(MASS) ) {
			String materialName = "TubeCustom";
			if ( preset.has(MATERIAL) ) {
				materialName = preset.get(MATERIAL).getName();
			}
			Material m = Material.newMaterial(Material.Type.BULK, materialName, preset.get(MASS)/bt.getComponentVolume(), false);
			preset.put(MATERIAL, m);
		}


	}

	private static void makeNoseCone( ComponentPreset preset ) throws InvalidComponentPresetException {

		checkRequiredFields( preset, LENGTH, SHAPE, OUTER_DIAMETER );

		if ( preset.has(MASS) ) {
			// compute a density for this component
			double mass = preset.get(MASS);
			NoseCone nc = new NoseCone();
			nc.loadPreset(preset);
			double density = mass / nc.getComponentVolume();

			String materialName = "NoseConeCustom";
			if ( preset.has(MATERIAL) ) {
				materialName = preset.get(MATERIAL).getName();
			}

			Material m = Material.newMaterial(Material.Type.BULK, materialName,density, false);
			preset.put(MATERIAL, m);

		}

	}

	private static void makeTransition( ComponentPreset preset ) throws InvalidComponentPresetException {
		checkRequiredFields(preset, LENGTH, OUTER_DIAMETER, FORE_OUTER_DIAMETER);

		if ( preset.has(MASS) ) {
			// compute a density for this component
			double mass = preset.get(MASS);
			Transition tr = new Transition();
			tr.loadPreset(preset);
			double density = mass / tr.getComponentVolume();

			String materialName = "TransitionCustom";
			if ( preset.has(MATERIAL) ) {
				materialName = preset.get(MATERIAL).getName();
			}

			Material m = Material.newMaterial(Material.Type.BULK, materialName,density, false);
			preset.put(MATERIAL, m);

		}

	}

	private static void makeBulkHead( ComponentPreset preset ) throws InvalidComponentPresetException {
		checkRequiredFields(preset, LENGTH, OUTER_DIAMETER );

		if ( preset.has(MASS) ) {
			// compute a density for this component
			double mass = preset.get(MASS);
			Bulkhead tr = new Bulkhead();
			tr.loadPreset(preset);
			// FIXME - Bulkhead.getComponentVolume does not exist!
			// double density = mass / tr.getComponentVolume();

			double volume = Math.pow(preset.get(OUTER_DIAMETER),2) * Math.PI / 4.0;
			double density = mass / volume;

			String materialName = "TransitionCustom";
			if ( preset.has(MATERIAL) ) {
				materialName = preset.get(MATERIAL).getName();
			}

			Material m = Material.newMaterial(Material.Type.BULK, materialName,density, false);
			preset.put(MATERIAL, m);

		}

	}

	private static void checkRequiredFields( ComponentPreset preset, TypedKey<?> ... keys ) throws InvalidComponentPresetException {
		for( TypedKey<?> key: keys ) {
			if (! preset.has(key) ) {
				throw new InvalidComponentPresetException( "No " + key.getName() + " specified for " + preset.getType().name() + " preset " + preset.toString());
			}
		}
	}

}
