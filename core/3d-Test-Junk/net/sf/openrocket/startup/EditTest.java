package net.sf.openrocket.startup;
import javax.swing.JFrame;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.openrocket.importt.OpenRocketLoader;
import net.sf.openrocket.gui.configdialog.NoseConeConfig;
import net.sf.openrocket.gui.configdialog.RocketComponentConfig;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.ConcurrentComponentPresetDatabaseLoader;
import net.sf.openrocket.startup.ExceptionHandler;

/**
 * An application for quickly testing 3d figure witout all the OpenRocket user
 * interface
 * 
 * @author bkuker
 * 
 */
public class EditTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		GUIUtil.setBestLAF();
		Application.setExceptionHandler(new ExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				throwable.printStackTrace();

			}

			@Override
			public void handleErrorCondition(Throwable exception) {
				exception.printStackTrace();
			}

			@Override
			public void handleErrorCondition(String message, Throwable exception) {
				exception.printStackTrace();

			}

			@Override
			public void handleErrorCondition(String message) {
				System.err.println(message);

			}
		});
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
				ConcurrentComponentPresetDatabaseLoader presetLoader = new ConcurrentComponentPresetDatabaseLoader( this );
				presetLoader.load();
				try {
					presetLoader.await();
				} catch ( InterruptedException iex) {
					
				}
			}
			
		};
		componentPresetDao.load("datafiles", ".*csv");
		componentPresetDao.startLoading();
		Application.setComponentPresetDao(componentPresetDao);

		OpenRocketDocument doc = new OpenRocketLoader().loadFromStream(
				EditTest.class.getResourceAsStream("/datafiles/examples/Clustered rocket design.ork"),
				new DatabaseMotorFinder());

		JFrame ff = new JFrame();
		ff.setSize(1200, 400);
		ff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		NoseCone nc = null;
		for (RocketComponent c : doc.getDefaultConfiguration()) {
			if (c instanceof NoseCone) {
				nc = (NoseCone) c;
			}
		}

		RocketComponentConfig ncc = new RocketComponentConfig(doc, nc);

		JFrame jf = new JFrame();
		jf.setSize(600, 400);
		jf.setContentPane(ncc);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);

	}
}
