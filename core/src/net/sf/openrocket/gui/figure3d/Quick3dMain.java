package net.sf.openrocket.gui.figure3d;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.openrocket.importt.OpenRocketLoader;
import net.sf.openrocket.gui.main.componenttree.ComponentTree;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.startup.Application;

/**
 * An application for quickly testing 3d figure witout all the OpenRocket user interface
 * 
 * @author bkuker
 *
 */
public class Quick3dMain {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Application.setBaseTranslator(new ResourceBundleTranslator(
				"l10n.messages"));
		// TODO: broken code due to motor db refactoring - now using Guice injectors
		//		Application.setMotorSetDatabase(new ThrustCurveMotorSetDatabase(false) {
		//			{
		//				startLoading();
		//			}
		//
		//			@Override
		//			protected void loadMotors() {
		//			}
		//		});
		Application.setPreferences(new SwingPreferences());
		
		// Must be done after localization is initialized
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase() {
			
			@Override
			protected void load() {
				// This test app doesn't need any presets loaded - just an empty database.
			}
			
		};
		Application.setComponentPresetDao(componentPresetDao);
		
		OpenRocketDocument doc = new OpenRocketLoader().loadFromStream(
				Quick3dMain.class.getResourceAsStream("/datafiles/examples/Clustered rocket design.ork"),
				new DatabaseMotorFinder());
		
		JFrame ff = new JFrame();
		ff.setSize(1200, 400);
		ff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		RocketPanel panel;
		
		panel = new RocketPanel(doc);
		
		ComponentTree ct = new ComponentTree(doc);
		panel.setSelectionModel(ct.getSelectionModel());
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(ct, BorderLayout.WEST);
		p.add(panel, BorderLayout.CENTER);
		ff.setContentPane(p);
		ff.setVisible(true);
	}
}
