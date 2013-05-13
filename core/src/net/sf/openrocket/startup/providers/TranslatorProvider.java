package net.sf.openrocket.startup.providers;

import java.util.Locale;
import java.util.prefs.Preferences;

import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

public class TranslatorProvider implements Provider<Translator> {
	
	private final static Logger log = LoggerFactory.getLogger(TranslatorProvider.class);
	
	@Override
	public Translator get() {
		
		// Check for locale propery
		String langcode = System.getProperty("openrocket.locale");
		
		if (langcode != null) {
			
			Locale l = L10N.toLocale(langcode);
			log.info("Setting custom locale " + l);
			Locale.setDefault(l);
			
		} else {
			
			// Check user-configured locale
			Locale l = getUserLocale();
			if (l != null) {
				log.info("Setting user-selected locale " + l);
				Locale.setDefault(l);
			} else {
				log.info("Using default locale " + Locale.getDefault());
			}
			
		}
		
		// Setup the translator
		Translator t;
		t = new ResourceBundleTranslator("l10n.messages");
		if (Locale.getDefault().getLanguage().equals("xx")) {
			t = new DebugTranslator(t);
		}
		
		log.info("Set up translation for locale " + Locale.getDefault() +
				", debug.currentFile=" + t.get("debug.currentFile"));
		
		return t;
		
	}
	
	private static Locale getUserLocale() {
		/*
		 * This method MUST NOT use the Prefs class, since is causes a multitude
		 * of classes to be initialized.  Therefore this duplicates the functionality
		 * of the Prefs class locally.
		 */
		
		if (System.getProperty("openrocket.debug.prefs") != null) {
			return null;
		}
		
		return L10N.toLocale(Preferences.userRoot().node("OpenRocket").get("locale", null));
	}
	
	
	
}
