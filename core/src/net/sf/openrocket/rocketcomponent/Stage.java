package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stage extends ComponentAssembly implements FlightConfigurableComponent, OutsideComponent {
	
	static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(Stage.class);
	
	private FlightConfigurationImpl<StageSeparationConfiguration> separationConfigurations;
	
	private boolean outside = false;
	private double angularPosition_rad = 0;
	private double radialPosition_m = 0;
	private double rotation_rad = 0;
	
	private int count = 2;
	private double separationAngle = Math.PI;
	
	
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
		if (Position.AFTER == this.relativePosition) {
			this.relativePosition = Position.BOTTOM;
			this.position = 0;
		}
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public int getCount() {
		return this.count;
	}
	
	@Override
	public void setCount(final int _count) {
		mutex.verify();
		this.count = _count;
		this.separationAngle = Math.PI * 2 / this.count;
		
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getAngularPosition() {
		if (this.outside) {
			return this.angularPosition_rad;
		} else {
			return 0.;
		}
		
	}
	
	@Override
	public void setAngularPosition(final double angle_rad) {
		this.angularPosition_rad = angle_rad;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getRadialPosition() {
		if (this.outside) {
			return this.radialPosition_m;
		} else {
			return 0.;
		}
	}
	
	@Override
	public void setRadialPosition(final double radius) {
		this.radialPosition_m = radius;
		log.error("  set radial position for: " + this.getName() + " to: " + this.radialPosition_m + " ... in meters?");
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
