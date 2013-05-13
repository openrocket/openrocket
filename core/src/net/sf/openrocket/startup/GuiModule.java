package net.sf.openrocket.startup;

import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.gui.watcher.WatchService;
import net.sf.openrocket.gui.watcher.WatchServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

public class GuiModule extends AbstractModule {
	
	private final Provider<ThrustCurveMotorSetDatabase> motorDatabaseProvider;
	private final Provider<? extends ComponentPresetDao> componentDatabaseProvider;
	
	public GuiModule() {
		this.componentDatabaseProvider = loadPresetComponents();
		this.motorDatabaseProvider = loadMotor();
	}
	
	@Override
	protected void configure() {
		
		bind(ComponentPresetDao.class).toProvider(componentDatabaseProvider);
		bind(ThrustCurveMotorSetDatabase.class).toProvider(motorDatabaseProvider);
		bind(MotorDatabase.class).toProvider(motorDatabaseProvider);
		bind(WatchService.class).to(WatchServiceImpl.class);
	}
	
	/**
	 * Start loading preset components in background thread.
	 * 
	 */
	public BlockingComponentPresetDatabaseProvider loadPresetComponents() {
		ComponentPresetDatabaseLoader bg = new ComponentPresetDatabaseLoader();
		bg.startLoading();
		BlockingComponentPresetDatabaseProvider db = new BlockingComponentPresetDatabaseProvider(bg);
		return db;
	}
	
	/**
	 * Start loading motors in background thread.
	 * 
	 * Public for Python bindings.
	 * 
	 * @return	a provider for the database which blocks before returning the db.
	 */
	public BlockingMotorDatabaseProvider loadMotor() {
		MotorDatabaseLoader bg = new MotorDatabaseLoader();
		bg.startLoading();
		BlockingMotorDatabaseProvider db = new BlockingMotorDatabaseProvider(bg);
		return db;
	}
	
	
}
