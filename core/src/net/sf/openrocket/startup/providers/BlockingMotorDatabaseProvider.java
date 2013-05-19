package net.sf.openrocket.startup.providers;

import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.MotorDatabaseLoader;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A motor database that waits until the db has loaded in the background before
 * returning it.  If appropriate, it displays a modal dialog while loading.
 * <p>
 * This class dual-functions as a MotorDatabase and a Guice Provider for the same.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class BlockingMotorDatabaseProvider implements Provider<ThrustCurveMotorSetDatabase> {
	
	private static final Logger log = LoggerFactory.getLogger(BlockingMotorDatabaseProvider.class);
	
	@Inject
	private Translator trans;
	
	private final MotorDatabaseLoader loader;
	
	public BlockingMotorDatabaseProvider(MotorDatabaseLoader loader) {
		this.loader = loader;
	}
	
	
	@Override
	public ThrustCurveMotorSetDatabase get() {
		check();
		return loader.getDatabase();
	}
	
	
	
	
	private void check() {
		if (loader.isLoaded()) {
			return;
		}
		
		SplashScreen splash = Splash.getSplashScreen();
		if (splash == null || !splash.isVisible()) {
			
			log.info("Motor database not loaded yet, displaying dialog");
			
			final LoadingDialog dialog = new LoadingDialog();
			
			Timer timer = new Timer(100, new ActionListener() {
				private int count = 0;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					count++;
					if (loader.isLoaded()) {
						log.debug("Database loaded, closing dialog");
						dialog.setVisible(false);
					} else if (count % 10 == 0) {
						log.debug("Database not loaded, count=" + count);
					}
				}
			});
			
			loader.cancelStartupDelay();
			timer.start();
			dialog.setVisible(true);
			timer.stop();
			
		} else {
			
			log.info("Motor database not loaded yet, splash screen still present, delaying until loaded");
			loader.blockUntilLoaded();
			
		}
		
		log.info("Motor database now loaded");
	}
	
	
	private class LoadingDialog extends JDialog {
		private LoadingDialog() {
			super(null, trans.get("MotorDbLoadDlg.title"), ModalityType.APPLICATION_MODAL);
			
			JPanel panel = new JPanel(new MigLayout("fill"));
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
	}
	
}
