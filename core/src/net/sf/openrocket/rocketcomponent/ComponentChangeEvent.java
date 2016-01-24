package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;

public class ComponentChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	public enum TYPE {
		ERROR(-1, "Error"),
		NON_FUNCTIONAL(1, "nonFunctional"),
		MASS(2, "Mass"),
		AERODYNAMIC(4, "Aerodynamic"),
		TREE( 8, "TREE"),
		UNDO( 16, "UNDO"),
		MOTOR( 32, "Motor"),
		EVENT( 64, "Event"),
		TEXTURE ( 128, "Texture")
		, GRAPHIC( 256, "Configuration")
		;
		
		protected int value;
		protected String name;
		
		private TYPE( final int _val, final String _name){
			this.value = _val;
			this.name = _name;
		}
		
		public boolean matches( final int testValue ){
			return (0 != (this.value & testValue ));
		}
		
	};
	
	/** A change that does not affect simulation results in any way (name, color, etc.) */
	public static final int NONFUNCTIONAL_CHANGE = TYPE.NON_FUNCTIONAL.value;
	/** A change that affects the mass properties of the rocket */
	public static final int MASS_CHANGE = TYPE.MASS.value;
	/** A change that affects the aerodynamic properties of the rocket */
	public static final int AERODYNAMIC_CHANGE = TYPE.AERODYNAMIC.value;
	/** A change that affects the mass and aerodynamic properties of the rocket */
	public static final int AEROMASS_CHANGE = (TYPE.MASS.value | TYPE.AERODYNAMIC.value );
	public static final int BOTH_CHANGE = AEROMASS_CHANGE;  // syntactic sugar / backward compatibility

	
	/** A change that affects the rocket tree structure */
	public static final int TREE_CHANGE = TYPE.TREE.value;
	/** A change caused by undo/redo. */
	public static final int UNDO_CHANGE = TYPE.UNDO.value;
	/** A change in the motor configurations or names */
	public static final int MOTOR_CHANGE = TYPE.MOTOR.value;
	/** A change that affects the events occurring during flight. */
	public static final int EVENT_CHANGE = TYPE.EVENT.value;
	/** A change to the 3D texture assigned to a component*/
	public static final int TEXTURE_CHANGE = TYPE.TEXTURE.value;
	// when a flight configuration fires an event, it is of this type
	// UI-only change, but does not effect the true
	public static final int GRAPHIC_CHANGE = TYPE.GRAPHIC.value;
	
	//// A bit-field that contains all possible change types. 
	//// Will output as -1. for an explanation, see "twos-complement" representation of signed integers
	//public static final int ALL_CHANGE =  0xFFFFFFFF;
			
	private final int type;
	
	
	public ComponentChangeEvent(RocketComponent component, final int type) {
		super(component);
		this.type = type;
	}
	

	public ComponentChangeEvent(RocketComponent component, final ComponentChangeEvent.TYPE type) {
		super(component);
		if ((TYPE.ERROR ==  type)||(null== type)) {
			throw new IllegalArgumentException("no event type provided");
		}
		this.type = type.value;
	}
	

	public static TYPE getTypeEnum( final int typeNumber ){
		for( TYPE ccet : ComponentChangeEvent.TYPE.values() ){
			if( ccet.value == typeNumber ){
				return ccet;
			}
		}
		throw new IllegalArgumentException(" type number "+typeNumber+" is not a valid Type enum...");
	}
	
	/**
	 * Return the source component of this event as specified in the constructor.
	 */
	@Override
	public RocketComponent getSource() {
		return (RocketComponent) super.getSource();
	}
	
	public boolean isAerodynamicChange() {
		return TYPE.AERODYNAMIC.matches( this.type);
	}
	

	public boolean isEventChange() {
		return TYPE.EVENT.matches( this.type);
	}

	public boolean isFunctionalChange() {
		return ! this.isNonFunctionalChange();
	}
	
	public boolean isNonFunctionalChange() {
		return (TYPE.NON_FUNCTIONAL.matches( this.type));
	}
	
	public boolean isMassChange() {
		return TYPE.MASS.matches(this.type);
	}

	public boolean isTextureChange() {
		return TYPE.TEXTURE.matches(this.type);
	}
	
	public boolean isTreeChange() {
		return TYPE.TREE.matches(this.type);
	}
	
	public boolean isUndoChange() {
		return TYPE.UNDO.matches(this.type);
	}
	
	
	public boolean isMotorChange() {
		return TYPE.MOTOR.matches(this.type);
	}
	
	public int getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		String s = "";
		
		if (isNonFunctionalChange())
			s += ",nonfunc";
		if (isMassChange())
			s += ",mass";
		if (isAerodynamicChange())
			s += ",aero";
		if (isTreeChange())
			s += ",tree";
		if (isUndoChange())
			s += ",undo";
		if (isMotorChange())
			s += ",motor";
		if (isEventChange())
			s += ",event";
		
		if (s.length() > 0)
			s = s.substring(1);
		
		return "ComponentChangeEvent[" + s + "]";
	}
}
