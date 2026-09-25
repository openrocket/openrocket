package info.openrocket.swing.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.MonteCarloSimulationRunner;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.simulation.LandingDispersionAnalysisCache;

import net.miginfocom.swing.MigLayout;

/** Runs report-only Monte Carlo analyses without blocking the Swing event thread. */
final class MonteCarloReportRunDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();

	private final List<Simulation> simulations;
	private final JProgressBar progressBar;
	private final JLabel progressLabel;
	private final JButton cancelButton;
	private AnalysisWorker worker;
	private boolean completed;
	private String failureMessage;

	private MonteCarloReportRunDialog(Window owner, List<Simulation> simulations) {
		super(owner, trans.get("printdlg.monteCarlo.running.title"), ModalityType.APPLICATION_MODAL);
		this.simulations = List.copyOf(simulations);
		this.progressBar = new JProgressBar(0, totalTrajectories(simulations));
		this.progressLabel = new JLabel(" ");
		this.cancelButton = new JButton(trans.get("button.cancel"));
		buildDialog();
	}

	static boolean run(Window owner, List<Simulation> simulations) {
		if (simulations.isEmpty()) {
			return true;
		}
		MonteCarloReportRunDialog dialog = new MonteCarloReportRunDialog(owner, simulations);
		dialog.start();
		dialog.setVisible(true);
		if (dialog.failureMessage != null) {
			JOptionPane.showMessageDialog(owner, dialog.failureMessage,
					trans.get("printdlg.monteCarlo.running.failed.title"), JOptionPane.ERROR_MESSAGE);
		}
		return dialog.completed;
	}

	private void buildDialog() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				cancel();
			}
		});

		progressBar.setStringPainted(true);
		JPanel content = new JPanel(new MigLayout("fillx, ins 12", "[grow]", "[][]"));
		content.add(progressLabel, "growx, wrap");
		content.add(progressBar, "growx");
		content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		add(content, BorderLayout.CENTER);

		cancelButton.addActionListener(event -> cancel());
		JPanel buttons = new JPanel(new MigLayout("ins 0 12 12 12", "[grow][button]"));
		buttons.add(new JPanel(), "growx");
		buttons.add(cancelButton, "tag cancel");
		add(buttons, BorderLayout.SOUTH);

		setSize(500, 155);
		setResizable(false);
		setLocationRelativeTo(getOwner());
	}

	private void start() {
		worker = new AnalysisWorker();
		worker.execute();
	}

	private void cancel() {
		if (worker == null || worker.isDone()) {
			return;
		}
		cancelButton.setEnabled(false);
		progressLabel.setText(trans.get("LandingDispersionDlg.lbl.cancelling"));
		worker.cancel(true);
	}

	private static int totalTrajectories(List<Simulation> simulations) {
		return simulations.stream()
				.map(Simulation::getLandingDispersionSettings)
				.filter(java.util.Objects::nonNull)
				.mapToInt(settings -> settings.getRunCount() + 1)
				.sum();
	}

	private final class AnalysisWorker extends SwingWorker<Void, ProgressState> {
		@Override
		protected Void doInBackground() {
			int completedBeforeSimulation = 0;
			int overallTotal = totalTrajectories(simulations);
			for (Simulation simulation : simulations) {
				if (isCancelled()) {
					throw new CancellationException();
				}
				MonteCarloSettings settings = simulation.getLandingDispersionSettings();
				if (settings == null) {
					continue;
				}
				int completedBase = completedBeforeSimulation;
				MonteCarloResult result = new MonteCarloSimulationRunner().run(simulation, settings,
						(completed, total) -> publish(new ProgressState(simulation.getName(),
								completedBase + completed, overallTotal)));
				LandingDispersionAnalysisCache.put(simulation, result);
				completedBeforeSimulation += settings.getRunCount() + 1;
			}
			return null;
		}

		@Override
		protected void process(List<ProgressState> chunks) {
			ProgressState latest = chunks.get(chunks.size() - 1);
			progressBar.setMaximum(latest.total);
			progressBar.setValue(latest.completed);
			progressLabel.setText(String.format(trans.get("printdlg.monteCarlo.running.progress"),
					latest.simulationName, latest.completed, latest.total));
		}

		@Override
		protected void done() {
			try {
				get();
				completed = true;
			} catch (CancellationException exception) {
				completed = false;
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				completed = false;
			} catch (ExecutionException exception) {
				Throwable cause = exception.getCause();
				failureMessage = cause == null ? exception.getLocalizedMessage() : cause.getLocalizedMessage();
				if (failureMessage == null || failureMessage.isBlank()) {
					failureMessage = cause == null ? exception.getClass().getSimpleName()
							: cause.getClass().getSimpleName();
				}
				completed = false;
			} finally {
				dispose();
			}
		}
	}

	private record ProgressState(String simulationName, int completed, int total) {
	}
}
