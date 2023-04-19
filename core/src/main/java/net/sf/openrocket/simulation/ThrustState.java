package net.sf.openrocket.simulation;


public enum ThrustState {
	SPENT("Spent", "Finished Producing thrust.", null)
	,DELAYING("Delaying", " After Burnout, but before ejection", SPENT){
		@Override
		public boolean needsSimulation(){ return true;}
	}
	,THRUSTING("Thrusting", "Currently Producing thrust", DELAYING){
		@Override   
		public boolean isThrusting(){ return true; }
		@Override
		public boolean needsSimulation(){ return true;}
	}
	,ARMED("Armed", "Armed, but not yet lit.", THRUSTING)
	;
	
	private final static int SEQUENCE_NUMBER_END = 10; // arbitrary number
	
	private final String name;
	private final String description;
	private final int sequenceNumber; 
	private final ThrustState nextState;
	
	ThrustState( final String name, final String description, final ThrustState _nextState) {
		this.name = name;
		this.description = description;
		if( null == _nextState ){
			this.sequenceNumber = SEQUENCE_NUMBER_END;
			this.nextState = null;
		}else{
			this.sequenceNumber = -1 + _nextState.getSequenceNumber() ;
			this.nextState = _nextState;
		}
	}

	/**
	 * Return a short name of this motor type.
	 * @return  a short name of the motor type.
	 */
	public String getName() {
		return this.name;
	}

	/* 
	 * 
	 * @Return a MotorState which should follow this one
	 */
	public ThrustState getNext( ){
		return this.nextState;
	}
	
	/**
	 * Return a long description of this motor type.
	 * @return  a description of the motor type.
	 */
	public String getDescription() {
		return this.description;
	}
	
	/** 
	 * Sequence numbers have no intrinsic meaning, but their sequence (and relative value) 
	 * indicate which states occur before other states.
	 * @see isAfter(MotorState)
	 * @see isBefore(MotorState)
	 * @return integer indicating this state's place in the allowable sequence
	 */
	public int getSequenceNumber(){
		return this.sequenceNumber;
	}

	public boolean isAfter( final ThrustState other ){
		return this.sequenceNumber > other.sequenceNumber;
	}
	
	public boolean isBefore( final ThrustState other ){
		return this.sequenceNumber < other.sequenceNumber;
	}
	
	/* 
	 * If this motor is in a state which produces thrust
	 */
	public boolean isThrusting(){
		return false;
	}
	
	/** 
	 * This flag determines whether the motor has its state updated, and updates of cg and thrust updated.
	 * A motor instance will always receive events -- which may affect the simulation yes/no state
	 *    
	 * @return should this motor be simulated
	 */
	public boolean needsSimulation(){
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
}
