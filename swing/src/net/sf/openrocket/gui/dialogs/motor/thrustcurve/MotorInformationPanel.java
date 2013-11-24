package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class MotorInformationPanel extends JPanel {
	
	private static final int ZOOM_ICON_POSITION_NEGATIVE_X = 50;
	private static final int ZOOM_ICON_POSITION_POSITIVE_Y = 12;

	private static final Color NO_COMMENT_COLOR = Color.GRAY;
	private static final Color WITH_COMMENT_COLOR = Color.BLACK;

	private static final Translator trans = Application.getTranslator();

	// Motors in set
	private List<ThrustCurveMotor> selectedMotorSet;
	// Selected motor
	private ThrustCurveMotor selectedMotor;
	
	private final JLabel totalImpulseLabel;
	private final JLabel classificationLabel;
	private final JLabel avgThrustLabel;
	private final JLabel maxThrustLabel;
	private final JLabel burnTimeLabel;
	private final JLabel launchMassLabel;
	private final JLabel emptyMassLabel;
	private final JLabel dataPointsLabel;
	private final JLabel digestLabel;

	private final JTextArea comment;
	private final Font noCommentFont;
	private final Font withCommentFont;

	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final JLabel zoomIcon;

	public MotorInformationPanel() {
		super(new MigLayout("fill"));
		
		// Thrust curve info
		//// Total impulse:
		{
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Totalimpulse")));
			totalImpulseLabel = new JLabel();
			this.add(totalImpulseLabel, "split");

			classificationLabel = new JLabel();
			classificationLabel.setEnabled(false); // Gray out
			this.add(classificationLabel, "gapleft unrel, wrap");

			//// Avg. thrust:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Avgthrust")));
			avgThrustLabel = new JLabel();
			this.add(avgThrustLabel, "wrap");

			//// Max. thrust:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Maxthrust")));
			maxThrustLabel = new JLabel();
			this.add(maxThrustLabel, "wrap");

			//// Burn time:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Burntime")));
			burnTimeLabel = new JLabel();
			this.add(burnTimeLabel, "wrap");

			//// Launch mass:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Launchmass")));
			launchMassLabel = new JLabel();
			this.add(launchMassLabel, "wrap");

			//// Empty mass:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Emptymass")));
			emptyMassLabel = new JLabel();
			this.add(emptyMassLabel, "wrap");

			//// Data points:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Datapoints")));
			dataPointsLabel = new JLabel();
			this.add(dataPointsLabel, "wrap para");

			if (System.getProperty("openrocket.debug.motordigest") != null) {
				//// Digest:
				this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Digest")));
				digestLabel = new JLabel();
				this.add(digestLabel, "w :300:, wrap para");
			} else {
				digestLabel = null;
			}


			comment = new JTextArea(5, 5);
			GUIUtil.changeFontSize(comment, -2);
			withCommentFont = comment.getFont();
			noCommentFont = withCommentFont.deriveFont(Font.ITALIC);
			comment.setLineWrap(true);
			comment.setWrapStyleWord(true);
			comment.setEditable(false);
			JScrollPane scrollpane = new JScrollPane(comment);
			this.add(scrollpane, "spanx, growx, wrap para");
		}

		// Thrust curve plot
		{
			chart = ChartFactory.createXYLineChart(
					null, // title
					null, // xAxisLabel
					null, // yAxisLabel
					null, // dataset
					PlotOrientation.VERTICAL,
					false, // legend
					false, // tooltips
					false // urls
					);

			// Add the data and formatting to the plot
			XYPlot plot = chart.getXYPlot();

			changeLabelFont(plot.getRangeAxis(), -2);
			changeLabelFont(plot.getDomainAxis(), -2);

			//// Thrust curve:
			chart.setTitle(new TextTitle(trans.get("TCMotorSelPan.title.Thrustcurve"), this.getFont()));
			chart.setBackgroundPaint(this.getBackground());
			plot.setBackgroundPaint(Color.WHITE);
			plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
			plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

			chartPanel = new ChartPanel(chart,
					false, // properties
					false, // save
					false, // print
					false, // zoom
					false); // tooltips
			chartPanel.setMouseZoomable(false);
			chartPanel.setPopupMenu(null);
			chartPanel.setMouseWheelEnabled(false);
			chartPanel.setRangeZoomable(false);
			chartPanel.setDomainZoomable(false);

			chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			chartPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (selectedMotor == null || selectedMotorSet == null)
						return;
					if (e.getButton() == MouseEvent.BUTTON1) {
						// Open plot dialog
						ThrustCurveMotorPlotDialog plotDialog = new ThrustCurveMotorPlotDialog(selectedMotorSet,
								selectedMotorSet.indexOf(selectedMotor),
								SwingUtilities.getWindowAncestor(MotorInformationPanel.this));
						plotDialog.setVisible(true);
					}
				}
			});

			JLayeredPane layer = new CustomLayeredPane();

			layer.setBorder(BorderFactory.createLineBorder(Color.BLUE));

			layer.add(chartPanel, (Integer) 0);

			zoomIcon = new JLabel(Icons.ZOOM_IN);
			zoomIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			layer.add(zoomIcon, (Integer) 1);

			this.add(layer, "width 300:300:, height 180:180:, grow, spanx");
		}
	}
	
	public void clearData() {
		selectedMotor = null;
		selectedMotorSet = null;
		totalImpulseLabel.setText("");
		totalImpulseLabel.setToolTipText(null);
		classificationLabel.setText("");
		classificationLabel.setToolTipText(null);
		avgThrustLabel.setText("");
		maxThrustLabel.setText("");
		burnTimeLabel.setText("");
		launchMassLabel.setText("");
		emptyMassLabel.setText("");
		dataPointsLabel.setText("");
		if (digestLabel != null) {
			digestLabel.setText("");
		}
		setComment("");
		chart.getXYPlot().setDataset(new XYSeriesCollection());
	}
	
	public void updateData( List<ThrustCurveMotor> motors, ThrustCurveMotor selectedMotor ) {
		
		if ( selectedMotor == null ) {
			clearData();
			return;
		}
		
		this.selectedMotorSet = motors;
		this.selectedMotor = selectedMotor;
		
		// Update thrust curve data
		double impulse = selectedMotor.getTotalImpulseEstimate();
		MotorClass mc = MotorClass.getMotorClass(impulse);
		totalImpulseLabel.setText(UnitGroup.UNITS_IMPULSE.getDefaultUnit().toStringUnit(impulse));
		classificationLabel.setText("(" + mc.getDescription(impulse) + ")");
		totalImpulseLabel.setToolTipText(mc.getClassDescription());
		classificationLabel.setToolTipText(mc.getClassDescription());

		avgThrustLabel.setText(UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(
				selectedMotor.getAverageThrustEstimate()));
		maxThrustLabel.setText(UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(
				selectedMotor.getMaxThrustEstimate()));
		burnTimeLabel.setText(UnitGroup.UNITS_SHORT_TIME.getDefaultUnit().toStringUnit(
				selectedMotor.getBurnTimeEstimate()));
		launchMassLabel.setText(UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
				selectedMotor.getLaunchCG().weight));
		emptyMassLabel.setText(UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
				selectedMotor.getEmptyCG().weight));
		dataPointsLabel.setText("" + (selectedMotor.getTimePoints().length - 1));
		if (digestLabel != null) {
			digestLabel.setText(selectedMotor.getDigest());
		}

		setComment(selectedMotor.getDescription());

		// Update the plot
		XYPlot plot = chart.getXYPlot();
		final int index = motors.indexOf(selectedMotor);

		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < motors.size(); i++) {
			ThrustCurveMotor m = motors.get(i);

			//// Thrust
			XYSeries series = new XYSeries(trans.get("TCMotorSelPan.title.Thrust") + " (" + i + ")");
			double[] time = m.getTimePoints();
			double[] thrust = m.getThrustPoints();

			for (int j = 0; j < time.length; j++) {
				series.add(time[j], thrust[j]);
			}

			dataset.addSeries(series);

			boolean selected = (i == index);
			plot.getRenderer().setSeriesStroke(i, new BasicStroke(selected ? 3 : 1));
			plot.getRenderer().setSeriesPaint(i, ThrustCurveMotorSelectionPanel.getColor(i));
		}

		plot.setDataset(dataset);

	}
	
	private void setComment(String s) {
		s = s.trim();
		if (s.length() == 0) {
			//// No description available.
			comment.setText(trans.get("TCMotorSelPan.noDescription"));
			comment.setFont(noCommentFont);
			comment.setForeground(NO_COMMENT_COLOR);
		} else {
			comment.setText(s);
			comment.setFont(withCommentFont);
			comment.setForeground(WITH_COMMENT_COLOR);
		}
		comment.setCaretPosition(0);
	}

	void changeLabelFont(ValueAxis axis, float size) {
		Font font = axis.getTickLabelFont();
		font = font.deriveFont(font.getSize2D() + size);
		axis.setTickLabelFont(font);
	}

	/**
	 * Custom layered pane that sets the bounds of the components on every layout.
	 */
	public class CustomLayeredPane extends JLayeredPane {
		@Override
		public void doLayout() {
			synchronized (getTreeLock()) {
				int w = getWidth();
				int h = getHeight();
				chartPanel.setBounds(0, 0, w, h);
				zoomIcon.setBounds(w - ZOOM_ICON_POSITION_NEGATIVE_X, ZOOM_ICON_POSITION_POSITIVE_Y, 50, 50);
			}
		}
	}

}