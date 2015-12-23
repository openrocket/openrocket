package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

public class AxialStage extends ComponentAssembly implements FlightConfigurableComponent {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(AxialStage.class);
	
	protected FlightConfigurableParameterSet<StageSeparationConfiguration> separations;
	
	protected int stageNumber;
	
	public AxialStage(){
		this.separations = new FlightConfigurableParameterSet<StageSeparationConfiguration>( new StageSeparationConfiguration());
		this.relativePosition = Position.AFTER;
		this.stageNumber = 0;
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
	
	public FlightConfigurableParameterSet<StageSeparationConfiguration> getSeparationConfigurations() {
		return separations;
	}
	
	// not strictly accurate, but this should provide an acceptable estimate for total vehicle size 
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<Coordinate>(8);
		Coordinate[] instanceLocations = this.getLocations();
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
		if (ParallelStage.class.isAssignableFrom(type)) {
			return true;
		} else if (PodSet.class.isAssignableFrom(type)) {
			return true;
		}
		
		return BodyComponent.class.isAssignableFrom(type);
	}
	
	@Override
	public void cloneFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		separations.cloneFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		AxialStage copy = (AxialStage) super.copyWithOriginalID();
		copy.separations = new FlightConfigurableParameterSet<StageSeparationConfiguration>(separations);
		return copy;
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
		} else if(1 == this.getInstanceCount()){
			return --this.stageNumber;
		} else {
			return this.parent.getStageNumber();
		}
	}
	
	@Override
	public int getStageNumber() {
		return this.stageNumber;
	}
	
	@Override
	public boolean isAfter(){ 
		return true;
	}
	
	public boolean isLaunchStage(){
		return ( getRocket().getBottomCoreStage().equals(this));
	}

	public void setStageNumber(final int newStageNumber) {
		this.stageNumber = newStageNumber;
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

	public String toDebugSeparation() {
		StringBuilder buff = new StringBuilder();
		buff.append( this.separations.toDebug() );
		return buff.toString();
	}
	
	
	
}
