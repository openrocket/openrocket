package net.sf.openrocket.database.motor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

/**
 * A database containing ThrustCurveMotorSet objects and allowing adding a motor
 * to the database.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ThrustCurveMotorSetDatabase implements MotorDatabase {
	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSetDatabase.class);
	
	private final List<ThrustCurveMotorSet> motorSets = new ArrayList<ThrustCurveMotorSet>();
	
	@Override
	public List<ThrustCurveMotor> findMotors(String digest, Motor.Type type, String manufacturer, String designation,
			double diameter, double length) {
		ArrayList<ThrustCurveMotor> fullMatches = new ArrayList<ThrustCurveMotor>();
		ArrayList<ThrustCurveMotor> digestMatches = new ArrayList<ThrustCurveMotor>();
		ArrayList<ThrustCurveMotor> descriptionMatches = new ArrayList<ThrustCurveMotor>();

		// Apply filters to see if we can find any motors that match the given criteria.  We'll return
		// the most restrictive nonempty list we find, or empty list if no matches at all
		for (ThrustCurveMotorSet set : motorSets) {
			for (ThrustCurveMotor m : set.getMotors()) {
				boolean matchDescription = true;
				boolean matchDigest = true;
				
				// unlike the description, digest must be present in search criteria to get a match
				if (digest == null || digest != m.getDigest())
					matchDigest = false;

				// match description
				if (type != null && type != set.getType())
					matchDescription = false;
				else if (manufacturer != null && !m.getManufacturer().matches(manufacturer))
					matchDescription = false;
				else if (designation != null &&
						 !m.getDesignation().toUpperCase().contains(designation.toUpperCase()) &&
						 !designation.toUpperCase().contains(m.getCommonName().toUpperCase()))
					matchDescription = false;
				else if (!Double.isNaN(diameter) && (Math.abs(diameter - m.getDiameter()) > 0.005))
					matchDescription = false;
				else if (!Double.isNaN(length) && (Math.abs(length - m.getLength()) > 0.005))
					matchDescription = false;

				if (matchDigest)
					digestMatches.add(m);

				if (matchDescription)
					descriptionMatches.add(m);

				if (matchDigest && matchDescription)
					fullMatches.add(m);
			}
		}
		
		if (!fullMatches.isEmpty())
			return fullMatches;

		if (!digestMatches.isEmpty())
			return digestMatches;

		return descriptionMatches;

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
