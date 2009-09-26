package net.sf.openrocket.material;

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
		public Line(String name, double density, boolean userDefined) {
			super(name, density, userDefined);
		}

		@Override
		public Type getType() {
			return Type.LINE;
		}
	}
	
	public static class Surface extends Material {
		
		public Surface(String name, double density, boolean userDefined) {
			super(name, density, userDefined);
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
		public Bulk(String name, double density, boolean userDefined) {
			super(name, density, userDefined);
		}

		@Override
		public Type getType() {
			return Type.BULK;
		}
	}
	
	
	
	private final String name;
	private final double density;
	private final boolean userDefined;
	
	
	public Material(String name, double density, boolean userDefined) {
		this.name = name;
		this.density = density;
		this.userDefined = userDefined;
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
		Material m = (Material)o;
		return ((m.name.equals(this.name)) && 
				MathUtil.equals(m.density, this.density)); 
	}


	/**
	 * A hashCode() method giving a hash code compatible with the equals() method.
	 */
	@Override
	public int hashCode() {
		return name.hashCode() + (int)(density*1000);
	}

	
	/**
	 * Order the materials according to their name, secondarily according to density.
	 */
	public int compareTo(Material o) {
		int c = this.name.compareTo(o.name);
		if (c != 0) {
			return c;
		} else {
			return (int)((this.density - o.density)*1000);
		}
	}
	
	
	/**
	 * Return a new material of the specified type.
	 */
	public static Material newMaterial(Type type, String name, double density, 
			boolean userDefined) {
		switch (type) {
		case LINE:
			return new Material.Line(name, density, userDefined);
			
		case SURFACE:
			return new Material.Surface(name, density, userDefined);
			
		case BULK:
			return new Material.Bulk(name, density, userDefined);
			
		default:
			throw new IllegalArgumentException("Unknown material type: "+type);
		}
	}
	
	
	public String toStorableString() {
		return getType().name() + "|" + name.replace('|', ' ') + '|' + density;
	}
	
	
	/**
	 * Return a material defined by the provided string.
	 * 
	 * @param str			the material storage string.
	 * @param userDefined	whether the created material is user-defined.
	 * @return				a new <code>Material</code> object.
	 * @throws IllegalArgumentException		if <code>str</code> is invalid or null.
	 */
	public static Material fromStorableString(String str, boolean userDefined) {
		if (str == null)
			throw new IllegalArgumentException("Material string is null");
		
		String[] split = str.split("\\|",3);
		if (split.length < 3)
			throw new IllegalArgumentException("Illegal material string: "+str);

		Type type = null;
		String name;
		double density;
		
		try {
			type = Type.valueOf(split[0]);
		} catch (Exception e) {
			throw new IllegalArgumentException("Illegal material string: "+str, e);
		}

		name = split[1];
		
		try {
			density = Double.parseDouble(split[2]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal material string: "+str, e);
		}
		
		switch (type) {
		case BULK:
			return new Material.Bulk(name, density, userDefined);
			
		case SURFACE:
			return new Material.Surface(name, density, userDefined);
			
		case LINE:
			return new Material.Line(name, density, userDefined);
			
		default:
			throw new IllegalArgumentException("Illegal material string: "+str);
		}
	}

}
