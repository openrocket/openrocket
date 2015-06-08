package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class Stage extends ComponentAssembly implements FlightConfigurableComponent, OutsideComponent {
	
	static final Translator trans = Application.getTranslator();
	
	private FlightConfigurationImpl<StageSeparationConfiguration> separationConfigurations;
	
	private boolean outside = false;
	private double position_angular_rad = 0;
	private double position_radial_m = 0;
	private double rotation_rad = 0;
	
	//	ParallelStagingConfiguration parallelConfiguration = null;
	
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
	
	//	public ParallelStagingConfiguration getParallelStageConfiguration() {
	//		return parallelConfiguration;
	//	}
	
	
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
	public boolean getOutside() {
		return this.outside;
	}
	
	
	public boolean isInline() {
		return !this.outside;
	}
	
	@Override
	public void setOutside(final boolean _outside) {
		this.outside = _outside;
	}
	
	@Override
	public double getAngularPosition() {
		if (this.isInline()) {
			return 0.;
		}
		return this.position_angular_rad;
	}
	
	@Override
	public void setAngularPosition(final double angle_rad) {
		this.position_angular_rad = angle_rad;
	}
	
	@Override
	public double getRadialPosition() {
		if (this.isInline()) {
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
		if (this.isInline()) {
			return 0.;
		}
		return this.rotation_rad;
	}
	
	@Override
	public void setRotation(final double rotation) {
		this.rotation_rad = rotation;
	}
	
}
