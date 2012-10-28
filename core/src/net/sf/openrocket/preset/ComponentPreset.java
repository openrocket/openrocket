package net.sf.openrocket.preset;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
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
public class ComponentPreset implements Comparable<ComponentPreset>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3199781221967306617L;

	private final TypedPropertyMap properties = new TypedPropertyMap();

	private String digest = "";

	public enum Type {
		BODY_TUBE(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH }),

		NOSE_CONE(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.SHAPE,
				ComponentPreset.AFT_OUTER_DIAMETER,
				ComponentPreset.AFT_SHOULDER_DIAMETER,
				ComponentPreset.AFT_SHOULDER_LENGTH,
				ComponentPreset.LENGTH }),

		TRANSITION(new TypedKey<?>[] {
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
				ComponentPreset.LENGTH }),

		TUBE_COUPLER(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.LENGTH }),

		BULK_HEAD(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH }),

		CENTERING_RING(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH }),

		ENGINE_BLOCK(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH }),

		LAUNCH_LUG(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.INNER_DIAMETER,
				ComponentPreset.OUTER_DIAMETER,
				ComponentPreset.LENGTH }),

		STREAMER(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.LENGTH,
				ComponentPreset.WIDTH,
				ComponentPreset.THICKNESS,
				ComponentPreset.MATERIAL }),

		PARACHUTE(new TypedKey<?>[] {
				ComponentPreset.MANUFACTURER,
				ComponentPreset.PARTNO,
				ComponentPreset.DESCRIPTION,
				ComponentPreset.DIAMETER,
				ComponentPreset.SIDES,
				ComponentPreset.LINE_COUNT,
				ComponentPreset.LINE_LENGTH,
				ComponentPreset.LINE_MATERIAL,
				ComponentPreset.MATERIAL });

		TypedKey<?>[] displayedColumns;

		Type(TypedKey<?>[] displayedColumns) {
			this.displayedColumns = displayedColumns;
		}

		public List<Type> getCompatibleTypes() {
			return compatibleTypeMap.get(Type.this);
		}

		public TypedKey<?>[] getDisplayedColumns() {
			return displayedColumns;
		}

		private static Map<Type, List<Type>> compatibleTypeMap = new HashMap<Type, List<Type>>();

		static {
			compatibleTypeMap.put(BODY_TUBE, Arrays.asList(BODY_TUBE, TUBE_COUPLER, LAUNCH_LUG));
			compatibleTypeMap.put(TUBE_COUPLER, Arrays.asList(BODY_TUBE, TUBE_COUPLER, LAUNCH_LUG));
			compatibleTypeMap.put(LAUNCH_LUG, Arrays.asList(BODY_TUBE, TUBE_COUPLER, LAUNCH_LUG));
			compatibleTypeMap.put(CENTERING_RING, Arrays.asList(CENTERING_RING, ENGINE_BLOCK));
			compatibleTypeMap.put(NOSE_CONE, Arrays.asList(NOSE_CONE, TRANSITION));
		}

	}

	public final static TypedKey<Manufacturer> MANUFACTURER = new TypedKey<Manufacturer>("Manufacturer", Manufacturer.class);
	public final static TypedKey<String> PARTNO = new TypedKey<String>("PartNo", String.class);
	public final static TypedKey<String> DESCRIPTION = new TypedKey<String>("Description", String.class);
	public final static TypedKey<Type> TYPE = new TypedKey<Type>("Type", Type.class);
	public final static TypedKey<Double> LENGTH = new TypedKey<Double>("Length", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> WIDTH = new TypedKey<Double>("Width", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> INNER_DIAMETER = new TypedKey<Double>("InnerDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> OUTER_DIAMETER = new TypedKey<Double>("OuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_SHOULDER_LENGTH = new TypedKey<Double>("ForeShoulderLength", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_SHOULDER_DIAMETER = new TypedKey<Double>("ForeShoulderDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> FORE_OUTER_DIAMETER = new TypedKey<Double>("ForeOuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_SHOULDER_LENGTH = new TypedKey<Double>("AftShoulderLength", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_SHOULDER_DIAMETER = new TypedKey<Double>("AftShoulderDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> AFT_OUTER_DIAMETER = new TypedKey<Double>("AftOuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Shape> SHAPE = new TypedKey<Shape>("Shape", Shape.class);
	public final static TypedKey<Material> MATERIAL = new TypedKey<Material>("Material", Material.class);
	public final static TypedKey<Finish> FINISH = new TypedKey<Finish>("Finish", Finish.class);
	public final static TypedKey<Double> THICKNESS = new TypedKey<Double>("Thickness", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Boolean> FILLED = new TypedKey<Boolean>("Filled", Boolean.class);
	public final static TypedKey<Double> MASS = new TypedKey<Double>("Mass", Double.class, UnitGroup.UNITS_MASS);
	public final static TypedKey<Double> DIAMETER = new TypedKey<Double>("Diameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Integer> SIDES = new TypedKey<Integer>("Sides", Integer.class);
	public final static TypedKey<Integer> LINE_COUNT = new TypedKey<Integer>("LineCount", Integer.class);
	public final static TypedKey<Double> LINE_LENGTH = new TypedKey<Double>("LineLength", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Material> LINE_MATERIAL = new TypedKey<Material>("LineMaterial", Material.class);
	public final static TypedKey<byte[]> IMAGE = new TypedKey<byte[]>("Image", byte[].class);

	public final static List<TypedKey<?>> ORDERED_KEY_LIST = Collections.unmodifiableList(Arrays.<TypedKey<?>> asList(
			MANUFACTURER,
			PARTNO,
			DESCRIPTION,
			OUTER_DIAMETER,
			FORE_OUTER_DIAMETER,
			AFT_OUTER_DIAMETER,
			INNER_DIAMETER,
			LENGTH,
			WIDTH,
			AFT_SHOULDER_DIAMETER,
			AFT_SHOULDER_LENGTH,
			FORE_SHOULDER_DIAMETER,
			FORE_SHOULDER_LENGTH,
			SHAPE,
			THICKNESS,
			FILLED,
			DIAMETER,
			SIDES,
			LINE_COUNT,
			LINE_LENGTH,
			LINE_MATERIAL,
			MASS,
			FINISH,
			MATERIAL
			));


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
	<T> void put(TypedKey<T> key, T value) {
		properties.put(key, value);
	}

	public <T> T get(TypedKey<T> key) {
		T value = properties.get(key);
		if (value == null) {
			throw new BugException("Preset did not contain key " + key + " " + properties.toString());
		}
		return value;
	}

	@Override
	public int compareTo(ComponentPreset p2) {
		int manuCompare = this.getManufacturer().getSimpleName().compareTo(p2.getManufacturer().getSimpleName());
		if (manuCompare != 0)
			return manuCompare;

		int partNoCompare = this.getPartNo().compareTo(p2.getPartNo());
		return partNoCompare;
	}

	@Override
	public String toString() {
		return get(PARTNO);
	}

	public String preferenceKey() {
		return String.valueOf(get(MANUFACTURER)) + "|" + String.valueOf(get(PARTNO));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ComponentPreset that = (ComponentPreset) o;

		if (digest != null ? !digest.equals(that.digest) : that.digest != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return digest != null ? digest.hashCode() : 0;
	}

	/**
	 * Package scope so the factory can call it.
	 */
	void computeDigest() {

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(bos);

			List<TypedKey<?>> keys = new ArrayList<TypedKey<?>>(properties.keySet());

			Collections.sort(keys, new Comparator<TypedKey<?>>() {
				@Override
				public int compare(TypedKey<?> a, TypedKey<?> b) {
					return a.getName().compareTo(b.getName());
				}
			});

			for (TypedKey<?> key : keys) {

				Object value = properties.get(key);

				os.writeBytes(key.getName());

				if (key.getType() == Double.class) {
					Double d = (Double) value;
					os.writeDouble(d);
				} else if (key.getType() == String.class) {
					String s = (String) value;
					os.writeBytes(s);
				} else if (key.getType() == Manufacturer.class) {
					String s = ((Manufacturer) value).getSimpleName();
					os.writeBytes(s);
				} else if (key.getType() == Finish.class) {
					String s = ((Finish) value).name();
					os.writeBytes(s);
				} else if (key.getType() == Type.class) {
					String s = ((Type) value).name();
					os.writeBytes(s);
				} else if (key.getType() == Boolean.class) {
					Boolean b = (Boolean) value;
					os.writeBoolean(b);
				} else if (key.getType() == Material.class) {
					double d = ((Material) value).getDensity();
					os.writeDouble(d);
				} else if (key.getType() == Shape.class) {
					// this is ugly to use the ordinal but what else?
					int i = ((Shape) value).ordinal();
					os.writeInt(i);
				}

			}

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			digest = TextUtil.hexString(md5.digest(bos.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new BugException(e);
		}
	}

	private static class MaterialSerializationProxy implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8704894438168047622L;
		String name;
		String type;
		boolean userDefined;
		Double density;
	}

	private void writeObject( ObjectOutputStream oos ) throws IOException {
		Map<String,Object> DTO = new HashMap<String,Object>();

		for ( Entry<TypedKey<?>, Object> entry :properties.entrySet() ) {

			TypedKey<?> key = entry.getKey();
			Object value = entry.getValue();

			String keyName = key.getName();
			if ( value instanceof Material ) {
				Material material = (Material) value;
				MaterialSerializationProxy m = new MaterialSerializationProxy();
				m.name = material.getName();
				m.type = material.getType().name();
				m.density = material.getDensity();
				m.userDefined = material.isUserDefined();
				value = m;
			}

			DTO.put(keyName,value);
		}

		oos.writeObject(DTO);
	}

	@SuppressWarnings("unchecked")
	private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String,Object> DTO = (Map<String,Object>) ois.readObject();

		Field propField = ComponentPreset.class.getDeclaredField("properties");
		propField.setAccessible(true);
		propField.set(this, new TypedPropertyMap());

		for ( Entry<String,Object> entry : DTO.entrySet() ) {
			String keyName = entry.getKey();
			Object value = entry.getValue();

			if ( value instanceof MaterialSerializationProxy ) {
				MaterialSerializationProxy m = (MaterialSerializationProxy) value;
				value = Material.newMaterial(Material.Type.valueOf(m.type), m.name, m.density, m.userDefined);
			}
			if ( TYPE.getName().equals(keyName)) {
				this.properties.put(TYPE, (ComponentPreset.Type) value);
			} else {
				for( @SuppressWarnings("rawtypes") TypedKey k : ORDERED_KEY_LIST ) {
					if ( k.getName().equals(keyName)) {
						this.properties.put( k, value );
						break;
					}
				}
			}
		}

		this.computeDigest();
	}
}
