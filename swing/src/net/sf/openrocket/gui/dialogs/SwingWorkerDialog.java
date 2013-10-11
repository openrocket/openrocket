package net.sf.openrocket.gui.dialogs;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;


/**
 * A modal dialog that runs specific SwingWorkers and waits until they complete.
 * A message and progress bar is provided and a cancel button.  If the cancel button
 * is pressed, the currently running worker is interrupted and the later workers are not
 * executed.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SwingWorkerDialog extends JDialog implements PropertyChangeListener {
	private static final Logger log = LoggerFactory.getLogger(SwingWorkerDialog.class);
	private static final Translator trans = Application.getTranslator();

	/** Number of milliseconds to wait at a time between checking worker status */
	private static final int DELAY = 100;
	
	/** Minimum number of milliseconds to wait before estimating work length */
	private static final int ESTIMATION_DELAY = 190;
	
	/** Open the dialog if estimated remaining time is longer than this */
	private static final int REMAINING_TIME_FOR_DIALOG = 1000;
	
	/** Open the dialog if estimated total time is longed than this */
	private static final int TOTAL_TIME_FOR_DIALOG = 2000;
	

	private final SwingWorker<?, ?> worker;
	private final JProgressBar progressBar;
	
	private boolean cancelled = false;
	
	
	private SwingWorkerDialog(Window parent, String title, String label, SwingWorker<?, ?> w) {
		super(parent, title, ModalityType.APPLICATION_MODAL);
		
		this.worker = w;
		w.addPropertyChangeListener(this);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		if (label != null) {
			panel.add(new JLabel(label), "wrap para");
		}
		
		progressBar = new JProgressBar();
		panel.add(progressBar, "growx, wrap para");
		
		//// Cancel button
		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "User cancelled SwingWorker operation");
				cancel();
			}
		});
		panel.add(cancel, "right");
		
		this.add(panel);
		this.setMinimumSize(new Dimension(250, 100));
		this.pack();
		this.setLocationRelativeTo(parent);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (worker.getState() == SwingWorker.StateValue.DONE) {
			close();
		}
		progressBar.setValue(worker.getProgress());
	}
	
	private void cancel() {
		cancelled = true;
		worker.cancel(true);
		close();
	}
	
	private void close() {
		worker.removePropertyChangeListener(this);
		this.setVisible(false);
		// For some reason setVisible(false) is not always enough...
		this.dispose();
	}
	
	
	/**
	 * Run a SwingWorker and if necessary show a dialog displaying the progress of
	 * the worker.  The progress information is obtained from the SwingWorker's
	 * progress property.  The dialog is shown only if the worker is estimated to
	 * take a notable amount of time.
	 * <p>
	 * The dialog contains a cancel button.  Clicking it will call
	 * <code>worker.cancel(true)</code> and close the dialog immediately.
	 * 
	 * @param parent	the parent window for the dialog, or <code>null</code>.
	 * @param title		the title for the dialog.
	 * @param label		an additional label for the dialog, or <code>null</code>.
	 * @param worker	the SwingWorker to execute.
	 * @return			<code>true</code> if the worker has completed normally,
	 * 					<code>false</code> if the user cancelled the operation
	 */
	public static boolean runWorker(Window parent, String title, String label,
			SwingWorker<?, ?> worker) {
		
		log.info("Running SwingWorker " + worker);
		
		// Start timing the worker
		final long startTime = System.currentTimeMillis();
		worker.execute();
		
		// Monitor worker thread before opening the dialog
		while (true) {
			
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				// Should never occur
				log.error("EDT was interrupted", e);
			}
			
			if (worker.isDone()) {
				// Worker has completed within time limits
				log.info("Worker completed before opening dialog");
				return true;
			}
			
			// Check whether enough time has gone to get realistic estimate
			long elapsed = System.currentTimeMillis() - startTime;
			if (elapsed < ESTIMATION_DELAY)
				continue;
			

			// Calculate and check estimated remaining time
			int progress = MathUtil.clamp(worker.getProgress(), 1, 100); // Avoid div-by-zero
			long estimate = elapsed * 100 / progress;
			long remaining = estimate - elapsed;
			
			log.debug("Estimated run time, estimate=" + estimate + " remaining=" + remaining);
			
			if (estimate >= TOTAL_TIME_FOR_DIALOG)
				break;
			
			if (remaining >= REMAINING_TIME_FOR_DIALOG)
				break;
		}
		

		// Dialog is required
		
		log.info("Opening dialog for SwingWorker " + worker);
		SwingWorkerDialog dialog = new SwingWorkerDialog(parent, title, label, worker);
		dialog.setVisible(true);
		log.info("Worker done, cancelled=" + dialog.cancelled);
		
		return !dialog.cancelled;
	}
}
