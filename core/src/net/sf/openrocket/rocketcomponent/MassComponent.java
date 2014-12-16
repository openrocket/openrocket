package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

/**
 * This class represents a generic component that has a specific mass and an approximate shape.
 * The mass is accessed via get/setComponentMass.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MassComponent extends MassObject {
	private static final Translator trans = Application.getTranslator();
	
	private double mass = 0;
	
	public static enum MassComponentType {
		MASSCOMPONENT(Application.getTranslator().get("MassComponent.MassComponent")),
		ALTIMETER(Application.getTranslator().get("MassComponent.Altimeter")),
		FLIGHTCOMPUTER(Application.getTranslator().get("MassComponent.FlightComputer")),
		DEPLOYMENTCHARGE(Application.getTranslator().get("MassComponent.DeploymentCharge")),
		TRACKER(Application.getTranslator().get("MassComponent.Tracker")),
		PAYLOAD(Application.getTranslator().get("MassComponent.Payload")),
		RECOVERYHARDWARE(Application.getTranslator().get("MassComponent.RecoveryHardware")),
		BATTERY(Application.getTranslator().get("MassComponent.Battery"));
		
		private String title;
		
		MassComponentType(String title) {
			this.title = title;
		}
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	private MassComponentType massComponentType = MassComponentType.MASSCOMPONENT;
	
	public MassComponent() {
		super();
	}
	
	public MassComponent(double length, double radius, double mass) {
		super(length, radius);
		this.mass = mass;
	}
	
	
	@Override
	public double getComponentMass() {
		return mass;
	}
	
	public void setComponentMass(double mass) {
		mass = Math.max(mass, 0);
		if (MathUtil.equals(this.mass, mass))
			return;
		this.mass = mass;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getDensity() {
		double d = getComponentMass() / getVolume();
		if (Double.isNaN(d))
			d = 0;
		return d;
	}
	
	public void setDensity(double density) {
		double m = density * getVolume();
		m = MathUtil.clamp(m, 0, 1000000);
		if (Double.isNaN(m))
			m = 0;
		setComponentMass(m);
	}
	
	
	private double getVolume() {
		return Math.PI * MathUtil.pow2(getRadius()) * getLength();
	}
	
	
	@Override
	public String getComponentName() {
		//// Mass component
		return trans.get("MassComponent.MassComponent");
	}
	
	public final MassComponent.MassComponentType getMassComponentType() {
		mutex.verify();
		return this.massComponentType;
	}
	
	public void setMassComponentType(MassComponent.MassComponentType compType) {
		mutex.verify();
		if (this.massComponentType == compType) {
			return;
		}
		checkState();
		this.massComponentType = compType;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		// Allow no components to be attached to a MassComponent
		return false;
	}
}
