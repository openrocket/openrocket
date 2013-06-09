package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.motor.CloseableDialog;
import net.sf.openrocket.gui.dialogs.motor.MotorSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.utils.MotorCorrelation;

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

public class ThrustCurveMotorSelectionPanel extends JPanel implements MotorSelector {
	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSelectionPanel.class);
	private static final Translator trans = Application.getTranslator();
	
	private static final double MOTOR_SIMILARITY_THRESHOLD = 0.95;
	
	private static final int SHOW_ALL = 0;
	private static final int SHOW_SMALLER = 1;
	private static final int SHOW_EXACT = 2;
	private static final String[] SHOW_DESCRIPTIONS = {
			//// Show all motors
			trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc1"),
			//// Show motors with diameter less than that of the motor mount
			trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc2"),
			//// Show motors with diameter equal to that of the motor mount
			trans.get("TCMotorSelPan.SHOW_DESCRIPTIONS.desc3")
	};
	private static final int SHOW_MAX = 2;
	
	private static final int ZOOM_ICON_POSITION_NEGATIVE_X = 50;
	private static final int ZOOM_ICON_POSITION_POSITIVE_Y = 12;
	
	private static final Paint[] CURVE_COLORS = ChartColor.createDefaultPaintArray();
	
	private static final Color NO_COMMENT_COLOR = Color.GRAY;
	private static final Color WITH_COMMENT_COLOR = Color.BLACK;
	
	private static final ThrustCurveMotorComparator MOTOR_COMPARATOR = new ThrustCurveMotorComparator();
	
	
	
	private final List<ThrustCurveMotorSet> database;
	
	private final double diameter;
	private CloseableDialog dialog = null;
	
	
	private final ThrustCurveMotorDatabaseModel model;
	private final JTable table;
	private final TableRowSorter<TableModel> sorter;
	
	private final JCheckBox hideSimilarBox;
	
	private final JTextField searchField;
	private String[] searchTerms = new String[0];
	
	
	private final JLabel curveSelectionLabel;
	private final JComboBox curveSelectionBox;
	private final DefaultComboBoxModel curveSelectionModel;
	
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
	
	private final JComboBox delayBox;
	
	private ThrustCurveMotor selectedMotor;
	private ThrustCurveMotorSet selectedMotorSet;
	private double selectedDelay;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param current	the currently selected ThrustCurveMotor, or <code>null</code> for none.
	 * @param delay		the currently selected ejection charge delay.
	 * @param diameter	the diameter of the motor mount.
	 */
	public ThrustCurveMotorSelectionPanel(ThrustCurveMotor current, double delay, double diameter) {
		super(new MigLayout("fill", "[grow][]"));
		
		this.diameter = diameter;
		
		
		// Construct the database (adding the current motor if not in the db already)
		List<ThrustCurveMotorSet> db;
		db = Application.getThrustCurveMotorSetDatabase().getMotorSets();
		
		// If current motor is not found in db, add a new ThrustCurveMotorSet containing it
		if (current != null) {
			selectedMotor = current;
			for (ThrustCurveMotorSet motorSet : db) {
				if (motorSet.getMotors().contains(current)) {
					selectedMotorSet = motorSet;
					break;
				}
			}
			if (selectedMotorSet == null) {
				db = new ArrayList<ThrustCurveMotorSet>(db);
				ThrustCurveMotorSet extra = new ThrustCurveMotorSet();
				extra.addMotor(current);
				selectedMotorSet = extra;
				db.add(extra);
				Collections.sort(db);
			}
		}
		database = db;
		
		
		
		////  GUI
		
		JPanel panel;
		JLabel label;
		
		panel = new JPanel(new MigLayout("fill"));
		this.add(panel, "grow");
		
		
		
		// Selection label
		//// Select rocket motor:
		label = new StyledLabel(trans.get("TCMotorSelPan.lbl.Selrocketmotor"), Style.BOLD);
		panel.add(label, "spanx, wrap para");
		
		// Diameter selection
		JComboBox filterComboBox = new JComboBox(SHOW_DESCRIPTIONS);
		filterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				int sel = cb.getSelectedIndex();
				if ((sel < 0) || (sel > SHOW_MAX))
					sel = SHOW_ALL;
				switch (sel) {
				case SHOW_ALL:
					sorter.setRowFilter(new MotorRowFilterAll());
					break;
				
				case SHOW_SMALLER:
					sorter.setRowFilter(new MotorRowFilterSmaller());
					break;
				
				case SHOW_EXACT:
					sorter.setRowFilter(new MotorRowFilterExact());
					break;
				
				default:
					throw new BugException("Invalid selection mode sel=" + sel);
				}
				Application.getPreferences().putChoice("MotorDiameterMatch", sel);
				scrollSelectionVisible();
			}
		});
		panel.add(filterComboBox, "spanx, growx, wrap rel");
		
		//// Hide very similar thrust curves
		hideSimilarBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideSimilar"));
		GUIUtil.changeFontSize(hideSimilarBox, -1);
		hideSimilarBox.setSelected(Application.getPreferences().getBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_SIMILAR, true));
		hideSimilarBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.getPreferences().putBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_SIMILAR, hideSimilarBox.isSelected());
				updateData();
			}
		});
		panel.add(hideSimilarBox, "gapleft para, spanx, growx, wrap para");
		
		
		// Motor selection table
		model = new ThrustCurveMotorDatabaseModel(database);
		table = new JTable(model);
		
		
		// Set comparators and widths
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sorter = new TableRowSorter<TableModel>(model);
		for (int i = 0; i < ThrustCurveMotorColumns.values().length; i++) {
			ThrustCurveMotorColumns column = ThrustCurveMotorColumns.values()[i];
			sorter.setComparator(i, column.getComparator());
			table.getColumnModel().getColumn(i).setPreferredWidth(column.getWidth());
		}
		table.setRowSorter(sorter);
		// force initial sort order to by diameter, total impulse, manufacturer
		{
			RowSorter.SortKey[] sortKeys = {
					new RowSorter.SortKey(ThrustCurveMotorColumns.DIAMETER.ordinal(), SortOrder.ASCENDING),
					new RowSorter.SortKey(ThrustCurveMotorColumns.TOTAL_IMPULSE.ordinal(), SortOrder.ASCENDING),
					new RowSorter.SortKey(ThrustCurveMotorColumns.MANUFACTURER.ordinal(), SortOrder.ASCENDING)
			};
			sorter.setSortKeys(Arrays.asList(sortKeys));
		}
		
		// Set selection and double-click listeners
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if (row >= 0) {
					row = table.convertRowIndexToModel(row);
					ThrustCurveMotorSet motorSet = model.getMotorSet(row);
					log.info(Markers.USER_MARKER, "Selected table row " + row + ": " + motorSet);
					if (motorSet != selectedMotorSet) {
						select(selectMotor(motorSet));
					}
				} else {
					log.info(Markers.USER_MARKER, "Selected table row " + row + ", nothing selected");
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					if (dialog != null) {
						dialog.close(true);
					}
				}
			}
		});
		
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(table);
		panel.add(scrollpane, "grow, width :500:, height :300:, spanx, wrap para");
		
		
		
		
		// Motor mount diameter label
		//// Motor mount diameter: 
		label = new StyledLabel(trans.get("TCMotorSelPan.lbl.Motormountdia") + " " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(diameter));
		panel.add(label, "gapright 30lp, spanx, split");
		
		
		
		// Search field
		//// Search:
		label = new StyledLabel(trans.get("TCMotorSelPan.lbl.Search"));
		panel.add(label, "");
		
		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			private void update() {
				String text = searchField.getText().trim();
				String[] split = text.split("\\s+");
				ArrayList<String> list = new ArrayList<String>();
				for (String s : split) {
					s = s.trim().toLowerCase(Locale.getDefault());
					if (s.length() > 0) {
						list.add(s);
					}
				}
				searchTerms = list.toArray(new String[0]);
				sorter.sort();
				scrollSelectionVisible();
			}
		});
		panel.add(searchField, "growx, wrap");
		
		
		
		// Vertical split
		this.add(panel, "grow");
		this.add(new JSeparator(JSeparator.VERTICAL), "growy, gap para para");
		panel = new JPanel(new MigLayout("fill"));
		
		
		
		// Thrust curve selection
		//// Select thrust curve:
		curveSelectionLabel = new JLabel(trans.get("TCMotorSelPan.lbl.Selectthrustcurve"));
		panel.add(curveSelectionLabel);
		
		curveSelectionModel = new DefaultComboBoxModel();
		curveSelectionBox = new JComboBox(curveSelectionModel);
		curveSelectionBox.setRenderer(new CurveSelectionRenderer(curveSelectionBox.getRenderer()));
		curveSelectionBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object value = curveSelectionBox.getSelectedItem();
				if (value != null) {
					select(((MotorHolder) value).getMotor());
				}
			}
		});
		panel.add(curveSelectionBox, "growx, wrap para");
		
		
		
		
		
		// Ejection charge delay:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Ejectionchargedelay")));
		
		delayBox = new JComboBox();
		delayBox.setEditable(true);
		delayBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String sel = (String) cb.getSelectedItem();
				//// None
				if (sel.equalsIgnoreCase(trans.get("TCMotorSelPan.equalsIgnoreCase.None"))) {
					selectedDelay = Motor.PLUGGED;
				} else {
					try {
						selectedDelay = Double.parseDouble(sel);
					} catch (NumberFormatException ignore) {
					}
				}
				setDelays(false);
			}
		});
		panel.add(delayBox, "growx, wrap rel");
		//// (Number of seconds or \"None\")
		panel.add(new StyledLabel(trans.get("TCMotorSelPan.lbl.NumberofsecondsorNone"), -3), "skip, wrap para");
		setDelays(false);
		
		
		panel.add(new JSeparator(), "spanx, growx, wrap para");
		
		
		
		// Thrust curve info
		//// Total impulse:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Totalimpulse")));
		totalImpulseLabel = new JLabel();
		panel.add(totalImpulseLabel, "split");
		classificationLabel = new JLabel();
		classificationLabel.setEnabled(false); // Gray out
		panel.add(classificationLabel, "gapleft unrel, wrap");
		
		//// Avg. thrust:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Avgthrust")));
		avgThrustLabel = new JLabel();
		panel.add(avgThrustLabel, "wrap");
		
		//// Max. thrust:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Maxthrust")));
		maxThrustLabel = new JLabel();
		panel.add(maxThrustLabel, "wrap");
		
		//// Burn time:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Burntime")));
		burnTimeLabel = new JLabel();
		panel.add(burnTimeLabel, "wrap");
		
		//// Launch mass:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Launchmass")));
		launchMassLabel = new JLabel();
		panel.add(launchMassLabel, "wrap");
		
		//// Empty mass:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Emptymass")));
		emptyMassLabel = new JLabel();
		panel.add(emptyMassLabel, "wrap");
		
		//// Data points:
		panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Datapoints")));
		dataPointsLabel = new JLabel();
		panel.add(dataPointsLabel, "wrap para");
		
		if (System.getProperty("openrocket.debug.motordigest") != null) {
			//// Digest:
			panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Digest")));
			digestLabel = new JLabel();
			panel.add(digestLabel, "w :300:, wrap para");
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
		scrollpane = new JScrollPane(comment);
		panel.add(scrollpane, "spanx, growx, wrap para");
		
		
		
		
		// Thrust curve plot
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
					List<ThrustCurveMotor> motors = getFilteredCurves();
					ThrustCurveMotorPlotDialog plotDialog = new ThrustCurveMotorPlotDialog(motors,
							motors.indexOf(selectedMotor),
							SwingUtilities.getWindowAncestor(ThrustCurveMotorSelectionPanel.this));
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
		
		
		panel.add(layer, "width 300:300:, height 180:180:, grow, spanx");
		
		
		
		this.add(panel, "grow");
		
		
		
		// Sets the filter:
		int showMode = Application.getPreferences().getChoice(net.sf.openrocket.startup.Preferences.MOTOR_DIAMETER_FILTER, SHOW_MAX, SHOW_EXACT);
		filterComboBox.setSelectedIndex(showMode);
		
		
		// Update the panel data
		updateData();
		selectedDelay = delay;
		setDelays(false);
		
	}
	
	@Override
	public Motor getSelectedMotor() {
		return selectedMotor;
	}
	
	
	@Override
	public double getSelectedDelay() {
		return selectedDelay;
	}
	
	
	@Override
	public JComponent getDefaultFocus() {
		return searchField;
	}
	
	@Override
	public void selectedMotor(Motor motorSelection) {
		if (!(motorSelection instanceof ThrustCurveMotor)) {
			log.error("Received argument that was not ThrustCurveMotor: " + motorSelection);
			return;
		}
		
		ThrustCurveMotor motor = (ThrustCurveMotor) motorSelection;
		ThrustCurveMotorSet set = findMotorSet(motor);
		if (set == null) {
			log.error("Could not find set for motor:" + motorSelection);
			return;
		}
		
		// Store selected motor in preferences node, set all others to false
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(net.sf.openrocket.startup.Preferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : set.getMotors()) {
			String digest = m.getDigest();
			prefs.putBoolean(digest, m == motor);
		}
	}
	
	public void setCloseableDialog(CloseableDialog dialog) {
		this.dialog = dialog;
	}
	
	
	
	private void changeLabelFont(ValueAxis axis, float size) {
		Font font = axis.getTickLabelFont();
		font = font.deriveFont(font.getSize2D() + size);
		axis.setTickLabelFont(font);
	}
	
	
	/**
	 * Called when a different motor is selected from within the panel.
	 */
	private void select(ThrustCurveMotor motor) {
		if (selectedMotor == motor)
			return;
		
		ThrustCurveMotorSet set = findMotorSet(motor);
		if (set == null) {
			throw new BugException("Could not find motor from database, motor=" + motor);
		}
		
		boolean updateDelays = (selectedMotorSet != set);
		
		selectedMotor = motor;
		selectedMotorSet = set;
		updateData();
		if (updateDelays) {
			setDelays(true);
		}
	}
	
	
	private void updateData() {
		
		if (selectedMotorSet == null) {
			// No motor selected
			curveSelectionModel.removeAllElements();
			curveSelectionBox.setEnabled(false);
			curveSelectionLabel.setEnabled(false);
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
			return;
		}
		
		
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
	}
	
	private List<ThrustCurveMotor> getFilteredCurves() {
		List<ThrustCurveMotor> motors = selectedMotorSet.getMotors();
		if (hideSimilarBox.isSelected()) {
			List<ThrustCurveMotor> filtered = new ArrayList<ThrustCurveMotor>(motors.size());
			for (int i = 0; i < motors.size(); i++) {
				ThrustCurveMotor m = motors.get(i);
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
		
		Collections.sort(motors, MOTOR_COMPARATOR);
		
		return motors;
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
	
	private void scrollSelectionVisible() {
		if (selectedMotorSet != null) {
			int index = table.convertRowIndexToView(model.getIndex(selectedMotorSet));
			//System.out.println("index=" + index);
			table.getSelectionModel().setSelectionInterval(index, index);
			Rectangle rect = table.getCellRect(index, 0, true);
			rect = new Rectangle(rect.x, rect.y - 100, rect.width, rect.height + 200);
			table.scrollRectToVisible(rect);
		}
	}
	
	
	public static Color getColor(int index) {
		return (Color) CURVE_COLORS[index % CURVE_COLORS.length];
	}
	
	
	/**
	 * Find the ThrustCurveMotorSet that contains a motor.
	 * 
	 * @param motor		the motor to look for.
	 * @return			the ThrustCurveMotorSet, or null if not found.
	 */
	private ThrustCurveMotorSet findMotorSet(ThrustCurveMotor motor) {
		for (ThrustCurveMotorSet set : database) {
			if (set.getMotors().contains(motor)) {
				return set;
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * Select the default motor from this ThrustCurveMotorSet.  This uses primarily motors
	 * that the user has previously used, and secondarily a heuristic method of selecting which
	 * thrust curve seems to be better or more reliable.
	 * 
	 * @param set	the motor set
	 * @return		the default motor in this set
	 */
	private ThrustCurveMotor selectMotor(ThrustCurveMotorSet set) {
		if (set.getMotorCount() == 0) {
			throw new BugException("Attempting to select motor from empty ThrustCurveMotorSet: " + set);
		}
		if (set.getMotorCount() == 1) {
			return set.getMotors().get(0);
		}
		
		
		// Find which motor has been used the most recently
		List<ThrustCurveMotor> list = set.getMotors();
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(net.sf.openrocket.startup.Preferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : list) {
			String digest = m.getDigest();
			if (prefs.getBoolean(digest, false)) {
				return m;
			}
		}
		
		// No motor has been used
		Collections.sort(list, MOTOR_COMPARATOR);
		return list.get(0);
	}
	
	
	/**
	 * Set the values in the delay combo box.  If <code>reset</code> is <code>true</code>
	 * then sets the selected value as the value closest to selectedDelay, otherwise
	 * leaves selection alone.
	 */
	private void setDelays(boolean reset) {
		if (selectedMotor == null) {
			
			//// None
			delayBox.setModel(new DefaultComboBoxModel(new String[] { trans.get("TCMotorSelPan.delayBox.None") }));
			delayBox.setSelectedIndex(0);
			
		} else {
			
			List<Double> delays = selectedMotorSet.getDelays();
			String[] delayStrings = new String[delays.size()];
			double currentDelay = selectedDelay; // Store current setting locally
			
			for (int i = 0; i < delays.size(); i++) {
				//// None
				delayStrings[i] = ThrustCurveMotor.getDelayString(delays.get(i), trans.get("TCMotorSelPan.delayBox.None"));
			}
			delayBox.setModel(new DefaultComboBoxModel(delayStrings));
			
			if (reset) {
				
				// Find and set the closest value
				double closest = Double.NaN;
				for (int i = 0; i < delays.size(); i++) {
					// if-condition to always become true for NaN
					if (!(Math.abs(delays.get(i) - currentDelay) > Math.abs(closest - currentDelay))) {
						closest = delays.get(i);
					}
				}
				if (!Double.isNaN(closest)) {
					selectedDelay = closest;
					//// None
					delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(closest, trans.get("TCMotorSelPan.delayBox.None")));
				} else {
					delayBox.setSelectedItem("None");
				}
				
			} else {
				
				selectedDelay = currentDelay;
				//// None
				delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(currentDelay, trans.get("TCMotorSelPan.delayBox.None")));
				
			}
			
		}
	}
	
	
	
	
	//////////////////////
	
	
	private class CurveSelectionRenderer implements ListCellRenderer {
		
		private final ListCellRenderer renderer;
		
		public CurveSelectionRenderer(ListCellRenderer renderer) {
			this.renderer = renderer;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			Component c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof MotorHolder) {
				MotorHolder m = (MotorHolder) value;
				c.setForeground(getColor(m.getIndex()));
			}
			
			return c;
		}
		
	}
	
	
	////////  Row filters
	
	/**
	 * Abstract adapter class.
	 */
	private abstract class MotorRowFilter extends RowFilter<TableModel, Integer> {
		@Override
		public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			int index = entry.getIdentifier();
			ThrustCurveMotorSet m = model.getMotorSet(index);
			return filterByDiameter(m) && filterByString(m);
		}
		
		public abstract boolean filterByDiameter(ThrustCurveMotorSet m);
		
		
		public boolean filterByString(ThrustCurveMotorSet m) {
			main: for (String s : searchTerms) {
				for (ThrustCurveMotorColumns col : ThrustCurveMotorColumns.values()) {
					String str = col.getValue(m).toString().toLowerCase(Locale.getDefault());
					if (str.indexOf(s) >= 0)
						continue main;
				}
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Show all motors.
	 */
	private class MotorRowFilterAll extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(ThrustCurveMotorSet m) {
			return true;
		}
	}
	
	/**
	 * Show motors smaller than the mount.
	 */
	private class MotorRowFilterSmaller extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(ThrustCurveMotorSet m) {
			return (m.getDiameter() <= diameter + 0.0004);
		}
	}
	
	/**
	 * Show motors that fit the mount.
	 */
	private class MotorRowFilterExact extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(ThrustCurveMotorSet m) {
			return ((m.getDiameter() <= diameter + 0.0004) && (m.getDiameter() >= diameter - 0.0015));
		}
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
