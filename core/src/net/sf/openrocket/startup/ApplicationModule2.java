package net.sf.openrocket.startup;

import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

public class ApplicationModule2 extends AbstractModule {
	
	private final Provider<ThrustCurveMotorSetDatabase> motorDatabaseProvider;
	
	
	public ApplicationModule2(Provider<ThrustCurveMotorSetDatabase> motorDatabaseProvider) {
		this.motorDatabaseProvider = motorDatabaseProvider;
	}
	
	
	@Override
	protected void configure() {
		bind(ThrustCurveMotorSetDatabase.class).toProvider(motorDatabaseProvider);
		bind(MotorDatabase.class).toProvider(motorDatabaseProvider);
	}
	
}
