package info.openrocket.core.startup.providers;

import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.database.MotorDatabaseLoader;

import com.google.inject.Provider;

/**
 * Provider for ThrustCurveMotorSetDatabase in core-only applications.
 * This provider blocks until the database is fully loaded.
 */
public class CoreMotorDatabaseProvider implements Provider<ThrustCurveMotorSetDatabase> {
	
	private final MotorDatabaseLoader loader;
	
	public CoreMotorDatabaseProvider(MotorDatabaseLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public ThrustCurveMotorSetDatabase get() {
		loader.blockUntilLoaded();
		return loader.getDatabase();
	}
}