package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.position.AngleMethod;
import info.openrocket.core.rocketcomponent.position.AnglePositionable;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.rocketcomponent.position.RadiusPositionable;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

public class PodSet extends ComponentAssembly implements RingInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(PodSet.class);
	
	protected int instanceCount = 2;

	
	protected AngleMethod angleMethod = AngleMethod.RELATIVE;
	// angle between each pod
	protected double angleSeparation = Math.PI;
	// angle to the first pod
	protected double angleOffset_rad = 0;

	protected RadiusMethod radiusMethod = RadiusMethod.RELATIVE;
	protected double radiusOffset_m = 0;
	
	public PodSet() {
		this.instanceCount = 2;
		this.axialMethod = AxialMethod.BOTTOM;
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("PodSet.PodSet");
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
	public double getInstanceAngleIncrement() {
		return angleSeparation;
	}
	
	@Override
	public double[] getInstanceAngles() {
		// , angleMethod, angleOffset_rad
		final double baseAngle = getAngleOffset();
		final double incrAngle = getInstanceAngleIncrement();

		double[] result = new double[getInstanceCount()];
		for (int i = 0; i < getInstanceCount(); ++i) {
			result[i] = baseAngle + incrAngle * i;
		}
		
		return result;
	}
	
	@Override
	public Coordinate[] getInstanceOffsets() {
		checkState();

		final double radius = this.radiusMethod.getRadius(this.parent, this, radiusOffset_m);

		Coordinate[] toReturn = new Coordinate[this.instanceCount];
		final double[] angles = getInstanceAngles();
		for (int instanceNumber = 0; instanceNumber < this.instanceCount; instanceNumber++) {
			final double curY = radius * Math.cos(angles[instanceNumber]);
			final double curZ = radius * Math.sin(angles[instanceNumber]);
			toReturn[instanceNumber] = new Coordinate(0, curY, curZ);
		}
		
		return toReturn;
	}
	
	@Override
	public boolean isAfter() {
		return false;
	}
	
	/** 
	 * Stages may be positioned relative to other stages. In that case, this will set the stage number 
	 * against which this stage is positioned.
	 * 
	 * @return the stage number which this stage is positioned relative to
	 */
	public int getRelativeToStage() {
		if (null == this.parent) {
			return -1;
		} else if (this.parent instanceof PodSet) {
			return this.parent.parent.getChildPosition(this.parent);
		}
		
		return -1;
	}
	
	@Override
	public void setAxialMethod(final AxialMethod newMethod) {
		super.setAxialMethod(newMethod);
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	@Override
	public double getAxialOffset() {
		return getAxialOffset(this.axialMethod);
	}

	@Override
	public double getAxialOffset(AxialMethod method) {
		double returnValue;

		if (this.isAfter()) {
			// remember the implicit (this instanceof Stage)
			throw new BugException("found a pod positioned via: AFTER, but is not on the centerline?!: " + this.getName() + "  is " + this.getAxialMethod().name() );
		} else {
			returnValue = super.getAxialOffset(method);
		}

		if (MathUtil.EPSILON > Math.abs(returnValue)) {
			returnValue = 0.0;
		}

		return returnValue;
	}

	@Override
	public double getAngleOffset() {
		return this.angleOffset_rad;
	}

	@Override
	public String getPatternName() {
		return (this.getInstanceCount() + "-ring");
	}
	
	@Override
	public double getRadiusOffset() {
		return this.radiusOffset_m;
	}
	
	@Override
	public int getInstanceCount() {
		return this.instanceCount;
	}

	@Override
	public void setInstanceCount(int newCount) {
		for (RocketComponent listener : configListeners) {
			listener.setInstanceCount(newCount);
		}

		mutex.verify();
		if (newCount < 1) {
			// there must be at least one instance....
			return;
		}

		this.instanceCount = newCount;
		this.angleSeparation = Math.PI * 2 / this.instanceCount;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	protected StringBuilder toDebugDetail() {
		StringBuilder buf = super.toDebugDetail();
		//		if (-1 == this.getRelativeToStage()) {
		//			System.err.println("      >>refStageName: " + null + "\n");
		//		} else {
		//			Stage refStage = (Stage) this.parent;
		//			System.err.println("      >>refStageName: " + refStage.getName() + "\n");
		//			System.err.println("      ..refCenterX: " + refStage.position.x + "\n");
		//			System.err.println("      ..refLength: " + refStage.getLengthAerodynamic() + "\n");
		//		}
		return buf;
	}

	@Override
	public void setAngleOffset(double angle_rad) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof AnglePositionable) {
				((AnglePositionable) listener).setAngleOffset(angle_rad);
			}
		}

		mutex.verify();
		this.angleOffset_rad = angle_rad;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	@Override
	public AngleMethod getAngleMethod() {
		return angleMethod;
	}

	@Override
	public void setAngleMethod(final AngleMethod newMethod) {

	}

	@Override
	public void setRadiusOffset(double radius_m) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RadiusPositionable) {
				((RadiusPositionable) listener).setRadiusOffset(radius_m);
			}
		}

		mutex.verify();

		if (radius_m == this.radiusOffset_m)
			return;

		if (this.radiusMethod.clampToZero()) {
			this.radiusOffset_m = 0.0;
		} else {
			this.radiusOffset_m = radius_m;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	@Override
	public RadiusMethod getRadiusMethod() {
		return this.radiusMethod;
	}

	@Override
	public void setRadiusMethod(RadiusMethod newMethod) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RadiusPositionable) {
				((RadiusPositionable) listener).setRadiusMethod(newMethod);
			}
		}

		if (newMethod == this.radiusMethod)
			return;

		mutex.verify();
		double radius = this.radiusMethod.getRadius(getParent(), this, this.radiusOffset_m); // Radius from the parent's
																								// center
		setRadius(newMethod, radius);
	}

	@Override
	public void setRadius(RadiusMethod requestMethod, double requestRadius) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RadiusPositionable) {
				((RadiusPositionable) listener).setRadius(requestMethod, requestRadius);
			}
		}

		mutex.verify();

		double newRadius = requestRadius;

		if (this.radiusMethod.clampToZero()) {
			newRadius = 0.0;
		}

		this.radiusMethod = requestMethod;
		this.radiusOffset_m = this.radiusMethod.getAsOffset(getParent(), this, newRadius);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

}
