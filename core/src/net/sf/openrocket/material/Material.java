package net.sf.openrocket.material;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;

/**
 * A class for different material types.  Each material has a name and density.
 * The interpretation of the density depends on the material type.  For
 * {@link Type#BULK} it is kg/m^3, for {@link Type#SURFACE} km/m^2.
 * <p>
 * Objects of this type are immutable.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class Material implements Comparable<Material> {
	
	private static final Translator trans = Application.getTranslator();

	public enum Type {
		LINE("Line", UnitGroup.UNITS_DENSITY_LINE),
		SURFACE("Surface", UnitGroup.UNITS_DENSITY_SURFACE),
		BULK("Bulk", UnitGroup.UNITS_DENSITY_BULK);
		
		private final String name;
		private final UnitGroup units;
		
		private Type(String name, UnitGroup units) {
			this.name = name;
			this.units = units;
		}
		
		public UnitGroup getUnitGroup() {
			return units;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	/////  Definitions of different material types  /////
	
	public static class Line extends Material {
		Line(String name, String key, double density, boolean userDefined) {
			super(name, key, density, userDefined);
		}

		@Override
		public Type getType() {
			return Type.LINE;
		}
	}
	
	public static class Surface extends Material {
		
		Surface(String name, String key, double density, boolean userDefined) {
			super(name, key, density, userDefined);
		}

		@Override
		public Type getType() {
			return Type.SURFACE;
		}
		
		@Override
		public String toStorableString() {
			return super.toStorableString();
		}
	}
	
	public static class Bulk extends Material {
		Bulk(String name, String key, double density, boolean userDefined) {
			super(name, key, density, userDefined);
		}

		@Override
		public Type getType() {
			return Type.BULK;
		}
	}
	
	

	private final String name;
	private final String key;
	private final double density;
	private final boolean userDefined;
	
	
	/**
	 * Constructor for materials.
	 * 
	 * @param name ignored when defining system materials.
	 * @param key ignored when defining user materials.
	 * @param density
	 * @param userDefined true if this is a user defined material, false if it is a system material.
	 */
	private Material(String name, String key, double density, boolean userDefined) {
		if ( userDefined ) {
			this.key = "UserDefined."+name;
			this.name = name;
		} else {
			this.key = key;
			this.name = trans.get("Databases.materials." + key);
		}
		this.userDefined = userDefined;
		this.density = density;
	}

	public String getKey() {
		return key;
	}
	
	public double getDensity() {
		return density;
	}
	
	public String getName() {
		return name;
	}
	
	public String getName(Unit u) {
		return name + " (" + u.toStringUnit(density) + ")";
	}
	
	public boolean isUserDefined() {
		return userDefined;
	}
	
	public abstract Type getType();
	
	@Override
	public String toString() {
		return this.getName(this.getType().getUnitGroup().getDefaultUnit());
	}
	
	
	/**
	 * Compares this object to another object.  Material objects are equal if and only if
	 * their types, names and densities are identical.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this.getClass() != o.getClass())
			return false;
		Material m = (Material) o;
		return ((m.name.equals(this.name)) && MathUtil.equals(m.density, this.density));
	}
	
	
	/**
	 * A hashCode() method giving a hash code compatible with the equals() method.
	 */
	@Override
	public int hashCode() {
		return name.hashCode() + (int) (density * 1000);
	}
	
	
	/**
	 * Order the materials according to their name, secondarily according to density.
	 */
	@Override
	public int compareTo(Material o) {
		int c = this.name.compareTo(o.name);
		if (c != 0) {
			return c;
		} else {
			return (int) ((this.density - o.density) * 1000);
		}
	}
	
	
	public static Material newSystemMaterial(Type type, String key, double density ) {
		switch (type) {
		case LINE:
			return new Material.Line(null, key, density, false);
			
		case SURFACE:
			return new Material.Surface(null,key, density, false);
			
		case BULK:
			return new Material.Bulk(null, key, density, false);
			
		default:
			throw new IllegalArgumentException("Unknown material type: " + type);
		}
	}
	
	/**
	 * Return a new user defined material of the specified type.
	 */
	public static Material newUserMaterial(Type type, String name, double density) {
		switch (type) {
		case LINE:
			return new Material.Line(name, null, density, true);
			
		case SURFACE:
			return new Material.Surface(name, null, density, true);
			
		case BULK:
			return new Material.Bulk(name, null, density, true);
			
		default:
			throw new IllegalArgumentException("Unknown material type: " + type);
		}
	}
	
	/**
	 * Return a new user defined material of the specified type and localizable key.
	 */
	public static Material newUserMaterialWithKey(Type type, String key, String name, double density) {
		switch (type) {
		case LINE:
			return new Material.Line(name, key, density, true);
			
		case SURFACE:
			return new Material.Surface(name, key, density, true);
			
		case BULK:
			return new Material.Bulk(name, key, density, true);
			
		default:
			throw new IllegalArgumentException("Unknown material type: " + type);
		}
	}
	
	
	
	public String toStorableString() {
		return getType().name() + "|" + key + "|" + name.replace('|', ' ') + '|' + density;
	}
	
	
	/**
	 * Return a material defined by the provided string.
	 * 
	 * @param str			the material storage string.
	 * @param userDefined	whether the created material is user-defined.
	 * @return				a new <code>Material</code> object.
	 * @throws IllegalArgumentException		if <code>str</code> is invalid or null.
	 */
	public static Material fromStorableString(String str) {
		if (str == null)
			throw new IllegalArgumentException("Material string is null");
		
		String[] split = str.split("\\|");
		if (split.length < 3)
			throw new IllegalArgumentException("Illegal material string: " + str);
		
		Type type = null;
		String name = null;
		String key= null;
		String densityString;
		
		try {
			type = Type.valueOf(split[0]);
		} catch (Exception e) {
			throw new IllegalArgumentException("Illegal material string: " + str, e);
		}
		
		if ( split.length == 3 ) {
			name = split[1];
			densityString =split[2];
		} else {
			key = split[1];
			name = split[2];
			densityString=split[3];
		}
		
		
		double density;

		try {
			density = Double.parseDouble(densityString);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal material string: " + str, e);
		}
		
		return Databases.findMaterial(type, key, name, density);
	}
	
}
