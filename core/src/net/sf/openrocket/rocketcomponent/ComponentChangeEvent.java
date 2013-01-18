package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;

public class ComponentChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	
	/** A change that does not affect simulation results in any way (name, color, etc.) */
	public static final int NONFUNCTIONAL_CHANGE = 1;
	/** A change that affects the mass properties of the rocket */
	public static final int MASS_CHANGE = 2;
	/** A change that affects the aerodynamic properties of the rocket */
	public static final int AERODYNAMIC_CHANGE = 4;
	/** A change that affects the mass and aerodynamic properties of the rocket */
	public static final int BOTH_CHANGE = MASS_CHANGE | AERODYNAMIC_CHANGE; // Mass & Aerodynamic
	
	/** A change that affects the rocket tree structure */
	public static final int TREE_CHANGE = 8;
	/** A change caused by undo/redo. */
	public static final int UNDO_CHANGE = 16;
	/** A change in the motor configurations or names */
	public static final int MOTOR_CHANGE = 32;
	/** A change that affects the events occurring during flight. */
	public static final int EVENT_CHANGE = 64;
	/** A change to the 3D texture assigned to a component*/
	public static final int TEXTURE_CHANGE = 128;
	
	/** A bit-field that contains all possible change types. */
	public static final int ALL_CHANGE = 0xFFFFFFFF;
	
	private final int type;
	
	
	public ComponentChangeEvent(RocketComponent component, int type) {
		super(component);
		if (type == 0) {
			throw new IllegalArgumentException("no event type provided");
		}
		this.type = type;
	}
	
	
	/**
	 * Return the source component of this event as specified in the constructor.
	 */
	@Override
	public RocketComponent getSource() {
		return (RocketComponent) super.getSource();
	}
	
	public boolean isTextureChange() {
		return (type & TEXTURE_CHANGE) != 0;
	}
	
	public boolean isAerodynamicChange() {
		return (type & AERODYNAMIC_CHANGE) != 0;
	}
	
	public boolean isMassChange() {
		return (type & MASS_CHANGE) != 0;
	}
	
	public boolean isOtherChange() {
		return (type & BOTH_CHANGE) == 0;
	}
	
	public boolean isTreeChange() {
		return (type & TREE_CHANGE) != 0;
	}
	
	public boolean isUndoChange() {
		return (type & UNDO_CHANGE) != 0;
	}
	
	public boolean isMotorChange() {
		return (type & MOTOR_CHANGE) != 0;
	}
	
	public int getType() {
		return type;
	}
	
	@Override
	public String toString() {
		String s = "";
		
		if ((type & NONFUNCTIONAL_CHANGE) != 0)
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
		if ((type & EVENT_CHANGE) != 0)
			s += ",event";
		
		if (s.length() > 0)
			s = s.substring(1);
		
		return "ComponentChangeEvent[" + s + "]";
	}
}
