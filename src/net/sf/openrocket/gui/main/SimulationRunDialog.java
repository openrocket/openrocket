package net.sf.openrocket.gui.main;


import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.DetailDialog;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationListener;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Prefs;


public class SimulationRunDialog extends JDialog {
	/** Update the dialog status every this many ms */
	private static final long UPDATE_MS = 200;
	
	/** Flight progress at motor burnout */
	private static final double BURNOUT_PROGRESS = 0.4;
	
	/** Flight progress at apogee */
	private static final double APOGEE_PROGRESS = 0.7;
	
	
	/*
	 * The executor service is not static since we want concurrent simulation
	 * dialogs to run in parallel, ie. they both have their own executor service.
	 */
	private final ExecutorService executor = Executors.newFixedThreadPool(
			Prefs.getMaxThreadCount());
	
	
	private final JLabel simLabel, timeLabel, altLabel, velLabel;
	private final JProgressBar progressBar;
	
	
	private final Simulation[] simulations;
	private final SimulationWorker[] simulationWorkers;
	private final SimulationStatus[] simulationStatuses;
	private final double[] simulationMaxAltitude;
	private final double[] simulationMaxVelocity;
	private final boolean[] simulationDone;
	
	public SimulationRunDialog(Window window, Simulation ... simulations) {
		super(window, "Running simulations...", Dialog.ModalityType.DOCUMENT_MODAL);
		
		if (simulations.length == 0) {
			throw new IllegalArgumentException("Called with no simulations to run");
		}
		
		this.simulations = simulations;
	
		// Initialize the simulations
		int n = simulations.length;
		simulationWorkers = new SimulationWorker[n];
		simulationStatuses = new SimulationStatus[n];
		simulationMaxAltitude = new double[n];
		simulationMaxVelocity = new double[n];
		simulationDone = new boolean[n];
		
		for (int i=0; i<n; i++) {
			simulationWorkers[i] = new InteractiveSimulationWorker(simulations[i], i);
			executor.execute(simulationWorkers[i]);
		}
		
		// Build the dialog
		JPanel panel = new JPanel(new MigLayout("fill", "[][grow]"));
		
		simLabel = new JLabel("Running ...");
		panel.add(simLabel, "spanx, wrap para");
		
		panel.add(new JLabel("Simulation time: "), "gapright para");
		timeLabel = new JLabel("");
		panel.add(timeLabel, "growx, wrap rel");
		
		panel.add(new JLabel("Altitude: "));
		altLabel = new JLabel("");
		panel.add(altLabel, "growx, wrap rel");
		
		panel.add(new JLabel("Velocity: "));
		velLabel = new JLabel("");
		panel.add(velLabel, "growx, wrap para");
		
		progressBar = new JProgressBar();
		panel.add(progressBar, "spanx, growx, wrap para");
		
		
		// Add cancel button
		JButton cancel = new JButton("Cancel");
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
		GUIUtil.installEscapeCloseOperation(this);

		updateProgress();
	}
	
	
	/**
	 * Cancel the currently running simulations.  This is equivalent to clicking
	 * the Cancel button on the dialog.
	 */
	public void cancelSimulations() {
		executor.shutdownNow();
		for (SimulationWorker w: simulationWorkers) {
			w.cancel(true);
		}
	}
	
	
	/**
	 * Static helper method to run simulations.
	 * 
	 * @param parent		the parent Window of the dialog to use.
	 * @param simulations	the simulations to run.
	 */
	public static void runSimulations(Window parent, Simulation ... simulations) {
		new SimulationRunDialog(parent, simulations).setVisible(true);
	}
	
	
	
	
	private void updateProgress() {
		System.out.println("updateProgress() called");
		int index;
		for (index=0; index < simulations.length; index++) {
			if (!simulationDone[index])
				break;
		}
		
		if (index >= simulations.length) {
			// Everything is done, close the dialog
			System.out.println("Everything done.");
			this.dispose();
			return;
		}

		// Update the progress bar status
		int progress = 0;
		for (SimulationWorker s: simulationWorkers) {
			progress += s.getProgress();
		}
		progress /= simulationWorkers.length;
		progressBar.setValue(progress);
		System.out.println("Progressbar value "+progress);
		
		// Update the simulation fields
		simLabel.setText("Running " + simulations[index].getName());
		if (simulationStatuses[index] == null) {
			timeLabel.setText("");
			altLabel.setText("");
			velLabel.setText("");
			System.out.println("Empty labels, how sad.");
			return;
		}
		
		Unit u = UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit();
		timeLabel.setText(u.toStringUnit(simulationStatuses[index].time));
		
		u = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		altLabel.setText(u.toStringUnit(simulationStatuses[index].position.z) + " (max. " +
				u.toStringUnit(simulationMaxAltitude[index]) + ")");
		
		u = UnitGroup.UNITS_VELOCITY.getDefaultUnit();
		velLabel.setText(u.toStringUnit(simulationStatuses[index].velocity.z) + " (max. " +
				u.toStringUnit(simulationMaxVelocity[index]) + ")");
		System.out.println("Set interesting labels.");
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
		
		/*
		 * -2 = time from 0 ... burnoutTimeEstimate
		 * -1 = velocity from v(burnoutTimeEstimate) ... 0
		 *  0 ... n = stages from alt(max) ... 0
		 */
		private volatile int simulationStage = -2;
		
		private int progress = 0;
		
		
		public InteractiveSimulationWorker(Simulation sim, int index) {
			super(sim);
			this.index = index;

			// Calculate estimate of motor burn time
			double launchBurn = 0;
			double otherBurn = 0;
			Configuration config = simulation.getConfiguration();
			String id = simulation.getConditions().getMotorConfigurationID();
			Iterator<MotorMount> iterator = config.motorIterator();
			while (iterator.hasNext()) {
				MotorMount m = iterator.next();
				if (m.getIgnitionEvent() == IgnitionEvent.LAUNCH)
					launchBurn = MathUtil.max(launchBurn, m.getMotor(id).getTotalTime());
				else
					otherBurn = otherBurn + m.getMotor(id).getTotalTime();
			}
			burnoutTimeEstimate = Math.max(launchBurn + otherBurn, 0.1);
			
		}


		/**
		 * Return the extra listeners to use, a progress listener and cancel listener.
		 */
		@Override
		protected SimulationListener[] getExtraListeners() {
			return new SimulationListener[] {
					new SimulationProgressListener()
			};
		}

		
		/**
		 * Processes simulation statuses published by the simulation listener.
		 * The statuses of the parent class and the progress property are updated.
		 */
		@Override
		protected void process(List<SimulationStatus> chunks) {
			
			// Update max. altitude and velocity
			for (SimulationStatus s: chunks) {
				simulationMaxAltitude[index] = Math.max(simulationMaxAltitude[index], 
						s.position.z);
				simulationMaxVelocity[index] = Math.max(simulationMaxVelocity[index], 
						s.velocity.length());
			}

			// Calculate the progress
			SimulationStatus status = chunks.get(chunks.size()-1);
			simulationStatuses[index] = status;

			// 1. time = 0 ... burnoutTimeEstimate
			if (simulationStage == -2 && status.time < burnoutTimeEstimate) {
				System.out.println("Method 1:  t="+status.time + "  est="+burnoutTimeEstimate);
				setSimulationProgress(MathUtil.map(status.time, 0, burnoutTimeEstimate, 
						0.0, BURNOUT_PROGRESS));
				updateProgress();
				return;
			}
			
			if (simulationStage == -2) {
				simulationStage++;
				burnoutVelocity = MathUtil.max(status.velocity.z, 0.1);
				System.out.println("CHANGING to Method 2, vel="+burnoutVelocity);
			}
			
			// 2. z-velocity from burnout velocity to zero
			if (simulationStage == -1 && status.velocity.z >= 0) {
				System.out.println("Method 2:  vel="+status.velocity.z + " burnout=" +
						burnoutVelocity);
				setSimulationProgress(MathUtil.map(status.velocity.z, burnoutVelocity, 0,
						BURNOUT_PROGRESS, APOGEE_PROGRESS));
				updateProgress();
				return;
			}
			
			if (simulationStage == -1 && status.velocity.z < 0) {
				simulationStage++;
				apogeeAltitude = status.position.z;
			}
			
			// 3. z-position from apogee to zero
			// TODO: MEDIUM: several stages
			System.out.println("Method 3:  alt="+status.position.z +"  apogee="+apogeeAltitude);
			setSimulationProgress(MathUtil.map(status.position.z, 
					apogeeAltitude, 0, APOGEE_PROGRESS, 1.0));
			updateProgress();
		}
		
		/**
		 * Marks this simulation as done and calls the progress update.
		 */
		@Override
		protected void simulationDone() {
			simulationDone[index] = true;
			System.out.println("DONE, setting progress");
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
				return;  // Ignore cancellations
			}
			
			// Retrieve the stack trace in a textual form
			CharArrayWriter arrayWriter = new CharArrayWriter();
			arrayWriter.append(t.toString() + "\n" + "\n");
			t.printStackTrace(new PrintWriter(arrayWriter));
			String stackTrace = arrayWriter.toString();
			
			// Analyze the exception type
			if (t instanceof SimulationLaunchException) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this, 
						new Object[] {
						"Unable to simulate:",
						t.getMessage()
						},
						null, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else if (t instanceof SimulationException) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this, 
						new Object[] {
						"A error occurred during the simulation:",
						t.getMessage()
						}, 
						stackTrace, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else if (t instanceof Exception) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this, 
						new Object[] {
						"An exception occurred during the simulation:",
						t.getMessage(),
						simulation.getSimulationListeners().isEmpty() ? 
						"Please report this as a bug along with the details below." : ""
						}, 
						stackTrace, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else if (t instanceof AssertionError) {
				
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this, 
						new Object[] {
							"A computation error occurred during the simulation.",
							"Please report this as a bug along with the details below."
						}, 
						stackTrace, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			} else {
				
				// Probably an Error
				DetailDialog.showDetailedMessageDialog(SimulationRunDialog.this, 
						new Object[] {
							"An unknown error was encountered during the simulation.",
							"The program may be unstable, you should save all your designs " +
							"and restart OpenRocket now!"
						}, 
						stackTrace, simulation.getName(), JOptionPane.ERROR_MESSAGE);
				
			}
			simulationDone();
		}
		
		

		private void setSimulationProgress(double p) {
			progress = Math.max(progress, (int)(100*p+0.5));
			progress = MathUtil.clamp(progress, 0, 100);
			System.out.println("Setting progress to "+progress+ " (real " + 
					((int)(100*p+0.5)) + ")");
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
			public Collection<FlightEvent> handleEvent(FlightEvent event,
					SimulationStatus status) {
				
				switch (event.getType()) {
				case APOGEE:
					simulationStage = 0;
					apogeeAltitude = status.position.z;
					System.out.println("APOGEE, setting progress");
					setSimulationProgress(APOGEE_PROGRESS);
					publish(status);
					break;
					
				case LAUNCH:
					publish(status);
					break;
					
				case SIMULATION_END:
					System.out.println("END, setting progress");
					setSimulationProgress(1.0);
					break;
				}
				return null;
			}

			@Override
			public Collection<FlightEvent> stepTaken(SimulationStatus status) {
				if (System.currentTimeMillis() >= time + UPDATE_MS) {
					time = System.currentTimeMillis();
					publish(status);
				}
				return null;
			}
		}
	}
}
