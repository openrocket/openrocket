package net.sf.openrocket.rocketcomponent;

import java.util.Iterator;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stage extends ComponentAssembly implements FlightConfigurableComponent, OutsideComponent {
	
	static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(Stage.class);
	
	private FlightConfigurationImpl<StageSeparationConfiguration> separationConfigurations;
	
	private boolean centerline = true;
	private double angularPosition_rad = 0;
	private double radialPosition_m = 0;
	
	private int count = 1;
	private double angularSeparation = Math.PI;
	
	private int stageNumber;
	private static int stageCount;
	
	public Stage() {
		this.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(this, ComponentChangeEvent.EVENT_CHANGE, new StageSeparationConfiguration());
		this.relativePosition = Position.AFTER;
		stageNumber = Stage.stageCount;
		Stage.stageCount++;
	}
	
	protected String toPositionString() {
		return ">> " + this.getName() + "   rel: " + this.getRelativePositionVector().x + "  abs: " + this.getAbsolutePositionVector().x;
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
	
	@Override
	public void toDebugTreeNode(final StringBuilder buffer, final String prefix) {
		
		String thisLabel = this.getName() + " (" + this.getStageNumber() + ")";
		
		buffer.append(String.format("%s    %-24s  %5.3f %24s %24s", prefix, thisLabel, this.getLength(),
				this.getRelativePositionVector(), this.getAbsolutePositionVector()));
		
		if (this.isCenterline()) {
			buffer.append("\n");
		} else {
			buffer.append(String.format("  %4.1f//%s \n", this.getAxialOffset(), this.relativePosition.name()));
			Coordinate componentAbsolutePosition = this.getAbsolutePositionVector();
			Coordinate[] instanceCoords = new Coordinate[] { componentAbsolutePosition };
			instanceCoords = this.shiftCoordinates(instanceCoords);
			
			for (int instance = 0; instance < this.count; instance++) {
				Coordinate instanceAbsolutePosition = instanceCoords[instance];
				buffer.append(String.format("%s                 [instance %2d of %2d]  %s\n", prefix, instance, count, instanceAbsolutePosition));
			}
		}
		
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
		if (type.equals(Stage.class)) {
			return true;
		} else {
			return BodyComponent.class.isAssignableFrom(type);
		}
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
		return !isCenterline();
	}
	
	/**
	 * Detects if this Stage is attached directly to the Rocket (and is thus centerline)
	 * Or if this stage is a parallel (external) stage.
	 * 
	 * @return whether this Stage is along the center line of the Rocket.
	 */
	@Override
	public boolean isCenterline() {
		if (this.parent instanceof Rocket) {
			this.centerline = true;
		} else {
			this.centerline = false;
		}
		return this.centerline;
	}
	
	/** 
	 * Stub. 
	 * The actual value is set via 'isCenterline()'
	 */
	@Override
	public void setOutside(final boolean _outside) {
	}
	
	@Override
	public int getInstanceCount() {
		if (this.isCenterline()) {
			return 1;
		} else {
			return this.count;
		}
	}
	
	@Override
	public void setInstanceCount(final int _count) {
		mutex.verify();
		if (this.centerline) {
			return;
		}
		
		this.count = _count;
		this.angularSeparation = Math.PI * 2 / this.count;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getAngularOffset() {
		if (this.centerline) {
			return 0.;
		} else {
			return this.angularPosition_rad;
		}
	}
	
	@Override
	public void setAngularOffset(final double angle_rad) {
		if (this.centerline) {
			return;
		}
		
		this.angularPosition_rad = angle_rad;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getRadialOffset() {
		if (this.centerline) {
			return 0.;
		} else {
			return this.radialPosition_m;
		}
	}
	
	@Override
	public void setRadialOffset(final double radius) {
		//		log.error("  set radial position for: " + this.getName() + " to: " + this.radialPosition_m + " ... in meters?");
		if (false == this.centerline) {
			this.radialPosition_m = radius;
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	public void setRelativePositionMethod(final Position _newPosition) {
		if (null == this.parent) {
			throw new NullPointerException(" a Stage requires a parent before any positioning! ");
		}
		if (this.isCenterline()) {
			// Centerline stages must be set via AFTER-- regardless of what was requested:
			super.setRelativePosition(Position.AFTER);
		} else if (this.parent instanceof Stage) {
			if (Position.AFTER == _newPosition) {
				log.warn("Stages cannot be relative to other stages via AFTER! Ignoring.");
				super.setRelativePosition(Position.TOP);
			} else {
				super.setRelativePosition(_newPosition);
			}
		}
	}
	
	@Override
	public double getPositionValue() {
		mutex.verify();
		
		return getAxialOffset();
	}
	
	/*
	 * @deprecated remove when the file is fixed....
	 */
	public void setRelativeToStage(final int _relToStage) {
		// no-op
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
		} else if (this.parent instanceof Stage) {
			return this.parent.parent.getChildPosition(this.parent);
		} else if (this.isCenterline()) {
			if (0 < this.stageNumber) {
				return --this.stageNumber;
			}
		}
		
		return -1;
	}
	
	public static void resetStageCount() {
		Stage.stageCount = 0;
	}
	
	@Override
	public int getStageNumber() {
		return this.stageNumber;
	}
	
	@Override
	public double getAxialOffset() {
		double returnValue;
		
		if (this.isCenterline()) {
			if (Position.AFTER == this.relativePosition) {
				returnValue = super.getAxialOffset();
			} else if (Position.TOP == this.relativePosition) {
				this.relativePosition = Position.AFTER;
				returnValue = super.getAxialOffset();
			} else {
				throw new BugException("found a Stage on centerline, but not positioned as AFTER.  Please fix this! " + this.getName() + "  is " + this.getRelativePosition().name());
			}
		} else {
			returnValue = super.asPositionValue(this.relativePosition);
		}
		
		if (0.000001 > Math.abs(returnValue)) {
			returnValue = 0.0;
		}
		
		return returnValue;
	}
	
	@Override
	public void setAxialOffset(final double _pos) {
		this.updateBounds();
		super.setAxialOffset(this.relativePosition, _pos);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public Coordinate[] shiftCoordinates(Coordinate[] c) {
		checkState();
		
		if (this.isCenterline()) {
			return c;
		}
		
		if (1 < c.length) {
			throw new BugException("implementation of 'shiftCoordinates' assumes the coordinate array has len == 1; this is not true, and may produce unexpected behavior! ");
		}
		
		double radius = this.radialPosition_m;
		double angle0 = this.angularPosition_rad;
		double angleIncr = this.angularSeparation;
		Coordinate[] toReturn = new Coordinate[this.count];
		Coordinate thisOffset;
		double thisAngle = angle0;
		for (int instanceNumber = 0; instanceNumber < this.count; instanceNumber++) {
			thisOffset = new Coordinate(0, radius * Math.cos(thisAngle), radius * Math.sin(thisAngle));
			
			toReturn[instanceNumber] = thisOffset.add(c[0]);
			thisAngle += angleIncr;
		}
		
		return toReturn;
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
		//			System.err.println("      ..refLength: " + refStage.getLength() + "\n");
		//		}
		return buf;
	}
	
	@Override
	public void updateBounds() {
		// currently only updates the length 
		this.length = 0;
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			if (curChild.isCenterline()) {
				this.length += curChild.getLength();
			}
		}
		
	}
	
	@Override
	protected void update() {
		if (null == this.parent) {
			return;
		}
		
		this.updateBounds();
		if (this.parent instanceof Rocket) {
			int childNumber = this.parent.getChildPosition(this);
			if (0 == childNumber) {
				this.setAfter(null);
			} else {
				RocketComponent prevStage = this.parent.getChild(childNumber - 1);
				this.setAfter(prevStage);
			}
		} else if (this.parent instanceof Stage) {
			this.updateBounds();
			super.update();
		}
		
		
		this.updateChildSequence();
		
		return;
	}
	
	protected void updateChildSequence() {
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		RocketComponent prevComp = null;
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			if (curChild.isCenterline()) {
				curChild.previousComponent = prevComp;
				prevComp = curChild;
			} else {
				curChild.previousComponent = null;
			}
		}
	}
	
	
	
}
