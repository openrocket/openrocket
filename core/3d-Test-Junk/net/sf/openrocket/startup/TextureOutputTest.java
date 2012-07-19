package net.sf.openrocket.startup;
import java.io.File;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;

/**
 * An application for quickly testing 3d figure witout all the OpenRocket user
 * interface
 * 
 * @author bkuker
 * 
 */
public class TextureOutputTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Application.setBaseTranslator(new ResourceBundleTranslator("l10n.messages"));
		Application.setMotorSetDatabase(new ThrustCurveMotorSetDatabase(false) {
			{
				startLoading();
			}

			@Override
			protected void loadMotors() {
			}
		});
		Application.setPreferences(new SwingPreferences());

		// Must be done after localization is initialized
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase(true) {

			@Override
			protected void load() {
			}
			
		};
		Application.setComponentPresetDao(componentPresetDao);
		
		OpenRocketDocument doc = new GeneralRocketLoader().load(
				new File("3d-Test-Junk/net/sf/openrocket/startup/al1 Apocalypse_54mmtestFr.rkt.xml"),
				new DatabaseMotorFinder());

		StorageOptions saver = new StorageOptions();
		saver.setIncludeDecals(true);
		
		new OpenRocketSaver().save(new File("3d-Test-Junk/net/sf/openrocket/startup/Apocalypse-ork.zip"), doc, saver);

	}
}
