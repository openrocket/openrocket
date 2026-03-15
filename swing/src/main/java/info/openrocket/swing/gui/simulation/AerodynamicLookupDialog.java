package info.openrocket.swing.gui.simulation;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StringUtils;
import info.openrocket.swing.gui.components.FieldSeparatorComboBox;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Dialog for configuring drag and stability lookup tables loaded from CSV files.
 */
class AerodynamicLookupDialog extends JDialog {

	private static final Translator trans = Application.getTranslator();
	private static final List<String> DRAG_VALUE_COLUMNS = List.of("cd");
	private static final List<String> STABILITY_VALUE_COLUMNS = List.of("cn", "cm", "cp");

	private final SimulationOptions options;

	private Path dragCsv;
	private MachAoALookup dragTable;
	private char dragSeparator = ',';
	private Path stabilityCsv;
	private MachAoALookup stabilityTable;
	private char stabilitySeparator = ',';

	private JLabel dragSummaryLabel;
	private JButton clearDragButton;
	private JButton dragRefreshButton;
	private FieldSeparatorComboBox dragSeparatorCombo;
	private JTextArea dragExampleArea;
	private JScrollPane dragExampleScroll;
	private javax.swing.border.TitledBorder dragExampleBorder;
	private boolean dragModified = false;
	private DocumentListener dragDocumentListener;

	private JLabel stabilitySummaryLabel;
	private JButton clearStabilityButton;
	private JButton stabilityRefreshButton;
	private FieldSeparatorComboBox stabilitySeparatorCombo;
	private JTextArea stabilityExampleArea;
	private JScrollPane stabilityExampleScroll;
	private javax.swing.border.TitledBorder stabilityExampleBorder;
	private boolean stabilityModified = false;
	private DocumentListener stabilityDocumentListener;

	private static Color textColor;
	private static Color infoTextColor;

	static {
		initColors();
	}

	public AerodynamicLookupDialog(Window owner, SimulationOptions options) {
		super(owner, trans.get("AerodynamicLookupDialog.title"), ModalityType.APPLICATION_MODAL);
		this.options = options;

		this.dragCsv = options.getDragLookupCsvPath();
		this.dragTable = options.getDragLookupTable();
		this.dragSeparator = ','; // Default separator
		this.stabilityCsv = options.getStabilityLookupCsvPath();
		this.stabilityTable = options.getStabilityLookupTable();
		this.stabilitySeparator = ','; // Default separator

		initUI();
		updateDisplays();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(AerodynamicLookupDialog::updateColors);
	}

	public static void updateColors() {
		textColor = UITheme.getColor(UITheme.Keys.TEXT);
		infoTextColor = UITheme.getColor(UITheme.Keys.INFO);
	}

	private void initUI() {
		JPanel content = new JPanel(new MigLayout("fill, gap para"));
		setContentPane(content);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(trans.get("AerodynamicLookupDialog.lbl.drag"),
				createLookupSection(false));
		tabbedPane.addTab(trans.get("AerodynamicLookupDialog.lbl.stability"),
				createLookupSection(true));
		content.add(tabbedPane, "spanx, growx, growy, wrap para");

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(e -> dispose());
		buttons.add(cancel);

		JButton ok = new JButton(trans.get("button.ok"));
		ok.addActionListener(e -> applyAndClose());
		buttons.add(ok);

		content.add(buttons, "spanx, growx");
	}

	private JPanel createLookupSection(boolean stability) {
		JPanel panel = new JPanel(new MigLayout("fill, ins para, gap para"));

		// Load from file button
		JButton loadButton = new JButton(trans.get("AerodynamicLookupDialog.btn.loadFromFile"));
		panel.add(loadButton, "split 2");

		// Refresh button
		JButton refreshButton = new JButton(Icons.REFRESH);
		refreshButton.setToolTipText(trans.get("AerodynamicLookupDialog.btn.refresh.ttip"));
		refreshButton.setEnabled(false); // Initially disabled until data is loaded
		panel.add(refreshButton);

		// Clear button
		JButton clear = new JButton(trans.get("AerodynamicLookupDialog.btn.clear"));
		panel.add(clear, "wrap");

		// Summary label (on its own row to allow wrapping)
		JLabel summary = new JLabel("<html>" + trans.get("AerodynamicLookupDialog.summary.none") + "</html>");
		summary.setForeground(infoTextColor);
		panel.add(summary, "spanx, growx, wrap");

		// Field separator
		JLabel separatorLabel = new JLabel(trans.get("SimExpPan.lbl.Fieldsepstr"));
		String tip = trans.get("SimExpPan.lbl.longA1") + trans.get("SimExpPan.lbl.longA2");
		separatorLabel.setToolTipText(tip);
		panel.add(separatorLabel, "gapleft para, gapright rel");

		FieldSeparatorComboBox separatorCombo = new FieldSeparatorComboBox();
		separatorCombo.setToolTipText(tip);
		panel.add(separatorCombo, "wrap para");

		// Example format textarea
		String exampleKey = stability ? "AerodynamicLookupDialog.example.stability" : "AerodynamicLookupDialog.example.drag";
		JTextArea example = new JTextArea(trans.get(exampleKey));
		example.setEditable(false);
		example.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN,
				example.getFont().getSize()));
		example.setBackground(Color.WHITE);
		example.setForeground(Color.GRAY);
		example.setFocusable(true); // Make selectable but not editable
		example.setRows(3);
		JScrollPane exampleScroll = new JScrollPane(example);
		javax.swing.border.TitledBorder exampleBorder = BorderFactory.createTitledBorder(trans.get("AerodynamicLookupDialog.lbl.formatHint"));
		exampleScroll.setBorder(exampleBorder);
		panel.add(exampleScroll, "spanx, growx, growy");

		if (stability) {
			stabilitySummaryLabel = summary;
			clearStabilityButton = clear;
			stabilityRefreshButton = refreshButton;
			stabilitySeparatorCombo = separatorCombo;
			stabilityExampleArea = example;
			stabilityExampleScroll = exampleScroll;
			stabilityExampleBorder = exampleBorder;
			loadButton.addActionListener(e -> chooseStabilityLookup());
			refreshButton.addActionListener(e -> refreshStabilityLookup());
			clear.addActionListener(e -> {
				stabilityCsv = null;
				stabilityTable = null;
				updateDisplays();
			});
			separatorCombo.addActionListener(e -> {
				// Just update the example format display, don't reload the file
				// The separator is only used when loading a new file
				updateExampleFormat(true);
			});
		} else {
			dragSummaryLabel = summary;
			clearDragButton = clear;
			dragRefreshButton = refreshButton;
			dragSeparatorCombo = separatorCombo;
			dragExampleArea = example;
			dragExampleScroll = exampleScroll;
			dragExampleBorder = exampleBorder;
			loadButton.addActionListener(e -> chooseDragLookup());
			refreshButton.addActionListener(e -> refreshDragLookup());
			clear.addActionListener(e -> {
				dragCsv = null;
				dragTable = null;
				updateDisplays();
			});
			separatorCombo.addActionListener(e -> {
				// Just update the example format display, don't reload the file
				// The separator is only used when loading a new file
				updateExampleFormat(false);
			});
		}

		return panel;
	}


	private void chooseDragLookup() {
		JFileChooser chooser = createCsvFileChooser(dragCsv);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());
		Path selected = chooser.getSelectedFile().toPath();
		try {
			char separator = dragSeparatorCombo.getSeparatorChar();
			MachAoALookup table = CsvMachAoALookup.fromCsv(selected, DRAG_VALUE_COLUMNS, separator);
			dragCsv = normalizePath(selected);
			dragTable = table;
			dragSeparator = separator;
			// Clear stored CSV rows when loading new file - will read from file
			options.setDragLookup(dragCsv, dragTable, null);
			updateDisplays();
		} catch (RuntimeException ex) {
			showLookupError(selected, ex);
		}
	}

	private void chooseStabilityLookup() {
		JFileChooser chooser = createCsvFileChooser(stabilityCsv);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());
		Path selected = chooser.getSelectedFile().toPath();
		try {
			char separator = stabilitySeparatorCombo.getSeparatorChar();
			MachAoALookup table = CsvMachAoALookup.fromCsv(selected, STABILITY_VALUE_COLUMNS, separator);
			stabilityCsv = normalizePath(selected);
			stabilityTable = table;
			stabilitySeparator = separator;
			// Clear stored CSV rows when loading new file - will read from file
			options.setStabilityLookup(stabilityCsv, stabilityTable, null);
			updateDisplays();
		} catch (RuntimeException ex) {
			showLookupError(selected, ex);
		}
	}

	private void reloadDragLookup() {
		if (dragCsv == null) {
			return;
		}
		try {
			char separator = dragSeparatorCombo.getSeparatorChar();
			MachAoALookup table = CsvMachAoALookup.fromCsv(dragCsv, DRAG_VALUE_COLUMNS, separator);
			dragTable = table;
			dragSeparator = separator;
			// Clear stored CSV rows when refreshing - will read fresh data from file
			options.setDragLookup(dragCsv, dragTable, null);
			updateDisplays();
		} catch (RuntimeException ex) {
			showLookupError(dragCsv, ex);
		}
	}

	private void reloadStabilityLookup() {
		if (stabilityCsv == null) {
			return;
		}
		try {
			char separator = stabilitySeparatorCombo.getSeparatorChar();
			MachAoALookup table = CsvMachAoALookup.fromCsv(stabilityCsv, STABILITY_VALUE_COLUMNS, separator);
			stabilityTable = table;
			stabilitySeparator = separator;
			// Clear stored CSV rows when refreshing - will read fresh data from file
			options.setStabilityLookup(stabilityCsv, stabilityTable, null);
			updateDisplays();
		} catch (RuntimeException ex) {
			showLookupError(stabilityCsv, ex);
		}
	}

	private void refreshDragLookup() {
		if (dragCsv == null) {
			return;
		}
		if (!Files.exists(dragCsv)) {
			JOptionPane.showMessageDialog(this,
					String.format(trans.get("AerodynamicLookupDialog.error.fileNotFound"), dragCsv),
					trans.get("AerodynamicLookupDialog.error.title"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		reloadDragLookup();
	}

	private void refreshStabilityLookup() {
		if (stabilityCsv == null) {
			return;
		}
		if (!Files.exists(stabilityCsv)) {
			JOptionPane.showMessageDialog(this,
					String.format(trans.get("AerodynamicLookupDialog.error.fileNotFound"), stabilityCsv),
					trans.get("AerodynamicLookupDialog.error.title"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		reloadStabilityLookup();
	}

	private void applyAndClose() {
		// Validate and apply drag lookup if modified
		List<String> dragCsvRows = null;
		if (dragModified && dragCsv != null) {
			try {
				String editedText = dragExampleArea.getText();
				char separator = dragSeparatorCombo.getSeparatorChar();
				// Store all lines (including comments) to preserve them
				List<String> allLines = editedText.lines()
						.map(String::trim)
						.collect(Collectors.toList());
				// Filter comments only when parsing for the table
				List<String> dataLines = allLines.stream()
						.filter(line -> !line.isEmpty() && !line.startsWith("#"))
						.collect(Collectors.toList());
				MachAoALookup table = CsvMachAoALookup.parse(dataLines, DRAG_VALUE_COLUMNS, separator);
				dragTable = table;
				dragCsvRows = allLines; // Store all lines including comments
				dragModified = false;
			} catch (Exception ex) {
				showLookupError(dragCsv, ex);
				return; // Don't close dialog on error
			}
		}
		
		// Validate and apply stability lookup if modified
		List<String> stabilityCsvRows = null;
		if (stabilityModified && stabilityCsv != null) {
			try {
				String editedText = stabilityExampleArea.getText();
				char separator = stabilitySeparatorCombo.getSeparatorChar();
				// Store all lines (including comments) to preserve them
				List<String> allLines = editedText.lines()
						.map(String::trim)
						.collect(Collectors.toList());
				// Filter comments only when parsing for the table
				List<String> dataLines = allLines.stream()
						.filter(line -> !line.isEmpty() && !line.startsWith("#"))
						.collect(Collectors.toList());
				MachAoALookup table = CsvMachAoALookup.parse(dataLines, STABILITY_VALUE_COLUMNS, separator);
				stabilityTable = table;
				stabilityCsvRows = allLines; // Store all lines including comments
				stabilityModified = false;
			} catch (Exception ex) {
				showLookupError(stabilityCsv, ex);
				return; // Don't close dialog on error
			}
		}

		// Apply the lookups
		if (dragCsv == null || dragTable == null) {
			options.clearDragLookup();
		} else {
			options.setDragLookup(dragCsv, dragTable, dragCsvRows);
		}

		if (stabilityCsv == null || stabilityTable == null) {
			options.clearStabilityLookup();
		} else {
			options.setStabilityLookup(stabilityCsv, stabilityTable, stabilityCsvRows);
		}

		dispose();
	}

	private void updateDisplays() {
		updateSectionDisplay(dragCsv, dragTable, dragSummaryLabel, clearDragButton, true);
		updateSectionDisplay(stabilityCsv, stabilityTable, stabilitySummaryLabel, clearStabilityButton, false);
		updateExampleFormat(false);
		updateExampleFormat(true);
	}

	private void updateSectionDisplay(Path path, MachAoALookup table, JLabel summaryLabel,
			JButton clearButton, boolean isDrag) {
		// Reset modification flag when clearing or loading new file
		if (isDrag) {
			dragModified = false;
		} else {
			stabilityModified = false;
		}
		
		// Update separator combo box if a file is loaded
		if (path != null && table != null) {
			if (isDrag && dragSeparatorCombo != null) {
				dragSeparatorCombo.setSeparatorChar(dragSeparator);
			} else if (!isDrag && stabilitySeparatorCombo != null) {
				stabilitySeparatorCombo.setSeparatorChar(stabilitySeparator);
			}
		}
		if (summaryLabel == null) {
			return;
		}
		if (path == null || table == null) {
			summaryLabel.setText("<html>" + trans.get("AerodynamicLookupDialog.summary.none") + "</html>");
			summaryLabel.setToolTipText(null);
			if (clearButton != null) {
				clearButton.setEnabled(false);
			}
			// Disable refresh button when no data is loaded
			if (isDrag && dragRefreshButton != null) {
				dragRefreshButton.setEnabled(false);
			} else if (!isDrag && stabilityRefreshButton != null) {
				stabilityRefreshButton.setEnabled(false);
			}
			return;
		}
		updateSummaryLabel(!isDrag); // isDrag: true=drag, false=stability; updateSummaryLabel expects stability flag
		if (clearButton != null) {
			clearButton.setEnabled(true);
		}
		// Enable refresh button when data is loaded
		if (isDrag && dragRefreshButton != null) {
			dragRefreshButton.setEnabled(true);
		} else if (!isDrag && stabilityRefreshButton != null) {
			stabilityRefreshButton.setEnabled(true);
		}
	}

	private void updateExampleFormat(boolean stability) {
		JTextArea exampleArea = stability ? stabilityExampleArea : dragExampleArea;
		JScrollPane exampleScroll = stability ? stabilityExampleScroll : dragExampleScroll;
		javax.swing.border.TitledBorder exampleBorder = stability ? stabilityExampleBorder : dragExampleBorder;
		FieldSeparatorComboBox separatorCombo = stability ? stabilitySeparatorCombo : dragSeparatorCombo;
		Path csvPath = stability ? stabilityCsv : dragCsv;
		MachAoALookup table = stability ? stabilityTable : dragTable;

		if (exampleArea == null || exampleScroll == null || exampleBorder == null) {
			return;
		}

		char separator = separatorCombo != null ? separatorCombo.getSeparatorChar() : ',';
		String separatorStr = String.valueOf(separator);

		// Remove existing document listener before making programmatic changes
		DocumentListener existingListener = stability ? stabilityDocumentListener : dragDocumentListener;
		if (existingListener != null) {
			exampleArea.getDocument().removeDocumentListener(existingListener);
		}

		if (csvPath != null && table != null) {
			// Show loaded data - make editable
			try {
				// Use stored CSV rows if available (preserves edits), otherwise read from file
				List<String> lines;
				if (stability) {
					lines = options.getStabilityLookupCsvRows();
				} else {
					lines = options.getDragLookupCsvRows();
				}
				
				if (lines == null || lines.isEmpty()) {
					// No stored rows, read from file
					lines = Files.readAllLines(csvPath);
				}
				
				// Display all lines (including comments) - limit to first 20 lines for display
				// Preserve comments and empty lines for user editing
				List<String> displayLines = lines.stream()
						.limit(20) // Limit to first 20 lines for display
						.collect(Collectors.toList());
				String loadedData = String.join("\n", displayLines);
				
				// Set text without triggering modification (listener is removed)
				exampleArea.setText(loadedData);
				exampleArea.setEditable(true);
				exampleArea.setForeground(Color.BLACK);
				exampleArea.setFocusable(true);
				exampleBorder.setTitle(trans.get("AerodynamicLookupDialog.lbl.loadedData"));
				
				// Add document listener to track modifications
				DocumentListener listener = new DocumentListener() {
					@Override
					public void insertUpdate(DocumentEvent e) {
						markModified(stability);
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						markModified(stability);
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						markModified(stability);
					}
				};
				exampleArea.getDocument().addDocumentListener(listener);
				
				// Store listener reference for future removal
				if (stability) {
					stabilityDocumentListener = listener;
				} else {
					dragDocumentListener = listener;
				}
			} catch (IOException e) {
				// If we can't read the file, fall back to example
				updateExampleFormatText(exampleArea, exampleBorder, separatorStr, stability);
				// Clear listener reference
				if (stability) {
					stabilityDocumentListener = null;
				} else {
					dragDocumentListener = null;
				}
			}
		} else {
			// Show example format
			updateExampleFormatText(exampleArea, exampleBorder, separatorStr, stability);
			// Clear listener reference
			if (stability) {
				stabilityDocumentListener = null;
			} else {
				dragDocumentListener = null;
			}
		}
		exampleScroll.repaint();
	}
	
	private void markModified(boolean stability) {
		if (stability) {
			if (!stabilityModified) {
				stabilityModified = true;
				updateSummaryLabel(stability);
			}
		} else {
			if (!dragModified) {
				dragModified = true;
				updateSummaryLabel(stability);
			}
		}
	}
	
	private void updateSummaryLabel(boolean stability) {
		JLabel summaryLabel = stability ? stabilitySummaryLabel : dragSummaryLabel;
		Path csvPath = stability ? stabilityCsv : dragCsv;
		MachAoALookup table = stability ? stabilityTable : dragTable;
		boolean modified = stability ? stabilityModified : dragModified;
		
		if (summaryLabel == null) {
			return;
		}
		
		if (csvPath == null || table == null) {
			summaryLabel.setText("<html>" + trans.get("AerodynamicLookupDialog.summary.none") + "</html>");
			summaryLabel.setToolTipText(null);
			return;
		}
		
		String fileName = csvPath.getFileName() != null ? csvPath.getFileName().toString() : csvPath.toString();
		String detail = formatLookupSummary(trans, table);
		String fullText = fileName + " - " + detail;
		String escapedText = StringUtils.escapeHtml(fullText);
		
		if (modified) {
			escapedText += " <i>(modified)</i>";
			fullText += " (modified)";
		}
		
		summaryLabel.setText("<html>" + escapedText + "</html>");
		summaryLabel.setToolTipText(fullText);
	}

	private void updateExampleFormatText(JTextArea exampleArea, javax.swing.border.TitledBorder exampleBorder,
			String separator, boolean stability) {
		String exampleKey = stability ? "AerodynamicLookupDialog.example.stability" : "AerodynamicLookupDialog.example.drag";
		String exampleText = trans.get(exampleKey);
		// Replace commas with the selected separator
		exampleText = exampleText.replace(",", separator);
		exampleArea.setText(exampleText);
		exampleArea.setEditable(false);
		exampleArea.setForeground(Color.GRAY);
		exampleArea.setFocusable(true); // Make selectable but not editable
		exampleBorder.setTitle(trans.get("AerodynamicLookupDialog.lbl.formatHint"));
	}

	private JFileChooser createCsvFileChooser(Path currentPath) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(trans.get("AerodynamicLookupDialog.title.choose"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				trans.get("AerodynamicLookupDialog.csvFilter"), "csv", "txt", "dat");
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (currentPath != null) {
			java.io.File currentFile = currentPath.toFile();
			if (currentFile.getParentFile() != null) {
				chooser.setCurrentDirectory(currentFile.getParentFile());
			}
			chooser.setSelectedFile(currentFile);
		}
		return chooser;
	}

	private void showLookupError(Path path, Exception ex) {
		String reason = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
		JOptionPane.showMessageDialog(this,
				String.format(trans.get("AerodynamicLookupDialog.error.msg"), path, reason),
				trans.get("AerodynamicLookupDialog.error.title"),
				JOptionPane.ERROR_MESSAGE);
	}

	private static Path normalizePath(Path path) {
		return path == null ? null : path.toAbsolutePath().normalize();
	}

	static String formatLookupSummary(Translator translator, MachAoALookup table) {
		String columns = table.getValueColumns().stream()
				.map(name -> name.toUpperCase(Locale.ROOT))
				.collect(java.util.stream.Collectors.joining(", "));
		String machMin = formatDouble(table.getMinMach());
		String machMax = formatDouble(table.getMaxMach());
		if (table.hasAoA()) {
			String aoaMin = formatDouble(table.getMinAoA());
			String aoaMax = formatDouble(table.getMaxAoA());
			return String.format(translator.get("AerodynamicLookupDialog.summaryWithAoA"),
					machMin, machMax, aoaMin, aoaMax, columns);
		}
		return String.format(translator.get("AerodynamicLookupDialog.summaryNoAoA"),
				machMin, machMax, columns);
	}

	private static String formatDouble(double value) {
		DecimalFormat format = new DecimalFormat("0.###");
		return format.format(value);
	}
}
