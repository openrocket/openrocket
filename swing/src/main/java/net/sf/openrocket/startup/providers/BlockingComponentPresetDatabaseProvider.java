package net.sf.openrocket.startup.providers;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.ComponentPresetDatabaseLoader;
import net.sf.openrocket.l10n.Translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class BlockingComponentPresetDatabaseProvider implements Provider<ComponentPresetDatabase> {
	
	private final static Logger log = LoggerFactory.getLogger(BlockingComponentPresetDatabaseProvider.class);
	
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
