package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;

public class MultiLevelWindEditDialog extends JDialog {
	private final MultiLevelWindTable windTable;
	private final WindProfilePanel visualization;
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private static Border border;

	static {
		initColors();
	}

	public MultiLevelWindEditDialog(Window owner, MultiLevelPinkNoiseWindModel model) {
		super(owner, trans.get("WindProfileEditorDlg.title"), ModalityType.APPLICATION_MODAL);

		// Create main panel with split layout
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true); // Smoother resizing

		// Create left panel with table
		windTable = new MultiLevelWindTable(model);
		
		// Create and configure the scroll pane
		JScrollPane tableScrollPane = new JScrollPane(windTable.getRowsPanel()) {
			// Override scrollPane preferred size to ensure consistent width
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				// Keep preferred width from getting smaller than header panel
				int minWidth = windTable.getHeaderPanel().getPreferredSize().width;
				size.width = Math.max(size.width, minWidth);
				return size;
			}
		};
		
		tableScrollPane.setColumnHeaderView(windTable.getHeaderPanel());
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
		tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		// Improve scrolling speed
		tableScrollPane.getHorizontalScrollBar().setUnitIncrement(16); // Faster horizontal scrolling
		tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);   // Faster vertical scrolling

		// Create visualization that uses the units from the table
		visualization = new WindProfilePanel(model, windTable);
		visualization.setPreferredSize(new Dimension(300, 400));

		// Set up synchronization between table and visualization
		windTable.addChangeListener(visualization);
		windTable.addSelectionListener(visualization::setSelectedLevel);

		JPanel visPanel = new JPanel(new BorderLayout());
		visPanel.setBorder(border);

		// Add title
		StyledLabel visualizationTitle = new StyledLabel(trans.get("WindProfilePanel.title.WindProfileVisualization"), 1.5f, StyledLabel.Style.BOLD);
		visualizationTitle.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		visPanel.add(visualizationTitle, BorderLayout.NORTH);

		// Add visualization
		visPanel.add(visualization, BorderLayout.CENTER);

		// Add table controls (add button, etc.)
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);

		// Controls panel
		JPanel controlsPanel = new JPanel(new MigLayout());
		controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		//// Altitude reference
		JPanel altitudeRefPanel = new JPanel(new MigLayout("ins 0"));
		JLabel altitudeRefLabel = new JLabel(trans.get("WindProfileEditorDlg.lbl.AltitudeReference"));
		altitudeRefLabel.setToolTipText(trans.get("WindProfileEditorDlg.lbl.AltitudeReference.ttip"));
		altitudeRefPanel.add(altitudeRefLabel);

		ButtonGroup altitudeRefGroup = new ButtonGroup();
		JRadioButton butMSL = new JRadioButton(trans.get("WindProfileEditorDlg.radio.MSL"));
		JRadioButton butAGL = new JRadioButton(trans.get("WindProfileEditorDlg.radio.AGL"));
		altitudeRefGroup.add(butMSL);
		altitudeRefGroup.add(butAGL);
		altitudeRefPanel.add(butMSL);
		altitudeRefPanel.add(butAGL, "wrap");

		butMSL.addActionListener(e -> {
			model.setAltitudeReference(WindModel.AltitudeReference.MSL);
			windTable.updateAltitudeHeader(WindModel.AltitudeReference.MSL);
			visualization.repaint();
		});
		butAGL.addActionListener(e -> {
			model.setAltitudeReference(WindModel.AltitudeReference.AGL);
			windTable.updateAltitudeHeader(WindModel.AltitudeReference.AGL);
			visualization.repaint();
		});

		switch (model.getAltitudeReference()) {
			case MSL -> butMSL.setSelected(true);
			case AGL -> butAGL.setSelected(true);
		}

		controlsPanel.add(altitudeRefPanel, "spanx, growx, wrap");

		//// Add New Level
		JButton addRowButton = new JButton(trans.get("WindProfileEditorDlg.but.AddNewLevel"));
		addRowButton.setIcon(Icons.FILE_NEW);
		addRowButton.addActionListener(e -> windTable.addRow());
		controlsPanel.add(addRowButton);

		//// Delete Level
		JButton deleteRowButton = new JButton(trans.get("WindProfileEditorDlg.but.DeleteLevel"));
		deleteRowButton.setToolTipText(trans.get("WindProfileEditorDlg.but.deleteWindLevel.ttip"));
		deleteRowButton.setIcon(Icons.EDIT_DELETE);
		deleteRowButton.addActionListener(e -> windTable.deleteSelectedRow());
		// Initially disable until a row is selected
		deleteRowButton.setEnabled(false);
		controlsPanel.add(deleteRowButton, "growx");

		// Add listener to enable/disable delete button based on selection
		windTable.addSelectionListener(selectedLevel -> {
			deleteRowButton.setEnabled(selectedLevel != null && model.getLevels().size() > 1);
		});

		//// Import Levels
		JButton importButton = new JButton(trans.get("WindProfileEditorDlg.but.importLevels"));
		importButton.setToolTipText(trans.get("WindProfileEditorDlg.but.importLevels.ttip"));
		importButton.setIcon(Icons.IMPORT);
		importButton.addActionListener(e -> {
			importLevels(model);
		});
		controlsPanel.add(importButton, "gapleft para, wrap");

		//// Reset Levels
		JButton resetButton = new JButton(trans.get("WindProfileEditorDlg.but.ResetLevels"));
		resetButton.setToolTipText(trans.get("WindProfileEditorDlg.but.ResetLevels.ttip"));
		resetButton.addActionListener(e -> {
			// Show confirmation dialog
			int result = JOptionPane.showConfirmDialog(this,
					trans.get("WindProfileEditorDlg.dlg.overwriteLevels.msg"),
					trans.get("WindProfileEditorDlg.dlg.overwriteLevels.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			// If confirmed, reset the levels
			if (result == JOptionPane.YES_OPTION) {
				windTable.resetLevels();
			}
		});
		controlsPanel.add(resetButton, "skip 1, growx");

		//// Export Levels
		/*JButton exportButton = new JButton(trans.get("WindProfileEditorDlg.but.exportLevels"));
		exportButton.setToolTipText(trans.get("WindProfileEditorDlg.but.exportLevels.ttip"));
		exportButton.setIcon(Icons.EXPORT);
		exportButton.addActionListener(e -> {
			// TODO
		});*/

		tablePanel.add(controlsPanel, BorderLayout.SOUTH);

		// Add panels to split pane
		splitPane.setLeftComponent(tablePanel);
		splitPane.setRightComponent(visPanel);
		splitPane.setResizeWeight(0.6); // 60% to table initially

		// Dialog buttons
		JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(trans.get("button.close"));
		closeButton.addActionListener(e -> dispose());
		dialogButtonPanel.add(closeButton);

		// Main layout
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);
		contentPane.add(dialogButtonPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		setSize(900, 500);
		setLocationRelativeTo(owner);
		
		// Set minimum size to ensure UI doesn't get too cramped
		setMinimumSize(new Dimension(700, 400));

		GUIUtil.installEscapeCloseButtonOperation(this, closeButton);
	}

	private void importLevels(MultiLevelPinkNoiseWindModel model) {
		// First, open the import settings dialog to configure column mappings and units
		CSVImportSettingsDialog settingsDialog = new CSVImportSettingsDialog(this);
		settingsDialog.setVisible(true);

		// If user canceled, return
		if (!settingsDialog.isApproved()) {
			return;
		}

		// Save the preferences
		prefs.setMultiLevelWindCsvImportHeader(settingsDialog.hasHeader());

		prefs.setMultiLevelWindCsvImportAltitudeColumn(settingsDialog.getAltitudeColumnName());
		prefs.setMultiLevelWindCsvImportSpeedColumn(settingsDialog.getSpeedColumnName());
		prefs.setMultiLevelWindCsvImportDirectionColumn(settingsDialog.getDirectionColumnName());
		prefs.setMultiLevelWindCsvImportStddevColumn(settingsDialog.getStdDeviationColumnName());

		prefs.setMultiLevelWindCsvImportAltitudeColumnIndex(settingsDialog.getAltitudeColumnIndex());
		prefs.setMultiLevelWindCsvImportSpeedColumnIndex(settingsDialog.getSpeedColumnIndex());
		prefs.setMultiLevelWindCsvImportDirectionColumnIndex(settingsDialog.getDirectionColumnIndex());
		prefs.setMultiLevelWindCsvImportStddevColumnIndex(settingsDialog.getStdDeviationColumnIndex());

		prefs.setMultiLevelWindCsvImportAltitudeUnit(settingsDialog.getAltitudeUnit());
		prefs.setMultiLevelWindCsvImportSpeedUnit(settingsDialog.getSpeedUnit());
		prefs.setMultiLevelWindCsvImportDirectionUnit(settingsDialog.getDirectionUnit());
		prefs.setMultiLevelWindCsvImportStddevUnit(settingsDialog.getStdDeviationUnit());

		prefs.putString(ApplicationPreferences.EXPORT_FIELD_SEPARATOR, settingsDialog.getSeparator());

		// Now open file chooser to select the CSV file
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		fileChooser.setDialogTitle(trans.get("WindProfileEditorDlg.dlg.importLevels.title"));

		fileChooser.addChoosableFileFilter(FileHelper.CSV_FILTER);
		fileChooser.setFileFilter(FileHelper.CSV_FILTER);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);

		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File selectedFile = fileChooser.getSelectedFile();
		selectedFile = FileHelper.forceExtension(selectedFile, "csv");

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(fileChooser.getCurrentDirectory());

		// Show a warning message that the current levels will be overwritten
		if (!model.getLevels().isEmpty()) {
			int result = JOptionPane.showConfirmDialog(this, trans.get("WindProfileEditorDlg.dlg.overwriteLevels.msg"),
					trans.get("WindProfileEditorDlg.dlg.overwriteLevels.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
		}

		// Import the CSV file with the configured settings
		try {
			windTable.importLevels(
					selectedFile,
					settingsDialog.getSeparator(),
					settingsDialog.getAltitudeColumn(),
					settingsDialog.getSpeedColumn(),
					settingsDialog.getDirectionColumn(),
					settingsDialog.getStdDeviationColumn(),
					settingsDialog.getAltitudeUnit(),
					settingsDialog.getSpeedUnit(),
					settingsDialog.getDirectionUnit(),
					settingsDialog.getStdDeviationUnit(),
					settingsDialog.hasHeader()
			);

			// Update the units from the wind table to match those from the settings dialog
			windTable.setAltitudeUnit(settingsDialog.getAltitudeUnit());
			windTable.setSpeedUnit(settingsDialog.getSpeedUnit());
			windTable.setDirectionUnit(settingsDialog.getDirectionUnit());
			windTable.setStdDeviationUnit(settingsDialog.getStdDeviationUnit());
		} catch (IllegalArgumentException ex) {
			windTable.resetLevels();
			JOptionPane.showMessageDialog(this, new String[] {
					trans.get("WindProfileEditorDlg.msg.importLevelsError"),
					ex.getMessage() }, trans.get("WindProfileEditorDlg.msg.importLevelsError.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(MultiLevelWindEditDialog::updateColors);
	}

	public static void updateColors() {
		border = GUIUtil.getUITheme().getBorder();
	}

	@Override
	public void dispose() {
		if (windTable != null) {
			windTable.invalidateModels();
			windTable.removeAllSelectionListeners();
			if (visualization != null) {
				windTable.removeChangeListener(visualization);
			}
		}

		super.dispose();
	}
}
