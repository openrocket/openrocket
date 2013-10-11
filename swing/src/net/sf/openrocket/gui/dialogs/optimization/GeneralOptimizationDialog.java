package net.sf.openrocket.gui.dialogs.optimization;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.CsvOptionPanel;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.DoubleCellEditor;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitCellEditor;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.scalefigure.RocketFigure;
import net.sf.openrocket.gui.scalefigure.ScaleScrollPane;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.OptimizationGoal;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.rocketoptimization.domains.IdentitySimulationDomain;
import net.sf.openrocket.optimization.rocketoptimization.domains.StabilityDomain;
import net.sf.openrocket.optimization.rocketoptimization.goals.MaximizationGoal;
import net.sf.openrocket.optimization.rocketoptimization.goals.MinimizationGoal;
import net.sf.openrocket.optimization.rocketoptimization.goals.ValueSeekGoal;
import net.sf.openrocket.optimization.services.OptimizationServiceHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.CaliberUnit;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Named;
import net.sf.openrocket.util.TextUtil;

import com.itextpdf.text.Font;

/**
 * General rocket optimization dialog. 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralOptimizationDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(GeneralOptimizationDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	private static final Collator collator = Collator.getInstance();
	
	private static final String GOAL_MAXIMIZE = trans.get("goal.maximize");
	private static final String GOAL_MINIMIZE = trans.get("goal.minimize");
	private static final String GOAL_SEEK = trans.get("goal.seek");
	
	private static final String START_TEXT = trans.get("btn.start");
	private static final String STOP_TEXT = trans.get("btn.stop");
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	private final List<OptimizableParameter> optimizationParameters = new ArrayList<OptimizableParameter>();
	private final Map<Object, List<SimulationModifier>> simulationModifiers =
			new HashMap<Object, List<SimulationModifier>>();
	
	
	private final OpenRocketDocument baseDocument;
	private OpenRocketDocument documentCopy;
	
	private final JButton addButton;
	private final JButton removeButton;
	private final JButton removeAllButton;
	
	private final ParameterSelectionTableModel selectedModifierTableModel;
	private final JTable selectedModifierTable;
	private final DescriptionArea selectedModifierDescription;
	private final SimulationModifierTree availableModifierTree;
	
	private final JComboBox simulationSelectionCombo;
	private final JComboBox optimizationParameterCombo;
	
	private final JComboBox optimizationGoalCombo;
	private final JSpinner optimizationGoalSpinner;
	private final UnitSelector optimizationGoalUnitSelector;
	private final DoubleModel optimizationSeekValue;
	
	private DoubleModel minimumStability;
	private DoubleModel maximumStability;
	private final JCheckBox minimumStabilitySelected;
	private final JSpinner minimumStabilitySpinner;
	private final UnitSelector minimumStabilityUnitSelector;
	private final JCheckBox maximumStabilitySelected;
	private final JSpinner maximumStabilitySpinner;
	private final UnitSelector maximumStabilityUnitSelector;
	
	private final JLabel bestValueLabel;
	private final JLabel stepCountLabel;
	private final JLabel evaluationCountLabel;
	private final JLabel stepSizeLabel;
	
	private final RocketFigure figure;
	private final JToggleButton startButton;
	private final JButton plotButton;
	private final JButton saveButton;
	
	private final JButton applyButton;
	private final JButton resetButton;
	private final JButton closeButton;
	
	private final List<SimulationModifier> selectedModifiers = new ArrayList<SimulationModifier>();
	
	/** List of components to disable while optimization is running */
	private final List<JComponent> disableComponents = new ArrayList<JComponent>();
	
	/** Whether optimization is currently running or not */
	private boolean running = false;
	/** The optimization worker that is running */
	private OptimizationWorker worker = null;
	
	private double bestValue = Double.NaN;
	private Unit bestValueUnit = Unit.NOUNIT;
	private int stepCount = 0;
	private int evaluationCount = 0;
	private double stepSize = 0;
	
	private final Map<Point, FunctionEvaluationData> evaluationHistory = new LinkedHashMap<Point, FunctionEvaluationData>();
	private final List<Point> optimizationPath = new LinkedList<Point>();
	
	private boolean updating = false;
	
	/**
	 * Sole constructor.
	 * 
	 * @param document  the document
	 * @param parent    the parent window
	 */
	public GeneralOptimizationDialog(OpenRocketDocument document, Window parent) {
		super(parent, trans.get("title"));
		
		this.baseDocument = document;
		this.documentCopy = document.copy();
		
		loadOptimizationParameters();
		loadSimulationModifiers();
		
		JPanel sub;
		JLabel label;
		JScrollPane scroll;
		String tip;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		ChangeListener clearHistoryChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				clearHistory();
			}
		};
		ActionListener clearHistoryActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearHistory();
			}
		};
		
		// // Selected modifiers table
		
		selectedModifierTableModel = new ParameterSelectionTableModel();
		selectedModifierTable = new JTable(selectedModifierTableModel);
		selectedModifierTable.setDefaultRenderer(Double.class, new DoubleCellRenderer());
		selectedModifierTable.setRowSelectionAllowed(true);
		selectedModifierTable.setColumnSelectionAllowed(false);
		selectedModifierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Make sure spinner editor fits into the cell height
		selectedModifierTable.setRowHeight(new JSpinner().getPreferredSize().height - 4);
		
		selectedModifierTable.setDefaultEditor(Double.class, new DoubleCellEditor());
		selectedModifierTable.setDefaultEditor(Unit.class, new UnitCellEditor() {
			@Override
			protected UnitGroup getUnitGroup(Unit value, int row, int column) {
				return selectedModifiers.get(row).getUnitGroup();
			}
		});
		
		disableComponents.add(selectedModifierTable);
		
		selectedModifierTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateComponents();
			}
		});
		
		// Set column widths
		TableColumnModel columnModel = selectedModifierTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(150);
		columnModel.getColumn(1).setPreferredWidth(40);
		columnModel.getColumn(2).setPreferredWidth(40);
		columnModel.getColumn(3).setPreferredWidth(40);
		
		scroll = new JScrollPane(selectedModifierTable);
		
		label = new StyledLabel(trans.get("lbl.paramsToOptimize"), Style.BOLD);
		disableComponents.add(label);
		panel.add(label, "split 3, flowy");
		panel.add(scroll, "wmin 300lp, height 200lp, grow");
		selectedModifierDescription = new DescriptionArea(2, -3);
		disableComponents.add(selectedModifierDescription);
		panel.add(selectedModifierDescription, "growx");
		
		// // Add/remove buttons
		sub = new JPanel(new MigLayout("fill"));
		
		addButton = new JButton(Chars.LEFT_ARROW + " " + trans.get("btn.add") + "   ");
		addButton.setToolTipText(trans.get("btn.add.ttip"));
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationModifier mod = getSelectedAvailableModifier();
				if (mod != null) {
					addModifier(mod);
					clearHistory();
				} else {
					log.error("Attempting to add simulation modifier when none is selected");
				}
			}
		});
		disableComponents.add(addButton);
		sub.add(addButton, "wrap para, sg button");
		
		removeButton = new JButton("   " + trans.get("btn.remove") + " " + Chars.RIGHT_ARROW);
		removeButton.setToolTipText(trans.get("btn.remove.ttip"));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationModifier mod = getSelectedModifier();
				if (mod == null) {
					log.error("Attempting to remove simulation modifier when none is selected");
					return;
				}
				removeModifier(mod);
				clearHistory();
			}
		});
		disableComponents.add(removeButton);
		sub.add(removeButton, "wrap para*2, sg button");
		
		removeAllButton = new JButton(trans.get("btn.removeAll"));
		removeAllButton.setToolTipText(trans.get("btn.removeAll.ttip"));
		removeAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Removing all selected modifiers");
				selectedModifiers.clear();
				selectedModifierTableModel.fireTableDataChanged();
				availableModifierTree.repaint();
				clearHistory();
			}
		});
		disableComponents.add(removeAllButton);
		sub.add(removeAllButton, "wrap para, sg button");
		
		panel.add(sub);
		
		// // Available modifier tree
		availableModifierTree = new SimulationModifierTree(documentCopy.getRocket(), simulationModifiers, selectedModifiers);
		availableModifierTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateComponents();
			}
		});
		
		// Handle double-click
		availableModifierTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					SimulationModifier mod = getSelectedAvailableModifier();
					if (mod != null) {
						addModifier(mod);
						clearHistory();
					} else {
						log.info(Markers.USER_MARKER, "Double-clicked non-available option");
					}
				}
			}
		});
		
		disableComponents.add(availableModifierTree);
		scroll = new JScrollPane(availableModifierTree);
		label = new StyledLabel(trans.get("lbl.availableParams"), Style.BOLD);
		disableComponents.add(label);
		panel.add(label, "split 2, flowy");
		panel.add(scroll, "width 300lp, height 200lp, grow, wrap para*2");
		
		// // Optimization options sub-panel
		
		sub = new JPanel(new MigLayout("fill"));
		TitledBorder border = BorderFactory.createTitledBorder(trans.get("lbl.optimizationOpts"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);
		disableComponents.add(sub);
		
		// // Simulation to optimize
		
		label = new JLabel(trans.get("lbl.optimizeSim"));
		tip = trans.get("lbl.optimizeSim.ttip");
		label.setToolTipText(tip);
		disableComponents.add(label);
		sub.add(label, "");
		
		simulationSelectionCombo = new JComboBox();
		simulationSelectionCombo.setToolTipText(tip);
		populateSimulations();
		simulationSelectionCombo.addActionListener(clearHistoryActionListener);
		disableComponents.add(simulationSelectionCombo);
		sub.add(simulationSelectionCombo, "growx, wrap unrel");
		
		// // Value to optimize
		label = new JLabel(trans.get("lbl.optimizeValue"));
		tip = trans.get("lbl.optimizeValue.ttip");
		label.setToolTipText(tip);
		disableComponents.add(label);
		sub.add(label, "");
		
		optimizationParameterCombo = new JComboBox();
		optimizationParameterCombo.setToolTipText(tip);
		populateParameters();
		optimizationParameterCombo.addActionListener(clearHistoryActionListener);
		disableComponents.add(optimizationParameterCombo);
		sub.add(optimizationParameterCombo, "growx, wrap unrel");
		
		// // Optimization goal
		label = new JLabel(trans.get("lbl.optimizeGoal"));
		tip = trans.get("lbl.optimizeGoal");
		label.setToolTipText(tip);
		disableComponents.add(label);
		sub.add(label, "");
		
		optimizationGoalCombo = new JComboBox(new String[] { GOAL_MAXIMIZE, GOAL_MINIMIZE, GOAL_SEEK });
		optimizationGoalCombo.setToolTipText(tip);
		optimizationGoalCombo.setEditable(false);
		optimizationGoalCombo.addActionListener(clearHistoryActionListener);
		disableComponents.add(optimizationGoalCombo);
		sub.add(optimizationGoalCombo, "growx");
		
		// // Optimization custom value
		optimizationSeekValue = new DoubleModel(0, UnitGroup.UNITS_NONE);
		optimizationSeekValue.addChangeListener(clearHistoryChangeListener);
		
		optimizationGoalSpinner = new JSpinner(optimizationSeekValue.getSpinnerModel());
		tip = trans.get("lbl.optimizeGoalValue.ttip");
		optimizationGoalSpinner.setToolTipText(tip);
		optimizationGoalSpinner.setEditor(new SpinnerEditor(optimizationGoalSpinner));
		disableComponents.add(optimizationGoalSpinner);
		sub.add(optimizationGoalSpinner, "width 30lp");
		
		optimizationGoalUnitSelector = new UnitSelector(optimizationSeekValue);
		optimizationGoalUnitSelector.setToolTipText(tip);
		disableComponents.add(optimizationGoalUnitSelector);
		sub.add(optimizationGoalUnitSelector, "width 20lp, wrap unrel");
		
		panel.add(sub, "grow");
		
		// // Required stability sub-panel
		
		sub = new JPanel(new MigLayout("fill"));
		border = BorderFactory.createTitledBorder(trans.get("lbl.requireStability"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		sub.setBorder(border);
		disableComponents.add(sub);
		
		double ref = CaliberUnit.calculateCaliber(baseDocument.getRocket());
		minimumStability = new DoubleModel(ref, UnitGroup.stabilityUnits(ref));
		maximumStability = new DoubleModel(5 * ref, UnitGroup.stabilityUnits(ref));
		minimumStability.addChangeListener(clearHistoryChangeListener);
		maximumStability.addChangeListener(clearHistoryChangeListener);
		
		// // Minimum stability
		tip = trans.get("lbl.requireMinStability.ttip");
		minimumStabilitySelected = new JCheckBox(trans.get("lbl.requireMinStability"));
		minimumStabilitySelected.setSelected(true);
		minimumStabilitySelected.setToolTipText(tip);
		minimumStabilitySelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateComponents();
			}
		});
		disableComponents.add(minimumStabilitySelected);
		sub.add(minimumStabilitySelected);
		
		minimumStabilitySpinner = new JSpinner(minimumStability.getSpinnerModel());
		minimumStabilitySpinner.setToolTipText(tip);
		minimumStabilitySpinner.setEditor(new SpinnerEditor(minimumStabilitySpinner));
		disableComponents.add(minimumStabilitySpinner);
		sub.add(minimumStabilitySpinner, "growx");
		
		minimumStabilityUnitSelector = new UnitSelector(minimumStability);
		minimumStabilityUnitSelector.setToolTipText(tip);
		disableComponents.add(minimumStabilityUnitSelector);
		sub.add(minimumStabilityUnitSelector, "growx, wrap unrel");
		
		// // Maximum stability
		tip = trans.get("lbl.requireMaxStability.ttip");
		maximumStabilitySelected = new JCheckBox(trans.get("lbl.requireMaxStability"));
		maximumStabilitySelected.setToolTipText(tip);
		maximumStabilitySelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateComponents();
			}
		});
		disableComponents.add(maximumStabilitySelected);
		sub.add(maximumStabilitySelected);
		
		maximumStabilitySpinner = new JSpinner(maximumStability.getSpinnerModel());
		maximumStabilitySpinner.setToolTipText(tip);
		maximumStabilitySpinner.setEditor(new SpinnerEditor(maximumStabilitySpinner));
		disableComponents.add(maximumStabilitySpinner);
		sub.add(maximumStabilitySpinner, "growx");
		
		maximumStabilityUnitSelector = new UnitSelector(maximumStability);
		maximumStabilityUnitSelector.setToolTipText(tip);
		disableComponents.add(maximumStabilityUnitSelector);
		sub.add(maximumStabilityUnitSelector, "growx, wrap para");
		
		
		
		//		DescriptionArea desc = new DescriptionArea("Stability requirements are verified during each time step of the simulation.",
		// 2, -2, false);
		// desc.setViewportBorder(null);
		// disableComponents.add(desc);
		// sub.add(desc, "span, growx");
		
		panel.add(sub, "span 2, grow, wrap para*2");
		
		// // Rocket figure
		figure = new RocketFigure(getSelectedSimulation().getConfiguration());
		figure.setBorderPixels(1, 1);
		ScaleScrollPane figureScrollPane = new ScaleScrollPane(figure);
		figureScrollPane.setFitting(true);
		panel.add(figureScrollPane, "span, split, height 200lp, grow");
		
		sub = new JPanel(new MigLayout("fill"));
		
		label = new JLabel(trans.get("status.bestValue"));
		tip = trans.get("status.bestValue.ttip");
		label.setToolTipText(tip);
		sub.add(label, "gapright unrel");
		
		bestValueLabel = new JLabel();
		bestValueLabel.setToolTipText(tip);
		sub.add(bestValueLabel, "wmin 60lp, wrap rel");
		
		label = new JLabel(trans.get("status.stepCount"));
		tip = trans.get("status.stepCount.ttip");
		label.setToolTipText(tip);
		sub.add(label, "gapright unrel");
		
		stepCountLabel = new JLabel();
		stepCountLabel.setToolTipText(tip);
		sub.add(stepCountLabel, "wrap rel");
		
		label = new JLabel(trans.get("status.evalCount"));
		tip = trans.get("status.evalCount.ttip");
		label.setToolTipText(tip);
		sub.add(label, "gapright unrel");
		
		evaluationCountLabel = new JLabel();
		evaluationCountLabel.setToolTipText(tip);
		sub.add(evaluationCountLabel, "wrap rel");
		
		label = new JLabel(trans.get("status.stepSize"));
		tip = trans.get("status.stepSize.ttip");
		label.setToolTipText(tip);
		sub.add(label, "gapright unrel");
		
		stepSizeLabel = new JLabel();
		stepSizeLabel.setToolTipText(tip);
		sub.add(stepSizeLabel, "wrap para");
		
		// // Start/Stop button
		
		startButton = new JToggleButton(START_TEXT);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updating) {
					log.debug("Updating, ignoring event");
					return;
				}
				if (running) {
					log.info(Markers.USER_MARKER, "Stopping optimization");
					stopOptimization();
				} else {
					log.info(Markers.USER_MARKER, "Starting optimization");
					startOptimization();
				}
			}
		});
		sub.add(startButton, "span, growx, wrap para*2");
		
		plotButton = new JButton(trans.get("btn.plotPath"));
		plotButton.setToolTipText(trans.get("btn.plotPath.ttip"));
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Plotting optimization path, dimensionality=" + selectedModifiers.size());
				OptimizationPlotDialog dialog = new OptimizationPlotDialog(
						Collections.unmodifiableList(optimizationPath),
						Collections.unmodifiableMap(evaluationHistory),
						Collections.unmodifiableList(selectedModifiers),
						getSelectedParameter(),
						UnitGroup.stabilityUnits(getSelectedSimulation().getRocket()),
						GeneralOptimizationDialog.this);
				dialog.setVisible(true);
			}
		});
		disableComponents.add(plotButton);
		sub.add(plotButton, "span, growx, wrap");
		
		saveButton = new JButton(trans.get("btn.save"));
		saveButton.setToolTipText(trans.get("btn.save.ttip"));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "User selected save path");
				savePath();
			}
		});
		disableComponents.add(saveButton);
		sub.add(saveButton, "span, growx");
		
		panel.add(sub, "wrap para*2");
		
		// // Bottom buttons
		
		applyButton = new JButton(trans.get("btn.apply"));
		applyButton.setToolTipText(trans.get("btn.apply.ttip"));
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Applying optimization changes");
				applyDesign();
			}
		});
		disableComponents.add(applyButton);
		panel.add(applyButton, "span, split, gapright para, right");
		
		resetButton = new JButton(trans.get("btn.reset"));
		resetButton.setToolTipText(trans.get("btn.reset.ttip"));
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Resetting optimization design");
				resetDesign();
			}
		});
		disableComponents.add(resetButton);
		panel.add(resetButton, "gapright para, right");
		
		closeButton = new JButton(trans.get("btn.close"));
		closeButton.setToolTipText(trans.get("btn.close.ttip"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Closing optimization dialog");
				stopOptimization();
				GeneralOptimizationDialog.this.dispose();
			}
		});
		panel.add(closeButton, "right");
		
		this.add(panel);
		clearHistory();
		updateComponents();
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	private void startOptimization() {
		if (running) {
			log.info("Optimization already running");
			return;
		}
		
		if (selectedModifiers.isEmpty()) {
			JOptionPane.showMessageDialog(this, trans.get("error.selectParams.text"),
					trans.get("error.selectParams.title"), JOptionPane.ERROR_MESSAGE);
			updating = true;
			startButton.setSelected(false);
			startButton.setText(START_TEXT);
			updating = false;
			return;
		}
		
		running = true;
		
		// Update the button status
		updating = true;
		startButton.setSelected(true);
		startButton.setText(STOP_TEXT);
		updating = false;
		
		
		// Create a copy of the simulation (we're going to modify the original in the current thread)
		Simulation simulation = getSelectedSimulation();
		Rocket rocketCopy = simulation.getRocket().copyWithOriginalID();
		simulation = simulation.duplicateSimulation(rocketCopy);
		
		OptimizableParameter parameter = getSelectedParameter();
		
		OptimizationGoal goal;
		String value = (String) optimizationGoalCombo.getSelectedItem();
		if (GOAL_MAXIMIZE.equals(value)) {
			goal = new MaximizationGoal();
		} else if (GOAL_MINIMIZE.equals(value)) {
			goal = new MinimizationGoal();
		} else if (GOAL_SEEK.equals(value)) {
			goal = new ValueSeekGoal(optimizationSeekValue.getValue());
		} else {
			throw new BugException("optimizationGoalCombo had invalid value: " + value);
		}
		
		SimulationDomain domain;
		if (minimumStabilitySelected.isSelected() || maximumStabilitySelected.isSelected()) {
			double min, max;
			boolean minAbsolute, maxAbsolute;
			
			/*
			 * Make minAbsolute/maxAbsolute consistent with each other to produce reasonable
			 * result in plot tool tips.  Yes, this is a bit ugly.
			 */
			
			// Min stability
			Unit unit = minimumStability.getCurrentUnit();
			if (unit instanceof CaliberUnit) {
				min = unit.toUnit(minimumStability.getValue());
				minAbsolute = false;
			} else {
				min = minimumStability.getValue();
				minAbsolute = true;
			}
			
			// Max stability
			unit = maximumStability.getCurrentUnit();
			if (unit instanceof CaliberUnit) {
				max = unit.toUnit(maximumStability.getValue());
				maxAbsolute = false;
			} else {
				max = maximumStability.getValue();
				maxAbsolute = true;
			}
			
			if (!minimumStabilitySelected.isSelected()) {
				min = Double.NaN;
				minAbsolute = maxAbsolute;
			}
			if (!maximumStabilitySelected.isSelected()) {
				max = Double.NaN;
				maxAbsolute = minAbsolute;
			}
			
			domain = new StabilityDomain(min, minAbsolute, max, maxAbsolute);
		} else {
			domain = new IdentitySimulationDomain();
		}
		
		SimulationModifier[] modifiers = selectedModifiers.toArray(new SimulationModifier[0]);
		
		// Check for DeploymentAltitude modifier, if it's there, we want to make certain the DeploymentEvent
		// is ALTITUDE:
		for (SimulationModifier mod : modifiers) {
			
			try {
				mod.initialize(simulation);
			} catch (OptimizationException ex) {
				updating = true;
				startButton.setSelected(false);
				startButton.setText(START_TEXT);
				updating = false;
				throw new BugException(ex);
			}
			
		}
		
		// Create and start the background worker
		worker = new OptimizationWorker(simulation, parameter, goal, domain, modifiers) {
			@Override
			protected void done(OptimizationException exception) {
				log.info("Optimization finished, exception=" + exception, exception);
				
				if (exception != null) {
					JOptionPane.showMessageDialog(GeneralOptimizationDialog.this,
							new Object[] {
									trans.get("error.optimizationFailure.text"),
									exception.getLocalizedMessage()
							}, trans.get("error.optimizationFailure.title"), JOptionPane.ERROR_MESSAGE);
				}
				
				worker = null;
				stopOptimization();
				
				// Disable the start/stop button for a short while after ending the simulation
				// to prevent accidentally starting a new optimization when trying to stop it
				startButton.setEnabled(false);
				Timer timer = new Timer(750, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						startButton.setEnabled(true);
					}
				});
				timer.setRepeats(false);
				timer.start();
				updateComponents();
			}
			
			@Override
			protected void functionEvaluated(List<FunctionEvaluationData> data) {
				for (FunctionEvaluationData d : data) {
					evaluationHistory.put(d.getPoint(), d);
					evaluationCount++;
				}
				updateCounters();
			}
			
			@Override
			protected void optimizationStepTaken(List<OptimizationStepData> data) {
				
				// Add starting point to the path
				if (optimizationPath.isEmpty()) {
					optimizationPath.add(data.get(0).getOldPoint());
				}
				
				// Add other points to the path
				for (OptimizationStepData d : data) {
					optimizationPath.add(d.getNewPoint());
				}
				
				// Get function value from the latest point
				OptimizationStepData latest = data.get(data.size() - 1);
				Point newPoint = latest.getNewPoint();
				
				FunctionEvaluationData pointValue = evaluationHistory.get(newPoint);
				if (pointValue != null && pointValue.getParameterValue() != null) {
					bestValue = pointValue.getParameterValue().getValue();
				} else {
					bestValue = Double.NaN;
				}
				
				// Update the simulation
				Simulation sim = getSelectedSimulation();
				for (int i = 0; i < newPoint.dim(); i++) {
					try {
						selectedModifiers.get(i).modify(sim, newPoint.get(i));
					} catch (OptimizationException e) {
						throw new BugException("Simulation modifier failed to modify the base simulation " +
								"modifier=" + selectedModifiers.get(i), e);
					}
				}
				figure.updateFigure();
				
				// Update other counter data
				stepCount += data.size();
				stepSize = latest.getStepSize();
				updateCounters();
			}
		};
		worker.start();
		
		clearHistory();
		
		updateComponents();
	}
	
	private void stopOptimization() {
		if (!running) {
			log.info("Optimization not running");
			return;
		}
		
		if (worker != null && worker.isAlive()) {
			log.info("Worker still running, interrupting it and setting to null");
			worker.interrupt();
			worker = null;
			return;
		}
		
		running = false;
		
		// Update the button status
		updating = true;
		startButton.setSelected(false);
		startButton.setText(START_TEXT);
		updating = false;
		
		updateComponents();
	}
	
	/**
	 * Reset the current optimization history and values.  This does not reset the design.
	 */
	private void clearHistory() {
		evaluationHistory.clear();
		optimizationPath.clear();
		bestValue = Double.NaN;
		bestValueUnit = getSelectedParameter().getUnitGroup().getDefaultUnit();
		stepCount = 0;
		evaluationCount = 0;
		stepSize = 0.5;
		updateCounters();
		updateComponents();
	}
	
	private void applyDesign() {
		// TODO: MEDIUM: Apply also potential changes to simulations
		Rocket src = getSelectedSimulation().getRocket().copyWithOriginalID();
		Rocket dest = baseDocument.getRocket();
		try {
			baseDocument.startUndo(trans.get("undoText"));
			dest.freeze();
			
			// Remove all children
			while (dest.getChildCount() > 0) {
				dest.removeChild(0);
			}
			
			// Move all children to the destination rocket
			while (src.getChildCount() > 0) {
				RocketComponent c = src.getChild(0);
				src.removeChild(0);
				dest.addChild(c);
			}
			
		} finally {
			dest.thaw();
			baseDocument.stopUndo();
		}
	}
	
	private void resetDesign() {
		clearHistory();
		
		documentCopy = baseDocument.copy();
		
		loadOptimizationParameters();
		loadSimulationModifiers();
		
		// Replace selected modifiers with corresponding new modifiers
		List<SimulationModifier> newSelected = new ArrayList<SimulationModifier>();
		for (SimulationModifier original : selectedModifiers) {
			List<SimulationModifier> newModifiers = simulationModifiers.get(original.getRelatedObject());
			if (newModifiers != null) {
				int index = newModifiers.indexOf(original);
				if (index >= 0) {
					SimulationModifier updated = newModifiers.get(index);
					updated.setMinValue(original.getMinValue());
					updated.setMaxValue(original.getMaxValue());
					newSelected.add(updated);
				}
			}
		}
		selectedModifiers.clear();
		selectedModifiers.addAll(newSelected);
		selectedModifierTableModel.fireTableDataChanged();
		
		// Update the available modifier tree
		availableModifierTree.populateTree(documentCopy.getRocket(), simulationModifiers);
		availableModifierTree.expandComponents();
		
		// Update selectable simulations
		populateSimulations();
		
		// Update selectable parameters
		populateParameters();
		
	}
	
	private void populateSimulations() {
		String current = null;
		Object selection = simulationSelectionCombo.getSelectedItem();
		if (selection != null) {
			current = selection.toString();
		}
		
		List<Named<Simulation>> simulations = new ArrayList<Named<Simulation>>();
		Rocket rocket = documentCopy.getRocket();
		
		for (Simulation s : documentCopy.getSimulations()) {
			String id = s.getConfiguration().getFlightConfigurationID();
			String name = createSimulationName(s.getName(), descriptor.format(rocket, id));
			simulations.add(new Named<Simulation>(s, name));
		}
		
		for (String id : rocket.getFlightConfigurationIDs()) {
			if (id == null) {
				continue;
			}
			Simulation sim = new Simulation(rocket);
			sim.getConfiguration().setFlightConfigurationID(id);
			String name = createSimulationName(trans.get("basicSimulationName"), descriptor.format(rocket, id));
			simulations.add(new Named<Simulation>(sim, name));
		}
		
		Simulation sim = new Simulation(rocket);
		sim.getConfiguration().setFlightConfigurationID(null);
		String name = createSimulationName(trans.get("noSimulationName"), descriptor.format(rocket, null));
		simulations.add(new Named<Simulation>(sim, name));
		
		
		simulationSelectionCombo.setModel(new DefaultComboBoxModel(simulations.toArray()));
		simulationSelectionCombo.setSelectedIndex(0);
		if (current != null) {
			for (int i = 0; i < simulations.size(); i++) {
				if (simulations.get(i).toString().equals(current)) {
					simulationSelectionCombo.setSelectedIndex(i);
					break;
				}
			}
		}
	}
	
	private void populateParameters() {
		String current = null;
		Object selection = optimizationParameterCombo.getSelectedItem();
		if (selection != null) {
			current = selection.toString();
		} else {
			// Default to apogee altitude event if it is not the first one in the list
			current = trans.get("MaximumAltitudeParameter.name");
		}
		
		List<Named<OptimizableParameter>> parameters = new ArrayList<Named<OptimizableParameter>>();
		for (OptimizableParameter p : optimizationParameters) {
			parameters.add(new Named<OptimizableParameter>(p, p.getName()));
		}
		
		optimizationParameterCombo.setModel(new DefaultComboBoxModel(parameters.toArray()));
		
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).toString().equals(current)) {
				optimizationParameterCombo.setSelectedIndex(i);
				break;
			}
		}
	}
	
	private void updateCounters() {
		bestValueLabel.setText(bestValueUnit.toStringUnit(bestValue));
		stepCountLabel.setText("" + stepCount);
		evaluationCountLabel.setText("" + evaluationCount);
		stepSizeLabel.setText(UnitGroup.UNITS_RELATIVE.toStringUnit(stepSize));
	}
	
	private void loadOptimizationParameters() {
		optimizationParameters.clear();
		optimizationParameters.addAll(OptimizationServiceHelper.getOptimizableParameters(documentCopy));
		
		if (optimizationParameters.isEmpty()) {
			throw new BugException("No rocket optimization parameters found, distribution built wrong.");
		}
		
		Collections.sort(optimizationParameters, new Comparator<OptimizableParameter>() {
			@Override
			public int compare(OptimizableParameter o1, OptimizableParameter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	private void loadSimulationModifiers() {
		simulationModifiers.clear();
		
		for (SimulationModifier m : OptimizationServiceHelper.getSimulationModifiers(documentCopy)) {
			Object key = m.getRelatedObject();
			List<SimulationModifier> list = simulationModifiers.get(key);
			if (list == null) {
				list = new ArrayList<SimulationModifier>();
				simulationModifiers.put(key, list);
			}
			list.add(m);
		}
		
		for (Object key : simulationModifiers.keySet()) {
			List<SimulationModifier> list = simulationModifiers.get(key);
			Collections.sort(list, new Comparator<SimulationModifier>() {
				@Override
				public int compare(SimulationModifier o1, SimulationModifier o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
	}
	
	private void addModifier(SimulationModifier mod) {
		if (!selectedModifiers.contains(mod)) {
			log.info(Markers.USER_MARKER, "Adding simulation modifier " + mod);
			selectedModifiers.add(mod);
			selectedModifierTableModel.fireTableDataChanged();
			availableModifierTree.repaint();
		} else {
			log.info(Markers.USER_MARKER, "Attempting to add an already existing simulation modifier " + mod);
		}
	}
	
	private void removeModifier(SimulationModifier mod) {
		log.info(Markers.USER_MARKER, "Removing simulation modifier " + mod);
		selectedModifiers.remove(mod);
		selectedModifierTableModel.fireTableDataChanged();
		availableModifierTree.repaint();
	}
	
	/**
	 * Update the enabled status of all components in the dialog.
	 */
	private void updateComponents() {
		boolean state;
		
		if (updating) {
			log.debug("Ignoring updateComponents");
			return;
		}
		
		log.debug("Running updateComponents()");
		
		updating = true;
		
		// First enable all components if optimization not running
		if (!running) {
			log.debug("Initially enabling all components");
			for (JComponent c : disableComponents) {
				c.setEnabled(true);
			}
		}
		
		// "Add" button
		SimulationModifier mod = getSelectedAvailableModifier();
		state = (mod != null && !selectedModifiers.contains(mod));
		log.debug("addButton enabled: " + state);
		addButton.setEnabled(state);
		
		// "Remove" button
		state = (selectedModifierTable.getSelectedRow() >= 0);
		log.debug("removeButton enabled: " + state);
		removeButton.setEnabled(state);
		
		// "Remove all" button
		state = (!selectedModifiers.isEmpty());
		log.debug("removeAllButton enabled: " + state);
		removeAllButton.setEnabled(state);
		
		// Optimization goal
		String selected = (String) optimizationGoalCombo.getSelectedItem();
		state = GOAL_SEEK.equals(selected);
		log.debug("optimizationGoalSpinner & UnitSelector enabled: " + state);
		optimizationGoalSpinner.setVisible(state);
		optimizationGoalUnitSelector.setVisible(state);
		
		// Minimum/maximum stability options
		state = minimumStabilitySelected.isSelected();
		log.debug("minimumStabilitySpinner & UnitSelector enabled: " + state);
		minimumStabilitySpinner.setEnabled(state);
		minimumStabilityUnitSelector.setEnabled(state);
		
		state = maximumStabilitySelected.isSelected();
		log.debug("maximumStabilitySpimmer & UnitSelector enabled: " + state);
		maximumStabilitySpinner.setEnabled(state);
		maximumStabilityUnitSelector.setEnabled(state);
		
		// Plot button (enabled if path exists and dimensionality is 1 or 2)
		state = (!optimizationPath.isEmpty() && (selectedModifiers.size() == 1 || selectedModifiers.size() == 2));
		log.debug("plotButton enabled: " + state + " optimizationPath.isEmpty=" + optimizationPath.isEmpty() +
				" selectedModifiers.size=" + selectedModifiers.size());
		plotButton.setEnabled(state);
		
		// Save button (enabled if path exists)
		state = (!evaluationHistory.isEmpty());
		log.debug("saveButton enabled: " + state);
		saveButton.setEnabled(state);
		
		// Last disable all components if optimization is running
		if (running) {
			log.debug("Disabling all components because optimization is running");
			for (JComponent c : disableComponents) {
				c.setEnabled(false);
			}
		}
		
		// Update description text
		mod = getSelectedModifier();
		if (mod != null) {
			selectedModifierDescription.setText(mod.getDescription());
		} else {
			selectedModifierDescription.setText("");
		}
		
		// Update the figure
		figure.setConfiguration(getSelectedSimulation().getConfiguration());
		
		updating = false;
	}
	
	private void savePath() {
		
		if (evaluationHistory.isEmpty()) {
			throw new BugException("evaluation history is empty");
		}
		
		CsvOptionPanel csvOptions = new CsvOptionPanel(GeneralOptimizationDialog.class,
				trans.get("export.header"), trans.get("export.header.ttip"));
		
		
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(FileHelper.CSV_FILE_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setAccessory(csvOptions);
		
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File file = chooser.getSelectedFile();
		if (file == null)
			return;
		
		file = FileHelper.ensureExtension(file, "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return;
		}
		
		String fieldSeparator = csvOptions.getFieldSeparator();
		String commentCharacter = csvOptions.getCommentCharacter();
		boolean includeHeader = csvOptions.getSelectionOption(0);
		csvOptions.storePreferences();
		
		log.info("Saving optimization path to " + file + ", fieldSeparator=" + fieldSeparator +
				", commentCharacter=" + commentCharacter + ", includeHeader=" + includeHeader);
		
		try {
			Writer writer = new BufferedWriter(new FileWriter(file));
			
			// Write header
			if (includeHeader) {
				FunctionEvaluationData data = evaluationHistory.values().iterator().next();
				
				writer.write(commentCharacter);
				for (SimulationModifier mod : selectedModifiers) {
					writer.write(mod.getRelatedObject().toString() + ": " + mod.getName() + " / " +
							mod.getUnitGroup().getDefaultUnit().getUnit());
					writer.write(fieldSeparator);
				}
				if (minimumStabilitySelected.isSelected() || maximumStabilitySelected.isSelected()) {
					writer.write(trans.get("export.stability") + " / " + data.getDomainReference().getUnit().getUnit());
					writer.write(fieldSeparator);
				}
				writer.write(getSelectedParameter().getName() + " / " +
						getSelectedParameter().getUnitGroup().getDefaultUnit().getUnit());
				
				writer.write("\n");
			}
			
			for (FunctionEvaluationData data : evaluationHistory.values()) {
				Value[] state = data.getState();
				
				for (int i = 0; i < state.length; i++) {
					writer.write(TextUtil.doubleToString(state[i].getUnitValue()));
					writer.write(fieldSeparator);
				}
				
				if (minimumStabilitySelected.isSelected() || maximumStabilitySelected.isSelected()) {
					writer.write(TextUtil.doubleToString(data.getDomainReference().getUnitValue()));
					writer.write(fieldSeparator);
				}
				
				if (data.getParameterValue() != null) {
					writer.write(TextUtil.doubleToString(data.getParameterValue().getUnitValue()));
				} else {
					writer.write("N/A");
				}
				writer.write("\n");
			}
			
			writer.close();
			log.info("File successfully saved");
			
		} catch (IOException e) {
			FileHelper.errorWriting(e, this);
		}
		
	}
	
	/**
	 * Return the currently selected available simulation modifier from the modifier tree,
	 * or <code>null</code> if none selected.
	 */
	private SimulationModifier getSelectedAvailableModifier() {
		TreePath treepath = availableModifierTree.getSelectionPath();
		if (treepath != null) {
			Object o = ((DefaultMutableTreeNode) treepath.getLastPathComponent()).getUserObject();
			if (o instanceof SimulationModifier) {
				return (SimulationModifier) o;
			}
		}
		return null;
	}
	
	/**
	 * Return the currently selected simulation.
	 * @return the selected simulation.
	 */
	@SuppressWarnings("unchecked")
	private Simulation getSelectedSimulation() {
		/* This is to debug a NPE where the returned selected item is null. */
		Object item = simulationSelectionCombo.getSelectedItem();
		if (item == null) {
			String s = "Selected simulation is null:";
			s = s + " item count=" + simulationSelectionCombo.getItemCount();
			for (int i = 0; i < simulationSelectionCombo.getItemCount(); i++) {
				s = s + " [" + i + "]=" + simulationSelectionCombo.getItemAt(i);
			}
			throw new BugException(s);
		}
		return ((Named<Simulation>) item).get();
	}
	
	/**
	 * Return the currently selected simulation modifier from the table,
	 * or <code>null</code> if none selected.
	 * @return the selected modifier or <code>null</code>.
	 */
	private SimulationModifier getSelectedModifier() {
		int row = selectedModifierTable.getSelectedRow();
		if (row < 0) {
			return null;
		}
		row = selectedModifierTable.convertRowIndexToModel(row);
		return selectedModifiers.get(row);
	}
	
	/**
	 * Return the currently selected optimization parameter.
	 * @return the selected optimization parameter.
	 */
	@SuppressWarnings("unchecked")
	private OptimizableParameter getSelectedParameter() {
		return ((Named<OptimizableParameter>) optimizationParameterCombo.getSelectedItem()).get();
	}
	
	private Unit getModifierUnit(int index) {
		return selectedModifiers.get(index).getUnitGroup().getDefaultUnit();
	}
	
	private String createSimulationName(String simulationName, String motorConfiguration) {
		String name;
		boolean hasParenthesis = motorConfiguration.matches("^[\\[\\(].*[\\]\\)]$");
		name = simulationName + " ";
		if (!hasParenthesis) {
			name += "(";
		}
		name += motorConfiguration;
		if (!hasParenthesis) {
			name += ")";
		}
		return name;
	}
	
	/**
	 * The table model for the parameter selection.
	 * 
	 * [Body tube: Length] [min] [max] [unit]
	 */
	private class ParameterSelectionTableModel extends AbstractTableModel {
		
		private static final int PARAMETER = 0;
		private static final int CURRENT = 1;
		private static final int MIN = 2;
		private static final int MAX = 3;
		private static final int COUNT = 4;
		
		@Override
		public int getColumnCount() {
			return COUNT;
		}
		
		@Override
		public int getRowCount() {
			return selectedModifiers.size();
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case PARAMETER:
				return trans.get("table.col.parameter");
			case CURRENT:
				return trans.get("table.col.current");
			case MIN:
				return trans.get("table.col.min");
			case MAX:
				return trans.get("table.col.max");
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case PARAMETER:
				return String.class;
			case CURRENT:
				return Double.class;
			case MIN:
				return Double.class;
			case MAX:
				return Double.class;
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			
			SimulationModifier modifier = selectedModifiers.get(row);
			
			switch (column) {
			case PARAMETER:
				return modifier.getRelatedObject().toString() + ": " + modifier.getName();
			case CURRENT:
				try {
					return getModifierUnit(row).toUnit(modifier.getCurrentSIValue(getSelectedSimulation()));
				} catch (OptimizationException e) {
					throw new BugException("Could not read current SI value from modifier " + modifier, e);
				}
			case MIN:
				return getModifierUnit(row).toUnit(modifier.getMinValue());
			case MAX:
				return getModifierUnit(row).toUnit(modifier.getMaxValue());
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			
			if (row >= selectedModifiers.size()) {
				throw new BugException("setValueAt with invalid row:  value=" + value + " row=" + row + " column=" + column +
						" selectedModifiers.size=" + selectedModifiers.size() + " selectedModifiers=" + selectedModifiers +
						" selectedModifierTable.getRowCount=" + selectedModifierTable.getRowCount());
			}
			
			switch (column) {
			case PARAMETER:
				break;
			
			case MIN:
				double min = (Double) value;
				min = getModifierUnit(row).fromUnit(min);
				selectedModifiers.get(row).setMinValue(min);
				break;
			
			case CURRENT:
				break;
			
			case MAX:
				double max = (Double) value;
				max = getModifierUnit(row).fromUnit(max);
				selectedModifiers.get(row).setMaxValue(max);
				break;
			
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
			this.fireTableRowsUpdated(row, row);
			
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
			case PARAMETER:
				return false;
			case CURRENT:
				return false;
			case MIN:
				return true;
			case MAX:
				return true;
			default:
				throw new IndexOutOfBoundsException("column=" + column);
			}
		}
		
	}
	
	private class DoubleCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			double val = (Double) value;
			Unit unit = getModifierUnit(row);
			
			val = unit.fromUnit(val);
			this.setText(unit.toStringUnit(val));
			
			return this;
		}
	}
	
	
	private static class SimulationModifierComparator implements Comparator<SimulationModifier> {
		
		@Override
		public int compare(SimulationModifier mod1, SimulationModifier mod2) {
			Object rel1 = mod1.getRelatedObject();
			Object rel2 = mod2.getRelatedObject();
			
			/*
			 * Primarily order by related object:
			 * 
			 * - RocketComponents first
			 * - Two RocketComponents are ordered based on their position in the rocket
			 */
			if (!rel1.equals(rel2)) {
				
				if (rel1 instanceof RocketComponent) {
					if (rel2 instanceof RocketComponent) {
						
						RocketComponent root = ((RocketComponent) rel1).getRoot();
						for (RocketComponent c : root) {
							if (c.equals(rel1)) {
								return -1;
							}
							if (c.equals(rel2)) {
								return 1;
							}
						}
						
						throw new BugException("Error sorting modifiers, mod1=" + mod1 + " rel1=" + rel1 +
								" mod2=" + mod2 + " rel2=" + rel2);
						
					} else {
						return -1;
					}
				} else {
					if (rel2 instanceof RocketComponent) {
						return 1;
					}
				}
				
			}
			
			// Secondarily sort by name
			return collator.compare(mod1.getName(), mod2.getName());
		}
	}
	
}
