package net.sf.openrocket.gui.dialogs;

import java.awt.SplashScreen;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * A progress dialog displayed while loading motors.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorDatabaseLoadingDialog extends JDialog {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	
	
	private MotorDatabaseLoadingDialog(Window parent) {
		//// Loading motors
		super(parent, trans.get("MotorDbLoadDlg.title"), ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		//// Loading motors...
		panel.add(new JLabel(trans.get("MotorDbLoadDlg.Loadingmotors")), "wrap para");
		
		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		panel.add(progress, "growx");
		
		this.add(panel);
		this.pack();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setLocationByPlatform(true);
		GUIUtil.setWindowIcons(this);
	}
	
	
	/**
	 * Check whether the motor database is loaded and block until it is.
	 * An uncloseable modal dialog window is opened while loading unless the splash screen
	 * is still being displayed.
	 * 
	 * @param parent	the parent window for the dialog, or <code>null</code>
	 */
	public static void check(Window parent) {
		// TODO - ugly blind cast
		final ThrustCurveMotorSetDatabase db = (ThrustCurveMotorSetDatabase) Application.getMotorSetDatabase();
		if (db.isLoaded())
			return;
		
		if (SplashScreen.getSplashScreen() == null) {
			
			log.info(1, "Motor database not loaded yet, displaying dialog");
			
			final MotorDatabaseLoadingDialog dialog = new MotorDatabaseLoadingDialog(parent);
			
			final Timer timer = new Timer(100, new ActionListener() {
				private int count = 0;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					count++;
					if (db.isLoaded()) {
						log.debug("Database loaded, closing dialog");
						dialog.setVisible(false);
					} else if (count % 10 == 0) {
						log.debug("Database not loaded, count=" + count);
					}
				}
			});
			
			db.setInUse();
			timer.start();
			dialog.setVisible(true);
			timer.stop();
			
		} else {
			
			log.info(1, "Motor database not loaded yet, splash screen still present, delaying until loaded");
			
			db.setInUse();
			int count = 0;
			while (!db.isLoaded()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// No-op
				}
				
				count++;
				if (count % 10 == 0) {
					log.debug("Database not loaded, count=" + count);
				}
			}
			
		}
		
		log.info("Motor database now loaded");
	}
	
}
