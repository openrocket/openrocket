package net.sf.openrocket.rocketcomponent;

import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * Class of components with well-defined physical appearance and which have an effect on
 * aerodynamic simulation.  They have material defined for them, and the mass of the component
 * is calculated using the component's volume.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class ExternalComponent extends RocketComponent {
	
	public enum Finish {

		/*	The surface roughness changes are intended to begin the implementation of known values derived from
		 *	the Thesis and Hoerner Fluid Dynamics. It is anticipated that the ability to enter a custom surface
		 * 	roughness value will be added as a feature in the future.
		 */

		//// Rough
		ROUGH("ExternalComponent.Rough", 500e-6),
		//// Rough unfinished
		ROUGHUNFINISHED("ExternalComponent.Roughunfinished", 250e-6),
		//// Unfinished
		UNFINISHED("ExternalComponent.Unfinished", 150e-6),
		//// Regular paint
		NORMAL("ExternalComponent.Regularpaint", 60e-6),
		//// Smooth paint
		SMOOTH("ExternalComponent.Smoothpaint", 20e-6),
		//// Optimum paint
		OPTIMUM("ExternalComponent.Optimumpaint", 5e-6),
		//// Polished
		POLISHED("ExternalComponent.Polished", 2e-6),

		/*	The "polished" surface roughness was originally set at 2.0 microns. However, after reviewing the Thesis
		 * 	and  Hoerner Fluid Dynamics, upon which the Thesis surface roughness values were based, it was determined
		 * 	that the "polished" surface roughness should have been 0.5 microns. To reduce the potential for causing
		 * 	inadvertent errors, "POLISHED" was retained and "FINISHPOLISHED" added, with the correct values for each.
		 * 	"POLISHED" is now described to the user as "Aircraft sheet-metal" with a value of 2.0 microns and
		 * 	"FINISHPOLISHED" is now described to the user as "Polished" with a value of 0.5 microns.
		 */

		//// Optimum paint
		FINISHPOLISHED("ExternalComponent.Finishedpolished", .5e-6),
		//// Optimum paint
		MIRROR("ExternalComponent.Mirror", 0.0e-6);
		
		private static final Translator trans = Application.getTranslator();
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
			return trans.get(name) + " (" + UnitGroup.UNITS_ROUGHNESS.toStringUnit(roughnessSize) + ")";
		}
	}
	
	
	/**
	 * The material of the component.
	 */
	protected Material material = null;
	
	protected Finish finish = Finish.NORMAL;
	
	
	
	/**
	 * Constructor that sets the relative position of the component.
	 */
	public ExternalComponent( AxialMethod relativePosition) {
		super(relativePosition);
		this.material = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
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
					" type=" + mat.getType());
		}

		for (RocketComponent listener : configListeners) {
			if (listener instanceof ExternalComponent) {
				((ExternalComponent) listener).setMaterial(mat);
			}
		}

		if (material.equals(mat))
			return;
		material = mat;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public Finish getFinish() {
		return finish;
	}
	
	public void setFinish(Finish finish) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof ExternalComponent) {
				((ExternalComponent) listener).setFinish(finish);
			}
		}

		if (this.finish == finish)
			return;
		this.finish = finish;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
		
		if (preset.has(ComponentPreset.FINISH)) {
			setFinish(preset.get(ComponentPreset.FINISH));
		}
		
		if (preset.has(ComponentPreset.MATERIAL)) {
			Material mat = preset.get(ComponentPreset.MATERIAL);
			if (mat != null) {
				material = mat;
			} /*
				TODO - 
				else if (c.isMassOverridden()) {
				double mass = c.getOverrideMass();
				double volume = getComponentVolume();
				double density;
				if (volume > 0.00001) {
					density = mass / volume;
				} else {
					density = 1000;
				}
				mat = Material.newMaterial(Type.BULK, mat.getName(), density, true);
				setMaterial(mat);
				}
				*/
		}
	}
	
	@Override
	protected List<RocketComponent> copyFrom(RocketComponent c) {
		ExternalComponent src = (ExternalComponent) c;
		this.finish = src.finish;
		this.material = src.material;
		return super.copyFrom(c);
	}
	
}
