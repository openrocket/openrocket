package net.sf.openrocket.gui.dialogs;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.Pair;


/**
 * A modal dialog that runs specific SwingWorkers and waits until they complete.
 * A message and progress bar is provided and a cancel button.  If the cancel button
 * is pressed, the currently running worker is interrupted and the later workers are not
 * executed.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SwingWorkerDialog extends JDialog implements PropertyChangeListener {

	private final JLabel label;
	private final JProgressBar progressBar;
	
	private int position;
	private Pair<String, SwingWorker<?,?>>[] workers;
	
	private boolean cancelled = false;
	
	public SwingWorkerDialog(Window parent, String title) {
		super(parent, title, ModalityType.APPLICATION_MODAL);

		JPanel panel = new JPanel(new MigLayout("fill"));
		
		label = new JLabel("");
		panel.add(label, "wrap para");
		
		progressBar = new JProgressBar();
		panel.add(progressBar, "growx, wrap para");
		
		JButton cancel = new JButton("Cancel");
		// TODO: CRITICAL: Implement cancel
		panel.add(cancel, "right");
		
		this.add(panel);
		this.pack();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	
	/**
	 * Execute the provided workers one after another.  When this call returns
	 * the workers will all have completed.
	 *   
	 * @param workers	pairs of description texts and workers to run.
	 */
	public void runWorkers(Pair<String, SwingWorker<?,?>> ... workers) {
		if (workers.length == 0) {
			throw new IllegalArgumentException("No workers provided.");
		}
		
		this.workers = workers;
		position = -1;
		
		for (int i=0; i < workers.length; i++) {
			workers[i].getV().addPropertyChangeListener(this);
		}
		
		nextWorker();
		this.setVisible(true);  // Waits until all have ended
	}

	
	
	/**
	 * Starts the execution of the next worker in the queue.  If the last worker
	 * has completed or the operation has been cancelled, closes the dialog.
	 */
	private void nextWorker() {
		if ((position >= workers.length-1) || cancelled) {
			close();
			return;
		}
		
		position++;
		
		label.setText(workers[position].getU());
		workers[position].getV().execute();
	}
	
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (workers[position].getV().getState() == SwingWorker.StateValue.DONE) {
			nextWorker();
		}
		
		int value = workers[position].getV().getProgress();
		value = (value + position*100 ) / workers.length;
		progressBar.setValue(value);
	}
	
	
	
	private void close() {
		for (int i=0; i < workers.length; i++) {
			workers[i].getV().removePropertyChangeListener(this);
		}
		this.setVisible(false);
	}
}
