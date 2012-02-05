package net.sf.openrocket.android.rocket;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;

public abstract class MissingMotorHelpers {

	public static Set<ThrustCurveMotorPlaceholder> findMissingMotors( Rocket rocket ) {

		Set<ThrustCurveMotorPlaceholder> missingMotors = new HashSet<ThrustCurveMotorPlaceholder>();
		Configuration config = rocket.getDefaultConfiguration();
		for( String configID : rocket.getMotorConfigurationIDs() ) {
			config.setMotorConfigurationID(configID);
			Iterator<MotorMount> mmts = config.motorIterator();
			while ( mmts.hasNext() ) {
				MotorMount mmt = mmts.next();
				Motor m = mmt.getMotor(configID);
				if ( m instanceof ThrustCurveMotorPlaceholder ) {
					missingMotors.add( (ThrustCurveMotorPlaceholder) m );
				}
			}

		}

		for ( ThrustCurveMotorPlaceholder m : missingMotors ) {
			AndroidLogWrapper.d(MissingMotorHelpers.class, "Missing Motor: {}", m);
		}

		return missingMotors;
	}

	public static void updateMissingMotors( Rocket rocket, WarningSet warnings ) {
	
		DatabaseMotorFinder finder = new DatabaseMotorFinder();
		
		Configuration config = rocket.getDefaultConfiguration();
		for( String configID : rocket.getMotorConfigurationIDs() ) {
			config.setMotorConfigurationID(configID);
			Iterator<MotorMount> mmts = config.motorIterator();
			while ( mmts.hasNext() ) {
				MotorMount mmt = mmts.next();
				Motor m = mmt.getMotor(configID);
				if ( m instanceof ThrustCurveMotorPlaceholder ) {
					
					ThrustCurveMotorPlaceholder placeholder = (ThrustCurveMotorPlaceholder)m;
					Motor newMotor = finder.findMotor(placeholder.getMotorType(),
							placeholder.getManufacturer(),
							placeholder.getDesignation(),
							placeholder.getDiameter(),
							placeholder.getLength(),
							placeholder.getDigest(),
							warnings);

					if ( newMotor != null ) {
						// one is now here so replace it
						mmt.setMotor(configID, newMotor);
					}
				}
			}

		}

		
	}
	
}
