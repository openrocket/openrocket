package net.sf.openrocket.rocketcomponent;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
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
public abstract class RecoveryDevice extends MassObject {
	private static final Translator trans = Application.getTranslator();
	
	private Map<String,DeploymentConfiguration> deploymentConfigurations = new HashMap<String,DeploymentConfiguration>();
	
	private DeploymentConfiguration defaultDeploymentConfig = new DeploymentConfiguration();
	
	private double cd = Parachute.DEFAULT_CD;
	private boolean cdAutomatic = true;
	
	
	private Material.Surface material;
	
	
	public RecoveryDevice() {
		this(Application.getPreferences().getDefaultComponentMaterial(RecoveryDevice.class, Material.Type.SURFACE));
	}
	
	public RecoveryDevice(Material material) {
		super();
		setMaterial(material);
	}
	
	public RecoveryDevice(double length, double radius, Material material) {
		super(length, radius);
		setMaterial(material);
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
	
	
	/** 
	 * Return the deployment configuration for this configurationId or null if using default.
	 * 
	 * @param configID
	 * @return
	 */
	public DeploymentConfiguration getDeploymentConfiguration( String configID ) {
		DeploymentConfiguration config = deploymentConfigurations.get(configID);
		return config;
	}
	
	public DeploymentConfiguration getDefaultDeploymentConfiguration() {
		return defaultDeploymentConfig;
	}
	
	public void setDeploymentConfiguration( String configID, DeploymentConfiguration config ) {
		deploymentConfigurations.put(configID, config);
	}
	
	public DeployEvent getDefaultDeployEvent() {
		return defaultDeploymentConfig.getDeployEvent();
	}
	
	public void setDefaultDeployEvent(DeployEvent deployEvent) {
		if (this.defaultDeploymentConfig.getDeployEvent() == deployEvent)
			return;
		this.defaultDeploymentConfig.setDeployEvent(deployEvent);
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	public double getDefaultDeployAltitude() {
		return defaultDeploymentConfig.getDeployAltitude();
	}
	
	public void setDefaultDeployAltitude(double deployAltitude) {
		if (MathUtil.equals(getDefaultDeployAltitude(), deployAltitude))
			return;
		defaultDeploymentConfig.setDeployAltitude(deployAltitude);
		if (getDefaultDeployEvent() == DeployEvent.ALTITUDE)
			fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public double getDefaultDeployDelay() {
		return defaultDeploymentConfig.getDeployDelay();
	}
	
	public void setDefaultDeployDelay(double delay) {
		delay = MathUtil.max(delay, 0);
		if (MathUtil.equals(getDefaultDeployDelay(), delay))
			return;
		defaultDeploymentConfig.setDeployDelay(delay);
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	
	@Override
	public double getComponentMass() {
		return getArea() * getMaterial().getDensity();
	}

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if ( preset.has(ComponentPreset.MATERIAL)) {
			Material m = preset.get(ComponentPreset.MATERIAL);
			this.material = (Material.Surface)m;
		}
		super.loadFromPreset(preset);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);

	}

}
