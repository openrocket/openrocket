package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;


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
	
	public static enum DeployEvent {
		LAUNCH(trans.get("RecoveryDevice.DeployEvent.LAUNCH")) {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return e.getType() == FlightEvent.Type.LAUNCH;
			}
		},
		EJECTION(trans.get("RecoveryDevice.DeployEvent.EJECTION")) {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				RocketComponent charge = e.getSource();
				return charge.getStageNumber() == source.getStageNumber();
			}
		},
		APOGEE(trans.get("RecoveryDevice.DeployEvent.APOGEE")) {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return e.getType() == FlightEvent.Type.APOGEE;
			}
		},
		ALTITUDE(trans.get("RecoveryDevice.DeployEvent.ALTITUDE")) {
			@SuppressWarnings("unchecked")
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.ALTITUDE)
					return false;
				
				double alt = ((RecoveryDevice) source).getDeployAltitude();
				Pair<Double, Double> altitude = (Pair<Double, Double>) e.getData();
				
				return (altitude.getU() >= alt) && (altitude.getV() <= alt);
			}
		},
		LOWER_STAGE_SEPARATION(trans.get("RecoveryDevice.DeployEvent.LOWER_STAGE_SEPARATION")) {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.STAGE_SEPARATION)
					return false;
				
				int separation = e.getSource().getStageNumber();
				int current = source.getStageNumber();
				return (current + 1 == separation);
			}
		},
		NEVER(trans.get("RecoveryDevice.DeployEvent.NEVER")) {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return false;
			}
		};
		
		private final String description;
		
		DeployEvent(String description) {
			this.description = description;
		}
		
		public abstract boolean isActivationEvent(FlightEvent e, RocketComponent source);
		
		@Override
		public String toString() {
			return description;
		}
		
	}
	
	
	private DeployEvent deployEvent = DeployEvent.EJECTION;
	private double deployAltitude = 200;
	private double deployDelay = 0;
	
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
	
	
	
	
	public DeployEvent getDeployEvent() {
		return deployEvent;
	}
	
	public void setDeployEvent(DeployEvent deployEvent) {
		if (this.deployEvent == deployEvent)
			return;
		this.deployEvent = deployEvent;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	public double getDeployAltitude() {
		return deployAltitude;
	}
	
	public void setDeployAltitude(double deployAltitude) {
		if (MathUtil.equals(this.deployAltitude, deployAltitude))
			return;
		this.deployAltitude = deployAltitude;
		if (getDeployEvent() == DeployEvent.ALTITUDE)
			fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public double getDeployDelay() {
		return deployDelay;
	}
	
	public void setDeployDelay(double delay) {
		delay = MathUtil.max(delay, 0);
		if (MathUtil.equals(this.deployDelay, delay))
			return;
		this.deployDelay = delay;
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
