package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;

public class ComponentChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	public enum TYPE {
		ERROR(0),
		NON_FUNCTIONAL(1),
		MASS(2),
		AERODYNAMIC(4),
		AERO_MASS ( AERODYNAMIC.value | MASS.value ), // 6
		TREE( 8),
		UNDO( 16),
		MOTOR( 32),
		EVENT( 64),
		TEXTURE ( 128),
		ALL(0xFFFFFFFF);
		
		protected int value;
		
		private TYPE( final int _val){
			this.value = _val;
		}
		
		public boolean has( final int testValue ){
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
	public static final int BOTH_CHANGE = TYPE.AERO_MASS.value; // Mass & Aerodynamic
	
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
	
	// A bit-field that contains all possible change types. 
	// Will output as -1. for an explanation, see "twos-complement" representation of signed integers
	public static final int ALL_CHANGE = TYPE.ALL.value;
	
	
	private final int type;
	
	
	public ComponentChangeEvent(RocketComponent component, final int type) {
		super(component);
		if (0 > type ) {
			throw new IllegalArgumentException("bad event type provided");
		}
		this.type = type;
	}
	

	public ComponentChangeEvent(RocketComponent component, final ComponentChangeEvent.TYPE type) {
		super(component);
		if ((TYPE.ERROR ==  type)||(null== type)) {
			throw new IllegalArgumentException("no event type provided");
		}
		this.type = type.value;
	}
	
	
	/**
	 * Return the source component of this event as specified in the constructor.
	 */
	@Override
	public RocketComponent getSource() {
		return (RocketComponent) super.getSource();
	}
	
	public boolean isAerodynamicChange() {
		return TYPE.AERODYNAMIC.has( this.type);
	}
	

	public boolean isEventChange() {
		return TYPE.EVENT.has( this.type);
	}
	
	public boolean isFunctionalChange() {
		return ! (TYPE.NON_FUNCTIONAL.has( this.type));
	}
	
	public boolean isMassChange() {
		return TYPE.MASS.has(this.type);
	}

	public boolean isTextureChange() {
		return TYPE.TEXTURE.has(this.type);
	}
	
	public boolean isTreeChange() {
		return TYPE.TREE.has(this.type);
	}
	
	public boolean isUndoChange() {
		return TYPE.UNDO.has(this.type);
	}
	
	
	public boolean isMotorChange() {
		return TYPE.MASS.has(this.type);
	}
	
	public int getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		String s = "";
		
		if (isFunctionalChange())
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
