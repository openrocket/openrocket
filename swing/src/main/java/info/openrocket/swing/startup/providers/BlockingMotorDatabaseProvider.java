package info.openrocket.swing.startup.providers;

import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.l10n.Translator;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.database.MotorDatabaseLoader;
import info.openrocket.swing.gui.main.Splash;
import info.openrocket.swing.gui.util.GUIUtil;

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

		/*
		 * Startup currently performs the motor database update check before the normal
		 * motor loader start call. Because that update check uses modal dialogs, the EDT
		 * can re-enter startup code and request the motor database early. If we only show
		 * the loading dialog here, it can wait forever because the original startup stack
		 * has not yet resumed to call startLoading(). Starting the loader on demand breaks
		 * that cycle and lets startup continue.
		 */
		if (!loader.hasStartedLoading()) {
			log.info("Motor database requested before background loading started, starting it now");
			loader.startLoading();
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
			super(null, trans.get("MotorDbLoadDlg.title"), ModalityType.DOCUMENT_MODAL);
			
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
