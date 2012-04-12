package net.sf.openrocket.preset;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.TextUtil;


/**
 * A model for a preset component.
 * <p>
 * A preset component contains a component class type, manufacturer information,
 * part information, and a method that returns a prototype of the preset component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
// FIXME - Implement clone.
public class ComponentPreset implements Comparable<ComponentPreset> {

	private final TypedPropertyMap properties = new TypedPropertyMap();

	private boolean favorite = false;
	private String digest = "";

	public enum Type {
		BODY_TUBE( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.LENGTH} ),
				
		NOSE_CONE( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.SHAPE,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH} ) ;

		Type[] compatibleTypes;
		TypedKey<?>[] displayedColumns;

		Type( TypedKey<?>[] displayedColumns) {
			compatibleTypes = new Type[1];
			compatibleTypes[0] = this;
			this.displayedColumns = displayedColumns;
		}

		Type( Type[] t, TypedKey<?>[] displayedColumns ) {

			compatibleTypes = new Type[t.length+1];
			compatibleTypes[0] = this;
			for( int i=0; i<t.length; i++ ) {
				compatibleTypes[i+1] = t[i];
			}

			this.displayedColumns = displayedColumns;
		}

		public Type[] getCompatibleTypes() {
			return compatibleTypes;
		}

		public TypedKey<?>[] getDisplayedColumns() {
			return displayedColumns;
		}

	}

	public final static TypedKey<Manufacturer> MANUFACTURER = new TypedKey<Manufacturer>("Manufacturer", Manufacturer.class);
	public final static TypedKey<String> PARTNO = new TypedKey<String>("PartNo",String.class);
	public final static TypedKey<String> DESCRIPTION = new TypedKey<String>("Description", String.class);
	public final static TypedKey<Type> TYPE = new TypedKey<Type>("Type",Type.class);
	public final static TypedKey<Double> LENGTH = new TypedKey<Double>("Length", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> INNER_DIAMETER = new TypedKey<Double>("InnerDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> OUTER_DIAMETER = new TypedKey<Double>("OuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> SHOULDER_LENGTH = new TypedKey<Double>("ShoulderLength", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> SHOULDER_DIAMETER = new TypedKey<Double>("ShoulderDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Shape> SHAPE = new TypedKey<Shape>("Shape", Shape.class);
	public final static TypedKey<Material> MATERIAL = new TypedKey<Material>("Material", Material.class);
	public final static TypedKey<Finish> FINISH = new TypedKey<Finish>("Finish", Finish.class);
	public final static TypedKey<Double> THICKNESS = new TypedKey<Double>("Thickness", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Boolean> FILLED = new TypedKey<Boolean>("Filled", Boolean.class);
	public final static TypedKey<Double> CG_OVERRIDE = new TypedKey<Double>("CGOverride", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> MASS = new TypedKey<Double>("Mass", Double.class, UnitGroup.UNITS_MASS);

	public final static Map<String, TypedKey<?>> keyMap = new HashMap<String, TypedKey<?>>();
	static {
		keyMap.put(MANUFACTURER.getName(), MANUFACTURER);
		keyMap.put(PARTNO.getName(), PARTNO);
		keyMap.put(TYPE.getName(), TYPE);
		keyMap.put(DESCRIPTION.getName(), DESCRIPTION);
		keyMap.put(LENGTH.getName(), LENGTH);
		keyMap.put(INNER_DIAMETER.getName(), INNER_DIAMETER);
		keyMap.put(OUTER_DIAMETER.getName(), OUTER_DIAMETER);
		keyMap.put(SHOULDER_LENGTH.getName(), SHOULDER_LENGTH);
		keyMap.put(SHOULDER_DIAMETER.getName(), SHOULDER_DIAMETER);
		keyMap.put(SHAPE.getName(), SHAPE);
		keyMap.put(MATERIAL.getName(), MATERIAL);
		keyMap.put(FINISH.getName(), FINISH);
		keyMap.put(THICKNESS.getName(), THICKNESS);
		keyMap.put(FILLED.getName(), FILLED);
		keyMap.put(CG_OVERRIDE.getName(), CG_OVERRIDE);
		keyMap.put(MASS.getName(), MASS);
	}

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

		preset.properties.putAll(props);

		// Should check for various bits of each of the types.
		Type t = props.get(TYPE);
		switch ( t ) {
		case BODY_TUBE: {

			if ( !props.containsKey(LENGTH) ) {
				throw new InvalidComponentPresetException( "No Length specified for body tube preset " + props.toString());
			}

			BodyTube bt = new BodyTube();

			bt.setLength(props.get(LENGTH));

			// Need to verify contains 2 of OD, thickness, ID.  Compute the third.
			boolean hasOd = props.containsKey(OUTER_DIAMETER);
			boolean hasId = props.containsKey(INNER_DIAMETER);
			boolean hasThickness = props.containsKey(THICKNESS);

			if ( hasOd ) {
				double outerRadius = props.get(OUTER_DIAMETER)/2.0;
				double thickness = 0;
				bt.setOuterRadius( outerRadius );
				if ( hasId ) {
					thickness = outerRadius - props.get(INNER_DIAMETER)/2.0;
				} else if ( hasThickness ) {
					thickness = props.get(THICKNESS);
				} else {
					throw new InvalidComponentPresetException("Body tube preset underspecified " + props.toString());
				}
				bt.setThickness( thickness );
			} else {
				if ( ! hasId && ! hasThickness ) {
					throw new InvalidComponentPresetException("Body tube preset underspecified " + props.toString());
				}
				double innerRadius = props.get(INNER_DIAMETER)/2.0;
				double thickness = props.get(THICKNESS);
				bt.setOuterRadius(innerRadius + thickness);
				bt.setThickness(thickness);
			}

			preset.properties.put(OUTER_DIAMETER, bt.getOuterRadius() *2.0);
			preset.properties.put(INNER_DIAMETER, bt.getInnerRadius() *2.0);
			preset.properties.put(THICKNESS, bt.getThickness());

			// Need to translate Mass to Density.
			if ( props.containsKey(MASS) ) {
				String materialName = "TubeCustom";
				if ( props.containsKey(MATERIAL) ) {
					materialName = props.get(MATERIAL).getName();
				}
				Material m = Material.newMaterial(Material.Type.BULK, materialName, props.get(MASS)/bt.getComponentVolume(), false);
				preset.properties.put(MATERIAL, m);
			}

			break;
		}
		case NOSE_CONE: {
			if ( !props.containsKey(LENGTH) ) {
				throw new InvalidComponentPresetException( "No Length specified for nose cone preset " + props.toString());
			}
			if ( !props.containsKey(SHAPE) ) {
				throw new InvalidComponentPresetException( "No Shape specified for nose cone preset " + props.toString());
			}
			if ( !props.containsKey(OUTER_DIAMETER) ) {
				throw new InvalidComponentPresetException( "No Outer Diameter specified for nose cone preset " + props.toString());
			}
			break;
		}
		}

		preset.computeDigest();

		return preset;

	}

	// Private constructor to encourage use of factory.
	private ComponentPreset() {
	}

	/**
	 * Convenience method to retrieve the Type of this ComponentPreset.
	 * 
	 * @return
	 */
	public Type getType() {
		return properties.get(TYPE);
	}

	/**
	 * Convenience method to retrieve the Manufacturer of this ComponentPreset.
	 * @return
	 */
	public Manufacturer getManufacturer() {
		return properties.get(MANUFACTURER);
	}

	/**
	 * Convenience method to retrieve the PartNo of this ComponentPreset.
	 * @return
	 */
	public String getPartNo() {
		return properties.get(PARTNO);
	}

	public String getDigest() {
		return digest;
	}

	public boolean has(Object key) {
		return properties.containsKey(key);
	}

	public <T> T get(TypedKey<T> key) {
		T value = properties.get(key);
		if (value == null) {
			throw new BugException("Preset did not contain key " + key + " " + properties.toString());
		}
		return (T) value;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	@Override
	public int compareTo(ComponentPreset p2) {
		int manuCompare = this.getManufacturer().getSimpleName().compareTo(p2.getManufacturer().getSimpleName());
		if ( manuCompare != 0 )
			return manuCompare;

		int partNoCompare = this.getPartNo().compareTo(p2.getPartNo());
		return partNoCompare;
	}

	@Override
	public String toString() {
		return get(MANUFACTURER).toString() + " " + get(PARTNO);
	}

	public String preferenceKey() {
		return get(MANUFACTURER).toString() + "|" + get(PARTNO);
	}

	private void computeDigest() {

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(bos);

			List<TypedKey<?>> keys = new ArrayList<TypedKey<?>>( properties.keySet());

			Collections.sort(keys, new Comparator<TypedKey<?>>() {
				@Override
				public int compare( TypedKey<?> a, TypedKey<?> b ) {
					return a.getName().compareTo(b.getName());
				}
			});

			for ( TypedKey<?> key : keys  ) {

				Object value = properties.get(key);

				os.writeBytes(key.getName());

				if ( key.getType() == Double.class ) {
					Double d = (Double) value;
					os.writeDouble(d);
				} else if (key.getType() == String.class ) {
					String s = (String) value;
					os.writeBytes(s);
				} else if (key.getType() == Manufacturer.class ) {
					String s = ((Manufacturer)value).getSimpleName();
					os.writeBytes(s);
				} else if ( key.getType() == Finish.class ) {
					String s = ((Finish)value).name();
					os.writeBytes(s);
				} else if ( key.getType() == Type.class ) {
					String s = ((Type)value).name();
					os.writeBytes(s);
				} else if ( key.getType() == Boolean.class ) {
					Boolean b = (Boolean) value;
					os.writeBoolean(b);
				} else if ( key.getType() == Material.class ) {
					double d = ((Material)value).getDensity();
					os.writeDouble(d);
				} else if ( key.getType() == Shape.class ) {
					// FIXME - this is ugly to use the ordinal but what else?
					int i = ((Shape)value).ordinal();
					os.writeInt(i);
				}

			}

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			digest = TextUtil.hexString(md5.digest( bos.toByteArray() ));
		}
		catch ( Exception e ) {
			throw new BugException(e);
		}
	}

}
