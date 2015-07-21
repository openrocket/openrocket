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
	
	private boolean outside = false;
	private double angularPosition_rad = 0;
	private double radialPosition_m = 0;
	private Stage stageRelativeTo = null;
	
	private int count = 1;
	private double angularSeparation = Math.PI;
	
	
	public Stage() {
		this.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(this, ComponentChangeEvent.EVENT_CHANGE, new StageSeparationConfiguration());
		this.relativePosition = Position.AFTER;
	}
	
	@Override
	protected void componentChanged(ComponentChangeEvent e) {
		checkState();
		
		if (e.isAerodynamicChange() || e.isMassChange()) {
			//			System.err.println(">> in (" + this.getStageNumber() + ")" + this.getName());
			// update this component		
			this.updateBounds();
			this.updateCenter();
			
			// now update children relative to this
			int childIndex = 0;
			int childCount = this.getChildCount();
			RocketComponent prevComp = null;
			while (childIndex < childCount) {
				RocketComponent curComp = this.getChild(childIndex);
				//				System.err.println("      updating position of " + curComp + " via (AFTER, O, " + prevComp + ")");
				if (0 == childIndex) {
					curComp.setAxialOffset(Position.TOP, 0, this);
				} else {
					if (Position.AFTER != curComp.getRelativePositionMethod()) {
						throw new IllegalStateException(" direct children of a Stage are expected to be positioned via AFTER.");
					}
					curComp.setAxialOffset(Position.AFTER, 0, prevComp);
				}
				//				System.err.println("        position updated to: " + curComp.getAxialOffset());
				
				prevComp = curComp;
				childIndex++;
			}
			
			
		}
	}
	
	protected String toPositionString() {
		return ">> " + this.getName() + "   rel: " + this.getRelativePositionVector().x + "  abs: " + this.getAbsolutePositionVector().x;
	}
	
	protected void dumpDetail() {
		StackTraceElement[] stackTrace = (new Exception()).getStackTrace();
		System.err.println(" >> Dumping Stage Detailed Information from: " + stackTrace[1].getMethodName());
		System.err.println("      curStageName: " + this.getName());
		System.err.println("      method: " + this.relativePosition.name());
		System.err.println("      thisCenterX: " + this.position.x);
		System.err.println("      this length: " + this.length);
		if (-1 == this.getRelativeToStage()) {
			System.err.println("      ..refStageName: " + null);
		} else {
			Stage refStage = this.stageRelativeTo;
			System.err.println("      ..refStageName: " + refStage.getName());
			System.err.println("      ..refCenterX: " + refStage.position.x);
			System.err.println("      ..refLength: " + refStage.getLength());
		}
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
	
	@Override
	public boolean isCenterline() {
		return !this.outside;
	}
	
	@Override
	public void setOutside(final boolean _outside) {
		if (this.outside == _outside) {
			return;
		}
		
		this.outside = _outside;
		if (this.outside) {
			this.relativePosition = Position.BOTTOM;
			if (null == this.stageRelativeTo) {
				this.stageRelativeTo = this.updatePrevAxialStage();
			}
		} else {
			this.relativePosition = Position.AFTER;
			this.stageRelativeTo = this.updatePrevAxialStage();
			this.count = 1;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
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
		this.count = _count;
		this.angularSeparation = Math.PI * 2 / this.count;
		
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getAngularOffset() {
		if (this.outside) {
			return this.angularPosition_rad;
		} else {
			return 0.;
		}
		
	}
	
	@Override
	public void setAngularOffset(final double angle_rad) {
		this.angularPosition_rad = angle_rad;
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	@Override
	public double getRadialOffset() {
		if (this.outside) {
			return this.radialPosition_m;
		} else {
			return 0.;
		}
	}
	
	@Override
	public void setRadialOffset(final double radius) {
		this.radialPosition_m = radius;
		//		log.error("  set radial position for: " + this.getName() + " to: " + this.radialPosition_m + " ... in meters?");
		if (this.outside) {
			fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		}
	}
	
	public void setRelativePositionMethod(final Position _newPosition) {
		if (Position.AFTER != _newPosition) {
			this.outside = true;
		}
		
		super.setRelativePosition(_newPosition);
	}
	
	@Override
	public double getPositionValue() {
		mutex.verify();
		
		if (null == this.stageRelativeTo) {
			return super.asPositionValue(this.relativePosition, this.getParent());
		} else {
			return getAxialOffset();
		}
	}
	
	/** 
	 * Stages may be positioned relative to other stages. In that case, this will set the stage number 
	 * against which this stage is positioned.
	 * 
	 * @return the stage number which this stage is positioned relative to
	 */
	public int getRelativeToStage() {
		if (null == this.stageRelativeTo) {
			return -1;
		}
		
		return this.getRocket().getChildPosition(this.stageRelativeTo);
	}
	
	
	/*
	 * 
	 * @param _relTo the stage number which this stage is positioned relative to
	 */
	public Stage setRelativeToStage(final int _relTo) {
		mutex.verify();
		if ((_relTo < 0) || (_relTo >= this.getRocket().getStageCount())) {
			log.error("attempt to position this stage relative to a non-existent stage number. Ignoring.");
			this.stageRelativeTo = null;
		} else if (_relTo == this.getRocket().getChildPosition(this)) {
			// self-referential: also an error
			this.stageRelativeTo = null;
		} else if (this.isCenterline()) {
			this.relativePosition = Position.AFTER;
			updatePrevAxialStage();
		} else {
			this.stageRelativeTo = (Stage) this.getRocket().getChild(_relTo);
		}
		
		return this.stageRelativeTo;
	}
	
	
	@Override
	public double getAxialOffset() {
		double returnValue;
		if (null == this.stageRelativeTo) {
			returnValue = super.asPositionValue(Position.TOP, this.getParent());
		} else if (this.isCenterline()) {
			returnValue = super.asPositionValue(Position.AFTER, this.stageRelativeTo);
		} else {
			returnValue = super.asPositionValue(this.relativePosition, this.stageRelativeTo);
		}
		
		if (0.000001 > Math.abs(returnValue)) {
			returnValue = 0.0;
		}
		
		return returnValue;
	}
	
	@Override
	public void setAxialOffset(final double _pos) {
		this.updateBounds();
		super.setAxialOffset(this.relativePosition, _pos, this.stageRelativeTo);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	// TOOD: unify with 'generate instanceOffsets()'
	// what is the use of this again? 
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
	public void updateBounds() {
		
		// currently only updates the length 
		this.length = 0;
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			this.length += curChild.getLength();
		}
		
	}
	
	/**
	 * @Warning this will return the previous axial stage REGARDLESS of whether 'this' is in the centerline stack or not. 
	 * @return previous axial stage (defined as above, in the direction of launch) 
	 */
	protected Stage updatePrevAxialStage() {
		if (null != this.getParent()) {
			Rocket rocket = this.getRocket();
			int thisStageNumber = rocket.getChildPosition(this);
			int curStageIndex = thisStageNumber - 1;
			while (curStageIndex >= 0) {
				Stage curStage = (Stage) rocket.getChild(curStageIndex);
				if (curStage.isCenterline()) {
					this.stageRelativeTo = curStage;
					return this.stageRelativeTo;
				}
				curStageIndex--;
			}
		}
		
		this.stageRelativeTo = null;
		return null;
	}
	
	protected void updateCenter() {
		if (null == this.stageRelativeTo) {
			this.updatePrevAxialStage();
			if (null == this.stageRelativeTo) {
				// this stage is actually the topmost Stage, instead of just out-of-date
				this.setAxialOffset(Position.ABSOLUTE, this.getLength() / 2, this.getRocket());
				return;
			}
		}
		
		// general case:
		double offset = super.asPositionValue(this.relativePosition, this.stageRelativeTo);
		this.setAxialOffset(this.relativePosition, offset, this.stageRelativeTo);
	}
	
}
