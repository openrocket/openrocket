package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import info.openrocket.swing.gui.plot.Util;

import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.theme.UITheme;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.StringUtils;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.startup.Application;
import info.openrocket.core.utils.MotorCorrelation;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.core.util.BugException;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;

@SuppressWarnings("serial")
class MotorInformationPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();

	private static final Logger log = LoggerFactory.getLogger(MotorInformationPanel.class);

	private static final double MOTOR_SIMILARITY_THRESHOLD = 0.95;

	private static final Paint[] CURVE_COLORS = ChartColor.createDefaultPaintArray();

	private static final ThrustCurveMotorComparator MOTOR_COMPARATOR = new ThrustCurveMotorComparator();
	
	private static final int ZOOM_ICON_POSITION_NEGATIVE_X = 50;
	private static final int ZOOM_ICON_POSITION_POSITIVE_Y = 12;

	private static Color NO_COMMENT_COLOR;
	private static Color WITH_COMMENT_COLOR;
	private static Color textColor;
	private static Color dimTextColor;
	private static Color backgroundColor;
	private static Color gridColor;
	private static Color infoColor;

	// Motors in set
	private ThrustCurveMotorSet selectedMotorSet;
	// Selected motor
	private ThrustCurveMotor selectedMotor;
	
	// delay selection	
	private double selectedDelay;
	private final JLabel ejectionChargeDelayLabel;
	private final JComboBox<String> delayBox;

	// hide similar
	private final JCheckBox hideSimilarBox;

	// thrustcurve selection
	private final JLabel curveSelectionLabel;
	private final JComboBox<MotorHolder> curveSelectionBox;
	private final DefaultComboBoxModel<MotorHolder> curveSelectionModel;
	
	// thrustcurve plot
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final JLabel zoomIcon;

	private final JLabel designationLabel;
	private final JLabel commonNameLabel;
	private final JLabel totalImpulseLabel;
	private final JLabel classificationLabel;
	private final JLabel avgThrustLabel;
	private final JLabel maxThrustLabel;
	private final JLabel burnTimeLabel;
	private final JLabel launchMassLabel;
	private final JLabel emptyMassLabel;
	private final JLabel motorTypeLabel;
	private final JLabel caseInfoLabel;
	private final JLabel propInfoLabel;
	private final JLabel dataPointsLabel;
	private final JLabel compatibleCasesLabel;
	private final JLabel digestLabel;

	private final JTextArea comment;
	private final Font noCommentFont;
	private final Font withCommentFont;

	static {
		initColors();
	}

	public MotorInformationPanel() {
		super(new MigLayout("fill"));

		// Ejection charge delay:
		{
			ejectionChargeDelayLabel = new JLabel(trans.get("TCMotorSelPan.lbl.Ejectionchargedelay"));
			add(ejectionChargeDelayLabel);

			delayBox = new JComboBox<>();
			delayBox.setEditable(true);
			delayBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String sel = (String) delayBox.getSelectedItem();
					if (sel == null) {
						log.debug("Selected charge delay is null");
						return;
					}
					//// Plugged (or None)
					if (sel.equalsIgnoreCase(trans.get("TCMotorSelPan.delayBox.Plugged")) ||
							sel.equalsIgnoreCase(trans.get("TCMotorSelPan.delayBox.PluggedNone"))) {
						selectedDelay = Motor.PLUGGED_DELAY;
					} else {
						try {
							selectedDelay = Double.parseDouble(sel);
						} catch (NumberFormatException ignore) {
						}
					}
					setDelays(false);
				}
			});
			add(delayBox, "growx,wrap");
			//// (or type in desired delay in seconds)
			add(new StyledLabel(trans.get("TCMotorSelPan.lbl.Numberofseconds"), -3), "skip, wrap");
			setDelays(false);
		}

		//// Hide very similar thrust curves
		{
			hideSimilarBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideSimilar"));
			GUIUtil.changeFontSize(hideSimilarBox, -1);
			hideSimilarBox.setSelected(Application.getPreferences().getBoolean(ApplicationPreferences.MOTOR_HIDE_SIMILAR, true));
			hideSimilarBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Application.getPreferences().putBoolean(ApplicationPreferences.MOTOR_HIDE_SIMILAR, hideSimilarBox.isSelected());
					updateData();
				}
			});
			add(hideSimilarBox, "gapleft para, spanx, growx, wrap");
		}

		//// Select thrust curve:
		{
			curveSelectionLabel = new JLabel(trans.get("TCMotorSelPan.lbl.Selectthrustcurve"));
			add(curveSelectionLabel);

			curveSelectionModel = new DefaultComboBoxModel<>();
			curveSelectionBox = new JComboBox<>(curveSelectionModel);
			@SuppressWarnings("unchecked")
			ListCellRenderer<MotorHolder> lcr = (ListCellRenderer<MotorHolder>) curveSelectionBox.getRenderer();
			curveSelectionBox.setRenderer(new CurveSelectionRenderer(lcr));
			curveSelectionBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MotorHolder value = (MotorHolder)curveSelectionBox.getSelectedItem();
					if (value != null) {
						curveSelectionBox.setForeground(getColor(value.getIndex()));
						setMotor(selectedMotorSet, value.getMotor(), selectedDelay);
					}
					updateData();
				}
			});
			add(curveSelectionBox, "growx, wrap");
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

			changeLabelFont(plot.getRangeAxis(), -2, textColor);
			changeLabelFont(plot.getDomainAxis(), -2, textColor);

			//// Thrust curve:
			TextTitle title = new TextTitle(trans.get("TCMotorSelPan.title.Thrustcurve"), this.getFont());
			title.setPaint(textColor);
			chart.setTitle(title);
			chart.setBackgroundPaint(this.getBackground());
			plot.setBackgroundPaint(backgroundColor);
			plot.setDomainGridlinePaint(gridColor);
			plot.setRangeGridlinePaint(gridColor);

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
						ThrustCurveMotorPlotDialog plotDialog =
							new ThrustCurveMotorPlotDialog(getFilteredCurves(), 
														   selectedMotorSet.indexOf(selectedMotor),
														   SwingUtilities.getWindowAncestor(MotorInformationPanel.this));
						plotDialog.setVisible(true);
					}
				}
			});

			JLayeredPane layer = new CustomLayeredPane();

			layer.setBorder(BorderFactory.createLineBorder(infoColor));

			layer.add(chartPanel, (Integer) 0);

			zoomIcon = new JLabel(Icons.ZOOM_IN);
			zoomIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			layer.add(zoomIcon, (Integer) 1);

			this.add(layer, "width 300:300:, height 180:180:, grow, spanx");
		}
		
		// Thrust curve info
		{
			//// Designation
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Designation")));
			designationLabel = new JLabel();
			this.add(designationLabel, "wrap");

			//// Common name
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.CommonName")));
			commonNameLabel = new JLabel();
			this.add(commonNameLabel, "wrap");
		
			//// Total impulse:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Totalimpulse")));
			totalImpulseLabel = new JLabel();
			this.add(totalImpulseLabel, "split");

			classificationLabel = new JLabel();
			classificationLabel.setForeground(dimTextColor);
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

			//// Motor type
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Motortype")));
			motorTypeLabel = new JLabel();
			this.add(motorTypeLabel, "wrap");

			//// case info:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Caseinfo")));
			caseInfoLabel = new JLabel();
			this.add(caseInfoLabel, "wrap");
			
			//// prop info:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.Propinfo")));
			propInfoLabel = new JLabel();
			this.add(propInfoLabel, "wrap");
			
			//// compatible cases:
			this.add(new JLabel(trans.get("TCMotorSelPan.lbl.CompatibleCases")));
			compatibleCasesLabel = new JLabel();
			this.add(compatibleCasesLabel, "wrap");
			
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
			this.add(scrollpane, "spanx, grow, pushy, wrap para");
		}

		hideSimilarBox.getActionListeners()[0].actionPerformed(null);
		setDelays(false);

	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(MotorInformationPanel::updateColors);
	}

	public static void updateColors() {
		NO_COMMENT_COLOR = GUIUtil.getUITheme().getDimTextColor();
		WITH_COMMENT_COLOR = GUIUtil.getUITheme().getTextColor();
		textColor = GUIUtil.getUITheme().getTextColor();
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
		backgroundColor = GUIUtil.getUITheme().getBackgroundColor();
		gridColor = GUIUtil.getUITheme().getFinPointGridMajorLineColor();
		infoColor = GUIUtil.getUITheme().getCGColor();
	}
	
	public void clearData() {
		selectedMotor = null;
		selectedMotorSet = null;
		curveSelectionModel.removeAllElements();
		curveSelectionBox.setEnabled(false);
		curveSelectionLabel.setEnabled(false);
		ejectionChargeDelayLabel.setEnabled(false);
		delayBox.setEnabled(false);
		setDelays(false);
		designationLabel.setText("");
		commonNameLabel.setText("");
		totalImpulseLabel.setText("");
		totalImpulseLabel.setToolTipText(null);
		classificationLabel.setText("");
		classificationLabel.setToolTipText(null);
		avgThrustLabel.setText("");
		maxThrustLabel.setText("");
		burnTimeLabel.setText("");
		launchMassLabel.setText("");
		emptyMassLabel.setText("");
		motorTypeLabel.setText("");
		caseInfoLabel.setText("");
		propInfoLabel.setText("");
		compatibleCasesLabel.setText("");
		dataPointsLabel.setText("");
		if (digestLabel != null) {
			digestLabel.setText("");
		}
		setComment("");
		chart.getXYPlot().setDataset(new XYSeriesCollection());
	}
	
	public void updateData() {
		
		if ( selectedMotor == null ) {
			clearData();
			return;
		}

		ejectionChargeDelayLabel.setEnabled(true);
		delayBox.setEnabled(true);
		setDelays(true);

		// Check which thrust curves to display
		List<ThrustCurveMotor> motors = getFilteredCurves();
		final int index = motors.indexOf(selectedMotor);

		// Update the thrust curve selection box
		curveSelectionModel.removeAllElements();
		for (int i = 0; i < motors.size(); i++) {
			curveSelectionModel.addElement(new MotorHolder(motors.get(i), i));
		}
		curveSelectionBox.setSelectedIndex(index);

		if (motors.size() > 1) {
			curveSelectionBox.setEnabled(true);
			curveSelectionLabel.setEnabled(true);
		} else {
			curveSelectionBox.setEnabled(false);
			curveSelectionLabel.setEnabled(false);
		}
		
		// Update thrust curve data
		designationLabel.setText(selectedMotor.getDesignation());
		commonNameLabel.setText(selectedMotor.getCommonName());
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
				selectedMotor.getLaunchMass()));
		emptyMassLabel.setText(UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
				selectedMotor.getBurnoutMass()));
		motorTypeLabel.setText(selectedMotor.getMotorType().getName());
		caseInfoLabel.setText(selectedMotor.getCaseInfo());
		propInfoLabel.setText(selectedMotor.getPropellantInfo());
		compatibleCasesLabel.setText("<html>" + StringUtils.join(", ",selectedMotor.getCompatibleCases()) + "<html>");
		dataPointsLabel.setText("" + (selectedMotor.getTimePoints().length - 1));

		if (digestLabel != null) {
			digestLabel.setText(selectedMotor.getDigest());
		}

		setComment(selectedMotor.getDescription());

		// Update the plot
		XYPlot plot = chart.getXYPlot();

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
			plot.getRenderer().setSeriesPaint(i, getColor(i));
		}

		plot.setDataset(dataset);

		invalidate();
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

	void changeLabelFont(ValueAxis axis, float size, Color color) {
		Font font = axis.getTickLabelFont();
		font = font.deriveFont(font.getSize2D() + size);
		axis.setTickLabelFont(font);
		axis.setTickLabelPaint(color);
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

	/**
	 * Set the values in the delay combo box.  If <code>reset</code> is <code>true</code>
	 * then sets the selected value as the value closest to selectedDelay, otherwise
	 * leaves selection alone.
	 */
	private void setDelays(boolean reset) {
		if (selectedMotor == null) {
			//// Display nothing
			delayBox.setModel(new DefaultComboBoxModel<>(new String[] {}));
		} else {
			List<Double> delays = selectedMotorSet.getDelays();
			boolean containsPlugged = delays.contains(Motor.PLUGGED_DELAY);
			int size = delays.size() + (containsPlugged ? 0 : 1);
			String[] delayStrings = new String[size];
			double currentDelay = selectedDelay; // Store current setting locally

			for (int i = 0; i < delays.size(); i++) {
				//// Plugged
				delayStrings[i] = ThrustCurveMotor.getDelayString(delays.get(i), trans.get("TCMotorSelPan.delayBox.Plugged"));
			}
			// We always want the plugged option in the combobox, even if the motor doesn't have it
			if (!containsPlugged) {
				delayStrings[delayStrings.length - 1] = trans.get("TCMotorSelPan.delayBox.Plugged");
			}
			delayBox.setModel(new DefaultComboBoxModel<>(delayStrings));

			if (reset) {
				// Find and set the closest value
				double closest = Double.NaN;
				for (Double delay : delays) {
					// if-condition to always become true for NaN
					if (!(Math.abs(delay - currentDelay) > Math.abs(closest - currentDelay))) {
						closest = delay;
					}
				}
				if (!Double.isNaN(closest)) {
					selectedDelay = closest;
					delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(closest, trans.get("TCMotorSelPan.delayBox.Plugged")));
				} else {
					//// Plugged
					delayBox.setSelectedItem(trans.get("TCMotorSelPan.delayBox.Plugged"));
				}

			} else {
				selectedDelay = currentDelay;
				delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(currentDelay, trans.get("TCMotorSelPan.delayBox.Plugged")));
			}

		}
	}

	List<ThrustCurveMotor> getFilteredCurves() {
		List<ThrustCurveMotor> motors = selectedMotorSet.getMotors();
		if (hideSimilarBox.isSelected()  && selectedMotor != null) {
			List<ThrustCurveMotor> filtered = new ArrayList<>(motors.size());
			for (ThrustCurveMotor m : motors) {
				if (m.equals(selectedMotor)) {
					filtered.add(m);
					continue;
				}
				double similarity = MotorCorrelation.similarity(selectedMotor, m);
				log.debug("Motor similarity: " + similarity);
				if (similarity < MOTOR_SIMILARITY_THRESHOLD) {
					filtered.add(m);
				}
			}
			motors = filtered;
		}

		motors.sort(MOTOR_COMPARATOR);

		return motors;
	}

	public Motor getSelectedMotor() {
		return selectedMotor;
	}

	public Double getSelectedDelay() {
		return selectedDelay;
	}

	private class CurveSelectionRenderer implements ListCellRenderer<MotorHolder> {

		private final ListCellRenderer<MotorHolder> renderer;

		public CurveSelectionRenderer(ListCellRenderer<MotorHolder> renderer) {
			this.renderer = renderer;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends MotorHolder> list, MotorHolder value, int index,
				boolean isSelected, boolean cellHasFocus) {

			JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				Color color = getColor(value.getIndex());
				if (isSelected || cellHasFocus) {
					label.setBackground(color);
					label.setOpaque(true);
					Color fg = list.getBackground();
					fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue());		// List background changes for some reason, so clone the color
					label.setForeground(fg);
				} else {
					label.setBackground(list.getBackground());
					label.setForeground(color);
				}
			}

			return label;
		}
	}

	public static Color getColor(int index) {
		Color color = Util.getPlotColor(index);
		if (UITheme.isLightTheme(GUIUtil.getUITheme())) {
			return color;
		} else {
			return color.brighter().brighter();
		}
	}

	/**
	 * Select a ThrustCurveMotorSet, ThrustCurveMotor from the set, and delay.
	 * If the motor is null, select the default motor from the set.  This uses primarily motors
	 * that the user has previously used, and secondarily a heuristic method of selecting which
	 * thrust curve seems to be better or more reliable.
	 * 
	 * @param set	the motor set
	 * @param motor the motor
	 * @param delay the motor delay
	 */
	public void setMotor(ThrustCurveMotorSet set, ThrustCurveMotor motor, double delay) {
		if (set.getMotorCount() == 0) {
			throw new BugException("Attempting to select motor from empty ThrustCurveMotorSet: " + set);
		}

		selectedMotorSet = set;
		selectedDelay = delay;

		// If we passed in a particular thrustcurve, use it
		if (null != motor) {
			selectedMotor = motor;
			
			return;
		}

		// If there's only one option, use it
		if (set.getMotorCount() == 1) {
			selectedMotor = set.getMotors().get(0);
		}

		// Applyl heuristics to select the most likely thrustcurve for the selected set
		// Find which motor has been used the most recently
		List<ThrustCurveMotor> list = set.getMotors();
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(ApplicationPreferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : list) {
			String digest = m.getDigest();
			if (prefs.getBoolean(digest, false)) {
				selectedMotor = m;
				return;
			}
		}

		// No motor has been used
		list.sort(MOTOR_COMPARATOR);
		selectedMotor = list.get(0);
	}
}
