package net.sf.openrocket.startup;

import com.google.inject.AbstractModule;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.util.watcher.DirectoryChangeReactor;
import net.sf.openrocket.util.watcher.DirectoryChangeReactorImpl;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(LogHelper.class).toInstance(Application.getLogger());
		bind(Preferences.class).toInstance(Application.getPreferences());
		bind(Translator.class).toInstance(Application.getTranslator());
        bind(DirectoryChangeReactor.class).to(DirectoryChangeReactorImpl.class);
	}

}
