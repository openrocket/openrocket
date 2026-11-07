package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.util.GUIUtil;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The 3D flight replay window: the telemetry strip above the {@link Flight3DPanel} view and the
 * {@link PlaybackTransportBar} controls below it. Each main window has at most one replay
 * window; opening a replay again shows the other simulation in it. Replay keyboard shortcuts
 * work wherever focus is in the window, except in text fields and drop-down lists. The window
 * releases its 3D resources when it or its owner closes.
 */
@SuppressWarnings("serial")
public class Flight3DFrame extends JFrame {
	private static final Map<Window, Flight3DFrame> activeFramesByOwner = new IdentityHashMap<>();

	private final Translator trans = Application.getTranslator();
	private final Flight3DPanel flightPanel;
	private final PlaybackTransportBar transportBar;
	private final FlightMetricsPanel metricsPanel;
	private final AtomicBoolean resourcesReleased = new AtomicBoolean(false);
	private final Window ownerWindow;
	private final WindowAdapter ownerWindowListener;
	private final KeyEventDispatcher replayKeyDispatcher;
	private volatile OpenRocketDocument currentDocument;
	private volatile Simulation currentSimulation;

	Flight3DFrame(OpenRocketDocument document, Simulation simulation, Window parent) {
		this.ownerWindow = parent;
		this.ownerWindowListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				dispose();
			}
		};
		this.currentDocument = document;
		this.currentSimulation = simulation;

		setMinimumSize(new Dimension(760, 480));
		setSize(1024, 768);
		setTitle(createTitle(simulation));
		flightPanel = new Flight3DPanel();
		transportBar = new PlaybackTransportBar();
		metricsPanel = new FlightMetricsPanel();
		flightPanel.setReplayReadyCallback((clock, replayData) -> {
			transportBar.setReplay(clock, replayData);
			metricsPanel.setReplay(currentSimulation, clock, replayData);
		});
		transportBar.setCameraModeListener(flightPanel::setCameraMode);
		transportBar.setTrackedBodyListener(index -> {
			flightPanel.setTrackedBody(index);
			metricsPanel.setTrackedBody(index);
		});
		transportBar.setViewControlListeners(flightPanel::zoomOut, flightPanel::zoomIn,
				flightPanel::fitView, flightPanel::setPanModeEnabled);
		transportBar.setReplayChangeListener(flightPanel::requestRenderNow);
		transportBar.setVisibilityListeners(flightPanel::setTrailVisible, flightPanel::setExhaustVisible);
		replayKeyDispatcher = event -> {
			Component source = event.getComponent();
			if (source == null || SwingUtilities.getWindowAncestor(source) != this
					|| source instanceof JTextComponent || source instanceof JComboBox<?>
					|| SwingUtilities.getAncestorOfClass(JComboBox.class, source) != null) return false;
			return transportBar.handleReplayKey(event);
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(replayKeyDispatcher);
		JPanel content = new JPanel(new BorderLayout());
		content.add(metricsPanel, BorderLayout.NORTH);
		content.add(flightPanel, BorderLayout.CENTER);
		content.add(transportBar, BorderLayout.SOUTH);
		setContentPane(content);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		if (parent != null) {
			parent.addWindowListener(ownerWindowListener);
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				attachCurrentSimulationIfReady();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				releaseResources();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (ownerWindow != null && activeFramesByOwner.get(ownerWindow) == Flight3DFrame.this) {
					activeFramesByOwner.remove(ownerWindow);
				}
			}
		});

		GUIUtil.rememberWindowSize(this);
		setLocationByPlatform(true);
		GUIUtil.rememberWindowPosition(this);
		GUIUtil.setWindowIcons(this);
	}

	/** Shows the given simulation's replay, reusing the owner window's replay window if it has one. */
	public static void openForSimulation(OpenRocketDocument document, Simulation simulation, Window parent) {
		Flight3DFrame existingFrame = activeFramesByOwner.get(parent);
		if (existingFrame != null && existingFrame.isDisplayable()) {
			existingFrame.setSimulation(document, simulation);
			existingFrame.setVisible(true);
			existingFrame.toFront();
			existingFrame.requestFocus();
			return;
		}

		Flight3DFrame frame = new Flight3DFrame(document, simulation, parent);
		activeFramesByOwner.put(parent, frame);
		frame.setVisible(true);
		frame.toFront();
		frame.requestFocus();
	}

	private void setSimulation(OpenRocketDocument document, Simulation simulation) {
		if (resourcesReleased.get()) {
			return;
		}
		if (document == currentDocument && simulation == currentSimulation) return;
		this.currentDocument = document;
		this.currentSimulation = simulation;
		setTitle(createTitle(simulation));
		transportBar.clearReplay();
		metricsPanel.setReplay(null, null, null);
		if (isShowing()) {
			flightPanel.setSimulation(document, simulation);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			SwingUtilities.invokeLater(this::attachCurrentSimulationIfReady);
		}
	}

	private void attachCurrentSimulationIfReady() {
		if (resourcesReleased.get() || !isShowing()) {
			return;
		}
		OpenRocketDocument doc = currentDocument;
		Simulation sim = currentSimulation;
		if (doc == null || sim == null) {
			return;
		}
		flightPanel.setSimulation(doc, sim);
	}

	private void releaseResources() {
		if (!resourcesReleased.compareAndSet(false, true)) {
			return;
		}
		currentDocument = null;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(replayKeyDispatcher);
		currentSimulation = null;
		if (ownerWindow != null) {
			ownerWindow.removeWindowListener(ownerWindowListener);
		}
		if (activeFramesByOwner.get(ownerWindow) == this) {
			activeFramesByOwner.remove(ownerWindow);
		}
		flightPanel.clearDoc();
		transportBar.dispose();
		metricsPanel.dispose();
	}

	@Override
	public void dispose() {
		releaseResources();
		super.dispose();
	}

	private String createTitle(Simulation simulation) {
		String base = trans.checkIfKeyExists("Flight3DFrame.title")
				? trans.get("Flight3DFrame.title")
				: "3D Flight Replay";
		if (simulation == null || simulation.getName() == null || simulation.getName().isBlank()) {
			return base;
		}
		return base + " - " + simulation.getName();
	}
}
