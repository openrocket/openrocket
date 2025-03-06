package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;

import static info.openrocket.swing.gui.components.CsvOptionPanel.SPACE;
import static info.openrocket.swing.gui.components.CsvOptionPanel.TAB;

public class MultiLevelWindEditDialog extends JDialog {
	private final MultiLevelWindTable windTable;
	private final WindProfileDialog.WindLevelVisualization visualization;
	private static final Translator trans = Application.getTranslator();

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

		// Create visualization panel
		visualization = new WindProfileDialog.WindLevelVisualization(model,
				UnitGroup.UNITS_DISTANCE.getSIUnit(), 
				UnitGroup.UNITS_WINDSPEED.getSIUnit());
		visualization.setPreferredSize(new Dimension(300, 400));

		// Set up synchronization between table and visualization
		windTable.addChangeListener(visualization);
		windTable.addSelectionListener(visualization::setSelectedLevel);

		// Add visualization controls
		JPanel visPanel = new JPanel(new BorderLayout());
		visPanel.add(visualization, BorderLayout.CENTER);

		JCheckBox showDirectionsCheckBox = new JCheckBox(trans.get("WindProfileEditorDlg.checkbox.ShowWindDirections"));
		showDirectionsCheckBox.setSelected(true);
		showDirectionsCheckBox.addActionListener(e -> {
			visualization.setShowDirections(showDirectionsCheckBox.isSelected());
			visualization.repaint();
		});

		JPanel visControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		visControlPanel.add(showDirectionsCheckBox);
		visPanel.add(visControlPanel, BorderLayout.SOUTH);

		// Add table controls (add button, etc.)
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);

		// Add New Level
		JButton addRowButton = new JButton(trans.get("WindProfileEditorDlg.but.AddNewLevel"));
		addRowButton.setIcon(Icons.FILE_NEW);
		addRowButton.addActionListener(e -> windTable.addRow());

		// Delete levels panel
		JPanel deletePanel = new JPanel();
		deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));

		//// Delete Level
		JButton deleteRowButton = new JButton(trans.get("WindProfileEditorDlg.but.DeleteLevel"));
		deleteRowButton.setToolTipText(trans.get("WindProfileEditorDlg.but.deleteWindLevel.ttip"));
		deleteRowButton.setIcon(Icons.EDIT_DELETE);
		deleteRowButton.addActionListener(e -> windTable.deleteSelectedRow());
		// Initially disable until a row is selected
		deleteRowButton.setEnabled(false);

		// Add listener to enable/disable delete button based on selection
		windTable.addSelectionListener(selectedLevel -> {
			deleteRowButton.setEnabled(selectedLevel != null && model.getLevels().size() > 1);
		});

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

		// Add the reset button to the button panel after the delete button
		deletePanel.add(deleteRowButton);
		deletePanel.add(resetButton);

		// Import Levels
		JButton importButton = new JButton(trans.get("WindProfileEditorDlg.but.importLevels"));
		importButton.setToolTipText(trans.get("WindProfileEditorDlg.but.importLevels.ttip"));
		importButton.setIcon(Icons.IMPORT);
		importButton.addActionListener(e -> {
			// Create a text box pop up where you can paste a CSV file
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
			fileChooser.setDialogTitle(trans.get("WindProfileEditorDlg.dlg.importLevels.title"));

			fileChooser.addChoosableFileFilter(FileHelper.CSV_FILTER);
			fileChooser.setFileFilter(FileHelper.CSV_FILTER);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);

			// Accessory panel
			//// CSV file description
			JPanel accessoryPanel = new JPanel(new MigLayout());
			accessoryPanel.setBorder(BorderFactory.createTitledBorder(trans.get("WindProfileEditorDlg.dlg.importLevels.accessoryPanel.title")));

			JTextArea descriptionArea = new JTextArea(trans.get("WindProfileEditorDlg.dlg.importLevels.accessoryPanel.desc"), 6, 30);
			descriptionArea.setEditable(false);
			descriptionArea.setBackground(null);
			accessoryPanel.add(descriptionArea, "spanx, wrap");

			accessoryPanel.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap");

			//// Field separation
			JLabel label = new JLabel(trans.get("SimExpPan.lbl.Fieldsepstr"));
			String ttip = trans.get("SimExpPan.lbl.longA1") +
					trans.get("SimExpPan.lbl.longA2");
			label.setToolTipText(ttip);
			accessoryPanel.add(label, "gapright unrel");

			JComboBox<String> fieldSeparator = new JComboBox<>(new String[]{",", ";", SPACE, TAB});
			fieldSeparator.setEditable(true);
			fieldSeparator.setSelectedItem(Application.getPreferences().getString(ApplicationPreferences.EXPORT_FIELD_SEPARATOR, ","));
			fieldSeparator.setToolTipText(ttip);
			accessoryPanel.add(fieldSeparator, "growx, wrap");

			fileChooser.setAccessory(accessoryPanel);

			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				selectedFile = FileHelper.forceExtension(selectedFile, "csv");

				((SwingPreferences) Application.getPreferences()).setDefaultDirectory(fileChooser.getCurrentDirectory());
				Application.getPreferences().putString(ApplicationPreferences.EXPORT_FIELD_SEPARATOR, (String) fieldSeparator.getSelectedItem());

				// Show a warning message that the current levels will be overwritten
				if (!model.getLevels().isEmpty()) {
					int result = JOptionPane.showConfirmDialog(this, trans.get("WindProfileEditorDlg.dlg.overwriteLevels.msg"),
							trans.get("WindProfileEditorDlg.dlg.overwriteLevels.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result != JOptionPane.YES_OPTION) {
						return;
					}
				}

				// Import the CSV file
				try {
					windTable.importLevels(selectedFile, (String) fieldSeparator.getSelectedItem());
				} catch (IllegalArgumentException ex) {
					windTable.resetLevels();
					JOptionPane.showMessageDialog(this, new String[] {
							trans.get("WindProfileEditorDlg.msg.importLevelsError"),
							ex.getMessage() }, trans.get("WindProfileEditorDlg.msg.importLevelsError.title"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JPanel buttonPanel = new JPanel(new MigLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		buttonPanel.add(addRowButton, "top");
		buttonPanel.add(deletePanel, "top");
		buttonPanel.add(importButton, "gapleft para, top");

		tablePanel.add(buttonPanel, BorderLayout.SOUTH);

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

		SwingUtilities.invokeLater(windTable::clearSelection);
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
