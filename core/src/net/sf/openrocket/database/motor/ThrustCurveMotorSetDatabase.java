package net.sf.openrocket.database.motor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

/**
 * A database containing ThrustCurveMotorSet objects and allowing adding a motor
 * to the database.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ThrustCurveMotorSetDatabase implements MotorDatabase {
	
	private final List<ThrustCurveMotorSet> motorSets = new ArrayList<ThrustCurveMotorSet>();
	
	
	@Override
	public List<ThrustCurveMotor> findMotors(Motor.Type type, String manufacturer, String designation,
			double diameter, double length) {
		ArrayList<ThrustCurveMotor> results = new ArrayList<ThrustCurveMotor>();
		
		for (ThrustCurveMotorSet set : motorSets) {
			for (ThrustCurveMotor m : set.getMotors()) {
				boolean match = true;
				if (type != null && type != set.getType())
					match = false;
				else if (manufacturer != null && !m.getManufacturer().matches(manufacturer))
					match = false;
				else if (designation != null && !designation.equalsIgnoreCase(m.getDesignation()))
					match = false;
				else if (!Double.isNaN(diameter) && (Math.abs(diameter - m.getDiameter()) > 0.0015))
					match = false;
				else if (!Double.isNaN(length) && (Math.abs(length - m.getLength()) > 0.0015))
					match = false;
				
				if (match)
					results.add(m);
			}
		}
		
		return results;
	}
	
	
	/**
	 * Return a list of all ThrustCurveMotorSets.
	 */
	public List<ThrustCurveMotorSet> getMotorSets() {
		return Collections.unmodifiableList(motorSets);
	}
	
	
	/**
	 * Add a motor to the database.  If a matching ThrustCurveMototSet is found, 
	 * the motor is added to that set, otherwise a new set is created and added to the
	 * database.
	 * 
	 * @param motor		the motor to add
	 */
	public void addMotor(ThrustCurveMotor motor) {
		// Iterate from last to first, as this is most likely to hit early when loading files
		for (int i = motorSets.size() - 1; i >= 0; i--) {
			ThrustCurveMotorSet set = motorSets.get(i);
			if (set.matches(motor)) {
				set.addMotor(motor);
				return;
			}
		}
		
		ThrustCurveMotorSet newSet = new ThrustCurveMotorSet();
		newSet.addMotor(motor);
		motorSets.add(newSet);
	}
	
}
