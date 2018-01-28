package net.sf.openrocket.motor;

import java.util.Locale;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;

public enum IgnitionEvent {
	
	//// Automatic (launch or ejection charge)
	AUTOMATIC( "AUTOMATIC", "MotorMount.IgnitionEvent.AUTOMATIC"){
		@Override
		public boolean isActivationEvent(FlightEvent testEvent, RocketComponent targetComponent) {
			AxialStage targetStage = (AxialStage)targetComponent.getStage();
			
	        if ( targetStage.isLaunchStage() ){
	        	return LAUNCH.isActivationEvent(testEvent, targetComponent);
	        } else {
				return EJECTION_CHARGE.isActivationEvent(testEvent, targetComponent);
			}
		}
	},
	LAUNCH ( "LAUNCH", "MotorMount.IgnitionEvent.LAUNCH"){
		@Override
		public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
			return (fe.getType() == FlightEvent.Type.LAUNCH);
		}
	},
	EJECTION_CHARGE ("EJECTION_CHARGE", "MotorMount.IgnitionEvent.EJECTION_CHARGE"){
		@Override
		public boolean isActivationEvent( FlightEvent testEvent, RocketComponent targetComponent){
			if (testEvent.getType() != FlightEvent.Type.EJECTION_CHARGE){
				return false;
			}
			    
			AxialStage targetStage = (AxialStage)targetComponent.getStage();
			AxialStage eventStage =  (AxialStage)testEvent.getSource().getStage();
			AxialStage eventParentStage = eventStage.getUpperStage();
			return ( targetStage.equals(eventParentStage));
		}
	},
	BURNOUT ("BURNOUT", "MotorMount.IgnitionEvent.BURNOUT"){
		@Override
		public boolean isActivationEvent( FlightEvent testEvent, RocketComponent targetComponent){
			if (testEvent.getType() != FlightEvent.Type.BURNOUT)
				return false;
			
			AxialStage targetStage = (AxialStage)targetComponent.getStage();
			AxialStage eventStage =  (AxialStage)testEvent.getSource().getStage();
			AxialStage eventParentStage = eventStage.getUpperStage();
			return ( targetStage.equals(eventParentStage));
		}
	},
	NEVER("NEVER", "MotorMount.IgnitionEvent.NEVER")
	;
	
	private static final Translator trans = Application.getTranslator();
	public final String name;
	private final String translationKey;
	protected String description=null;

	//public static final IgnitionEvent[] events = {AUTOMATIC, LAUNCH, EJECTION_CHARGE, BURNOUT, NEVER};
	
	public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
		// default behavior. Also for the NEVER case. 
		return false;
	}		
	
	private IgnitionEvent(final String _name, final String _key) {
		this.name = _name;
		this.translationKey = _key; 
	}
	
	public boolean equals( final String content){
		String comparator = this.name.toLowerCase(Locale.ENGLISH).replaceAll("_", "");
		return comparator.equals(content);
	}
	
	public String getName(){
		return this.name;
	}
	
	@Override
	public String toString() {
		if( null == this.description ){
			this.description = trans.get(this.translationKey);
		}
		return this.description;
	}
	
}
