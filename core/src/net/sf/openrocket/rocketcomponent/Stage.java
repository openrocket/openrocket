package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class Stage extends ComponentAssembly implements FlightConfigurableComponent, OutsideComponent {
	
	static final Translator trans = Application.getTranslator();
	
	private FlightConfigurationImpl<StageSeparationConfiguration> separationConfigurations;
	
	private boolean axial = true;
	private double position_angular_rad = 0;
	private double position_radial_m = 0;
	private double rotation_rad = 0;
	
	public Stage() {
		this.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(this, ComponentChangeEvent.EVENT_CHANGE, new StageSeparationConfiguration());
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("Stage.Stage");
	}
	
	
	public FlightConfiguration<StageSeparationConfiguration> getStageSeparationConfiguration() {
		return separationConfigurations;
	}
	
	
	
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Check whether the given type can be added to this component.  A Stage allows
	 * only BodyComponents to be added.
	 *
	 * @param type The RocketComponent class type to add.
	 *
	 * @return Whether such a component can be added.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return BodyComponent.class.isAssignableFrom(type);
	}
	
	
	
	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		separationConfigurations.cloneFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		Stage copy = (Stage) super.copyWithOriginalID();
		copy.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(separationConfigurations,
				copy, ComponentChangeEvent.EVENT_CHANGE);
		return copy;
	}
	
	@Override
	public boolean isInline() {
		return this.axial;
	}
	
	@Override
	public void setInline(final boolean inline) {
		this.axial = inline;
	}
	
	@Override
	public double getAngularPosition() {
		if (axial) {
			return 0.;
		}
		return this.position_angular_rad;
	}
	
	@Override
	public void setAngularPosition(final double phi) {
		this.position_angular_rad = phi;
	}
	
	@Override
	public double getRadialPosition() {
		if (axial) {
			return 0.;
		}
		return this.position_radial_m;
	}
	
	@Override
	public void setRadialPosition(final double radius) {
		this.position_radial_m = radius;
	}
	
	@Override
	public double getRotation() {
		if (axial) {
			return 0.;
		}
		return this.rotation_rad;
	}
	
	@Override
	public void setRotation(final double rotation) {
		this.rotation_rad = rotation;
	}
	
}
