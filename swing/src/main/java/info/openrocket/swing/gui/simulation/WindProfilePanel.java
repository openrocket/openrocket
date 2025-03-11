package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StateChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

/**
 * Panel for visualizing wind levels in a multi-level wind model.
 * It displays a scatter plot of wind speed vs. altitude, with optional wind direction arrows.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class WindProfilePanel extends JPanel implements StateChangeListener {
	private static final Translator trans = Application.getTranslator();

	private final WindProfileVisualization visualization;
	private final MultiLevelPinkNoiseWindModel model;
	private final JCheckBox showDirectionsCheckBox;

	private static final Color LEVEL_COLOR = Color.BLUE;
	private static final Color SELECTED_LEVEL_COLOR = Color.RED;
	private static final Color DIRECTION_COLOR = new Color(40, 40, 199);
	private static final Color SELECTED_DIRECTION_COLOR = new Color(199, 40, 40);
	private static final Color LINE_COLOR = Color.GRAY;

	public WindProfilePanel(MultiLevelPinkNoiseWindModel model, MultiLevelWindTable windTable) {
		super(new BorderLayout());

		this.model = model;
		visualization = new WindProfileVisualization(model, windTable);
		visualization.setPreferredSize(new Dimension(400, 500));
		
		// Listen for changes in the wind table to update the visualization
		windTable.addChangeListener(this);

		add(visualization, BorderLayout.CENTER);

		// Create control panel with checkbox
		JPanel controlPanel = new JPanel(new BorderLayout());
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add some padding

		// Checkbox to toggle direction arrows
		showDirectionsCheckBox = new JCheckBox(trans.get("WindProfilePanel.checkbox.ShowDirections"));
		showDirectionsCheckBox.setSelected(true);
		showDirectionsCheckBox.addActionListener(e -> {
			visualization.setShowDirections(showDirectionsCheckBox.isSelected());
			visualization.repaint();
		});
		controlPanel.add(showDirectionsCheckBox, BorderLayout.WEST);

		add(controlPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Sets the selected level to highlight in the visualization
	 */
	public void setSelectedLevel(LevelWindModel level) {
		visualization.setSelectedLevel(level);
	}

	@Override
	public void stateChanged(EventObject e) {
		visualization.repaint();
	}

	public static class WindProfileVisualization extends JPanel implements StateChangeListener {
		private final MultiLevelPinkNoiseWindModel model;
		private static final int MARGIN = 50;
		private static final int ARROW_SIZE = 10;
		private static final int TICK_LENGTH = 5;

		private final MultiLevelWindTable windTable;
		private boolean showDirections = true;

		private LevelWindModel selectedLevel = null;

		public WindProfileVisualization(MultiLevelPinkNoiseWindModel model, MultiLevelWindTable windTable) {
			this.model = model;
			this.windTable = windTable;

			// Enable tooltips for this component
			ToolTipManager.sharedInstance().registerComponent(this);
		}

		public void setSelectedLevel(LevelWindModel level) {
			this.selectedLevel = level;
			repaint();
		}

		public void setShowDirections(boolean showDirections) {
			this.showDirections = showDirections;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			int width = getWidth();
			int height = getHeight();

			// Enable antialiasing for smoother lines
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Draw background
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, width, height);

			List<LevelWindModel> levels = model.getLevels();
			if (levels.isEmpty()) {
				// Draw axes
				drawAxes(g2d, width, height);
				return;
			}

			// Sort levels before drawing
			levels.sort(Comparator.comparingDouble(LevelWindModel::getAltitude));

			double maxAltitude = levels.stream().mapToDouble(LevelWindModel::getAltitude).max().orElse(1000);
			double maxSpeed = levels.stream().mapToDouble(LevelWindModel::getSpeed).max().orElse(10);

			// Extend axis ranges by 10% for drawing
			double extendedMaxAltitude = maxAltitude * 1.1;
			double extendedMaxSpeed = maxSpeed * 1.1;

			// Draw axes
			drawAxes(g2d, width, height, maxSpeed, maxAltitude, extendedMaxSpeed, extendedMaxAltitude);

			// Draw wind levels - using the currently selected units
			for (int i = 0; i < levels.size(); i++) {
				LevelWindModel level = levels.get(i);
				
				// Convert values are already in SI units, which is what we use for calculations
				double speed = level.getSpeed();
				double altitude = level.getAltitude();

				int x = MARGIN + (int) (speed / extendedMaxSpeed * (width - 2 * MARGIN));
				int y = height - MARGIN - (int) (altitude / extendedMaxAltitude * (height - 2 * MARGIN));

				// Draw point
				if (level.equals(selectedLevel)) {
					// Draw highlighted point
					g2d.setColor(SELECTED_LEVEL_COLOR);
					g2d.fillOval(x - 5, y - 5, 10, 10);
					g2d.setColor(Color.BLACK);
					g2d.drawOval(x - 5, y - 5, 10, 10);
				} else {
					// Draw normal point
					g2d.setColor(LEVEL_COLOR);
					g2d.fillOval(x - 3, y - 3, 6, 6);
				}

				// Draw wind direction arrow
				if (showDirections) {
					drawWindArrow(g2d, x, y, level.getDirection(), level.equals(selectedLevel));
				}

				// Draw connecting line if not the first point
				if (i > 0) {
					LevelWindModel prevLevel = levels.get(i - 1);
					int prevX = MARGIN + (int) (prevLevel.getSpeed() / extendedMaxSpeed * (width - 2 * MARGIN));
					int prevY = height - MARGIN - (int) (prevLevel.getAltitude() / extendedMaxAltitude * (height - 2 * MARGIN));
					g2d.setColor(LINE_COLOR);
					g2d.drawLine(prevX, prevY, x, y);
				}
			}
		}

		private void drawAxes(Graphics2D g2d, int width, int height, Double maxSpeed, Double maxAltitude,
							  Double extendedMaxSpeed, Double extendedMaxAltitude) {
			g2d.setColor(Color.BLACK);

			// Draw X-axis
			g2d.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN);
			drawFilledArrowHead(g2d, width - MARGIN + (ARROW_SIZE - 2), height - MARGIN, ARROW_SIZE, 0);

			// Draw Y-axis
			g2d.drawLine(MARGIN, height - MARGIN, MARGIN, MARGIN);
			drawFilledArrowHead(g2d, MARGIN, MARGIN - (ARROW_SIZE - 2), ARROW_SIZE, -Math.PI / 2);

			// Draw max value ticks and labels
			g2d.setFont(g2d.getFont().deriveFont(10f));
			FontMetrics fm = g2d.getFontMetrics();

			// X-axis max value
			Unit speedUnit = windTable.getSpeedUnit();
			if (maxSpeed != null && extendedMaxSpeed != null) {
				int xTickX = MARGIN + (int) ((maxSpeed / extendedMaxSpeed) * (width - 2 * MARGIN));
				g2d.drawLine(xTickX, height - MARGIN, xTickX, height - MARGIN + TICK_LENGTH);
				String xMaxLabel = speedUnit.toString(maxSpeed);
				g2d.drawString(xMaxLabel, xTickX - fm.stringWidth(xMaxLabel) / 2, height - MARGIN + TICK_LENGTH + fm.getHeight());
			}

			// Y-axis max value
			Unit altitudeUnit = windTable.getAltitudeUnit();
			if (maxAltitude != null && extendedMaxAltitude != null) {
				int yTickY = height - MARGIN - (int) ((maxAltitude / extendedMaxAltitude) * (height - 2 * MARGIN));
				g2d.drawLine(MARGIN - TICK_LENGTH, yTickY, MARGIN, yTickY);
				String yMaxLabel = altitudeUnit.toString(maxAltitude);
				g2d.drawString(yMaxLabel, MARGIN - TICK_LENGTH - fm.stringWidth(yMaxLabel) - 2, yTickY + fm.getAscent() / 2);
			}

			// Draw axis labels
			g2d.setFont(g2d.getFont().deriveFont(12f));
			fm = g2d.getFontMetrics();

			// X-axis label with updated units
			String xLabel = trans.get("WindProfilePanel.lbl.WindSpeed") + " (" + speedUnit.getUnit() + ")";
			g2d.drawString(xLabel, width / 2 - fm.stringWidth(xLabel) / 2, height - 10);

			// Y-axis label with updated units
			String key;
			switch (model.getAltitudeReference()) {
				case MSL -> key = "AltitudeMSL";
				case AGL -> key = "AltitudeAGL";
				default -> key = "Altitude";
			}
			String yLabel = trans.get("WindProfilePanel.lbl." + key) + " (" + altitudeUnit.getUnit() + ")";
			AffineTransform originalTransform = g2d.getTransform();
			g2d.rotate(-Math.PI / 2);
			g2d.drawString(yLabel, -height / 2 - fm.stringWidth(yLabel) / 2, MARGIN / 2);
			g2d.setTransform(originalTransform);
		}

		private void drawAxes(Graphics2D g2d, int width, int height) {
			drawAxes(g2d, width, height, null, null, null, null);
		}

		private void drawFilledArrowHead(Graphics2D g, int x, int y, int arrowSize, double angle) {
			int[] xPoints = new int[3];
			int[] yPoints = new int[3];

			xPoints[0] = x;
			yPoints[0] = y;
			xPoints[1] = x - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
			yPoints[1] = y - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
			xPoints[2] = x - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
			yPoints[2] = y - (int) (arrowSize * Math.sin(angle + Math.PI / 6));

			g.fillPolygon(xPoints, yPoints, 3);
		}

		private void drawWindArrow(Graphics2D g, int x, int y, double direction, boolean selected) {
			int directionVectorLength = 15;
			int dx = (int) (directionVectorLength * Math.sin(direction));
			int dy = (int) (directionVectorLength * Math.cos(direction));
			int arrowSize = 10;

			g.setColor(selected ? SELECTED_DIRECTION_COLOR : DIRECTION_COLOR);

			// Draw the main line
			g.drawLine(x, y, x + dx, y - dy);

			int dx_arrow = (int) ((arrowSize-1) * Math.sin(direction));
			int dy_arrow = (int) ((arrowSize-1) * Math.cos(direction));

			// Draw filled arrow head
			drawFilledArrowHead(g, x + dx + dx_arrow, y - dy - dy_arrow, arrowSize, direction - Math.PI/2);
		}

		@Override
		public void stateChanged(EventObject e) {
			repaint();
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			List<LevelWindModel> levels = model.getLevels();
			if (levels.isEmpty()) {
				return null;
			}

			// Sort levels by altitude
			levels = new ArrayList<>(levels);
			levels.sort(Comparator.comparingDouble(LevelWindModel::getAltitude));

			int mouseX = event.getX();
			int mouseY = event.getY();
			int width = getWidth();
			int height = getHeight();

			double maxAltitude = levels.stream().mapToDouble(LevelWindModel::getAltitude).max().orElse(1000);
			double maxSpeed = levels.stream().mapToDouble(LevelWindModel::getSpeed).max().orElse(10);

			// Extend axis ranges by 10% for drawing (same as in paintComponent)
			double extendedMaxAltitude = maxAltitude * 1.1;
			double extendedMaxSpeed = maxSpeed * 1.1;

			// Find closest dot
			LevelWindModel closestLevel = null;
			double closestDistance = Double.MAX_VALUE;

			for (LevelWindModel level : levels) {
				// Calculate dot position (same calculation as in paintComponent)
				double speed = level.getSpeed();
				double altitude = level.getAltitude();

				int x = MARGIN + (int) (speed / extendedMaxSpeed * (width - 2 * MARGIN));
				int y = height - MARGIN - (int) (altitude / extendedMaxAltitude * (height - 2 * MARGIN));

				// Calculate distance to mouse position
				double distance = Math.sqrt((mouseX - x) * (mouseX - x) + (mouseY - y) * (mouseY - y));

				// Update closest dot if this one is closer
				int dotRadius = level.equals(selectedLevel) ? 5 : 3;
				if (distance <= dotRadius * 2 && distance < closestDistance) {  // Use a slightly larger detection area
					closestLevel = level;
					closestDistance = distance;
				}
			}

			// Return tooltip text for closest dot, if any
			if (closestLevel != null) {
				return formatTooltipText(closestLevel);
			}

			return null;
		}

		private String formatTooltipText(LevelWindModel level) {
			StringBuilder sb = new StringBuilder("<html>");

			// Add altitude
			Unit altitudeUnit = windTable.getAltitudeUnit();
			String key;
			switch (model.getAltitudeReference()) {
				case MSL -> key = "AltitudeMSL";
				case AGL -> key = "AltitudeAGL";
				default -> key = "Altitude";
			}
			sb.append(trans.get("MultiLevelWindTable.col." + key)).append(": ")
					.append(altitudeUnit.toStringUnit(level.getAltitude())).append("<br>");

			// Add speed
			Unit speedUnit = windTable.getSpeedUnit();
			sb.append(trans.get("MultiLevelWindTable.col.Speed")).append(": ")
					.append(speedUnit.toStringUnit(level.getSpeed())).append("<br>");

			// Add direction (using a degrees unit for readability)
			Unit directionUnit = windTable.getDirectionUnit();
			sb.append(trans.get("MultiLevelWindTable.col.Direction")).append(": ")
					.append(directionUnit.toStringUnit(level.getDirection())).append("<br>");

			// Add standard deviation
			Unit deviationUnit = windTable.getStdDeviationUnit();
			sb.append(trans.get("MultiLevelWindTable.col.StandardDeviation")).append(": ")
					.append(deviationUnit.toStringUnit(level.getStandardDeviation()));

			sb.append("</html>");
			return sb.toString();
		}
	}
}