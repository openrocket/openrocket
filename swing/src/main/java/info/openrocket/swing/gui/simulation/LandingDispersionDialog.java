package info.openrocket.swing.gui.simulation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.BallisticTrajectoryException;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.MonteCarloSimulationRunner;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;

import net.miginfocom.swing.MigLayout;

/**
 * Configures and runs a non-destructive Monte Carlo landing-dispersion analysis
 * for one simulation.
 */
public final class LandingDispersionDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final String CARD_SETUP = "setup";
	private static final String CARD_PROGRESS = "progress";

	private final Window owner;
	private final Simulation simulation;
	private final List<ParameterRow> parameterRows;
	private final ParameterTableModel parameterTableModel;
	private final JTable parameterTable;
	private final JSpinner runCountSpinner;
	private final JTextField seedField;
	private final JTextArea parameterDescription;
	private final JLabel presetNotice;
	private final JPanel cards;
	private final CardLayout cardLayout;
	private final JProgressBar progressBar;
	private final JLabel progressLabel;
	private final JButton runButton;
	private final JButton plotCachedButton;
	private final JButton closeButton;
	private final JButton cancelButton;

	private AnalysisWorker worker;
	private boolean loadedSettingsNotice;
	private MonteCarloSettings settingsAtLastSave;

	public LandingDispersionDialog(Window owner, Simulation simulation) {
		super(owner, String.format(trans.get("LandingDispersionDlg.title"), simulation.getName()),
				ModalityType.APPLICATION_MODAL);
		this.owner = owner;
		this.simulation = simulation;
		MonteCarloResult cachedResult = LandingDispersionAnalysisCache.get(simulation);
		MonteCarloSettings savedSettings = simulation.getLandingDispersionSettings();
		MonteCarloSettings initialSettings = selectInitialSettings(savedSettings, cachedResult);
		this.parameterRows = createParameterRows();
		this.parameterTableModel = new ParameterTableModel(parameterRows);
		this.runCountSpinner = new JSpinner(new SpinnerNumberModel(MonteCarloSettings.DEFAULT_RUN_COUNT,
				MonteCarloSettings.MIN_RUN_COUNT, MonteCarloSettings.MAX_RUN_COUNT, 50));
		this.seedField = new JTextField(Integer.toString(ThreadLocalRandom.current().nextInt()), 11);
		if (initialSettings != null) {
			loadSettings(initialSettings);
		}
		this.parameterDescription = createDescriptionArea();
		this.parameterTable = createParameterTable();
		String noticeKey = savedSettings != null
				? "LandingDispersionDlg.lbl.savedValuesNotice"
				: cachedResult != null
						? "LandingDispersionDlg.lbl.cachedValuesNotice"
						: "LandingDispersionDlg.lbl.defaultValuesNotice";
		this.presetNotice = new JLabel(trans.get(noticeKey));
		this.loadedSettingsNotice = initialSettings != null;
		this.cardLayout = new CardLayout();
		this.cards = new JPanel(cardLayout);
		this.progressBar = new JProgressBar(0, 100);
		this.progressLabel = new JLabel(" ");
		this.runButton = new JButton(trans.get("LandingDispersionDlg.but.run"));
		this.plotCachedButton = new JButton(trans.get("LandingDispersionDlg.but.plotCached"));
		this.plotCachedButton.setToolTipText(trans.get("LandingDispersionDlg.but.plotCached.ttip"));
		this.plotCachedButton.setVisible(cachedResult != null);
		this.closeButton = new JButton(trans.get("dlg.but.close"));
		this.cancelButton = new JButton(trans.get("dlg.but.cancel"));

		buildDialog();
		this.settingsAtLastSave = buildSettings(false);
		installAnalysisSettingsListeners();
		updateCachedAnalysisButton();
	}

	private void buildDialog() {
		cards.add(createSetupPanel(), CARD_SETUP);
		cards.add(createProgressPanel(), CARD_PROGRESS);

		JPanel buttonPanel = new JPanel(new MigLayout("ins 0", "[grow][][button][button][button]"));
		buttonPanel.setMinimumSize(new Dimension(0, 0));
		presetNotice.setMinimumSize(new Dimension(0, presetNotice.getPreferredSize().height));
		buttonPanel.add(presetNotice, "growx, wmin 0");
		cancelButton.setVisible(false);
		buttonPanel.add(cancelButton);
		buttonPanel.add(plotCachedButton);
		buttonPanel.add(closeButton, "tag close");
		buttonPanel.add(runButton, "tag ok");

		JPanel content = new JPanel(new BorderLayout(0, 10));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(cards, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(content);

		runButton.addActionListener(event -> startAnalysis());
		plotCachedButton.addActionListener(event -> plotCachedAnalysis());
		closeButton.addActionListener(event -> closeDialog());
		cancelButton.addActionListener(event -> cancelAnalysis());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				closeDialog();
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		GUIUtil.installEscapeCloseOperation(this);
		GUIUtil.setWindowIcons(this);
		GUIUtil.addModelNullingListener(this);
		getRootPane().setDefaultButton(runButton);
		pack();
		setSize(800, 590);
		setMinimumSize(new Dimension(620, 470));
		setLocationRelativeTo(owner);
	}

	private JPanel createSetupPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		JPanel top = new JPanel(new MigLayout("fillx, ins 0", "[grow,fill]", "[][]"));
		JTextArea introduction = createIntroductionArea();
		top.add(introduction, "growx, wmin 0, wrap para");
		top.add(createConfigurationControlsPanel(), "growx");
		panel.add(top, BorderLayout.NORTH);

		JScrollPane tableScrollPane = new JScrollPane(parameterTable);
		tableScrollPane.setMinimumSize(new Dimension(0, 0));
		panel.add(tableScrollPane, BorderLayout.CENTER);

		// Parameter help
		JPanel helpPanel = new JPanel(new BorderLayout());
		helpPanel.setBorder(BorderFactory.createTitledBorder(trans.get("LandingDispersionDlg.border.parameterHelp")));
		helpPanel.add(parameterDescription, BorderLayout.CENTER);
		helpPanel.setPreferredSize(new Dimension(0, 72));
		panel.add(helpPanel, BorderLayout.SOUTH);
		return panel;
	}

	private JPanel createConfigurationControlsPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0, fillx"));

		// Dispersed runs
		panel.add(new JLabel(trans.get("LandingDispersionDlg.lbl.runs")));
		panel.add(runCountSpinner, "w 90!, left");

		// Master seed
		JLabel seedLabel = new JLabel(trans.get("LandingDispersionDlg.lbl.seed"));
		String seedTooltip = trans.get("LandingDispersionDlg.lbl.seed.ttip");
		seedLabel.setToolTipText(seedTooltip);
		seedField.setToolTipText(seedTooltip);
		panel.add(seedLabel);
		JPanel seedControls = new JPanel(new MigLayout("ins 0", "[110!][][grow]"));
		seedControls.add(seedField, "growx");
		JButton newSeedButton = new JButton(trans.get("LandingDispersionDlg.but.newSeed"));
		newSeedButton.addActionListener(event -> seedField.setText(
				Integer.toString(ThreadLocalRandom.current().nextInt())));
		seedControls.add(newSeedButton);
		panel.add(seedControls, "growx, wrap");

		// Input spreads
		panel.add(new JLabel(trans.get("LandingDispersionDlg.lbl.spreads")));
		JPanel spreadActions = new JPanel(new MigLayout("ins 0", "[button][button][grow]"));

		// Reset to default
		JButton defaultsButton = new JButton(trans.get("LandingDispersionDlg.but.defaultValues"));
		defaultsButton.setToolTipText(trans.get("LandingDispersionDlg.but.defaultValues.ttip"));
		defaultsButton.addActionListener(event -> {
			parameterTableModel.setDefaultValues();
			presetNotice.setText(trans.get("LandingDispersionDlg.lbl.defaultValuesNotice"));
		});
		spreadActions.add(defaultsButton);

		// Zero every spread, to isolate the run-to-run effect of the stochastic wind model
		JButton allFixedButton = new JButton(trans.get("LandingDispersionDlg.but.allFixed"));
		allFixedButton.setToolTipText(trans.get("LandingDispersionDlg.but.allFixed.ttip"));
		allFixedButton.addActionListener(event -> {
			parameterTableModel.setAllFixed();
			presetNotice.setText(trans.get("LandingDispersionDlg.lbl.allFixedNotice"));
		});
		spreadActions.add(allFixedButton);
		panel.add(spreadActions, "growx");
		return panel;
	}

	/**
	 * Use a wrapping text component that follows the available dialog width. It has
	 * no column-based preferred width, so translated text cannot widen the dialog.
	 */
	private static JTextArea createIntroductionArea() {
		JTextArea area = new JTextArea(trans.get("LandingDispersionDlg.lbl.introduction"));
		area.setRows(3);
		area.setEditable(false);
		area.setFocusable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setFont(UIManager.getFont("Label.font"));
		area.setBorder(null);
		area.setMinimumSize(new Dimension(0, 0));
		return area;
	}

	private JPanel createProgressPanel() {
		JPanel panel = new JPanel(new MigLayout("fill, ins 30", "[grow]", "[grow][][grow]"));
		JPanel center = new JPanel(new MigLayout("fillx, ins 0", "[grow]"));
		JLabel heading = new JLabel(trans.get("LandingDispersionDlg.lbl.running"));
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, heading.getFont().getSize2D() + 2));
		center.add(heading, "wrap para");
		progressBar.setStringPainted(true);
		center.add(progressBar, "growx, w 500::, wrap rel");
		center.add(progressLabel, "growx");
		panel.add(center, "growx");
		return panel;
	}

	private JTable createParameterTable() {
		// Every row lists every distribution. A log-normal spread is only defined for a
		// parameter sampled as a multiplier, so on the other rows it is shown greyed out
		// and cannot be picked. Hiding it there instead made the option undiscoverable:
		// whether it appeared at all depended on which row happened to be selected.
		TableCellEditor distributionEditor = new DistributionCellEditor();

		JTable table = new JTable(parameterTableModel) {
			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				if (convertColumnIndexToModel(column) == 2) {
					return distributionEditor;
				}
				return super.getCellEditor(row, column);
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setMinimumSize(new Dimension(0, 0));
		table.setRowHeight(Math.max(table.getRowHeight(), 22));
		table.getColumnModel().getColumn(0).setPreferredWidth(85);
		table.getColumnModel().getColumn(1).setPreferredWidth(220);
		table.getColumnModel().getColumn(2).setPreferredWidth(145);
		table.getColumnModel().getColumn(3).setPreferredWidth(90);
		table.getColumnModel().getColumn(4).setPreferredWidth(85);

		table.getColumnModel().getColumn(2).setCellRenderer(new DistributionTableRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new SpreadTableRenderer());
		table.getColumnModel().getColumn(0).setCellRenderer(new GroupTableRenderer());

		table.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				updateParameterDescription(table.getSelectedRow());
			}
		});
		parameterTableModel.addTableModelListener(event -> updateParameterDescription(table.getSelectedRow()));
		if (!parameterRows.isEmpty()) {
			table.setRowSelectionInterval(0, 0);
			updateParameterDescription(0);
		}
		return table;
	}

	private JTextArea createDescriptionArea() {
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		return area;
	}

	private void updateParameterDescription(int selectedRow) {
		if (selectedRow < 0 || selectedRow >= parameterRows.size()) {
			parameterDescription.setText("");
			return;
		}
		ParameterRow row = parameterRows.get(selectedRow);
		StringBuilder text = new StringBuilder(row.description).append("  ")
				.append(distributionExplanation(row.distribution));
		// Say why an option is greyed out here rather than in a popup tooltip, which the
		// combo's list does not reliably show.
		if (!row.parameter.isRelative()) {
			text.append("  ").append(trans.get("LandingDispersionDlg.distribution.relativeOnly"));
		}
		parameterDescription.setText(text.toString());
		parameterDescription.setCaretPosition(0);
	}

	private void startAnalysis() {
		MonteCarloSettings settings = readSettings(true);
		if (settings == null) {
			return;
		}
		if (settings.getUncertainties().isEmpty()) {
			int answer = JOptionPane.showConfirmDialog(this,
					trans.get("LandingDispersionDlg.msg.noUncertainties"),
					trans.get("LandingDispersionDlg.msg.noUncertainties.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer != JOptionPane.YES_OPTION) {
				return;
			}
		}
		saveSettings(settings);

		progressBar.setValue(0);
		progressLabel.setText(String.format(trans.get("LandingDispersionDlg.lbl.progress"), 0,
				settings.getRunCount() + 1));
		cardLayout.show(cards, CARD_PROGRESS);
		runButton.setVisible(false);
		plotCachedButton.setVisible(false);
		closeButton.setVisible(false);
		cancelButton.setVisible(true);
		cancelButton.setEnabled(true);
		getRootPane().setDefaultButton(cancelButton);

		worker = new AnalysisWorker(settings);
		worker.execute();
	}

	private MonteCarloSettings readSettings(boolean showErrors) {
		if (parameterTable.isEditing() && !parameterTable.getCellEditor().stopCellEditing()) {
			return null;
		}
		try {
			runCountSpinner.commitEdit();
		} catch (ParseException exception) {
			if (showErrors) {
				showInputError(trans.get("LandingDispersionDlg.msg.invalidRuns"));
			}
			return null;
		}
		return buildSettings(showErrors);
	}

	private MonteCarloSettings buildSettings(boolean showErrors) {
		int seed;
		try {
			seed = Integer.parseInt(seedField.getText().trim());
		} catch (NumberFormatException exception) {
			if (showErrors) {
				showInputError(trans.get("LandingDispersionDlg.msg.invalidSeed"));
			}
			return null;
		}

		MonteCarloSettings.Builder builder = MonteCarloSettings.builder()
				.runCount(((Number) runCountSpinner.getValue()).intValue())
				.seed(seed)
				.threadCount(Math.max(1, SwingPreferences.getMaxThreadCount()));
		for (ParameterRow row : parameterRows) {
			if (!Double.isFinite(row.spread) || row.spread < 0) {
				if (showErrors) {
					showInputError(String.format(trans.get("LandingDispersionDlg.msg.invalidSpread"), row.name));
				}
				return null;
			}
			double internalSpread = row.spread * row.internalUnitsPerDisplayUnit;
			builder.uncertainty(row.parameter, row.distribution, internalSpread);
		}
		return builder.build();
	}

	private void loadSettings(MonteCarloSettings settings) {
		runCountSpinner.setValue(settings.getRunCount());
		seedField.setText(Integer.toString(settings.getSeed()));
		for (ParameterRow row : parameterRows) {
			var uncertainty = settings.getUncertainty(row.parameter);
			row.distribution = uncertainty.distribution();
			row.spread = uncertainty.spread() / row.internalUnitsPerDisplayUnit;
		}
	}

	private void installAnalysisSettingsListeners() {
		runCountSpinner.addChangeListener(event -> analysisSettingsChanged());
		parameterTableModel.addTableModelListener(event -> analysisSettingsChanged());
		seedField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				analysisSettingsChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				analysisSettingsChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				analysisSettingsChanged();
			}
		});
	}

	private void analysisSettingsChanged() {
		if (loadedSettingsNotice) {
			presetNotice.setText(" ");
			loadedSettingsNotice = false;
		}
		updateCachedAnalysisButton();
	}

	private void updateCachedAnalysisButton() {
		MonteCarloSettings settings = buildSettings(false);
		plotCachedButton.setVisible(settings != null
				&& LandingDispersionAnalysisCache.get(simulation, settings) != null);
	}

	private void plotCachedAnalysis() {
		MonteCarloSettings settings = readSettings(true);
		if (settings == null) {
			updateCachedAnalysisButton();
			return;
		}
		MonteCarloResult result = LandingDispersionAnalysisCache.get(simulation, settings);
		if (result == null) {
			updateCachedAnalysisButton();
			return;
		}
		saveSettings(settings);
		dispose();
		new LandingDispersionResultsDialog(owner, simulation.getName(), result).setVisible(true);
	}

	private void showInputError(String message) {
		JOptionPane.showMessageDialog(this, message, trans.get("LandingDispersionDlg.msg.invalidInput.title"),
				JOptionPane.ERROR_MESSAGE);
	}

	private void cancelAnalysis() {
		if (worker == null) {
			return;
		}
		cancelButton.setEnabled(false);
		progressLabel.setText(trans.get("LandingDispersionDlg.lbl.cancelling"));
		worker.cancel(true);
	}

	private void closeDialog() {
		if (worker != null && !worker.isDone()) {
			cancelAnalysis();
			return;
		}
		MonteCarloSettings settings = readSettings(false);
		if (settings != null
				&& !LandingDispersionAnalysisCache.settingsMatch(settingsAtLastSave, settings)) {
			saveSettings(settings);
		}
		dispose();
	}

	private void saveSettings(MonteCarloSettings settings) {
		persistSettings(simulation, settings);
		settingsAtLastSave = settings;
	}

	static MonteCarloSettings selectInitialSettings(MonteCarloSettings savedSettings,
			MonteCarloResult cachedResult) {
		return savedSettings != null
				? savedSettings
				: cachedResult != null ? cachedResult.getSettings() : null;
	}

	static void persistSettings(Simulation simulation, MonteCarloSettings settings) {
		MonteCarloSettings current = simulation.getLandingDispersionSettings();
		if (current == null || !LandingDispersionAnalysisCache.settingsMatch(current, settings)) {
			simulation.setLandingDispersionSettings(settings);
		}
	}

	private void restoreSetupAfterFailure() {
		worker = null;
		cardLayout.show(cards, CARD_SETUP);
		runButton.setVisible(true);
		closeButton.setVisible(true);
		cancelButton.setVisible(false);
		updateCachedAnalysisButton();
		getRootPane().setDefaultButton(runButton);
	}

	private static String distributionLabel(MonteCarloDistribution distribution) {
		return switch (distribution) {
			case NORMAL -> trans.get("LandingDispersionDlg.distribution.normal");
			case UNIFORM -> trans.get("LandingDispersionDlg.distribution.uniform");
			case LOG_NORMAL -> trans.get("LandingDispersionDlg.distribution.lognormal");
		};
	}

	private static String distributionExplanation(MonteCarloDistribution distribution) {
		return switch (distribution) {
			case NORMAL -> trans.get("LandingDispersionDlg.distribution.normal.help");
			case UNIFORM -> trans.get("LandingDispersionDlg.distribution.uniform.help");
			case LOG_NORMAL -> trans.get("LandingDispersionDlg.distribution.lognormal.help");
		};
	}

	private static List<ParameterRow> createParameterRows() {
		List<ParameterRow> rows = new ArrayList<>();
		String weather = trans.get("LandingDispersionDlg.group.weather");
		String launcher = trans.get("LandingDispersionDlg.group.launcher");
		String vehicle = trans.get("LandingDispersionDlg.group.vehicle");
		String propulsion = trans.get("LandingDispersionDlg.group.propulsion");
		String recovery = trans.get("LandingDispersionDlg.group.recovery");
		Unit windSpeedUnit = UnitGroup.UNITS_WINDSPEED.getDefaultUnit();
		Unit angleUnit = UnitGroup.UNITS_ANGLE.getDefaultUnit();
		Unit relativeUnit = UnitGroup.UNITS_RELATIVE.getDefaultUnit();
		Unit smallLengthUnit = UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit();
		Unit timeUnit = UnitGroup.UNITS_SHORT_TIME.getDefaultUnit();

		rows.add(row(weather, "windSpeed", windSpeedUnit, 1, MonteCarloParameter.WIND_SPEED));
		rows.add(row(weather, "windDirection", angleUnit, Math.toRadians(10),
				MonteCarloParameter.WIND_DIRECTION));
		rows.add(row(weather, "airDensity", relativeUnit, 0.02, MonteCarloParameter.AIR_DENSITY));
		rows.add(row(launcher, "guideAngle", angleUnit, Math.toRadians(1),
				MonteCarloParameter.LAUNCH_GUIDE_ANGLE));
		rows.add(row(launcher, "guideDirection", angleUnit, Math.toRadians(5),
				MonteCarloParameter.LAUNCH_GUIDE_DIRECTION));
		rows.add(row(vehicle, "mass", relativeUnit, 0.02, MonteCarloParameter.TOTAL_MASS));
		rows.add(row(vehicle, "axialCg", smallLengthUnit, 0.005, MonteCarloParameter.CG_AXIAL));
		rows.add(row(vehicle, "axialDrag", relativeUnit, 0.10, MonteCarloParameter.AXIAL_DRAG));
		rows.add(row(vehicle, "normalForce", relativeUnit, 0.10, MonteCarloParameter.NORMAL_FORCE));
		rows.add(row(propulsion, "thrust", relativeUnit, 0.05, MonteCarloParameter.THRUST));
		rows.add(row(propulsion, "ignitionDelay", timeUnit, 0.1, MonteCarloParameter.IGNITION_DELAY));
		rows.add(row(recovery, "recoveryDrag", relativeUnit, 0.15, MonteCarloParameter.RECOVERY_DRAG));
		rows.add(row(recovery, "deploymentDelay", timeUnit, 0.5,
				MonteCarloParameter.DEPLOYMENT_DELAY));
		return rows;
	}

	private static ParameterRow row(String group, String key, Unit unit, double defaultSpreadInternal,
			MonteCarloParameter parameter) {
		return new ParameterRow(group, trans.get("LandingDispersionDlg.parameter." + key),
				trans.get("LandingDispersionDlg.parameter." + key + ".help"), parameter, unit.getUnit(),
				unit.getMultiplier(), unit.toUnit(defaultSpreadInternal));
	}

	/**
	 * Whether a distribution can be applied to a parameter. A multiplicative distribution
	 * is meaningless for a parameter sampled as an additive offset.
	 *
	 * @param distribution distribution the user is offered
	 * @param parameter parameter of the row being edited
	 * @return {@code true} if the combination is valid
	 */
	static boolean isDistributionAvailable(MonteCarloDistribution distribution, MonteCarloParameter parameter) {
		return !distribution.requiresRelativeParameter() || parameter.isRelative();
	}

	private final class AnalysisWorker extends SwingWorker<MonteCarloResult, ProgressState> {
		private final MonteCarloSettings settings;

		private AnalysisWorker(MonteCarloSettings settings) {
			this.settings = settings;
		}

		@Override
		protected MonteCarloResult doInBackground() {
			MonteCarloSimulationRunner runner = new MonteCarloSimulationRunner();
			return runner.run(simulation, settings, (completed, total) -> {
				setProgress((int) Math.round(100.0 * completed / total));
				publish(new ProgressState(completed, total));
			});
		}

		@Override
		protected void process(List<ProgressState> chunks) {
			ProgressState latest = chunks.get(chunks.size() - 1);
			progressBar.setValue(getProgress());
			progressLabel.setText(String.format(trans.get("LandingDispersionDlg.lbl.progress"),
					latest.completed, latest.total));
		}

		@Override
		protected void done() {
			if (isCancelled()) {
				worker = null;
				dispose();
				return;
			}
			try {
				MonteCarloResult result = get();
				LandingDispersionAnalysisCache.put(simulation, result);
				worker = null;
				dispose();
				new LandingDispersionResultsDialog(owner, simulation.getName(), result).setVisible(true);
			} catch (CancellationException exception) {
				worker = null;
				dispose();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				restoreSetupAfterFailure();
			} catch (ExecutionException exception) {
				Throwable cause = exception.getCause();
				if (cause instanceof BallisticTrajectoryException) {
					restoreSetupAfterFailure();
					JOptionPane.showMessageDialog(LandingDispersionDialog.this,
							trans.get("LandingDispersionDlg.msg.ballistic"),
							trans.get("LandingDispersionDlg.msg.ballistic.title"),
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				String message = cause == null ? exception.getLocalizedMessage() : cause.getLocalizedMessage();
				if (message == null || message.isBlank()) {
					message = cause == null ? exception.getClass().getSimpleName()
							: cause.getClass().getSimpleName();
				}
				restoreSetupAfterFailure();
				JOptionPane.showMessageDialog(LandingDispersionDialog.this, message,
						trans.get("LandingDispersionDlg.msg.failed.title"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static final class ParameterTableModel extends AbstractTableModel {
		private final List<ParameterRow> rows;
		private final String[] columns = {
				trans.get("LandingDispersionDlg.col.group"),
				trans.get("LandingDispersionDlg.col.parameter"),
				trans.get("LandingDispersionDlg.col.distribution"),
				trans.get("LandingDispersionDlg.col.spread"),
				trans.get("LandingDispersionDlg.col.unit")
		};

		private ParameterTableModel(List<ParameterRow> rows) {
			this.rows = rows;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch (column) {
				case 2 -> MonteCarloDistribution.class;
				case 3 -> Double.class;
				default -> String.class;
			};
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 2 || columnIndex == 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			ParameterRow row = rows.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> row.group;
				case 1 -> row.name;
				case 2 -> row.distribution;
				case 3 -> row.spread;
				case 4 -> row.unit;
				default -> throw new IndexOutOfBoundsException("Invalid table column " + columnIndex);
			};
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			ParameterRow row = rows.get(rowIndex);
			if (columnIndex == 2 && value instanceof MonteCarloDistribution distribution) {
				row.distribution = distribution;
			} else if (columnIndex == 3 && value instanceof Number number) {
				row.spread = number.doubleValue();
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		private void setAllFixed() {
			for (ParameterRow row : rows) {
				row.spread = 0;
			}
			fireTableRowsUpdated(0, rows.size() - 1);
		}

		private void setDefaultValues() {
			for (ParameterRow row : rows) {
				row.spread = row.defaultSpread;
				row.distribution = MonteCarloDistribution.NORMAL;
			}
			fireTableRowsUpdated(0, rows.size() - 1);
		}
	}

	private static final class ParameterRow {
		private final String group;
		private final String name;
		private final String description;
		private final MonteCarloParameter parameter;
		private final String unit;
		private final double internalUnitsPerDisplayUnit;
		private final double defaultSpread;
		private MonteCarloDistribution distribution = MonteCarloDistribution.NORMAL;
		private double spread;

		private ParameterRow(String group, String name, String description,
				MonteCarloParameter parameter, String unit, double internalUnitsPerDisplayUnit,
				double defaultSpread) {
			this.group = group;
			this.name = name;
			this.description = description;
			this.parameter = parameter;
			this.unit = unit;
			this.internalUnitsPerDisplayUnit = internalUnitsPerDisplayUnit;
			this.defaultSpread = defaultSpread;
			this.spread = defaultSpread;
		}
	}

	/**
	 * Offers every distribution on every row, disabling the ones that do not apply to the
	 * row being edited so that they stay visible without becoming selectable.
	 */
	private final class DistributionCellEditor extends DefaultCellEditor {
		private DistributionCellEditor() {
			super(new DistributionComboBox());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
				int row, int column) {
			// Recorded before the superclass selects the current value, so that the combo's
			// selection guard and its renderer both see the row being edited.
			((DistributionComboBox) getComponent())
					.setParameter(parameterRows.get(table.convertRowIndexToModel(row)).parameter);
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}

	/** Lists every distribution but refuses to select one the edited row cannot use. */
	static final class DistributionComboBox extends JComboBox<MonteCarloDistribution> {
		private MonteCarloParameter parameter;

		DistributionComboBox() {
			super(MonteCarloDistribution.values());
			setRenderer(new DistributionListRenderer(this::isAvailable));
		}

		void setParameter(MonteCarloParameter parameter) {
			this.parameter = parameter;
		}

		private boolean isAvailable(MonteCarloDistribution distribution) {
			return parameter == null || isDistributionAvailable(distribution, parameter);
		}

		@Override
		public void setSelectedItem(Object item) {
			if (item instanceof MonteCarloDistribution distribution && !isAvailable(distribution)) {
				return;
			}
			super.setSelectedItem(item);
		}
	}

	private static final class DistributionListRenderer extends DefaultListCellRenderer {
		private final Predicate<MonteCarloDistribution> available;

		private DistributionListRenderer(Predicate<MonteCarloDistribution> available) {
			this.available = available;
		}

		@Override
		public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof MonteCarloDistribution distribution) {
				setText(distributionLabel(distribution));
				boolean enabled = available.test(distribution);
				component.setEnabled(enabled);
				setToolTipText(enabled ? null
						: trans.get("LandingDispersionDlg.distribution.relativeOnly"));
			}
			return component;
		}
	}

	private static final class DistributionTableRenderer extends DefaultTableCellRenderer {
		@Override
		protected void setValue(Object value) {
			if (value instanceof MonteCarloDistribution distribution) {
				super.setValue(distributionLabel(distribution));
			} else {
				super.setValue(value);
			}
		}
	}

	private static final class GroupTableRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			component.setFont(component.getFont().deriveFont(Font.BOLD));
			return component;
		}
	}

	private static final class SpreadTableRenderer extends DefaultTableCellRenderer {
		private final DecimalFormat format = new DecimalFormat("0.####");

		@Override
		protected void setValue(Object value) {
			if (value instanceof Number number) {
				super.setValue(format.format(number.doubleValue()));
			} else {
				super.setValue(value);
			}
		}
	}

	private record ProgressState(int completed, int total) {
	}
}
