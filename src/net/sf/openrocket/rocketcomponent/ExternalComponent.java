package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Prefs;

/**
 * Class of components with well-defined physical appearance and which have an effect on
 * aerodynamic simulation.  They have material defined for them, and the mass of the component 
 * is calculated using the component's volume.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class ExternalComponent extends RocketComponent {
	
	public enum Finish {
		ROUGH("Rough", 500e-6),
		UNFINISHED("Unfinished", 150e-6),
		NORMAL("Regular paint", 60e-6),
		SMOOTH("Smooth paint", 20e-6),
		POLISHED("Polished", 2e-6);
		
		private final String name;
		private final double roughnessSize;
		
		Finish(String name, double roughness) {
			this.name = name;
			this.roughnessSize = roughness;
		}
		
		public double getRoughnessSize() {
			return roughnessSize;
		}
		
		@Override
		public String toString() {
			return name + " (" + UnitGroup.UNITS_ROUGHNESS.toStringUnit(roughnessSize) + ")";
		}
	}
	

	/**
	 * The material of the component.
	 */
	protected Material material=null;
	
	protected Finish finish = Finish.NORMAL;
	
	
	
	/**
	 * Constructor that sets the relative position of the component.
	 */
	public ExternalComponent(RocketComponent.Position relativePosition) {
		super(relativePosition);
		this.material = Prefs.getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
	}

	/**
	 * Returns the volume of the component.  This value is used in calculating the mass
	 * of the object.
	 */
	public abstract double getComponentVolume();

	/**
	 * Calculates the mass of the component as the product of the volume and interior density.
	 */
	@Override
	public double getComponentMass() {
		return material.getDensity() * getComponentVolume();
	}

	/**
	 * ExternalComponent has aerodynamic effect, so return true.
	 */
	@Override
	public boolean isAerodynamic() {
		return true;
	}
	
	/**
	 * ExternalComponent has effect on the mass, so return true.
	 */
	@Override
	public boolean isMassive() {
		return true;
	}
	
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material mat) {
		if (mat.getType() != Material.Type.BULK) {
			throw new IllegalArgumentException("ExternalComponent requires a bulk material" +
					" type="+mat.getType());
		}

		if (material.equals(mat))
			return;
		material = mat;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public Finish getFinish() {
		return finish;
	}
	
	public void setFinish(Finish finish) {
		if (this.finish == finish)
			return;
		this.finish = finish;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	
	@Override
	protected void copyFrom(RocketComponent c) {
		super.copyFrom(c);
		
		ExternalComponent src = (ExternalComponent)c;
		this.material = src.material;
		this.finish = src.finish;
	}
	
}
