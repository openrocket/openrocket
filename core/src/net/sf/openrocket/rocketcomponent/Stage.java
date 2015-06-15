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
	public boolean getOutside() {
		return this.outside;
	}
	
	public boolean isInline() {
		return !this.outside;
	}
	
	@Override
	public void setOutside(final boolean _outside) {
		this.outside = _outside;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getAngularPosition() {
		if (this.outside) {
			return this.position_angular_rad;
		} else {
			return 0.;
		}
		
	}
	
	@Override
	public void setAngularPosition(final double angle_rad) {
		this.position_angular_rad = angle_rad;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getRadialPosition() {
		if (this.outside) {
			return this.position_radial_m;
		} else {
			return 0.;
		}
	}
	
	@Override
	public void setRadialPosition(final double radius) {
		this.position_radial_m = radius;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
		
	}
	
	@Override
	public double getRotation() {
		if (this.outside) {
			return this.rotation_rad;
		} else {
			return 0.;
		}
		
	}
	
	@Override
	public void setRotation(final double rotation) {
		this.rotation_rad = rotation;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	public RocketComponent.Position getRelativePositionMethod() {
		return this.relativePosition;
	}
	
	@Override
	public void setRelativePosition(final Position position) {
		super.setRelativePosition(position);
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	public double getAxialPosition() {
		return super.getPositionValue();
	}
	
	public void setAxialPosition(final double _pos) {
		super.setPositionValue(_pos);
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	
	
	
}
