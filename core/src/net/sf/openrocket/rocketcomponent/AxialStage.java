package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

public class AxialStage extends ComponentAssembly implements FlightConfigurableComponent {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(AxialStage.class);
	
	/** list of separations to be happening*/
	protected FlightConfigurableParameterSet<StageSeparationConfiguration> separations;
	/** number of stages */
	protected int stageNumber;
	
	/**
	 * default constructor, builds a rocket with zero stages
	 */
	public AxialStage(){
		this.separations = new FlightConfigurableParameterSet<StageSeparationConfiguration>( new StageSeparationConfiguration());
		this.relativePosition = Position.AFTER;
		this.stageNumber = 0;
	}
	
	/**
	 * {@inheritDoc}
	 * AxialStage will always accept children
	 */
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("Stage.Stage");
	}
	
	/**
	 * gets the separation configuration of the rocket
	 * @return	the separation configuration of the rocket
	 */
	public FlightConfigurableParameterSet<StageSeparationConfiguration> getSeparationConfigurations() {
		return separations;
	}
	
	@Override
	public void reset( final FlightConfigurationId fcid){
		separations.reset(fcid);
	}
	
	/**
	 * {@inheritDoc}
	 * not strictly accurate, but this should provide an acceptable estimate for total vehicle size 
	 */
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
	public void copyFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		separations.copyFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		AxialStage copy = (AxialStage) super.copyWithOriginalID();
		copy.separations = new FlightConfigurableParameterSet<StageSeparationConfiguration>(separations);
		return copy;
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
	
	/**
	 * {@inheritDoc}
	 * axialStage is always after 
	 */
	@Override
	public boolean isAfter(){ 
		return true;
	}

	/**
	 * returns if the object is a launch stage
	 * @return	if the object is a launch stage
	 */
	public boolean isLaunchStage(){
		return ( this instanceof ParallelStage )
				||( getRocket().getBottomCoreStage().equals(this));
	}

	/**
	 * sets the stage number
	 * @param newStageNumber
	 */
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

	/**
	 * method used for debugging separation
	 * @return	a string that represents the debug message of separation
	 */
	public String toDebugSeparation() {
		StringBuilder buff = new StringBuilder();
		buff.append( this.separations.toDebug() );
		return buff.toString();
	}

	/**
	 * gets the previous stage installed in the rockets
	 * returns null if this is the first stage
	 * @return	the previous stage in the rocket
	 */
	public AxialStage getPreviousStage() {
		if( this instanceof ParallelStage ){
			return (AxialStage) this.parent;
		}
		AxialStage thisStage = this.getStage();  // necessary in case of pods or other assemblies
		if( thisStage.parent instanceof Rocket ){
			final int thisIndex = parent.getChildPosition( thisStage );
			if( 0 < thisIndex ){
				return (AxialStage)thisStage.parent.getChild(thisIndex-1);
			}
		}
		return null; 
	}
	
	@Override
	public void toDebugTreeNode(final StringBuilder buffer, final String indent) {
		
		Coordinate[] relCoords = this.getInstanceOffsets();
		Coordinate[] absCoords = this.getLocations();
		if( 1 == getInstanceCount()){
			buffer.append(String.format("%-40s|  %5.3f; %24s; %24s;", indent+this.getName()+" (# "+this.getStageNumber()+")", 
					this.getLength(), this.getOffset(), this.getLocations()[0]));
			buffer.append(String.format("len: %6.4f )(offset: %4.1f  via: %s )\n", this.getLength(), this.getAxialOffset(), this.relativePosition.name()));
		}else{
			buffer.append(String.format("%-40s|(len: %6.4f )(offset: %4.1f via: %s)\n", (indent+this.getName()+"(# "+this.getStageNumber()+")"), this.getLength(), this.getAxialOffset(), this.relativePosition.name()));
			for (int instanceNumber = 0; instanceNumber < this.getInstanceCount(); instanceNumber++) {
				Coordinate instanceRelativePosition = relCoords[instanceNumber];
				Coordinate instanceAbsolutePosition = absCoords[instanceNumber];
				final String prefix = String.format("%s    [%2d/%2d]", indent, instanceNumber+1, getInstanceCount()); 
				buffer.append(String.format("%-40s|  %5.3f; %24s; %24s;\n", prefix, this.getLength(), instanceRelativePosition, instanceAbsolutePosition));
			}
		}
		
	}

	
}
