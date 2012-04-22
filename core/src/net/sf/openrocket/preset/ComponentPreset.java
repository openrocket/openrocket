package net.sf.openrocket.preset;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.TextUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH} ),

		NOSE_CONE( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.SHAPE,
				ComponentPreset.AFT_OUTER_DIAMETER,
				ComponentPreset.AFT_SHOULDER_DIAMETER,
				ComponentPreset.LENGTH} ),

		TRANSITION( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.SHAPE,
				ComponentPreset.FORE_OUTER_DIAMETER,
				ComponentPreset.FORE_SHOULDER_DIAMETER,
				ComponentPreset.FORE_SHOULDER_LENGTH,
				ComponentPreset.AFT_OUTER_DIAMETER,
				ComponentPreset.AFT_SHOULDER_DIAMETER,
				ComponentPreset.AFT_SHOULDER_LENGTH,
				ComponentPreset.LENGTH} ),

		TUBE_COUPLER( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.LENGTH} ),

		BULK_HEAD( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH} ),

		CENTERING_RING( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH} ),

		ENGINE_BLOCK( new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH} );

		TypedKey<?>[] displayedColumns;

		Type( TypedKey<?>[] displayedColumns) {
			this.displayedColumns = displayedColumns;
		}

		public List<Type> getCompatibleTypes() {
			return compatibleTypeMap.get(Type.this);
		}

		public TypedKey<?>[] getDisplayedColumns() {
			return displayedColumns;
		}

		private static Map<Type,List<Type>> compatibleTypeMap = new HashMap<Type,List<Type>>();

		static {
			compatibleTypeMap.put( BODY_TUBE, Arrays.asList( BODY_TUBE, TUBE_COUPLER) );
			compatibleTypeMap.put( TUBE_COUPLER, Arrays.asList( BODY_TUBE,TUBE_COUPLER) );
			compatibleTypeMap.put( CENTERING_RING, Arrays.asList( CENTERING_RING, ENGINE_BLOCK ) );
			compatibleTypeMap.put( NOSE_CONE, Arrays.asList( NOSE_CONE, TRANSITION));
		}

	}

	public final static TypedKey<Manufacturer> MANUFACTURER = new TypedKey<Manufacturer>("Manufacturer", Manufacturer.class);
	public final static TypedKey<String> PARTNO = new TypedKey<String>("PartNo",String.class);
	public final static TypedKey<String> DESCRIPTION = new TypedKey<String>("Description", String.class);
	public final static TypedKey<Type> TYPE = new TypedKey<Type>("Type",Type.class);
	public final static TypedKey<Double> LENGTH = new TypedKey<Double>("Length", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> INNER_DIAMETER = new TypedKey<Double>("InnerDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> OUTER_DIAMETER = new TypedKey<Double>("OuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_SHOULDER_LENGTH = new TypedKey<Double>("ForeShoulderLength",Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_SHOULDER_DIAMETER = new TypedKey<Double>("ForeShoulderDiameter",Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_OUTER_DIAMETER = new TypedKey<Double>("ForeOuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_SHOULDER_LENGTH = new TypedKey<Double>("AftShoulderLength",Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_SHOULDER_DIAMETER = new TypedKey<Double>("AftShoulderDiameter",Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_OUTER_DIAMETER = new TypedKey<Double>("AftOuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Shape> SHAPE = new TypedKey<Shape>("Shape", Shape.class);
	public final static TypedKey<Material> MATERIAL = new TypedKey<Material>("Material", Material.class);
	public final static TypedKey<Finish> FINISH = new TypedKey<Finish>("Finish", Finish.class);
	public final static TypedKey<Double> THICKNESS = new TypedKey<Double>("Thickness", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Boolean> FILLED = new TypedKey<Boolean>("Filled", Boolean.class);
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
		keyMap.put(FORE_SHOULDER_LENGTH.getName(), FORE_SHOULDER_LENGTH);
		keyMap.put(FORE_SHOULDER_DIAMETER.getName(), FORE_SHOULDER_DIAMETER);
		keyMap.put(FORE_OUTER_DIAMETER.getName(), FORE_OUTER_DIAMETER);
		keyMap.put(AFT_SHOULDER_LENGTH.getName(), AFT_SHOULDER_LENGTH);
		keyMap.put(AFT_SHOULDER_DIAMETER.getName(), AFT_SHOULDER_DIAMETER);
		keyMap.put(AFT_OUTER_DIAMETER.getName(), AFT_OUTER_DIAMETER);
		keyMap.put(SHAPE.getName(), SHAPE);
		keyMap.put(MATERIAL.getName(), MATERIAL);
		keyMap.put(FINISH.getName(), FINISH);
		keyMap.put(THICKNESS.getName(), THICKNESS);
		keyMap.put(FILLED.getName(), FILLED);
		keyMap.put(MASS.getName(), MASS);
	}

	public final static List<TypedKey<?>> orderedKeyList = Arrays.<TypedKey<?>>asList(
			MANUFACTURER,
			PARTNO,
			DESCRIPTION,
			OUTER_DIAMETER,
			FORE_OUTER_DIAMETER,
			AFT_OUTER_DIAMETER,
			INNER_DIAMETER,
			LENGTH,
			AFT_SHOULDER_DIAMETER,
			AFT_SHOULDER_LENGTH,
			FORE_SHOULDER_DIAMETER,
			FORE_SHOULDER_LENGTH,
			SHAPE,
			THICKNESS,
			FILLED,
			MASS,
			FINISH,
			MATERIAL
			);


	// package scope constructor to encourage use of factory.
	ComponentPreset() {
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

	/**
	 * Package scope so the ComponentPresetFactory can call it.
	 * @param other
	 */
	void putAll(TypedPropertyMap other) {
		if (other == null) {
			return;
		}
		properties.putAll(other);
	}

	/**
	 * Package scope so the ComponentPresetFactory can call it.
	 * @param key
	 * @param value
	 */
	<T> void put( TypedKey<T> key, T value ) {
		properties.put(key, value);
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

	/**
	 * Package scope so the factory can call it.
	 */
	void computeDigest() {

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
            e.printStackTrace();
			throw new BugException(e);
		}
	}

}
