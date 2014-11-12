package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;



public class LaunchLug extends ExternalComponent implements Coaxial {
	
	private static final Translator trans = Application.getTranslator();
	
	private double radius;
	private double thickness;
	
	private double radialDirection = 0;
	
	/* These are calculated when the component is first attached to any Rocket */
	private double shiftY, shiftZ;
	
	

	public LaunchLug() {
		super(Position.MIDDLE);
		radius = 0.01 / 2;
		thickness = 0.001;
		length = 0.03;
	}
	
	
	@Override
	public double getOuterRadius() {
		return radius;
	}
	
	@Override
	public void setOuterRadius(double radius) {
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
		setOuterRadius(innerRadius + thickness);
	}
	
	@Override
	public double getThickness() {
		return thickness;
	}
	
	public void setThickness(double thickness) {
		if (MathUtil.equals(this.thickness, thickness))
			return;
		this.thickness = MathUtil.clamp(thickness, 0, radius);
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public double getRadialDirection() {
		return radialDirection;
	}
	
	public void setRadialDirection(double direction) {
		direction = MathUtil.reduce180(direction);
		if (MathUtil.equals(this.radialDirection, direction))
			return;
		this.radialDirection = direction;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	public void setLength(double length) {
		if (MathUtil.equals(this.length, length))
			return;
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	
	
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public void setPositionValue(double value) {
		super.setPositionValue(value);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
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
	public Coordinate[] shiftCoordinates(Coordinate[] array) {
		array = super.shiftCoordinates(array);
		
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i].add(0, shiftY, shiftZ);
		}
		
		return array;
	}
	
	
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
		
		shiftY = Math.cos(radialDirection) * (parentRadius + radius);
		shiftZ = Math.sin(radialDirection) * (parentRadius + radius);
		
		//		System.out.println("Computed shift: y="+shiftY+" z="+shiftZ);
	}
	
	
	
	
	@Override
	public double getComponentVolume() {
		return length * Math.PI * (MathUtil.pow2(radius) - MathUtil.pow2(radius - thickness));
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
		return new Coordinate(length / 2, 0, 0, getComponentMass());
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
	
}
