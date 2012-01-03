package net.sf.openrocket.android;

import java.util.Locale;

import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;

public class Application extends android.app.Application {

	private OpenRocketDocument rocketDocument;
	
	// Big B boolean so I can synchronize on it.
	private static Boolean initialized = false;
	
	public static void initialize() {
		synchronized (initialized) {
			if ( initialized == true ) {
				return;
			}

			// Android does not have a default sax parser set.  This needs to be defined first.
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

			net.sf.openrocket.startup.Application.setLogger( new LogHelper() );
			
			net.sf.openrocket.startup.Application.setPreferences( new PreferencesAdapter() );
			
			ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase(false) {
				
				@Override
				protected void loadMotors() {
				}
			};
			db.startLoading();

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
		initialize();
	}

	/**
	 * @return the rocketDocument
	 */
	public OpenRocketDocument getRocketDocument() {
		return rocketDocument;
	}

	/**
	 * @param rocketDocument the rocketDocument to set
	 */
	public void setRocketDocument(OpenRocketDocument rocketDocument) {
		this.rocketDocument = rocketDocument;
	}
	
	
	
}
