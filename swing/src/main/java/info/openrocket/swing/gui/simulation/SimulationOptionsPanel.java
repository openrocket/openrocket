package info.openrocket.swing.gui.simulation;

import static info.openrocket.core.util.StringUtils.escapeHtml;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import info.openrocket.core.rocketcomponent.RocketComponent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.View;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.gravity.GravityModelType;
import info.openrocket.core.simulation.RK4SimulationStepper;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.simulation.extension.SimulationExtensionProvider;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.GeodeticComputationStrategy;

import info.openrocket.core.simulation.SimulationStepperMethod;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.FlatLafOutlines;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.simulation.extension.SwingSimulationExtensionConfigurator;

import com.google.inject.Key;

class SimulationOptionsPanel extends JPanel {

	private static final long serialVersionUID = -5251458539346201239L;

	private static final Translator trans = Application.getTranslator();
	private static final String PANEL_LAYOUT = "fillx, gap rel rel";
	private static final String OPTIONS_SECTION_LAYOUT = "fillx, gap rel rel";
	private static final int EXTENSION_DESCRIPTION_ROWS = 3;
	private static final int EXTENSION_DESCRIPTION_MIN_HEIGHT = 60;
	private static final int EXTENSION_LIST_HEIGHT = 140;

	private OpenRocketDocument document;
	final Simulation simulation;
	private final SimulationOptions options;
	
	private JLabel aerodynamicLookupSummaryIconLabel;
	
	private JPanel currentExtensions;
	final JPopupMenu extensionMenu;
	JMenu extensionMenuCopyExtension;

	private JSpinner gravitySpinner;
	private UnitSelector gravityUnit;
	private BasicSlider gravitySlider;
	private JLabel gravityLabel;
	private final JCheckBox fixedRandomSeedCheckBox;
	private final JTextField randomSeedField;
	private final FlatLafOutlines.Validator randomSeedValidator;
	private boolean updatingRandomSeedControls;

	private static Color textColor;
	private static Color dimTextColor;
	private static Color infoTextColor;

	static {
		initColors();
	}

	SimulationOptionsPanel(OpenRocketDocument document, final Simulation simulation) {
		super(new MigLayout(PANEL_LAYOUT));
		this.document = document;
		this.simulation = simulation;
		this.options = simulation.getOptions();

		final SimulationOptions conditions = this.options;

		JPanel sub, subsub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;

		// // Simulation options
		sub = new JPanel(new MigLayout(OPTIONS_SECTION_LAYOUT));
		// // Simulator options
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.border.Simopt")));
		this.add(sub, "growx, growy, aligny 0");

		// Separate panel for computation methods, as they use a different
		// layout
		subsub = new JPanel(new MigLayout("fillx, ins 0, gap rel rel", "[grow][min!][]"));
		
		// // Calculation method:
		tip = trans.get("simedtdlg.lbl.ttip.Calcmethod");
		label = new JLabel(trans.get("simedtdlg.lbl.Calcmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		// // Extended Barrowman
		label = new JLabel(trans.get("simedtdlg.lbl.ExtBarrowman"));
		label.setToolTipText(tip);
		subsub.add(label, "growx, spanx, wrap");
		
		// Simulation method
		tip = trans.get("simedtdlg.lbl.ttip.Simmethod1")
				+ trans.get("simedtdlg.lbl.ttip.Simmethod2");
		label = new JLabel(trans.get("simedtdlg.lbl.Simmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");


		EnumModel<SimulationStepperMethod> simulationStepperMethodChoice = new EnumModel<>(
				conditions, "SimulationStepperMethodChoice");
		final JComboBox<SimulationStepperMethod> SimulationStepperMethodChoiceCombo = new JComboBox<>(simulationStepperMethodChoice);
		ActionListener SimulationStepperMethodChoiceComboTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationStepperMethod SimulationStepperMethodChoiceSelected = (SimulationStepperMethod) SimulationStepperMethodChoiceCombo
						.getSelectedItem();
				SimulationStepperMethodChoiceCombo.setToolTipText(SimulationStepperMethodChoiceSelected.getDescription());
			}
		};
		SimulationStepperMethodChoiceCombo.addActionListener(SimulationStepperMethodChoiceComboTTipListener);
		SimulationStepperMethodChoiceComboTTipListener.actionPerformed(null);
		subsub.add(SimulationStepperMethodChoiceCombo, "span 3, wrap");

		// Aerodynamic data
		label = new JLabel(trans.get("AerodynamicLookupDialog.lbl.summary"));
		subsub.add(label);

		/// Configure
		JButton configureLookupButton = new JButton(trans.get("AerodynamicLookupDialog.btn.configure"));
		configureLookupButton.addActionListener(e -> openLookupDialog());
		subsub.add(configureLookupButton);

		aerodynamicLookupSummaryIconLabel = new JLabel(Icons.HELP);
		aerodynamicLookupSummaryIconLabel.setToolTipText(buildLookupSummaryTooltip());
		configureImmediateTooltipDelay(aerodynamicLookupSummaryIconLabel);
		subsub.add(aerodynamicLookupSummaryIconLabel, "gapleft rel, wrap para");

		sub.add(subsub, "spanx, growx, wmin 0, wrap para");

		/*label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		subsub.add(label, "growx, span 3, wrap");*/

		// // Geodetic calculation method:
		label = new JLabel(trans.get("simedtdlg.lbl.GeodeticMethod"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GeodeticMethodTip"));
		subsub.add(label, "gapright para");

		EnumModel<GeodeticComputationStrategy> gcsModel = new EnumModel<>(
				conditions, "GeodeticComputation");
		final JComboBox<GeodeticComputationStrategy> gcsCombo = new JComboBox<>(gcsModel);
		ActionListener gcsTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) gcsCombo
						.getSelectedItem();
				gcsCombo.setToolTipText(gcs.getDescription());
			}
		};
		gcsCombo.addActionListener(gcsTTipListener);
		gcsTTipListener.actionPerformed(null);
		subsub.add(gcsCombo, "spanx, wrap");
		
		// // Gravity model:
		label = new JLabel(trans.get("simedtdlg.lbl.GravityModel"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GravityModel"));
		subsub.add(label, "gapright para");

		EnumModel<GravityModelType> gravityModelTypeModel = new EnumModel<>(
				conditions, "GravityModelType");
		final JComboBox<GravityModelType> gravityModelCombo = new JComboBox<>(gravityModelTypeModel);

		// Update tooltip based on selected gravity model type
		ActionListener gravityModelTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravityModelType selectedType = (GravityModelType) gravityModelCombo.getSelectedItem();
				if (selectedType == GravityModelType.WGS) {
					gravityModelCombo.setToolTipText(trans.get("simedtdlg.GravityModel.WGS84.ttip"));
				} else if (selectedType == GravityModelType.CONSTANT) {
					gravityModelCombo.setToolTipText(trans.get("simedtdlg.GravityModel.Constant.ttip"));
				}
			}
		};
		gravityModelCombo.addActionListener(gravityModelTTipListener);
		gravityModelTTipListener.actionPerformed(null);
		subsub.add(gravityModelCombo, "spanx, wrap");
		
		// // Constant gravity value:
		gravityLabel = new JLabel(trans.get("simedtdlg.lbl.GravityValue"));
		tip = trans.get("simedtdlg.lbl.ttip.GravityValue");
		gravityLabel.setToolTipText(tip);
		subsub.add(gravityLabel, "gapright para, hidemode 3");

		m = new DoubleModel(conditions, "ConstantGravity", UnitGroup.UNITS_ACCELERATION, 0);

		gravitySpinner = new JSpinner(m.getSpinnerModel());
		gravitySpinner.setEditor(new SpinnerEditor(gravitySpinner));
		gravitySpinner.setToolTipText(tip);
		subsub.add(gravitySpinner, "hidemode 3");

		gravityUnit = new UnitSelector(m);
		gravityUnit.setToolTipText(tip);
		subsub.add(gravityUnit, "hidemode 3");
		gravitySlider = new BasicSlider(m.getSliderModel(0, 20));
		gravitySlider.setToolTipText(tip);
		subsub.add(gravitySlider, "w 90lp, hidemode 3, wrap");
		
		// Update visibility of constant gravity components based on selected model
		ActionListener gravityModelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravityModelType selectedType = (GravityModelType) gravityModelCombo.getSelectedItem();
				boolean isConstant = selectedType == GravityModelType.CONSTANT;
				gravityLabel.setVisible(isConstant);
				gravitySpinner.setVisible(isConstant);
				gravityUnit.setVisible(isConstant);
				gravitySlider.setVisible(isConstant);
				subsub.revalidate();
				subsub.repaint();
			}
		};
		gravityModelCombo.addActionListener(gravityModelListener);
		gravityModelListener.actionPerformed(null); // Initialize visibility


		// // Time step:
		label = new JLabel(trans.get("simedtdlg.lbl.Timestep"));
		tip = trans.get("simedtdlg.lbl.ttip.Timestep1")
				+ trans.get("simedtdlg.lbl.ttip.Timestep2")
				+ " "
				+ UnitGroup.UNITS_TIME_STEP
				.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ".";
		label.setToolTipText(tip);
		subsub.add(label, "gaptop para, gapright para");

		m = new DoubleModel(conditions, "TimeStep", UnitGroup.UNITS_TIME_STEP,
				0.01, 1);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		subsub.add(spin, "split 2");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		subsub.add(unit, "");
		slider = new BasicSlider(m.getSliderModel(0.01, 0.2));
		slider.setToolTipText(tip);
		subsub.add(slider, "w 100, wrap");

		// // Maximum simulation time:
		label = new JLabel(trans.get("simedtdlg.lbl.MaxSimTime"));
		tip = trans.get("simedtdlg.lbl.ttip.MaxSimTime");
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		m = new DoubleModel(conditions, "MaxSimulationTime",
				UnitGroup.UNITS_LONG_TIME, 1);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		subsub.add(spin, "split 2");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		subsub.add(unit, "wrap");

		// // Fixed random seed:
		tip = trans.get("simedtdlg.checkbox.ttip.FixedRandomSeed");
		fixedRandomSeedCheckBox = new JCheckBox(trans.get("simedtdlg.checkbox.FixedRandomSeed"),
				conditions.isRandomSeedFixed());
		fixedRandomSeedCheckBox.setToolTipText(tip);
		subsub.add(fixedRandomSeedCheckBox, "gaptop para, gapright para");

		randomSeedField = new JTextField(12);
		randomSeedField.setToolTipText(trans.get("simedtdlg.lbl.ttip.RandomSeed"));
		updateRandomSeedControlsFromOptions();
		subsub.add(randomSeedField, "w 130lp!, spanx, wrap");

		randomSeedValidator = FlatLafOutlines.validator(randomSeedField)
				.errorIf(() -> fixedRandomSeedCheckBox.isSelected()
						&& parseRandomSeed(randomSeedField.getText()) == null,
						() -> trans.get("simedtdlg.error.RandomSeed"))
				.showMessagePopup(2500);
		randomSeedValidator.update();

		fixedRandomSeedCheckBox.addActionListener(e -> {
			if (updatingRandomSeedControls) {
				return;
			}

			updatingRandomSeedControls = true;
			if (fixedRandomSeedCheckBox.isSelected()) {
				// Start each newly enabled fixed-seed session with a usable value that the user can override.
				conditions.randomizeSeed();
				randomSeedField.setText(Integer.toString(conditions.getRandomSeed()));
				randomSeedField.setEnabled(true);
				conditions.setRandomSeedFixed(true);
			} else {
				conditions.setRandomSeedFixed(false);
				randomSeedField.setText("");
				randomSeedField.setEnabled(false);
			}
			updatingRandomSeedControls = false;
			randomSeedValidator.update();
		});

		randomSeedField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				updateRandomSeedFromField();
				randomSeedValidator.update();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				updateRandomSeedFromField();
				randomSeedValidator.update();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				updateRandomSeedFromField();
				randomSeedValidator.update();
			}
		});
		randomSeedField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				if (!event.isTemporary()) {
					replaceInvalidRandomSeed();
				}
			}
		});

		// Reset to default button
		JButton resetBtn = new JButton(trans.get("simedtdlg.but.resettodefault"));
		// Reset the time step to its default value (
		resetBtn.setToolTipText(trans.get("simedtdlg.but.ttip.resettodefault")
				+ UnitGroup.UNITS_SHORT_TIME
				.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ").");
		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ApplicationPreferences preferences = Application.getPreferences();
				conditions.setTimeStep(preferences.getDouble(
						ApplicationPreferences.SIMULATION_TIME_STEP,
						RK4SimulationStepper.RECOMMENDED_TIME_STEP));
				conditions.setMaxSimulationTime(preferences.getDouble(
						ApplicationPreferences.SIMULATION_MAX_TIME,
						RK4SimulationStepper.RECOMMENDED_MAX_TIME));
				conditions.setGeodeticComputation(preferences.getEnum(
						ApplicationPreferences.GEODETIC_COMPUTATION,
						GeodeticComputationStrategy.SPHERICAL));
				conditions.setRecoverySpeedWarning(preferences.getRecoverySpeedWarning());
				//conditions.setDrogueLowSpeedWarning(preferences.getDrogueLowSpeedWarning());
				conditions.setRecoveryDrogueMainHighSpeedWarning(preferences.getRecoveryDrogueMainHighSpeedWarning());
				conditions.setRecoveryDrogueMainLowSpeedWarning(preferences.getRecoveryDrogueMainLowSpeedWarning());
				if (preferences.isRandomSeedFixed()) {
					conditions.setRandomSeed(preferences.getRandomSeed());
					conditions.setRandomSeedFixed(true);
				} else {
					conditions.setRandomSeedFixed(false);
				}
				updateRandomSeedControlsFromOptions();
				randomSeedValidator.update();
			}
		});

		// Save as default button
		JButton saveBtn = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ApplicationPreferences preferences = Application.getPreferences();
				preferences.setTimeStep(conditions.getTimeStep());
				preferences.setMaxSimulationTime(conditions.getMaxSimulationTime());
				preferences.setGeodeticComputation(conditions.getGeodeticComputation());
				preferences.setRecoverySpeedWarning(conditions.getRecoverySpeedWarning());
				//preferences.setDrogueLowSpeedWarning(conditions.getDrogueLowSpeedWarning());
				preferences.setRecoveryDrogueMainHighSpeedWarning(conditions.getRecoveryDrogueMainHighSpeedWarning());
				preferences.setRecoveryDrogueMainLowSpeedWarning(conditions.getRecoveryDrogueMainLowSpeedWarning());
				prepareForSimulation();
				if (conditions.isRandomSeedFixed()) {
					preferences.setRandomSeed(conditions.getRandomSeed());
				}
				preferences.setRandomSeedFixed(conditions.isRandomSeedFixed());
			}
		});

		// Recovery speed warnings - tabbed by deployment type
		JTabbedPane recoveryTabs = new JTabbedPane();

		// --- Single deployment tab ---
		JPanel singleTab = new JPanel(new MigLayout("insets n, fillx", "[][min!][min!][grow]"));
		singleTab.add(createWrappingInfoText(trans.get("simedtdlg.lbl.SingleDeployment.desc"), Style.ITALIC), "spanx, growx, wmin 0, wrap para");
		label = new JLabel(trans.get("simedtdlg.lbl.RecoverySpeedWarning"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.RecoverySpeedWarning"));
		singleTab.add(label, "gapright para");
		m = new DoubleModel(conditions, "RecoverySpeedWarning", UnitGroup.UNITS_VELOCITY, 0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(trans.get("simedtdlg.lbl.ttip.RecoverySpeedWarning"));
		singleTab.add(spin, "");
		singleTab.add(new UnitSelector(m), "wrap");
		recoveryTabs.addTab(trans.get("simedtdlg.border.SingleDeployment"), singleTab);

		// --- Dual deployment tab ---
		JPanel dualTab = new JPanel(new MigLayout("insets n, fillx", "[][min!][min!][grow]"));
		dualTab.add(createWrappingInfoText(trans.get("simedtdlg.lbl.DualDeployment.desc2"), Style.ITALIC), "spanx, growx, wmin 0, wrap para");

		dualTab.add(createWrappingInfoText(trans.get("simedtdlg.lbl.DualDeployment.HowTo"), Style.PLAIN), "spanx, growx, wmin 0, wrap para");

		label = new JLabel(trans.get("simedtdlg.lbl.MainLowSpeedWarning"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.MainLowSpeedWarning"));
		dualTab.add(label, "gapright para");
		m = new DoubleModel(conditions, "RecoveryDrogueMainLowSpeedWarning", UnitGroup.UNITS_VELOCITY, 0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(trans.get("simedtdlg.lbl.ttip.MainLowSpeedWarning"));
		dualTab.add(spin, "");
		dualTab.add(new UnitSelector(m), "wrap");

		label = new JLabel(trans.get("simedtdlg.lbl.MainHighSpeedWarning"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.MainHighSpeedWarning"));
		dualTab.add(label, "gapright para");
		m = new DoubleModel(conditions, "RecoveryDrogueMainHighSpeedWarning", UnitGroup.UNITS_VELOCITY, 0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(trans.get("simedtdlg.lbl.ttip.MainHighSpeedWarning"));
		dualTab.add(spin, "");
		dualTab.add(new UnitSelector(m), "wrap");

		recoveryTabs.addTab(trans.get("simedtdlg.border.DualDeployment"), dualTab);
		subsub.add(recoveryTabs, "spanx, gaptop para, growx, wmin 0, wrap");

		sub.add(resetBtn, "align left, split 2");
		sub.add(saveBtn, "wrap");
		
		
		
		//// Simulation extensions
		sub = new JPanel(new MigLayout("fillx, gap 0 0"));
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.SimExt")));
		this.add(sub, "wmin 300lp, growx, growy");
		
		
		DescriptionArea desc = new DescriptionArea(EXTENSION_DESCRIPTION_ROWS);
		desc.setText(trans.get("simedtdlg.SimExt.desc"));
		sub.add(desc, "aligny 0, hmin " + EXTENSION_DESCRIPTION_MIN_HEIGHT + "lp, growx, wrap rel");
		
		
		final JButton addExtension = new JButton(trans.get("simedtdlg.SimExt.add"));
		extensionMenu = getExtensionMenu();
		addExtension.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				extensionMenu.show(addExtension, 5, addExtension.getBounds().height);
			}
		});
		sub.add(addExtension, "growx, wrap 0");

		currentExtensions = new JPanel(new MigLayout("fillx, gap 0 0, ins 0"));
		JScrollPane scroll = new JScrollPane(currentExtensions);
		scroll.setForeground(textColor);
		scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, EXTENSION_LIST_HEIGHT));
		scroll.setMinimumSize(new Dimension(0, 90));
		//  &#$%! scroll pane will not honor "growy"...
		sub.add(scroll, "growx");

		updateCurrentExtensions();

		options.addChangeListener(e -> SwingUtilities.invokeLater(this::updateLookupSummary));
		updateLookupSummary();

	}

	/**
	 * Resolves the seed controls before a simulation starts. An empty or invalid fixed seed falls back to the normal
	 * per-run random seed behavior.
	 */
	void prepareForSimulation() {
		boolean useFixedSeed = applyRandomSeedInput(options, fixedRandomSeedCheckBox.isSelected(),
				randomSeedField.getText());

		updatingRandomSeedControls = true;
		fixedRandomSeedCheckBox.setSelected(useFixedSeed);
		randomSeedField.setEnabled(useFixedSeed);
		if (!useFixedSeed) {
			randomSeedField.setText("");
		}
		updatingRandomSeedControls = false;
		randomSeedValidator.update();
	}

	/**
	 * Applies a final seed field value and reports whether fixed-seed mode remains enabled.
	 *
	 * @param options simulation options to update
	 * @param fixedSeedRequested whether the fixed-seed checkbox is selected
	 * @param seedText contents of the random seed field
	 * @return {@code true} if a valid fixed seed was applied
	 */
	static boolean applyRandomSeedInput(SimulationOptions options, boolean fixedSeedRequested, String seedText) {
		Integer seed = fixedSeedRequested ? parseRandomSeed(seedText) : null;
		if (seed == null) {
			options.setRandomSeedFixed(false);
			return false;
		}

		options.setRandomSeed(seed);
		options.setRandomSeedFixed(true);
		return true;
	}

	private void updateRandomSeedFromField() {
		if (updatingRandomSeedControls || !fixedRandomSeedCheckBox.isSelected()) {
			return;
		}
		Integer seed = parseRandomSeed(randomSeedField.getText());
		if (seed != null) {
			options.setRandomSeed(seed);
		}
	}

	/**
	 * Replaces an invalid committed edit with a fresh seed while preserving fixed-seed mode.
	 */
	private void replaceInvalidRandomSeed() {
		if (!fixedRandomSeedCheckBox.isSelected() || parseRandomSeed(randomSeedField.getText()) != null) {
			return;
		}

		options.randomizeSeed();
		updatingRandomSeedControls = true;
		randomSeedField.setText(Integer.toString(options.getRandomSeed()));
		updatingRandomSeedControls = false;
		randomSeedValidator.update();
	}

	private void updateRandomSeedControlsFromOptions() {
		updatingRandomSeedControls = true;
		fixedRandomSeedCheckBox.setSelected(options.isRandomSeedFixed());
		randomSeedField.setEnabled(options.isRandomSeedFixed());
		randomSeedField.setText(options.isRandomSeedFixed() ? Integer.toString(options.getRandomSeed()) : "");
		updatingRandomSeedControls = false;
	}

	private static Integer parseRandomSeed(String seedText) {
		try {
			return Integer.valueOf(seedText.trim());
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationOptionsPanel::updateColors);
	}

	public static void updateColors() {
		textColor = UITheme.getColor(UITheme.Keys.TEXT);
		dimTextColor = UITheme.getColor(UITheme.Keys.TEXT_DIM);
		infoTextColor = UITheme.getColor(UITheme.Keys.INFO);
	}

	private JPopupMenu getExtensionMenu() {
		Set<SimulationExtensionProvider> extensions = Application.getInjector().getInstance(new Key<>() {
		});

		JPopupMenu basemenu = new JPopupMenu();

		//// Use code / Launch conditions
		for (final SimulationExtensionProvider provider : extensions) {
			List<String> ids = provider.getIds();
			for (final String id : ids) {
				List<String> menuItems = provider.getName(id);
				if (menuItems != null) {
					JComponent menu = findMenu(basemenu, menuItems);
					JMenuItem item = new JMenuItem(menuItems.get(menuItems.size() - 1));
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							SimulationExtension e = provider.getInstance(id);
							simulation.getSimulationExtensions().add(e);
							updateCurrentExtensions();
							SwingSimulationExtensionConfigurator configurator = findConfigurator(e);
							if (configurator != null) {
								configurator.configure(e, simulation, SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
								updateCurrentExtensions();
							}
						}
					});
					menu.add(item);
				}
			}
		}

		//// Copy extension
		updateExtensionMenuCopyExtension(basemenu);

		return basemenu;
	}

	/**
	 * Updates the contents of the "Copy extension" menu item in the extension menu.
	 * @param extensionMenu extension menu to add the "Copy extension" menu item to
	 */
	private void updateExtensionMenuCopyExtension(JPopupMenu extensionMenu) {
		if (extensionMenu == null) {
			return;
		}
		if (this.extensionMenuCopyExtension != null) {
			extensionMenu.remove(this.extensionMenuCopyExtension);
		}

		this.extensionMenuCopyExtension = null;
		for (Simulation sim : document.getSimulations()) {
			if (sim.getSimulationExtensions().isEmpty()) {
				continue;
			}

			JMenu menu = new JMenu(sim.getName());
			for (final SimulationExtension ext : sim.getSimulationExtensions()) {
				JMenuItem item = new JMenuItem(ext.getName());
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						SimulationExtension e = ext.clone();
						simulation.getSimulationExtensions().add(e);
						updateCurrentExtensions();
						SwingSimulationExtensionConfigurator configurator = findConfigurator(e);
						if (configurator != null) {
							configurator.configure(e, simulation, SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
							updateCurrentExtensions();
						}
					}
				});
				menu.add(item);
			}

			if (this.extensionMenuCopyExtension == null) {
				this.extensionMenuCopyExtension = new JMenu(trans.get("simedtdlg.SimExt.copyExtension"));
			}
			this.extensionMenuCopyExtension.add(menu);
		}
		if (this.extensionMenuCopyExtension != null) {
			extensionMenu.add(this.extensionMenuCopyExtension);
		}
	}

	private JComponent findMenu(MenuElement menu, List<String> menuItems) {
		for (int i = 0; i < menuItems.size() - 1; i++) {
			String menuItem = menuItems.get(i);

			MenuElement found = null;
			for (MenuElement e : menu.getSubElements()) {
				if (e instanceof JMenu && ((JMenu) e).getText().equals(menuItem)) {
					found = e;
					break;
				}
			}

			if (found != null) {
				menu = found;
			} else {
				JMenu m = new JMenu(menuItem);
				((JComponent) menu).add(m);
				menu = m;
			}
		}
		return (JComponent) menu;
	}
	private void openLookupDialog() {
		AerodynamicLookupDialog dialog = new AerodynamicLookupDialog(
				SwingUtilities.windowForComponent(this),
				options);
		dialog.setVisible(true);
		updateLookupSummary();
	}

	private void updateLookupSummary() {
		if (aerodynamicLookupSummaryIconLabel == null) {
			return;
		}
		aerodynamicLookupSummaryIconLabel.setToolTipText(buildLookupSummaryTooltip());
	}

	private String buildLookupDetail(Path path, MachAoALookup table) {
		if (path == null || table == null) {
			return trans.get("AerodynamicLookupDialog.summary.none");
		}
		String fileName = path.getFileName() != null ? path.getFileName().toString() : path.toString();
		String detail = AerodynamicLookupDialog.formatLookupSummary(trans, table);
		return fileName + " - " + detail;
	}

	/**
	 * Build the tooltip content for the aerodynamic lookup info icon.
	 */
	private String buildLookupSummaryTooltip() {
		String dragDetail = buildLookupDetail(options.getDragLookupCsvPath(), options.getDragLookupTable());
		String stabilityDetail = buildLookupDetail(options.getStabilityLookupCsvPath(), options.getStabilityLookupTable());
		return "<html>"
				+ escapeHtml(String.format(trans.get("AerodynamicLookupDialog.lbl.summaryDrag"), dragDetail))
				+ "<br>"
				+ escapeHtml(String.format(trans.get("AerodynamicLookupDialog.lbl.summaryStability"), stabilityDetail))
				+ "</html>";
	}

	/**
	 * Make a tooltip appear immediately for a single component without changing the
	 * application's normal tooltip delay outside hover.
	 */
	private void configureImmediateTooltipDelay(JComponent component) {
		component.addMouseListener(new MouseAdapter() {
			private final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			private int previousInitialDelay = toolTipManager.getInitialDelay();

			@Override
			public void mouseEntered(MouseEvent e) {
				previousInitialDelay = toolTipManager.getInitialDelay();
				toolTipManager.setInitialDelay(0);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				toolTipManager.setInitialDelay(previousInitialDelay);
			}
		});
	}

	private void updateCurrentExtensions() {
		currentExtensions.removeAll();

		if (simulation.getSimulationExtensions().isEmpty()) {
			StyledLabel l = new StyledLabel(trans.get("simedtdlg.SimExt.noExtensions"), Style.ITALIC);
			l.setForeground(dimTextColor);
			currentExtensions.add(l, "growx, pad 5 5 5 5, wrap");
		} else {
			for (SimulationExtension e : simulation.getSimulationExtensions()) {
				currentExtensions.add(new SimulationExtensionPanel(e), "growx, wrap");
			}
		}

		updateExtensionMenuCopyExtension(this.extensionMenu);

		// Both needed:
		this.revalidate();
		this.repaint();
	}

	private JTextPane createWrappingInfoText(String text, Style style) {
		JTextPane pane = new JTextPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				// An HTML JTextPane reports a large "natural" preferred width (it prefers
				// to keep text on one line). If that width is allowed to influence the
				// enclosing layout, the panel - which lives inside a JScrollPane and is
				// therefore sized to its preferred width - keeps growing wider on every
				// re-layout (e.g. each recovery tab switch). Report only the minimum
				// wrap width so the pane never drives the container wider, and derive the
				// height from the width actually allocated so the text isn't clipped.
				View view = getUI().getRootView(this);
				int width = getWidth();
				if (width > 0) {
					view.setSize(width, Float.MAX_VALUE);
					d.height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
				}
				d.width = (int) Math.ceil(view.getMinimumSpan(View.X_AXIS));
				return d;
			}
		};
		pane.setEditable(false);
		pane.setFocusable(false);
		pane.setOpaque(false);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setContentType("text/html");
		pane.setMinimumSize(new Dimension(0, 0));

		String normalizedText = text == null ? "" : text;
		String trimmed = normalizedText.trim().toLowerCase();
		if (trimmed.startsWith("<html")) {
			pane.setText(normalizedText);
		} else {
			String styleCss = "";
			switch (style) {
				case ITALIC:
					styleCss = "font-style:italic;";
					break;
				case BOLD:
					styleCss = "font-weight:bold;";
					break;
				case BOLD_ITALIC:
					styleCss = "font-weight:bold;font-style:italic;";
					break;
				case PLAIN:
				default:
					break;
			}

			pane.setText("<html><body style='margin:0;" + styleCss + "'>"
					+ escapeHtml(normalizedText).replace("\n", "<br>")
					+ "</body></html>");
		}

		pane.setCaretPosition(0);
		return pane;
	}


	private class SimulationExtensionPanel extends JPanel {

		/**
		 *
		 */
		private static final long serialVersionUID = -3296795614810745035L;

		public SimulationExtensionPanel(final SimulationExtension extension) {
			super(new MigLayout("fillx, gapx 0"));

			this.setBorder(BorderFactory.createLineBorder(dimTextColor));
			this.add(new JLabel(extension.getName()), "spanx, growx, wrap");

			JButton button;

			this.add(new JPanel(), "spanx, split, growx, right");

			// Configure
			if (findConfigurator(extension) != null) {
				button = new JButton(Icons.CONFIGURE);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						findConfigurator(extension).configure(extension, simulation,
								SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
						updateCurrentExtensions();
					}
				});
				this.add(button, "right");
			}

			// Help
			if (extension.getDescription() != null) {
				button = new JButton(Icons.HELP);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final JDialog dialog = new JDialog(SwingUtilities.windowForComponent(SimulationOptionsPanel.this),
								extension.getName(), ModalityType.APPLICATION_MODAL);
						JPanel panel = new JPanel(new MigLayout("fill"));
						DescriptionArea area = new DescriptionArea(extension.getDescription(), 10, 0);
						panel.add(area, "width 400lp, wrap para");
						JButton close = new JButton(trans.get("button.close"));
						close.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								dialog.setVisible(false);
							}
						});
						panel.add(close, "right");
						dialog.add(panel);
						GUIUtil.setDisposableDialogOptions(dialog, close);
						dialog.setLocationRelativeTo(SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
						dialog.setVisible(true);
					}
				});
				this.add(button, "right");
			}

			// Delete
			button = new JButton(Icons.EDIT_DELETE);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Iterator<SimulationExtension> iter = simulation.getSimulationExtensions().iterator();
					while (iter.hasNext()) {
						// Compare with identity
						if (iter.next() == extension) {
							iter.remove();
							break;
						}
					}
					updateCurrentExtensions();
				}
			});
			this.add(button, "right");
			
		}
	}

	private SwingSimulationExtensionConfigurator findConfigurator(SimulationExtension extension) {
		Set<SwingSimulationExtensionConfigurator> configurators = Application.getInjector().getInstance(new Key<>() {
		});
		for (SwingSimulationExtensionConfigurator c : configurators) {
			if (c.support(extension)) {
				return c;
			}
		}
		return null;
	}

}
