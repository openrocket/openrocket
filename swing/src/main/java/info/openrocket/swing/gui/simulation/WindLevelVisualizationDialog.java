package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StateChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

public class WindLevelVisualizationDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();

	private final WindLevelVisualization visualization;
	private final JCheckBox showDirectionsCheckBox;

	public WindLevelVisualizationDialog(Dialog owner, MultiLevelPinkNoiseWindModel model, Unit altitudeUnit, Unit speedUnit) {
		super(owner, trans.get("WindLevelVisualizationDialog.title.WindLevelVisualization"), false);

		visualization = new WindLevelVisualization(model, altitudeUnit, speedUnit);
		visualization.setPreferredSize(new Dimension(400, 500));

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(visualization, BorderLayout.CENTER);

		// Use BorderLayout for the control panel
		JPanel controlPanel = new JPanel(new BorderLayout());
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add some padding

		// Checkbox on the left
		showDirectionsCheckBox = new JCheckBox(trans.get("WindLevelVisualizationDialog.checkbox.ShowDirections"));
		showDirectionsCheckBox.setSelected(true);
		showDirectionsCheckBox.addActionListener(e -> {
			visualization.setShowDirections(showDirectionsCheckBox.isSelected());
			visualization.repaint();
		});
		controlPanel.add(showDirectionsCheckBox, BorderLayout.WEST);

		// Close button on the right
		JButton closeButton = new JButton(trans.get("button.close"));
		closeButton.addActionListener(e -> dispose());
		JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		closeButtonPanel.add(closeButton);
		controlPanel.add(closeButtonPanel, BorderLayout.EAST);

		contentPane.add(controlPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(owner);

		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setAlwaysOnTop(true);
	}

	public void updateUnits(Unit altitudeUnit, Unit speedUnit) {
		visualization.updateUnits(altitudeUnit, speedUnit);
	}

	public static class WindLevelVisualization extends JPanel implements StateChangeListener {
		private final MultiLevelPinkNoiseWindModel model;
		private static final int MARGIN = 50;
		private static final int ARROW_SIZE = 10;
		private static final int TICK_LENGTH = 5;

		private Unit altitudeUnit;
		private Unit speedUnit;
		private boolean showDirections = true;

		private MultiLevelPinkNoiseWindModel.LevelWindModel selectedLevel = null;

		public WindLevelVisualization(MultiLevelPinkNoiseWindModel model, Unit altitudeUnit, Unit speedUnit) {
			this.model = model;
			this.altitudeUnit = altitudeUnit;
			this.speedUnit = speedUnit;
		}

		public void updateUnits(Unit altitudeUnit, Unit speedUnit) {
			this.altitudeUnit = altitudeUnit;
			this.speedUnit = speedUnit;
			repaint();
		}

		public void setSelectedLevel(MultiLevelPinkNoiseWindModel.LevelWindModel level) {
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

			List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
			if (levels.isEmpty()) return;

			// Sort levels before drawing
			levels.sort(Comparator.comparingDouble(MultiLevelPinkNoiseWindModel.LevelWindModel::getAltitude));

			double maxAltitude = levels.stream().mapToDouble(MultiLevelPinkNoiseWindModel.LevelWindModel::getAltitude).max().orElse(1000);
			double maxSpeed = levels.stream().mapToDouble(MultiLevelPinkNoiseWindModel.LevelWindModel::getSpeed).max().orElse(10);

			// Extend axis ranges by 10% for drawing
			double extendedMaxAltitude = maxAltitude * 1.1;
			double extendedMaxSpeed = maxSpeed * 1.1;

			// Draw axes
			drawAxes(g2d, width, height, maxSpeed, maxAltitude, extendedMaxSpeed, extendedMaxAltitude);

			// Draw wind levels
			for (int i = 0; i < levels.size(); i++) {
				MultiLevelPinkNoiseWindModel.LevelWindModel level = levels.get(i);

				int x = MARGIN + (int) (level.getSpeed() / extendedMaxSpeed * (width - 2 * MARGIN));
				int y = height - MARGIN - (int) (level.getAltitude() / extendedMaxAltitude * (height - 2 * MARGIN));

				// Draw point
				if (level.equals(selectedLevel)) {
					// Draw highlighted point
					g2d.setColor(Color.ORANGE);
					g2d.fillOval(x - 5, y - 5, 10, 10);
					g2d.setColor(Color.BLACK);
					g2d.drawOval(x - 5, y - 5, 10, 10);
				} else {
					// Draw normal point
					g2d.setColor(Color.BLUE);
					g2d.fillOval(x - 3, y - 3, 6, 6);
				}

				// Draw wind direction arrow
				if (showDirections) {
					drawWindArrow(g2d, x, y, level.getDirection());
				}

				// Draw connecting line if not the first point
				if (i > 0) {
					MultiLevelPinkNoiseWindModel.LevelWindModel prevLevel = levels.get(i - 1);
					int prevX = MARGIN + (int) (prevLevel.getSpeed() / extendedMaxSpeed * (width - 2 * MARGIN));
					int prevY = height - MARGIN - (int) (prevLevel.getAltitude() / extendedMaxAltitude * (height - 2 * MARGIN));
					g2d.setColor(Color.GRAY);
					g2d.drawLine(prevX, prevY, x, y);
				}
			}
		}

		private void drawAxes(Graphics2D g2d, int width, int height, double maxSpeed, double maxAltitude,
							  double extendedMaxSpeed, double extendedMaxAltitude) {
			g2d.setColor(Color.BLACK);

			// Draw X-axis
			g2d.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN);
			drawFilledArrowHead(g2d, width - MARGIN + (ARROW_SIZE-2), height - MARGIN, ARROW_SIZE, 0);

			// Draw Y-axis
			g2d.drawLine(MARGIN, height - MARGIN, MARGIN, MARGIN);
			drawFilledArrowHead(g2d, MARGIN, MARGIN - (ARROW_SIZE-2), ARROW_SIZE, -Math.PI / 2);

			// Draw max value ticks and labels
			g2d.setFont(g2d.getFont().deriveFont(10f));
			FontMetrics fm = g2d.getFontMetrics();

			// X-axis max value
			int xTickX = MARGIN + (int) ((maxSpeed / extendedMaxSpeed) * (width - 2 * MARGIN));
			g2d.drawLine(xTickX, height - MARGIN, xTickX, height - MARGIN + TICK_LENGTH);
			String xMaxLabel = speedUnit.toString(maxSpeed);
			g2d.drawString(xMaxLabel, xTickX - fm.stringWidth(xMaxLabel) / 2, height - MARGIN + TICK_LENGTH + fm.getHeight());

			// Y-axis max value
			int yTickY = height - MARGIN - (int) ((maxAltitude / extendedMaxAltitude) * (height - 2 * MARGIN));
			g2d.drawLine(MARGIN - TICK_LENGTH, yTickY, MARGIN, yTickY);
			String yMaxLabel = altitudeUnit.toString(maxAltitude);
			g2d.drawString(yMaxLabel, MARGIN - TICK_LENGTH - fm.stringWidth(yMaxLabel) - 2, yTickY + fm.getAscent() / 2);

			// Draw axis labels
			g2d.setFont(g2d.getFont().deriveFont(12f));
			fm = g2d.getFontMetrics();

			// X-axis label
			String xLabel = trans.get("WindLevelVisualizationDialog.lbl.WindSpeed") + " (" + speedUnit.getUnit() + ")";
			g2d.drawString(xLabel, width / 2 - fm.stringWidth(xLabel) / 2, height - 10);

			// Y-axis label
			String yLabel = trans.get("WindLevelVisualizationDialog.lbl.Altitude") + " (" + altitudeUnit.getUnit() + ")";
			AffineTransform originalTransform = g2d.getTransform();
			g2d.rotate(-Math.PI / 2);
			g2d.drawString(yLabel, -height / 2 - fm.stringWidth(yLabel) / 2, MARGIN / 2);
			g2d.setTransform(originalTransform);
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

		private void drawWindArrow(Graphics2D g, int x, int y, double direction) {
			int directionVectorLength = 15;
			int dx = (int) (directionVectorLength * Math.sin(direction));
			int dy = (int) (directionVectorLength * Math.cos(direction));
			int arrowSize = 10;

			g.setColor(Color.RED);

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
	}

	@Override
	public void dispose() {
		for (WindowListener listener : getWindowListeners()) {
			removeWindowListener(listener);
		}

		super.dispose();
	}
}
