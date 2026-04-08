package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.figureelements.RocketInfoContextHelper;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figureelements.RocketInfo;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Swing panel that displays rocket information overlay on top of the 3D scene.
 * Integrates with OpenRocket's RocketInfo component and provides rate-limited updates.
 */
public class HUDPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final AerodynamicCalculator aerodynamicCalculator = new BarrowmanCalculator();

	private final OpenRocketDocument document;
	private final Rocket rocket;
	private Scene3DOrchestrator scene3DOrchestrator;
	private final RocketInfo rocketInfo;
	private volatile boolean panModeEnabled;

	private volatile boolean needsRepaint = true;

	// Rate limiting for change events
	private long lastRepaintTime = 0;
	private static final long MIN_REPAINT_INTERVAL = 50; // milliseconds

	// Reference to the GL panel for update notifications
	private HUDUpdateListener hudUpdateListener;

	/**
	 * Creates a HUD panel for displaying rocket information.
	 * @param rocket the rocket to display information for
	 * @param rocketInfo the OpenRocket info component for rendering
	 */
	public HUDPanel(Rocket rocket, RocketInfo rocketInfo) {
		this(null, rocket, rocketInfo);
	}

	public HUDPanel(OpenRocketDocument document, RocketInfo rocketInfo) {
		this(document, document.getRocket(), rocketInfo);
	}

	private HUDPanel(OpenRocketDocument document, Rocket rocket, RocketInfo rocketInfo) {
		this.document = document;
		this.rocket = rocket;
		this.rocketInfo = rocketInfo;
		setOpaque(false);

		// Double buffering is already enabled by default in Swing,
		// but we can ensure it's on
		setDoubleBuffered(true);
	}

	/**
	 * Sets up the scene orchestrator and configures change listeners.
	 * @param svc the scene orchestrator to coordinate with
	 */
	public void setSceneViewController(Scene3DOrchestrator svc) {
		this.scene3DOrchestrator = svc;

		StateChangeListener changeListener = e -> {
			refreshRocketInfo();

			// Rate limit repaints to avoid excessive updates
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastRepaintTime >= MIN_REPAINT_INTERVAL) {
				needsRepaint = true;
				lastRepaintTime = currentTime;
				// Notify GL panel if set
				if (hudUpdateListener != null) {
					hudUpdateListener.markHudForUpdate();
				}
			}
		};

		rocket.addChangeListener(changeListener);
		if (document != null) {
			document.addDocumentChangeListener(event -> changeListener.stateChanged(event));
		}

		// Only listen for significant component changes
		rocket.addComponentChangeListener(e -> {
			if (e.getType() != ComponentChangeEvent.MASS_CHANGE) {
				changeListener.stateChanged(e);
			}
		});

		refreshRocketInfo();
		needsRepaint = true;
	}

	private void refreshRocketInfo() {
		FlightConfiguration currentConfiguration = rocket.getSelectedConfiguration();
		rocketInfo.setCurrentConfig(currentConfiguration);
		rocketInfo.setCalculatingData(false);
		if (document == null) {
			return;
		}

		WarningSet warnings = new WarningSet();
		FlightConditions conditions = new FlightConditions(currentConfiguration);
		conditions.setMach(Application.getPreferences().getDefaultMach());
		conditions.setAOA(0);
		conditions.setRollRate(0);
		RocketInfoContextHelper.calculateCp(currentConfiguration, conditions, warnings, aerodynamicCalculator, true);
		rocketInfo.setWarnings(warnings);
		Simulation simulation = RocketInfoContextHelper.findCurrentConfigurationSimulation(document, currentConfiguration);
		rocketInfo.setSimulation(simulation);
		rocketInfo.setCalculatingData(RocketInfoContextHelper.shouldShowCalculatingState(currentConfiguration, simulation));
	}

	/**
	 * Sets the GL scene panel for HUD update notifications.
	 * @param panel the panel to notify (can be GLScenePanel or RobustGLScenePanel)
	 */
	public void setGLScenePanel(HUDUpdateListener panel) {
		this.hudUpdateListener = panel;
	}

	public boolean needsRepaint() {
		return needsRepaint;
	}

	public void setPanModeEnabled(boolean enabled) {
		if (panModeEnabled == enabled) {
			return;
		}
		panModeEnabled = enabled;
		needsRepaint = true;
		if (hudUpdateListener != null) {
			hudUpdateListener.markHudForUpdate();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (scene3DOrchestrator == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		final double paintScale = 1.0;

		// Paint rocket info
		rocketInfo.paint(g2, paintScale, this.getBounds());
		paintPanModeHint(g2);

		needsRepaint = false;
	}

	private void paintPanModeHint(Graphics2D g2) {
		if (!panModeEnabled) {
			return;
		}

		String hintText = trans.get("RocketFigure3d.HUD.panModeHint");
		FontMetrics metrics = g2.getFontMetrics();
		int paddingX = 10;
		int paddingY = 6;
		int textWidth = metrics.stringWidth(hintText);
		int textHeight = metrics.getHeight();
		int boxWidth = textWidth + paddingX * 2;
		int boxHeight = textHeight + paddingY * 2;
		int boxX = (getWidth() - boxWidth) / 2;
		int boxY = 12;

		Color originalColor = g2.getColor();
		g2.setColor(new Color(0, 0, 0, 170));
		g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
		g2.setColor(Color.WHITE);
		g2.drawString(hintText, boxX + paddingX, boxY + paddingY + metrics.getAscent());
		g2.setColor(originalColor);
	}
}
