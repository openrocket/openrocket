package net.sf.openrocket.rocketcomponent;

import java.util.Locale;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;

public class IgnitionEvent {
	
	private static final Translator trans = Application.getTranslator();
	public final String name;
	private final String key;
	protected String description=null;
	
	public static final IgnitionEvent AUTOMATIC = new IgnitionEvent( "AUTOMATIC", "MotorMount.IgnitionEvent.AUTOMATIC"){
		@Override
		public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
			int count = source.getRocket().getStageCount();
			int stage = source.getStageNumber();
			
			if (stage == count - 1) {
				return LAUNCH.isActivationEvent( fe, source);
			} else {
				return EJECTION_CHARGE.isActivationEvent( fe, source);
			}
		}
	};
	
	public static final IgnitionEvent LAUNCH = new IgnitionEvent( "LAUNCH", "MotorMount.IgnitionEvent.LAUNCH"){
		@Override
		public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
			return (fe.getType() == FlightEvent.Type.LAUNCH);
		}
	};
	
	public static final IgnitionEvent EJECTION_CHARGE= new IgnitionEvent("EJECTION_CHARGE", "MotorMount.IgnitionEvent.EJECTION_CHARGE"){
		@Override
		public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
			if (fe.getType() != FlightEvent.Type.EJECTION_CHARGE){
				return false;
			}
			int charge = fe.getSource().getStageNumber();
			int mount = source.getStageNumber();
			return (mount + 1 == charge);
		}
	};
	
	public static final IgnitionEvent BURNOUT = new IgnitionEvent("BURNOUT", "MotorMount.IgnitionEvent.BURNOUT"){
		@Override
		public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
			if (fe.getType() != FlightEvent.Type.BURNOUT)
				return false;
			
			int charge = fe.getSource().getStageNumber();
			int mount = source.getStageNumber();
			return (mount + 1 == charge);
		}
	};

	public static final IgnitionEvent NEVER= new IgnitionEvent("NEVER", "MotorMount.IgnitionEvent.NEVER");
	
	public static final IgnitionEvent[] events = {AUTOMATIC, LAUNCH, EJECTION_CHARGE, BURNOUT, NEVER};
	
	public boolean isActivationEvent( FlightEvent fe, RocketComponent source){
		// default behavior. Also for the NEVER case. 
		return false;
	}		
	
	public IgnitionEvent(final String _name, final String _key) {
		this.name = _name;
		this.key = _key; 
		this.description = trans.get(this.key);
	}
	
	public boolean equals( final String content){
		String comparator = this.name.toLowerCase(Locale.ENGLISH).replaceAll("_", "");
		
		return comparator.equals(content);
	}
	
	public String name(){
		return this.name;
	}
	
	@Override
	public String toString() {
		if( null == this.description ){
			this.description = trans.get(this.key);
		}
		return this.description;
	}
	
}
