package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.util.HashSet;
import java.util.Set;

import static info.openrocket.swing.gui.components.CsvOptionPanel.SPACE;
import static info.openrocket.swing.gui.components.CsvOptionPanel.TAB;

/**
 * Dialog for configuring CSV import settings for wind data
 */
public class CSVImportSettingsDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	// Fields for mapping column names
	private final JTextField altitudeColumnField;
	private final JTextField speedColumnField;
	private final JTextField directionColumnField;
	private final JTextField stdDeviationColumnField;

	// Spinners for mapping column indices
	private final JSpinner altitudeColumnSpinner;
	private final JSpinner speedColumnSpinner;
	private final JSpinner directionColumnSpinner;
	private final JSpinner stdDeviationColumnSpinner;

	// Panels to switch between text fields and spinners
	private final JPanel altitudeColumnPanel;
	private final JPanel speedColumnPanel;
	private final JPanel directionColumnPanel;
	private final JPanel stdDeviationColumnPanel;

	// Unit selectors
	private final UnitSelector altitudeUnitSelector;
	private final UnitSelector speedUnitSelector;
	private final UnitSelector directionUnitSelector;
	private final UnitSelector stdDeviationUnitSelector;

	private final JComboBox<String> separatorComboBox;
	private final JCheckBox hasHeaderCheckBox;
	private final JTextPane previewTextPane;
	private final JScrollPane scrollPane;

	// Column type label (changes between "Column Name" and "Column Index")
	private final JLabel columnTypeLabel;

	// Result flag
	private boolean approved = false;

	public CSVImportSettingsDialog(Window owner) {
		super(owner, trans.get("CSVImportSettingsDialog.title"), ModalityType.APPLICATION_MODAL);

		// Main panel with MigLayout
		JPanel mainPanel = new JPanel(new MigLayout("fill, insets dialog, gap para"));

		// Description text
		JTextArea descriptionArea = new JTextArea(trans.get("CSVImportSettingsDialog.description"));
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBackground(null);
		descriptionArea.setOpaque(false);
		descriptionArea.setFocusable(false);
		mainPanel.add(descriptionArea, "spanx, growx, wrap para");

		// CSV settings section
		JPanel csvSettings = new JPanel(new MigLayout());
		csvSettings.setBorder(BorderFactory.createTitledBorder(trans.get("CSVImportSettingsDialog.csvSettings")));
		mainPanel.add(csvSettings, "growx, spanx, wrap");

		// Column mapping help text
		JTextArea columnMappingHelp = new JTextArea(trans.get("CSVImportSettingsDialog.columnMappingHelp"));
		columnMappingHelp.setEditable(false);
		columnMappingHelp.setLineWrap(true);
		columnMappingHelp.setWrapStyleWord(true);
		columnMappingHelp.setBackground(null);
		columnMappingHelp.setOpaque(false);
		columnMappingHelp.setFocusable(false);
		columnMappingHelp.setFont(columnMappingHelp.getFont().deriveFont(columnMappingHelp.getFont().getSize() - 1f));
		csvSettings.add(columnMappingHelp, "spanx, growx, wrap para");

		// Header checkbox
		hasHeaderCheckBox = new JCheckBox(trans.get("CSVImportSettingsDialog.hasHeaders"));
		hasHeaderCheckBox.setToolTipText(trans.get("CSVImportSettingsDialog.hasHeaders.ttip"));
		hasHeaderCheckBox.setSelected(prefs.isMultiLevelWindCsvImportHeader());
		csvSettings.add(hasHeaderCheckBox, "spanx, wrap");

		// Grid header labels
		csvSettings.add(new StyledLabel(trans.get("CSVImportSettingsDialog.dataType"), StyledLabel.Style.BOLD), "width 120lp");
		// Use a regular JLabel for the column type so we can change it dynamically
		columnTypeLabel = new JLabel(trans.get("CSVImportSettingsDialog.columnName"));
		columnTypeLabel.setFont(columnTypeLabel.getFont().deriveFont(Font.BOLD));
		csvSettings.add(columnTypeLabel, "growx");
		csvSettings.add(new StyledLabel(trans.get("CSVImportSettingsDialog.unit"), StyledLabel.Style.BOLD), "width 120lp, wrap");

		//// Altitude mapping
		csvSettings.add(new JLabel(trans.get("CSVImportSettingsDialog.altitude")));

		// Create both text field and spinner for altitude
		altitudeColumnField = new JTextField(10);
		altitudeColumnField.setText(prefs.getMultiLevelWindCsvImportAltitudeColumn());

		altitudeColumnSpinner = new JSpinner(new SpinnerNumberModel(prefs.getMultiLevelWindCsvImportAltitudeColumnIndex(),
				0, Integer.MAX_VALUE, 1));
		altitudeColumnSpinner.setEditor(new JSpinner.NumberEditor(altitudeColumnSpinner, "#"));

		// Create panel to hold both components
		altitudeColumnPanel = new JPanel(new CardLayout());
		altitudeColumnPanel.add(altitudeColumnField, "text");
		altitudeColumnPanel.add(altitudeColumnSpinner, "spinner");

		csvSettings.add(altitudeColumnPanel, "growx");

		DoubleModel altitudeModel = new DoubleModel(0, UnitGroup.UNITS_DISTANCE);
		altitudeModel.setCurrentUnit(prefs.getMultiLevelWindCsvImportAltitudeUnit());
		altitudeUnitSelector = new UnitSelector(altitudeModel);
		csvSettings.add(altitudeUnitSelector, "wrap");

		//// Speed mapping
		csvSettings.add(new JLabel(trans.get("CSVImportSettingsDialog.speed")));

		// Create both text field and spinner for speed
		speedColumnField = new JTextField(10);
		speedColumnField.setText(prefs.getMultiLevelWindCsvImportSpeedColumn());

		speedColumnSpinner = new JSpinner(new SpinnerNumberModel(prefs.getMultiLevelWindCsvImportSpeedColumnIndex(),
				0, Integer.MAX_VALUE, 1));
		speedColumnSpinner.setEditor(new JSpinner.NumberEditor(speedColumnSpinner, "#"));

		// Create panel to hold both components
		speedColumnPanel = new JPanel(new CardLayout());
		speedColumnPanel.add(speedColumnField, "text");
		speedColumnPanel.add(speedColumnSpinner, "spinner");

		csvSettings.add(speedColumnPanel, "growx");

		DoubleModel speedModel = new DoubleModel(0, UnitGroup.UNITS_WINDSPEED);
		speedModel.setCurrentUnit(prefs.getMultiLevelWindCsvImportSpeedUnit());
		speedUnitSelector = new UnitSelector(speedModel);
		csvSettings.add(speedUnitSelector, "wrap");

		//// Direction mapping
		csvSettings.add(new JLabel(trans.get("CSVImportSettingsDialog.direction")));

		// Create both text field and spinner for direction
		directionColumnField = new JTextField(10);
		directionColumnField.setText(prefs.getMultiLevelWindCsvImportDirectionColumn());

		directionColumnSpinner = new JSpinner(new SpinnerNumberModel(prefs.getMultiLevelWindCsvImportDirectionColumnIndex(),
				0, Integer.MAX_VALUE, 1));
		directionColumnSpinner.setEditor(new JSpinner.NumberEditor(directionColumnSpinner, "#"));

		// Create panel to hold both components
		directionColumnPanel = new JPanel(new CardLayout());
		directionColumnPanel.add(directionColumnField, "text");
		directionColumnPanel.add(directionColumnSpinner, "spinner");

		csvSettings.add(directionColumnPanel, "growx");

		DoubleModel directionModel = new DoubleModel(0, UnitGroup.UNITS_ANGLE);
		directionModel.setCurrentUnit(prefs.getMultiLevelWindCsvImportDirectionUnit());
		directionUnitSelector = new UnitSelector(directionModel);
		csvSettings.add(directionUnitSelector, "wrap");

		//// Std Deviation mapping (optional)
		csvSettings.add(new JLabel(trans.get("CSVImportSettingsDialog.stdDev")));

		// Create both text field and spinner for std deviation
		stdDeviationColumnField = new JTextField(10);
		stdDeviationColumnField.setText(prefs.getMultiLevelWindCsvImportStddevColumn());

		stdDeviationColumnSpinner = new JSpinner(new SpinnerNumberModel(prefs.getMultiLevelWindCsvImportStddevColumnIndex(),
				0, Integer.MAX_VALUE, 1));
		stdDeviationColumnSpinner.setEditor(new JSpinner.NumberEditor(stdDeviationColumnSpinner, "#"));

		// Create panel to hold both components
		stdDeviationColumnPanel = new JPanel(new CardLayout());
		stdDeviationColumnPanel.add(stdDeviationColumnField, "text");
		stdDeviationColumnPanel.add(stdDeviationColumnSpinner, "spinner");

		csvSettings.add(stdDeviationColumnPanel, "growx");

		DoubleModel stdDevModel = new DoubleModel(0, UnitGroup.UNITS_WINDSPEED);
		stdDevModel.setCurrentUnit(prefs.getMultiLevelWindCsvImportStddevUnit());
		stdDeviationUnitSelector = new UnitSelector(stdDevModel);
		csvSettings.add(stdDeviationUnitSelector, "wrap");

		csvSettings.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap para");

		//// Field separator
		csvSettings.add(new JLabel(trans.get("CSVImportSettingsDialog.separator")));
		separatorComboBox = new JComboBox<>(new String[]{",", ";", SPACE, TAB});
		separatorComboBox.setEditable(true);
		separatorComboBox.setSelectedItem(prefs.getString(ApplicationPreferences.EXPORT_FIELD_SEPARATOR, ","));
		csvSettings.add(separatorComboBox, "wrap");

		// CSV file preview panel
		JPanel previewPanel = new JPanel(new MigLayout("fillx"));
		previewPanel.setBorder(BorderFactory.createTitledBorder(trans.get("CSVImportSettingsDialog.preview")));
		mainPanel.add(previewPanel, "growx, spanx, wrap");

		//// Preview help text
		JTextArea previewHelp = new JTextArea(trans.get("CSVImportSettingsDialog.previewHelp"));
		previewHelp.setEditable(false);
		previewHelp.setLineWrap(true);
		previewHelp.setWrapStyleWord(true);
		previewHelp.setBackground(null);
		previewHelp.setOpaque(false);
		previewHelp.setFocusable(false);
		previewHelp.setFont(previewHelp.getFont().deriveFont(previewHelp.getFont().getSize() - 1f));
		previewPanel.add(previewHelp, "spanx, growx, wrap para");

		//// Preview text pane (with styled text)
		previewTextPane = new JTextPane();
		previewTextPane.setEditable(false);
		previewTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
		previewTextPane.setBackground(Color.WHITE);
		previewTextPane.setMargin(new Insets(5, 5, 5, 5));

		previewPanel.add(previewTextPane, "grow, spanx");

		// Add event listeners for preview updates
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updatePreview();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updatePreview();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updatePreview();
			}
		};

		// Add listeners to all components that affect the preview
		altitudeColumnField.getDocument().addDocumentListener(documentListener);
		speedColumnField.getDocument().addDocumentListener(documentListener);
		directionColumnField.getDocument().addDocumentListener(documentListener);
		stdDeviationColumnField.getDocument().addDocumentListener(documentListener);

		// Add change listeners to all spinners - validate and update preview
		altitudeColumnSpinner.addChangeListener(e -> {
			validateColumnIndices();
			updatePreview();
		});
		speedColumnSpinner.addChangeListener(e -> {
			validateColumnIndices();
			updatePreview();
		});
		directionColumnSpinner.addChangeListener(e -> {
			validateColumnIndices();
			updatePreview();
		});
		stdDeviationColumnSpinner.addChangeListener(e -> {
			validateColumnIndices();
			updatePreview();
		});

		// Add header checkbox listener to update column field validation and label
		hasHeaderCheckBox.addItemListener(e -> {
			boolean hasHeaders = hasHeaderCheckBox.isSelected();

			// Update the column type label based on whether headers are present
			columnTypeLabel.setText(hasHeaders ?
					trans.get("CSVImportSettingsDialog.columnName") :
					trans.get("CSVImportSettingsDialog.columnIndex"));

			// Update field validation
			updateColumnFieldValidation();

			// If switching to non-header mode, validate indices immediately
			if (!hasHeaders) {
				validateColumnIndices();
			} else {
				// When switching to header mode, clear any error outlines
				resetErrorOutlines();
			}

			// Update preview
			updatePreview();
		});

		separatorComboBox.addItemListener(e -> updatePreview());

		altitudeUnitSelector.addItemListener(e -> updatePreview());
		speedUnitSelector.addItemListener(e -> updatePreview());
		directionUnitSelector.addItemListener(e -> updatePreview());
		stdDeviationUnitSelector.addItemListener(e -> updatePreview());

		// Initial column field validation setup
		updateColumnFieldValidation();

		// Initial preview update
		updatePreview();

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton cancelButton = new JButton(trans.get("button.cancel"));
		cancelButton.addActionListener(e -> dispose());

		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(e -> {
			// Validate column indices one final time
			validateColumnIndices();

			// When headers are not used, check for duplicate column indices
			if (!hasHeaderCheckBox.isSelected() && hasDuplicateColumnIndices()) {
				// Show error message
				JOptionPane.showMessageDialog(
						this,
						trans.get("CSVImportSettingsDialog.duplicateColumnError"),
						trans.get("CSVImportSettingsDialog.validationError"),
						JOptionPane.ERROR_MESSAGE
				);
				return; // Don't close the dialog
			}

			approved = true;
			dispose();
		});

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		// Create a scrollpane for the main content
		scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Better scrolling speed
		SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0)); // Scroll to top

		// Create a container panel with BorderLayout
		JPanel containerPanel = new JPanel(new BorderLayout());
		containerPanel.add(scrollPane, BorderLayout.CENTER);   // Scrollable content in center
		containerPanel.add(buttonPanel, BorderLayout.SOUTH);   // Buttons fixed at bottom

		// Set the container as content pane
		setContentPane(containerPanel);

		// Set preferred size instead of fixed size
		setPreferredSize(new Dimension(600, 550));
		pack();
		setLocationRelativeTo(owner);

		GUIUtil.installEscapeCloseButtonOperation(this, cancelButton);
		getRootPane().setDefaultButton(okButton);
	}

	/**
	 * Updates the UI based on whether headers are used
	 */
	private void updateColumnFieldValidation() {
		boolean hasHeaders = hasHeaderCheckBox.isSelected();

		// Get the card layout for each panel and show the appropriate component
		((CardLayout) altitudeColumnPanel.getLayout()).show(altitudeColumnPanel, hasHeaders ? "text" : "spinner");
		((CardLayout) speedColumnPanel.getLayout()).show(speedColumnPanel, hasHeaders ? "text" : "spinner");
		((CardLayout) directionColumnPanel.getLayout()).show(directionColumnPanel, hasHeaders ? "text" : "spinner");
		((CardLayout) stdDeviationColumnPanel.getLayout()).show(stdDeviationColumnPanel, hasHeaders ? "text" : "spinner");
	}

	/**
	 * Validates column indices dynamically and applies error styling to duplicates.
	 * Should be called whenever a spinner value changes or when switching to non-header mode.
	 */
	private void validateColumnIndices() {
		// Only validate if headers are not selected
		if (hasHeaderCheckBox.isSelected()) {
			return;
		}

		// Reset any previous error styling
		resetErrorOutlines();

		// Get all values
		int altIndex = (Integer) altitudeColumnSpinner.getValue();
		int speedIndex = (Integer) speedColumnSpinner.getValue();
		int dirIndex = (Integer) directionColumnSpinner.getValue();
		int stdDevIndex = (Integer) stdDeviationColumnSpinner.getValue();

		// Check for duplicates
		Set<Integer> usedIndices = new HashSet<>();
		Set<JSpinner> errorSpinners = new HashSet<>();

		// Check altitude index
		usedIndices.add(altIndex);

		// Check speed index
		if (!usedIndices.add(speedIndex)) {
			errorSpinners.add(speedColumnSpinner);
			// Find the other spinner with this value
			if (altIndex == speedIndex) errorSpinners.add(altitudeColumnSpinner);
		}

		// Check direction index
		if (!usedIndices.add(dirIndex)) {
			errorSpinners.add(directionColumnSpinner);
			// Find the other spinners with this value
			if (altIndex == dirIndex) errorSpinners.add(altitudeColumnSpinner);
			if (speedIndex == dirIndex) errorSpinners.add(speedColumnSpinner);
		}

		// Check std deviation index only if the field is being used (non-empty in text mode)
		if (!stdDeviationColumnField.getText().trim().isEmpty()) {
			if (!usedIndices.add(stdDevIndex)) {
				errorSpinners.add(stdDeviationColumnSpinner);
				// Find the other spinners with this value
				if (altIndex == stdDevIndex) errorSpinners.add(altitudeColumnSpinner);
				if (speedIndex == stdDevIndex) errorSpinners.add(speedColumnSpinner);
				if (dirIndex == stdDevIndex) errorSpinners.add(directionColumnSpinner);
			}
		}

		// Mark error spinners
		for (JSpinner spinner : errorSpinners) {
			spinner.putClientProperty("JComponent.outline", "error");
			// Mark the editor component as well for better visibility
			JComponent editor = spinner.getEditor();
			if (editor != null) {
				editor.putClientProperty("JComponent.outline", "error");
			}
		}
	}

	/**
	 * Checks if there are duplicate column indices when headers are not used.
	 * This method just calls validateColumnIndices() and then checks if any spinners have errors.
	 *
	 * @return true if duplicates are found, false otherwise
	 */
	private boolean hasDuplicateColumnIndices() {
		// Validate columns (this will apply error styling if needed)
		validateColumnIndices();

		// Check if any spinners have error styling
		return altitudeColumnSpinner.getClientProperty("JComponent.outline") == "error" ||
				speedColumnSpinner.getClientProperty("JComponent.outline") == "error" ||
				directionColumnSpinner.getClientProperty("JComponent.outline") == "error" ||
				stdDeviationColumnSpinner.getClientProperty("JComponent.outline") == "error";
	}

	/**
	 * Resets the error outlines on all column fields
	 */
	private void resetErrorOutlines() {
		// Reset outlines on text fields
		altitudeColumnField.putClientProperty("JComponent.outline", "");
		speedColumnField.putClientProperty("JComponent.outline", "");
		directionColumnField.putClientProperty("JComponent.outline", "");
		stdDeviationColumnField.putClientProperty("JComponent.outline", "");

		// Reset outlines on spinners
		altitudeColumnSpinner.putClientProperty("JComponent.outline", "");
		speedColumnSpinner.putClientProperty("JComponent.outline", "");
		directionColumnSpinner.putClientProperty("JComponent.outline", "");
		stdDeviationColumnSpinner.putClientProperty("JComponent.outline", "");

		// Reset outlines on spinner editors
		JComponent altEditor = altitudeColumnSpinner.getEditor();
		JComponent speedEditor = speedColumnSpinner.getEditor();
		JComponent dirEditor = directionColumnSpinner.getEditor();
		JComponent stdDevEditor = stdDeviationColumnSpinner.getEditor();

		if (altEditor != null) altEditor.putClientProperty("JComponent.outline", "");
		if (speedEditor != null) speedEditor.putClientProperty("JComponent.outline", "");
		if (dirEditor != null) dirEditor.putClientProperty("JComponent.outline", "");
		if (stdDevEditor != null) stdDevEditor.putClientProperty("JComponent.outline", "");
	}

	/**
	 * @return The name or index of the altitude column
	 */
	public String getAltitudeColumn() {
		if (hasHeader()) {
			return getAltitudeColumnName();
		} else {
			return getAltitudeColumnIndex().toString();
		}
	}

	/**
	 * @return The name of the altitude column
	 */
	public String getAltitudeColumnName() {
		return altitudeColumnField.getText().trim();
	}

	/**
	 * @return The index of the altitude column
	 */
	public Integer getAltitudeColumnIndex() {
		return (Integer) altitudeColumnSpinner.getValue();
	}

	/**
	 * @return The name or index of the speed column
	 */
	public String getSpeedColumn() {
		if (hasHeader()) {
			return getSpeedColumnName();
		} else {
			return getSpeedColumnIndex().toString();
		}
	}

	/**
	 * @return The name of the speed column
	 */
	public String getSpeedColumnName() {
		return speedColumnField.getText().trim();
	}

	/**
	 * @return The index of the speed column
	 */
	public Integer getSpeedColumnIndex() {
		return (Integer) speedColumnSpinner.getValue();
	}

	/**
	 * @return The name or index of the direction column
	 */
	public String getDirectionColumn() {
		if (hasHeader()) {
			return getDirectionColumnName();
		} else {
			return getDirectionColumnIndex().toString();
		}
	}

	/**
	 * @return The name of the direction column, even if the file has no header
	 */
	public String getDirectionColumnName() {
		return directionColumnField.getText().trim();
	}

	/**
	 * @return The index of the direction column
	 */
	public Integer getDirectionColumnIndex() {
		return (Integer) directionColumnSpinner.getValue();
	}

	/**
	 * @return The name or index of the standard deviation column (can be empty)
	 */
	public String getStdDeviationColumn() {
		if (hasHeader()) {
			return getStdDeviationColumnName();
		} else {
			return getStdDeviationColumnIndex().toString();
		}
	}

	/**
	 * @return The name of the std deviation column, even if the file has no header
	 */
	public String getStdDeviationColumnName() {
		return stdDeviationColumnField.getText().trim();
	}

	/**
	 * @return The index of the standard deviation column
	 */
	public Integer getStdDeviationColumnIndex() {
		return (Integer) stdDeviationColumnSpinner.getValue();
	}

	/**
	 * @return The selected unit for altitude values
	 */
	public Unit getAltitudeUnit() {
		return altitudeUnitSelector.getSelectedUnit();
	}

	/**
	 * @return The selected unit for speed values
	 */
	public Unit getSpeedUnit() {
		return speedUnitSelector.getSelectedUnit();
	}

	/**
	 * @return The selected unit for direction values
	 */
	public Unit getDirectionUnit() {
		return directionUnitSelector.getSelectedUnit();
	}

	/**
	 * @return The selected unit for standard deviation values
	 */
	public Unit getStdDeviationUnit() {
		return stdDeviationUnitSelector.getSelectedUnit();
	}

	/**
	 * @return The selected CSV separator
	 */
	public String getSeparator() {
		return (String) separatorComboBox.getSelectedItem();
	}

	/**
	 * @return Whether the CSV file has a header
	 */
	public boolean hasHeader() {
		return hasHeaderCheckBox.isSelected();
	}

	/**
	 * @return Whether the user approved the settings (clicked OK)
	 */
	public boolean isApproved() {
		return approved;
	}

	/**
	 * Updates the CSV file preview based on current settings while preserving scroll position
	 */
	private void updatePreview() {
		// Store the current viewport position
		final Point viewportPosition = scrollPane != null ? scrollPane.getViewport().getViewPosition() : null;

		// Clear the existing text
		previewTextPane.setText("");
		StyledDocument doc = previewTextPane.getStyledDocument();

		// Create style attributes for normal text
		SimpleAttributeSet normalStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(normalStyle, Color.BLACK);
		StyleConstants.setFontFamily(normalStyle, "Monospaced");

		// Create style attributes for optional fields (gray text)
		SimpleAttributeSet optionalStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(optionalStyle, Color.GRAY);
		StyleConstants.setFontFamily(optionalStyle, "Monospaced");

		// Create style attributes for column indices
		SimpleAttributeSet indexStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(indexStyle, new Color(0, 100, 0)); // Dark green
		StyleConstants.setFontFamily(indexStyle, "Monospaced");

		// Get selected separator
		String separator = (String) separatorComboBox.getSelectedItem();
		if (separator == null) separator = ",";  // Default if nothing selected

		// Replace tab and space with their visible representation for display
		String displaySeparator = separator;
		if (TAB.equals(separator)) {
			displaySeparator = "\\t";
		} else if (SPACE.equals(separator)) {
			displaySeparator = "\\s";
		}

		try {
			// Create header row if CSV has a header
			if (hasHeaderCheckBox.isSelected()) {
				doc.insertString(doc.getLength(), getAltitudeColumn() + displaySeparator, normalStyle);
				doc.insertString(doc.getLength(), getSpeedColumn() + displaySeparator, normalStyle);
				doc.insertString(doc.getLength(), getDirectionColumn(), normalStyle);

				// Add std deviation column if provided
				if (!getStdDeviationColumn().isEmpty()) {
					doc.insertString(doc.getLength(), displaySeparator + getStdDeviationColumn(), optionalStyle);
				}

				doc.insertString(doc.getLength(), "\n", normalStyle);
			}
			// If no header, add a comment showing which column is which
			else {
				doc.insertString(doc.getLength(), "# Column mapping: ", normalStyle);
				doc.insertString(doc.getLength(), getAltitudeColumn() + "=Altitude, ", indexStyle);
				doc.insertString(doc.getLength(), getSpeedColumn() + "=Speed, ", indexStyle);
				doc.insertString(doc.getLength(), getDirectionColumn() + "=Direction", indexStyle);

				// Add std deviation column if provided
				if (!getStdDeviationColumn().isEmpty()) {
					doc.insertString(doc.getLength(), ", " + getStdDeviationColumn() + "=StdDev", optionalStyle);
				}

				doc.insertString(doc.getLength(), "\n", normalStyle);
			}

			// Generate sample data rows
			double[] altitudes = {100.0, 200.0, 300.0, 400.0};
			double[] speeds = {5.0, 7.5, 10.0, 12.5};
			double[] directions = {Math.PI / 2, Math.PI * 5/12, Math.PI * 7/12, Math.PI * 3/4};
			double[] stdDevs = {1.0, 1.5, 2.0, 2.5};

			for (int i = 0; i < 4; i++) {
				// Format values based on selected units - just using raw values for preview
				doc.insertString(doc.getLength(),
						altitudeUnitSelector.getSelectedUnit().toString(altitudes[i]) + displaySeparator, normalStyle);
				doc.insertString(doc.getLength(),
						speedUnitSelector.getSelectedUnit().toString(speeds[i]) + displaySeparator, normalStyle);
				doc.insertString(doc.getLength(),
						directionUnitSelector.getSelectedUnit().toString(directions[i]), normalStyle);

				// Add std deviation if column specified (in gray)
				if (!getStdDeviationColumn().isEmpty()) {
					doc.insertString(doc.getLength(),
							displaySeparator + stdDeviationUnitSelector.getSelectedUnit().toString(stdDevs[i]), optionalStyle);
				}

				doc.insertString(doc.getLength(), "\n", normalStyle);
			}

			// Add ellipsis to indicate more data could follow
			doc.insertString(doc.getLength(), "...", normalStyle);

		} catch (BadLocationException e) {
			// This shouldn't happen, but log it just in case
			e.printStackTrace();
		}

		// TODO: I'd rather have a way to disable the auto scrolling, but I haven't found a way for it yet.
		//   using scrollPane.setAutoscrolls(false) doesn't seem to work
		if (viewportPosition != null) {
			SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(viewportPosition));
		}
	}
}