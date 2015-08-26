package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

public class AxialStage extends ComponentAssembly implements FlightConfigurableComponent {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(AxialStage.class);
	
	private FlightConfigurationImpl<StageSeparationConfiguration> separationConfigurations;
	
	protected int stageNumber;
	private static int stageCount;
	
	public AxialStage() {
		this.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(this, ComponentChangeEvent.EVENT_CHANGE, new StageSeparationConfiguration());
		this.relativePosition = Position.AFTER;
		stageNumber = AxialStage.stageCount;
		AxialStage.stageCount++;
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("Stage.Stage");
	}
	
	public static int getStageCount() {
		return AxialStage.stageCount;
	}
	
	public FlightConfiguration<StageSeparationConfiguration> getStageSeparationConfiguration() {
		return separationConfigurations;
	}
	
	// not strictly accurate, but this should provide an acceptable estimate for total vehicle size 
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<Coordinate>(8);
		Coordinate[] instanceLocations = this.getLocation();
		double x_min = instanceLocations[0].x;
		double x_max = x_min + this.length;
		double r_max = 0;
		
		addBound(bounds, x_min, r_max);
		addBound(bounds, x_max, r_max);
		
		return bounds;
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
		if (BoosterSet.class.isAssignableFrom(type)) {
			return true;
		} else if (PodSet.class.isAssignableFrom(type)) {
			return true;
			// DEBUG ONLY.  Remove this clause before production.
		} else if (AxialStage.class.isAssignableFrom(type)) {
			return true;
		}
		
		return BodyComponent.class.isAssignableFrom(type);
	}
	
	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		separationConfigurations.cloneFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		AxialStage copy = (AxialStage) super.copyWithOriginalID();
		copy.separationConfigurations = new FlightConfigurationImpl<StageSeparationConfiguration>(separationConfigurations,
				copy, ComponentChangeEvent.EVENT_CHANGE);
		return copy;
	}
	
	
	public void setRelativePositionMethod(final Position _newPosition) {
		// Axial Stages are restricted to .AFTER, and cannot be changed.
		return;
	}
	
	@Override
	public double getPositionValue() {
		mutex.verify();
		
		return this.getAxialOffset();
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
		} else if (this.isCenterline()) {
			if (0 < this.stageNumber) {
				return --this.stageNumber;
			}
		}
		
		return -1;
	}
	
	public static void resetStageCount() {
		AxialStage.stageCount = 0;
	}
	
	@Override
	public int getStageNumber() {
		return this.stageNumber;
	}
	
	@Override
	public double getAxialOffset() {
		double returnValue = super.asPositionValue(this.relativePosition);
		
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
		return c;
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
	public void toDebugTreeNode(final StringBuilder buffer, final String prefix) {
		
		String thisLabel = this.getName() + " (" + this.getStageNumber() + ")";
		
		buffer.append(String.format("%s    %-24s  %5.3f", prefix, thisLabel, this.getLength()));
		buffer.append(String.format("  %24s  %24s\n", this.getOffset(), this.getLocation()[0]));
		
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
		// stages which are directly children of the rocket are inline, and positioned
		int childNumber = this.parent.getChildPosition(this);
		if (0 == childNumber) {
			this.setAfter(this.parent);
		} else {
			RocketComponent prevStage = this.parent.getChild(childNumber - 1);
			this.setAfter(prevStage);
		}
		
		// updates the internal 'previousComponent' field.
		this.updateChildSequence();
		
		return;
	}
	
	protected void updateChildSequence() {
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		RocketComponent prevComp = null;
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			if (curChild.isCenterline()) {
				//curChild.previousComponent = prevComp;
				curChild.setAfter(prevComp);
				prevComp = curChild;
				//			} else {
				//				curChild.previousComponent = null;
			}
		}
	}
	
	
	
}
