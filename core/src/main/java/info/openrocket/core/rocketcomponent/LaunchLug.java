package info.openrocket.core.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.rocketcomponent.position.*;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

public class LaunchLug extends Tube implements AnglePositionable, BoxBounded, LineInstanceable, InsideColorComponent {
	
	private static final Translator trans = Application.getTranslator();
	
	private double radius;
	private double thickness;
	
	private double angleOffsetRad = Math.PI;
	private double radialOffset = 0;
	
	private int instanceCount = 1;
	private double instanceSeparation = 0; // front-front along the positive rocket axis. i.e. [1,0,0];

	private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
	
	public LaunchLug() {
		super(AxialMethod.MIDDLE);
		radius = 0.01 / 2;
		thickness = 0.001;
		length = 0.03;
		this.setInstanceSeparation(this.length * 2);
		super.displayOrder_side = 15;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 12;		// Order for displaying the component in the 2D back view
	}
	
	
	@Override
	public double getOuterRadius() {
		return radius;
	}
	
	@Override
	public void setOuterRadius(double radius) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Coaxial) {
				((Coaxial) listener).setOuterRadius(radius);
			}
		}

		if (MathUtil.equals(this.radius, radius))
			return;
		this.radius = radius;
		this.thickness = Math.min(this.thickness, this.radius);
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getInnerRadius() {
		return radius - thickness;
	}
	
	@Override
	public void setInnerRadius(double innerRadius) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Coaxial) {
				((Coaxial) listener).setInnerRadius(innerRadius);
			}
		}

		setOuterRadius(innerRadius + thickness);
	}
	
	@Override
	public double getThickness() {
		return thickness;
	}
	
	public void setThickness(double thickness) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof LaunchLug) {
				((LaunchLug) listener).setThickness(thickness);
			}
		}

		if (MathUtil.equals(this.thickness, thickness))
			return;
		this.thickness = MathUtil.clamp(thickness, 0, radius);
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getAngleOffset() {
		return this.angleOffsetRad;
	}
	
	@Override
	public void setAngleOffset(double newAngleRadians) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof AnglePositionable) {
				((AnglePositionable) listener).setAngleOffset(newAngleRadians);
			}
		}

		double clamped_rad = MathUtil.clamp(newAngleRadians, -Math.PI, Math.PI);
		if (MathUtil.equals(this.angleOffsetRad, clamped_rad))
			return;
		this.angleOffsetRad = clamped_rad;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public void setLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof LaunchLug) {
				((LaunchLug) listener).setLength(length);
			}
		}

		if (MathUtil.equals(this.length, length))
			return;
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public boolean isAfter() {
		return false;
	}

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
			double outerDiameter = preset.get(ComponentPreset.OUTER_DIAMETER);
			this.radius = outerDiameter / 2.0;
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				double innerDiameter = preset.get(ComponentPreset.INNER_DIAMETER);
				this.thickness = (outerDiameter - innerDiameter) / 2.0;
			}
		}
		
		super.loadFromPreset(preset);
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.LAUNCH_LUG;
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		Coordinate[] toReturn = new Coordinate[this.getInstanceCount()];
		
		final double yOffset = Math.cos(angleOffsetRad) * (radialOffset);
		final double zOffset = Math.sin(angleOffsetRad) * (radialOffset);
		
		for ( int index=0; index < this.getInstanceCount(); index++){
			toReturn[index] = new Coordinate(index*this.instanceSeparation, yOffset, zOffset);
		}
		
		return toReturn;
	}
	
//	@Override
//	protected Coordinate[] shiftCoordinates(Coordinate[] array) {
//		array = super.shiftCoordinates(array);
//		
//		for (int i = 0; i < array.length; i++) {
//			array[i] = new Coordinate(xOffset + index*this.instanceSeparation, yOffset, zOffset);
//			array[i] = array[i].add(0, shiftY, shiftZ);
//		}
//		
//		return array;
//	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		super.componentChanged(e);
		
		/*
		 * shiftY and shiftZ must be computed here since calculating them
		 * in shiftCoordinates() would cause an infinite loop due to .toRelative
		 */
		RocketComponent body;
		double parentRadius;
		
		for (body = this.getParent(); body != null; body = body.getParent()) {
			if (body instanceof SymmetricComponent)
				break;
		}
		
		if (body == null) {
			parentRadius = 0;
		} else {
			SymmetricComponent s = (SymmetricComponent) body;
			double x1, x2;
			x1 = this.toRelative(Coordinate.NUL, body)[0].x;
			x2 = this.toRelative(new Coordinate(length, 0, 0), body)[0].x;
			x1 = MathUtil.clamp(x1, 0, body.getLength());
			x2 = MathUtil.clamp(x2, 0, body.getLength());
			parentRadius = Math.max(s.getRadius(x1), s.getRadius(x2));
		}
		
		this.radialOffset = parentRadius + radius;
	}
	
	@Override
	public double getComponentVolume() {
		return length * Math.PI * (MathUtil.pow2(radius) - MathUtil.pow2(radius - thickness)) * getInstanceCount();
	}
	
	@Override
	public Collection<Coordinate> getComponentBounds() {
		ArrayList<Coordinate> set = new ArrayList<Coordinate>();
		addBound(set, 0, radius);
		addBound(set, length, radius);
		return set;
	}
	
	@Override
	public Coordinate getComponentCG() {
		final double parentRadius = parent instanceof SymmetricComponent ?
				((SymmetricComponent) parent).getRadius(getAxialOffset()) : 0;

		final double CMx = length / 2 + (instanceSeparation * (instanceCount-1)) / 2;
		final double CMy = Math.cos(this.angleOffsetRad) * (parentRadius + getOuterRadius());
		final double CMz = Math.sin(this.angleOffsetRad) * (parentRadius + getOuterRadius());
		return new Coordinate(CMx, CMy, CMz, getComponentMass());
	}
	
	@Override
	public String getComponentName() {
		//// Launch lug
		return trans.get("LaunchLug.Launchlug");
	}
	
	@Override
	public double getLongitudinalUnitInertia() {
		// 1/12 * (3 * (r2^2 + r1^2) + h^2)
		return (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLength())) / 12;
	}
	
	@Override
	public double getRotationalUnitInertia() {
		// 1/2 * (r1^2 + r2^2)
		return (MathUtil.pow2(getInnerRadius()) + MathUtil.pow2(getOuterRadius())) / 2;
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		// Allow nothing to be attached to a LaunchLug
		return false;
	}
	

	
	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(final double _separation){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof LineInstanceable) {
				((LineInstanceable) listener).setInstanceSeparation(_separation);
			}
		}

		if (MathUtil.equals(this.instanceSeparation, _separation)) {
			return;
		}
		this.instanceSeparation = _separation;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		for (RocketComponent listener : configListeners) {
			listener.setInstanceCount(newCount);
		}

		if (newCount == this.instanceCount || newCount <= 0) {
			return;
		}
		this.instanceCount = newCount;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public int getInstanceCount(){
		return this.instanceCount;
	}
	
	@Override
	public BoundingBox getInstanceBoundingBox() {
		BoundingBox instanceBounds = new BoundingBox();
		
		instanceBounds.update(new Coordinate(this.getLength(), 0,0));
		
		final double r = getOuterRadius();
		instanceBounds.update(new Coordinate(0,r,r));
		instanceBounds.update(new Coordinate(0,-r,-r));
		
		return instanceBounds;
	}
	
	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-Line");
	}


	@Override
	public AngleMethod getAngleMethod() {
		return AngleMethod.RELATIVE;
	}


	@Override
	public void setAngleMethod(AngleMethod newMethod) {
		// no-op
	}

	@Override
	public InsideColorComponentHandler getInsideColorComponentHandler() {
		return this.insideColorComponentHandler;
	}

	@Override
	public void setInsideColorComponentHandler(InsideColorComponentHandler handler) {
		this.insideColorComponentHandler = handler;
	}
}
