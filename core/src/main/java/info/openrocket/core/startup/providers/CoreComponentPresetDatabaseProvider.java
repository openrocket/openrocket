package info.openrocket.core.startup.providers;

import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.ComponentPresetDatabaseLoader;

import com.google.inject.Provider;

/**
 * Provider for ComponentPresetDatabase in core-only applications.
 * This provider blocks until the database is fully loaded.
 */
public class CoreComponentPresetDatabaseProvider implements Provider<ComponentPresetDatabase> {
	
	private final ComponentPresetDatabaseLoader loader;
	
	public CoreComponentPresetDatabaseProvider(ComponentPresetDatabaseLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public ComponentPresetDatabase get() {
		loader.blockUntilLoaded();
		return loader.getDatabase();
	}
}