package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;


/**
 * RecoveryDevice is a class representing devices that slow down descent.
 * Recovery devices report that they have no aerodynamic effect, since they
 * are within the rocket during ascent.
 * <p>
 * A recovery device includes a surface material of which it is made of.
 * The mass of the component is calculated based on the material and the
 * area of the device from {@link #getArea()}.  {@link #getComponentMass()}
 * may be overridden if additional mass needs to be included.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class RecoveryDevice extends MassObject implements FlightConfigurableComponent {
	
	private double cd = Parachute.DEFAULT_CD;
	private boolean cdAutomatic = true;
	
	private Material.Surface material;
	
	private FlightConfigurationImpl<DeploymentConfiguration> deploymentConfigurations;
	
	
	
	public RecoveryDevice() {
		this.deploymentConfigurations = new FlightConfigurationImpl<DeploymentConfiguration>(this, ComponentChangeEvent.EVENT_CHANGE, new DeploymentConfiguration());
		setMaterial(Application.getPreferences().getDefaultComponentMaterial(RecoveryDevice.class, Material.Type.SURFACE));
	}
	
	
	
	
	
	public abstract double getArea();
	
	public abstract double getComponentCD(double mach);
	
	
	
	public double getCD() {
		return getCD(0);
	}
	
	public double getCD(double mach) {
		if (cdAutomatic)
			cd = getComponentCD(mach);
		return cd;
	}
	
	public void setCD(double cd) {
		if (MathUtil.equals(this.cd, cd) && !isCDAutomatic())
			return;
		this.cd = cd;
		this.cdAutomatic = false;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	public boolean isCDAutomatic() {
		return cdAutomatic;
	}
	
	public void setCDAutomatic(boolean auto) {
		if (cdAutomatic == auto)
			return;
		this.cdAutomatic = auto;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	
	public final Material getMaterial() {
		return material;
	}
	
	public final void setMaterial(Material mat) {
		if (!(mat instanceof Material.Surface)) {
			throw new IllegalArgumentException("Attempted to set non-surface material " + mat);
		}
		if (mat.equals(material))
			return;
		this.material = (Material.Surface) mat;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public FlightConfiguration<DeploymentConfiguration> getDeploymentConfiguration() {
		return deploymentConfigurations;
	}
	
	
	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		deploymentConfigurations.cloneFlightConfiguration(oldConfigId, newConfigId);
	}
	
	
	@Override
	public double getComponentMass() {
		return getArea() * getMaterial().getDensity();
	}
	
	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if (preset.has(ComponentPreset.MATERIAL)) {
			Material m = preset.get(ComponentPreset.MATERIAL);
			this.material = (Material.Surface) m;
		}
		super.loadFromPreset(preset);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		RecoveryDevice copy = (RecoveryDevice) super.copyWithOriginalID();
		copy.deploymentConfigurations = new FlightConfigurationImpl<DeploymentConfiguration>(deploymentConfigurations,
				copy, ComponentChangeEvent.EVENT_CHANGE);
		return copy;
	}
}
