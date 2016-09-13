package net.sf.openrocket.startup.providers;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.ComponentPresetDatabaseLoader;
import net.sf.openrocket.l10n.Translator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class BlockingComponentPresetDatabaseProvider implements Provider<ComponentPresetDatabase> {
	
	@Inject
	private Translator trans;
	
	private final ComponentPresetDatabaseLoader loader;
	
	public BlockingComponentPresetDatabaseProvider(ComponentPresetDatabaseLoader loader) {
		this.loader = loader;
	}
	
	
	@Override
	public ComponentPresetDatabase get() {
		loader.blockUntilLoaded();
		return loader.getDatabase();
	}
	
}
