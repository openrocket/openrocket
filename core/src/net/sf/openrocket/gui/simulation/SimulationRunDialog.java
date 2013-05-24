package net.sf.openrocket.gui.simulation;


import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.DetailDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.customexpression.CustomExpressionSimulationListener;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimulationRunDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(SimulationRunDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	
	/** Update the dialog status every this many ms */
	private static final long UPDATE_MS = 200;
	
	/** Flight progress at motor burnout */
	private static final double BURNOUT_PROGRESS = 0.4;
	
	/** Flight progress at apogee */
	private static final double APOGEE_PROGRESS = 0.7;
	
	
	/**
	 * A single ThreadPoolExecutor that will be used for all simulations.
	 * This executor must not be shut down.
	 */
	private static final ThreadPoolExecutor executor;
	static {
		int n = SwingPreferences.getMaxThreadCount();
		executor = new ThreadPoolExecutor(n, n,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					private ThreadFactory factory = Executors.defaultThreadFactory();
					
					@Override
					public Thread newThread(Runnable r) {
						Thread t = factory.newThread(r);
						t.setDaemon(true);
						return t;
					}
				});
	}
	
	
	
	private final JLabel simLabel, timeLabel, altLabel, velLabel;
	private final JProgressBar progressBar;
	
	
	/*
	 * NOTE:  Care must be used when accessing the simulation parameters, since they
	 * are being run in another thread.  Mutexes are used to avoid concurrent usage, which
	 * will result in an exception being thrown!
	 */
	private final Simulation[] simulations;
	@SuppressWarnings("unused")
	private final OpenRocketDocument document;
	private final String[] simulationNames;
	private final SimulationWorker[] simulationWorkers;
	private final SimulationStatus[] simulationStatuses;
	private final double[] simulationMaxAltitude;
	private final double[] simulationMaxVelocity;
	private final boolean[] simulationDone;
	
	public SimulationRunDialog(Window window, OpenRocketDocument document, Simulation... simulations) {
		//// Running simulations...
		super(window, trans.get("SimuRunDlg.title.RunSim"), Dialog.ModalityType.APPLICATION_MODAL);
		this.document = document;
		
		if (simulations.length == 0) {
			throw new IllegalArgumentException("Called with no simulations to run");
		}
		
		this.simulations = simulations;
		
		
		// Randomize the simulation random seeds
		for (Simulation sim : simulations) {
			sim.getOptions().randomizeSeed();
		}
		
		// Initialize the simulations
		int n = simulations.length;
		simulationNames = new String[n];
		simulationWorkers = new SimulationWorker[n];
		simulationStatuses = new SimulationStatus[n];
		simulationMaxAltitude = new double[n];
		simulationMaxVelocity = new double[n];
		simulationDone = new boolean[n];
		
		for (int i = 0; i < n; i++) {
			simulationNames[i] = simulations[i].getName();
			simulationWorkers[i] = new InteractiveSimulationWorker(document, simulations[i], i);
			executor.execute(simulationWorkers[i]);
		}
		
		// Build the dialog
		JPanel panel = new JPanel(new MigLayout("fill", "[][grow]"));
		
		//// Running ...
		simLabel = new JLabel(trans.get("SimuRunDlg.lbl.Running"));
		panel.add(simLabel, "spanx, wrap para");
		//// Simulation time: 
		panel.add(new JLabel(trans.get("SimuRunDlg.lbl.Simutime") + " "), "gapright para");
		timeLabel = new JLabel("");
		panel.add(timeLabel, "growx, wmin 200lp, wrap rel");
		
		//// Altitude:
		panel.add(new JLabel(trans.get("SimuRunDlg.lbl.Altitude") + " "));
		altLabel = new JLabel("");
		panel.add(altLabel, "growx, wrap rel");
		
		//// Velocity:
		panel.add(new JLabel(trans.get("SimuRunDlg.lbl.Velocity") + " "));
		velLabel = new JLabel("");
		panel.add(velLabel, "growx, wrap para");
		
		progressBar = new JProgressBar();
		panel.add(progressBar, "spanx, growx, wrap para");
		
		
		// Add cancel button
		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelSimulations();
			}
		});
		panel.add(cancel, "spanx, tag cancel");
		
		
		// Cancel simulations when user closes the window
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelSimulations();
			}
		});
		
		
		this.add(panel);
		this.setMinimumSize(new Dimension(300, 0));
		this.setLocationByPlatform(true);
		this.validate();
		this.pack();
		
		GUIUtil.setDisposableDialogOptions(this, null);
		
		updateProgress();
	}
	
	
	/**
	 * Cancel the currently running simulations.  This is equivalent to clicking
	 * the Cancel button on the dialog.
	 */
	public void cancelSimulations() {
		for (SimulationWorker w : simulationWorkers) {
			w.cancel(true);
		}
		executor.purge();
	}
	
	
	/**
	 * Static helper method to run simulations.
	 * 
	 * @param parent		the parent Window of the dialog to use.
	 * @param simulations	the simulations to run.
	 */
	public static void runSimulations(Window parent, OpenRocketDocument document, Simulation... simulations) {
		new SimulationRunDialog(parent, document, simulations).setVisible(true);
	}
	
	
	
	
	private void updateProgress() {
		int index;
		for (index = 0; index < simulations.length; index++) {
			if (!simulationDone[index])
				break;
		}
		
		if (index >= simulations.length) {
			// Everything is done, close the dialog
			log.debug("Everything done.");
			this.dispose();
			return;
		}
		
		// Update the progress bar status
		int progress = 0;
		for (SimulationWorker s : simulationWorkers) {
			progress += s.getProgress();
		}
		progress /= simulationWorkers.length;
		progressBar.setValue(progress);
		log.debug("Progressbar value " + progress);
		
		// Update the simulation fields
		simLabel.setText("Running " + simulationNames[index]);
		if (simulationStatuses[index] == null) {
			log.debug("No simulation status data available, setting empty labels");
			timeLabel.setText("");
			altLabel.setText("");
			velLabel.setText("");
			return;
		}
		
		Unit u = UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit();
		timeLabel.setText(u.toStringUnit(simulationStatuses[index].getSimulationTime()));
		
		u = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		altLabel.setText(u.toStringUnit(simulationStatuses[index].getRocketPosition().z) + " (max. " +
				u.toStringUnit(simulationMaxAltitude[index]) + ")");
		
		u = UnitGroup.UNITS_VELOCITY.getDefaultUnit();
		velLabel.setText(u.toStringUnit(simulationStatuses[index].getRocketVelocity().z) + " (max. " +
				u.toStringUnit(simulationMaxVelocity[index]) + ")");
	}
	
	
	
	/**
	 * A SwingWorker that performs a flight simulation.  It periodically updates the
	 * simulation statuses of the parent class and calls updateProgress().
	 * The progress of the simulation is stored in the progress property of the
	 * SwingWorker.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private class InteractiveSimulationWorker extends SimulationWorker {
		
		private final int index;
		private final double burnoutTimeEstimate;
		private volatile double burnoutVelocity;
		private volatile double apogeeAltitude;
		
		private final CustomExpressionSimulationListener exprListener;
		
		/*
		 * -2 = time from 0 ... burnoutTimeEstimate
		 * -1 = velocity from v(burnoutTimeEstimate) ... 0
		 *  0 ... n = stages from alt(max) ... 0
		 */
		private volatile int simulationStage = -2;
		
		private int progress = 0;
		
		
		public InteractiveSimulationWorker(OpenRocketDocument doc, Simulation sim, int index) {
			super(sim);
			List<CustomExpression> exprs = doc.getCustomExpressions();
			exprListener = new CustomExpressionSimulationListener(exprs);
			this.index = index;
			
			// Calculate estimate of motor burn time
			double launchBurn = 0;
			double otherBurn = 0;
			Configuration config = simulation.getConfiguration();
			String id = simulation.getOptions().getMotorConfigurationID();
			Iterator<MotorMount> iterator = config.motorIterator();
			while (iterator.hasNext()) {
				MotorMount m = iterator.next();
				if (m.getIgnitionConfiguration().getDefault().getIgnitionEvent() == IgnitionConfiguration.IgnitionEvent.LAUNCH)
					launchBurn = MathUtil.max(launchBurn, m.getMotor(id).getBurnTimeEstimate());
				else
					otherBurn = otherBurn + m.getMotor(id).getBurnTimeEstimate();
			}
			burnoutTimeEstimate = Math.max(launchBurn + otherBurn, 0.1);
		}
		
		
		/**
		 * Return the extra listeners to use, a progress listener and cancel listener.
		 */
		@Override
		protected SimulationListener[] getExtraListeners() {
			return new SimulationListener[] { new SimulationProgressListener(), exprListener };
		}
		
		
		/**
		 * Processes simulation statuses published by the simulation listener.
		 * The statuses of the parent class and the progress property are updated.
		 */
		@Override
		protected void process(List<SimulationStatus> chunks) {
			
			// Update max. altitude and velocity
			for (SimulationStatus s : chunks) {
				simulationMaxAltitude[index] = Math.max(simulationMaxAltitude[index],
						s.getRocketPosition().z);
				simulationMaxVelocity[index] = Math.max(simulationMaxVelocity[index],
						s.getRocketVelocity().length());
			}
			
			// Calculate the progress
			SimulationStatus status = chunks.get(chunks.size() - 1);
			simulationStatuses[index] = status;
			
			// 1. time = 0 ... burnoutTimeEstimate
			if (simulationStage == -2 && status.getSimulationTime() < burnoutTimeEstimate) {
				log.debug("Method 1:  t=" + status.getSimulationTime() + "  est=" + burnoutTimeEstimate);
				setSimulationProgress(MathUtil.map(status.getSimulationTime(), 0, burnoutTimeEstimate,
						0.0, BURNOUT_PROGRESS));
				updateProgress();
				return;
			}
			
			if (simulationStage == -2) {
				simulationStage++;
				burnoutVelocity = MathUtil.max(status.getRocketVelocity().z, 0.1);
				log.debug("CHANGING to Method 2, vel=" + burnoutVelocity);
			}
			
			// 2. z-velocity from burnout velocity to zero
			if (simulationStage == -1 && status.getRocketVelocity().z >= 0) {
				log.debug("Method 2:  vel=" + status.getRocketVelocity().z + " burnout=" + burnoutVelocity);
				setSimulationProgress(MathUtil.map(status.getRocketVelocity().z, burnoutVelocity, 0,
						BURNOUT_PROGRESS, APOGEE_PROGRESS));
				updateProgress();
				return;
			}
			
			if (simulationStage == -1 && status.getRocketVelocity().z < 0) {
				simulationStage++;
				apogeeAltitude = MathUtil.max(status.getRocketPosition().z, 1);
				log.debug("CHANGING to Method 3, apogee=" + apogeeAltitude);
			}
			
			// 3. z-position from apogee to zero
			// TODO: MEDIUM: several stages
			log.debug("Method 3:  alt=" + status.getRocketPosition().z + "  apogee=" + apogeeAltitude);
			setSimulationProgress(MathUtil.map(status.getRocketPosition().z,
					apogeeAltitude, 0, APOGEE_PROGRESS, 1.0));
			updateProgress();
		}
		
		/**
		 * Marks this simulation as done and calls the progress update.
		 */
		@Override
		protected void simulationDone() {
			simulationDone[index] = true;
			log.debug("Simulation done");
			setSimulationProgress(1.0);
			updateProgress();
		}
		
		
		/**
		 * Marks the simulation as done and shows a dialog presenting
		 * the error, unless the simulation was cancelled.
		 */
		@Override
		protected void simulationInterrupted(Throwable t) {
			
			if (t instanceof SimulationCancelledException) {
				simulationDone();
				return; // Ignore cancellations
			}
			
			// Analyze the exception type
			if (t instanceof SimulationLaunchException) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this,
						new Object[] {
								//// Unable to simulate:
								trans.get("SimuRunDlg.msg.Unabletosim"),
								t.getMessage()
						},
						null, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else if (t instanceof SimulationException) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this,
						new Object[] {
								//// A error occurred during the simulation:
								trans.get("SimuRunDlg.msg.errorOccurred"),
								t.getMessage()
						},
						null, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else {
				
				Application.getExceptionHandler().handleErrorCondition("An exception occurred during the simulation", t);
				
			}
			simulationDone();
		}
		
		
		private void setSimulationProgress(double p) {
			int exact = Math.max(progress, (int) (100 * p + 0.5));
			progress = MathUtil.clamp(exact, 0, 100);
			log.debug("Setting progress to " + progress + " (real " + exact + ")");
			super.setProgress(progress);
		}
		
		
		/**
		 * A simulation listener that regularly updates the progress property of the 
		 * SimulationWorker and publishes the simulation status for the run dialog to process.
		 * 
		 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
		 */
		private class SimulationProgressListener extends AbstractSimulationListener {
			private long time = 0;
			
			@Override
			public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) {
				switch (event.getType()) {
				case APOGEE:
					simulationStage = 0;
					apogeeAltitude = status.getRocketPosition().z;
					log.debug("APOGEE, setting progress");
					setSimulationProgress(APOGEE_PROGRESS);
					publish(status);
					break;
				
				case LAUNCH:
					publish(status);
					break;
				
				case SIMULATION_END:
					log.debug("END, setting progress");
					setSimulationProgress(1.0);
					break;
				}
				return true;
			}
			
			@Override
			public void postStep(SimulationStatus status) {
				if (System.currentTimeMillis() >= time + UPDATE_MS) {
					time = System.currentTimeMillis();
					publish(status);
				}
			}
		}
	}
}
