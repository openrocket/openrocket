package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;

public interface MotorMount extends ChangeSource {
	
	public static enum IgnitionEvent {
		//// Automatic (launch or ejection charge)
		AUTOMATIC("MotorMount.IgnitionEvent.AUTOMATIC") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				int count = source.getRocket().getStageCount();
				int stage = source.getStageNumber();
				
				if (stage == count - 1) {
					return LAUNCH.isActivationEvent(e, source);
				} else {
					return EJECTION_CHARGE.isActivationEvent(e, source);
				}
			}
		},
		//// Launch
		LAUNCH("MotorMount.IgnitionEvent.LAUNCH") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return (e.getType() == FlightEvent.Type.LAUNCH);
			}
		},
		//// First ejection charge of previous stage
		EJECTION_CHARGE("MotorMount.IgnitionEvent.EJECTION_CHARGE") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				
				int charge = e.getSource().getStageNumber();
				int mount = source.getStageNumber();
				return (mount + 1 == charge);
			}
		},
		//// First burnout of previous stage
		BURNOUT("MotorMount.IgnitionEvent.BURNOUT") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.BURNOUT)
					return false;
				
				int charge = e.getSource().getStageNumber();
				int mount = source.getStageNumber();
				return (mount + 1 == charge);
			}
		},
		//// Never
		NEVER("MotorMount.IgnitionEvent.NEVER") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return false;
			}
		},
		;
		

		private static final Translator trans = Application.getTranslator();
		private final String description;
		
		IgnitionEvent(String description) {
			this.description = description;
		}
		
		public abstract boolean isActivationEvent(FlightEvent e, RocketComponent source);
		
		@Override
		public String toString() {
			return trans.get(description);
		}
	};
	
	
	/**
	 * Is the component currently a motor mount.
	 * 
	 * @return  whether the component holds a motor.
	 */
	public boolean isMotorMount();
	
	/**
	 * Set whether the component is currently a motor mount.
	 */
	public void setMotorMount(boolean mount);
	
	
	/**
	 * Return the motor for the motor configuration.  May return <code>null</code>
	 * if no motor has been set.  This method must return <code>null</code> if ID
	 * is <code>null</code> or if the ID is not valid for the current rocket
	 * (or if the component is not part of any rocket).
	 * 
	 * @param id	the motor configuration ID
	 * @return  	the motor, or <code>null</code> if not set.
	 */
	public Motor getMotor(String id);
	
	/**
	 * Set the motor for the motor configuration.  May be set to <code>null</code>
	 * to remove the motor.
	 * 
	 * @param id	 the motor configuration ID
	 * @param motor  the motor, or <code>null</code>.
	 */
	public void setMotor(String id, Motor motor);
	
	/**
	 * Get the number of similar motors clustered.
	 * 
	 * TODO: HIGH: This should not be used, since the components themselves can be clustered
	 * 
	 * @return  the number of motors.
	 */
	@Deprecated
	public int getMotorCount();
	
	

	/**
	 * Return the ejection charge delay of given motor configuration.
	 * A "plugged" motor without an ejection charge is given by
	 * {@link Motor#PLUGGED} (<code>Double.POSITIVE_INFINITY</code>).
	 * 
	 * @param id	the motor configuration ID
	 * @return  	the ejection charge delay.
	 */
	public double getMotorDelay(String id);
	
	/**
	 * Set the ejection change delay of the given motor configuration.  
	 * The ejection charge is disable (a "plugged" motor) is set by
	 * {@link Motor#PLUGGED} (<code>Double.POSITIVE_INFINITY</code>).
	 * 
	 * @param id	 the motor configuration ID
	 * @param delay  the ejection charge delay.
	 */
	public void setMotorDelay(String id, double delay);
	
	
	/**
	 * Return the event that ignites this motor.
	 * 
	 * @return   the {@link IgnitionEvent} that ignites this motor.
	 */
	public IgnitionEvent getIgnitionEvent();
	
	/**
	 * Sets the event that ignites this motor.
	 * 
	 * @param event   the {@link IgnitionEvent} that ignites this motor.
	 */
	public void setIgnitionEvent(IgnitionEvent event);
	
	
	/**
	 * Returns the ignition delay of this motor.
	 * 
	 * @return  the ignition delay
	 */
	public double getIgnitionDelay();
	
	/**
	 * Sets the ignition delay of this motor.
	 * 
	 * @param delay   the ignition delay.
	 */
	public void setIgnitionDelay(double delay);
	
	
	/**
	 * Return the distance that the motors hang outside this motor mount.
	 * 
	 * @return  the overhang length.
	 */
	public double getMotorOverhang();
	
	/**
	 * Sets the distance that the motors hang outside this motor mount.
	 * 
	 * @param overhang   the overhang length.
	 */
	public void setMotorOverhang(double overhang);
	
	

	/**
	 * Return the inner diameter of the motor mount.
	 * 
	 * @return  the inner diameter of the motor mount.
	 */
	public double getMotorMountDiameter();
	
	
	/**
	 * Return the position of the motor relative to this component.  The coordinate
	 * is that of the front cap of the motor.
	 * 
	 * @return	the position of the motor relative to this component.
	 * @throws  IllegalArgumentException if a motor with the specified ID does not exist.
	 */
	public Coordinate getMotorPosition(String id);
	
}
