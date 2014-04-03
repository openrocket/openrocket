package net.sf.openrocket.startup.providers;

import net.sf.openrocket.database.MotorDatabaseLoader;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

public class APIBlockingMotorDatabaseProvider implements Provider<ThrustCurveMotorSetDatabase>{
	
	public APIBlockingMotorDatabaseProvider(MotorDatabaseLoader loader){
		this.loader = loader;
	}
	
	private static final Logger log = LoggerFactory.getLogger(APIBlockingMotorDatabaseProvider.class);
	
	private MotorDatabaseLoader loader;
	
	private void check(){
		if (loader.isLoaded()) {
			return;
		}
		
		log.info("Motor database not loaded yet, delaying until loaded");
		loader.blockUntilLoaded();
		log.info("Motor database now loaded");
	}

	@Override
	public ThrustCurveMotorSetDatabase get() {
		check();
		return loader.getDatabase();
	}
}

