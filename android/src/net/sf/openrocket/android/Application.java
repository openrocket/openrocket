package net.sf.openrocket.android;

import java.util.Locale;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;

public class Application extends android.app.Application {

	// Big B boolean so I can synchronize on it.
	private static Boolean initialized = false;

	public void initialize() {
		synchronized (initialized) {
			if ( initialized == true ) {
				return;
			}

			// Android does not have a default sax parser set.  This needs to be defined first.
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

			net.sf.openrocket.startup.Application.setLogger( new AndroidLogWrapper.LogHelper() );

			net.sf.openrocket.startup.Application.setPreferences( new PreferencesAdapter() );

			net.sf.openrocket.startup.Application.setComponentPresetDao( new ComponentPresetDatabase(){
				@Override
				protected void load() {
					// We don't need components
				} 
			} );

			MotorDatabaseAdapter db = new MotorDatabaseAdapter(this);

			net.sf.openrocket.startup.Application.setMotorSetDatabase(db);

			Translator t;
			t = new ResourceBundleTranslator("l10n.messages");
			if (Locale.getDefault().getLanguage().equals("xx")) {
				t = new DebugTranslator(t);
			}

			net.sf.openrocket.startup.Application.setBaseTranslator(t);

			initialized = true;
		}
	}

	public Application() {
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		initialize();
		boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		AndroidLogWrapper.setLogEnabled(isDebuggable);
		PreferencesActivity.initializePreferences(this, PreferenceManager.getDefaultSharedPreferences(this));
	}

}
