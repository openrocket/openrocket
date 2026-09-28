package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.core.startup.Application;
import info.openrocket.swing.startup.MotorDatabaseUpdateChecker;
import info.openrocket.swing.gui.util.UpdateInfoRunner;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.SaveSimulationDataMode;
import info.openrocket.core.l10n.L10N;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.Named;
import info.openrocket.core.util.Utils;

import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.util.PreferencesExporter;
import info.openrocket.swing.gui.util.PreferencesImporter;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.DropdownButton;
import info.openrocket.swing.gui.widgets.NativeFileChooser;

import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;


@SuppressWarnings("serial")
public class GeneralPreferencesPanel extends PreferencesPanel {

	public GeneralPreferencesPanel(PreferencesDialog parent) {
		super(parent, new MigLayout("fillx, ins 30lp n n n"));
		
		//// Language selector
		Locale userLocale;
		{
			String locale = preferences.getString("locale", null);
			userLocale = L10N.toLocale(locale);
		}
		List<Named<Locale>> locales = new ArrayList<>();
		for (Locale l : SwingPreferences.getSupportedLocales()) {
			locales.add(new Named<>(l, l.getDisplayLanguage(l) + "/" + l.getDisplayLanguage()));
		}
		Collections.sort(locales);
		locales.add(0, new Named<>(null, trans.get("generalprefs.languages.default")));
		
		final JComboBox<?> languageCombo = new JComboBox<>(locales.toArray());
		for (int i = 0; i < locales.size(); i++) {
			if (Utils.equals(userLocale, locales.get(i).get())) {
				languageCombo.setSelectedIndex(i);
			}
		}
		languageCombo.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				Named<Locale> selection = (Named<Locale>) languageCombo.getSelectedItem();
				if (selection == null) return;
				Locale l = selection.get();
				preferences.putString(ApplicationPreferences.USER_LOCAL, l == null ? null : l.toString());
			}
		});
		this.add(new JLabel(trans.get("generalprefs.lbl.language")), "gapright para");
		this.add(languageCombo, "wrap rel, growx, sg combos");
		
		this.add(new StyledLabel(trans.get("generalprefs.lbl.languageEffect"), -3, Style.ITALIC), "span, wrap rel");

		this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap para");

		//// User-defined thrust curves:
		this.add(new JLabel(trans.get("pref.dlg.lbl.User-definedthrust")), "spanx, wrap");
		final JTextField field = new JTextField();
		String str = preferences.getUserThrustCurveFilesAsString();
		field.setText(str);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			
			private void changed() {
				String text = field.getText();
				List<File> list = new ArrayList<>();
				for (String s : text.split(";")) {
					s = s.trim();
					if (s.length() > 0) {
						list.add(new File(s));
					}
				}
				preferences.setUserThrustCurveFiles(list);
			}
		});
		this.add(field, "w 100px, gapright unrel, spanx, growx, split");
		
		//// Add button
		JButton button = createAddPathsButton(field, "Adding user thrust curve: ",
				//// All thrust curve files (*.eng; *.rse; *.zip; *.db)
				new FileNameExtensionFilter(trans.get("pref.dlg.Allthrustcurvefiles"), "eng", "rse", "zip", "db"),
				//// RASP motor files (*.eng)
				new FileNameExtensionFilter(trans.get("pref.dlg.RASPfiles"), "eng"),
				//// RockSim engine files (*.rse)
				new FileNameExtensionFilter(trans.get("pref.dlg.RockSimfiles"), "rse"),
				//// ZIP archives (*.zip)
				new FileNameExtensionFilter(trans.get("pref.dlg.ZIParchives"), "zip"));
		this.add(button, "gapright unrel");
		
		//// Reset button
		button = new JButton(trans.get("pref.dlg.but.reset"));
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// First one sets to the default, but does not un-set the pref
				field.setText(preferences.getDefaultUserThrustCurveFile().getAbsolutePath());
				preferences.setUserThrustCurveFiles(null);
			}
		});
		this.add(button, "wrap");
		
		//// Add directories, RASP motor files (*.eng), RockSim engine files (*.rse) or ZIP archives separated by a semicolon (;) to load external thrust curves.  Changes will take effect the next time you start OpenRocket.
		DescriptionArea desc = new DescriptionArea(trans.get("pref.dlg.DescriptionArea.Adddirectories"), 3, -1.5f, false);
		desc.setBackground(UITheme.getColor(UITheme.Keys.BACKGROUND));
		desc.setForeground(UITheme.getColor(UITheme.Keys.TEXT));
		this.add(desc, "spanx, growx, wrap unrel");

		//// User-defined component presets:
		this.add(new JLabel(trans.get("pref.dlg.lbl.User-definedComponentPreset")), "spanx, wrap");
		final JTextField fieldCompPres = new JTextField();
		str = preferences.getUserComponentPresetFilesAsString();
		fieldCompPres.setText(str);
		fieldCompPres.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}

			private void changed() {
				String text = fieldCompPres.getText();
				List<File> list = new ArrayList<>();
				for (String s : text.split(";")) {
					s = s.trim();
					if (s.length() > 0) {
						list.add(new File(s));
					}
				}
				preferences.setUserComponentPresetFiles(list);
			}
		});
		this.add(fieldCompPres, "w 100px, gapright unrel, spanx, growx, split");

		//// Add button
		button = createAddPathsButton(fieldCompPres, "Adding component preset file: ",
				//// OpenRocket component files (*.orc)
				new FileNameExtensionFilter(trans.get("pref.dlg.ORCfiles"), "orc"));
		this.add(button, "gapright unrel");

		//// Reset button
		button = new JButton(trans.get("pref.dlg.but.reset"));

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// First one sets to the default, but does not un-set the pref
				fieldCompPres.setText(preferences.getDefaultUserComponentFile().getAbsolutePath());
				preferences.setUserComponentPresetFiles(null);
			}
		});
		this.add(button, "wrap");

		this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap para");


		//// Check for software updates at startup
		final JCheckBox softwareUpdateBox =
				new JCheckBox(trans.get("pref.dlg.checkbox.Checkupdates"));
		softwareUpdateBox.setSelected(preferences.getCheckUpdates());
		softwareUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setCheckUpdates(softwareUpdateBox.isSelected());
			}
		});
		this.add(softwareUpdateBox);
		
		//// Check now button
		button = new JButton(trans.get("pref.dlg.but.checknow"));
		//// Check for software updates now
		button.setToolTipText(trans.get("pref.dlg.ttip.Checkupdatesnow"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UpdateInfoRunner.checkForUpdates(parent);
			}
		});
		this.add(button, "right, wrap");

		//// Check for beta releases
		final JCheckBox betaUpdateBox = new JCheckBox(trans.get("pref.dlg.checkbox.CheckBetaupdates"));
		betaUpdateBox.setToolTipText(trans.get("pref.dlg.checkbox.CheckBetaupdates.ttip"));
		betaUpdateBox.setSelected(preferences.getCheckBetaUpdates());
		betaUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setCheckBetaUpdates(betaUpdateBox.isSelected());
			}
		});
		this.add(betaUpdateBox, "gapleft para, wrap");

		//// Check for motor database updates at startup
		final JCheckBox motorDatabaseUpdateBox = new JCheckBox(trans.get("pref.dlg.checkbox.CheckMotorDbUpdates"));
		motorDatabaseUpdateBox.setToolTipText(trans.get("pref.dlg.checkbox.CheckMotorDbUpdates.ttip"));
		motorDatabaseUpdateBox.setSelected(preferences.getCheckMotorDatabaseUpdates());
		motorDatabaseUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setCheckMotorDatabaseUpdates(motorDatabaseUpdateBox.isSelected());
			}
		});
		this.add(motorDatabaseUpdateBox);

		//// Check now button (motor database)
		button = new JButton(trans.get("pref.dlg.but.checknow"));
		button.setToolTipText(trans.get("pref.dlg.ttip.CheckMotorDbUpdatesNow"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorDatabaseUpdateChecker.checkForUpdatesNowAndInstallIfRequested(parentDialog);
			}
		});
		this.add(button, "right, wrap");

		//// Automatically install motor database updates
		final JCheckBox autoInstallMotorDatabaseUpdateBox =
				new JCheckBox(trans.get("pref.dlg.checkbox.AutoInstallMotorDbUpdates"));
		autoInstallMotorDatabaseUpdateBox.setToolTipText(trans.get("pref.dlg.checkbox.AutoInstallMotorDbUpdates.ttip"));
		autoInstallMotorDatabaseUpdateBox.setSelected(preferences.getAutoInstallMotorDatabaseUpdates());
		autoInstallMotorDatabaseUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoInstallMotorDatabaseUpdates(autoInstallMotorDatabaseUpdateBox.isSelected());
			}
		});
		this.add(autoInstallMotorDatabaseUpdateBox, "gapleft para, wrap");

		//// Open most recent file on startup
		final JCheckBox openRecentOnStartupBox = new JCheckBox(trans.get("pref.dlg.but.openlast"));
		openRecentOnStartupBox.setSelected(preferences.isAutoOpenLastDesignOnStartupEnabled());
		openRecentOnStartupBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoOpenLastDesignOnStartup(openRecentOnStartupBox.isSelected());
			}
		});
		this.add(openRecentOnStartupBox,"spanx, wrap");

		//// Simulation data to store when saving
		this.add(new JLabel(trans.get("pref.dlg.lbl.SaveSimulationData")), "spanx, split 2, gapright rel");
		final JComboBox<SaveSimulationDataMode> saveSimulationDataCombo = new JComboBox<>(SaveSimulationDataMode.values());
		saveSimulationDataCombo.setToolTipText(trans.get("pref.dlg.lbl.SaveSimulationData.ttip"));
		saveSimulationDataCombo.setSelectedItem(preferences.getSaveSimulationDataMode());
		saveSimulationDataCombo.addActionListener(e -> preferences.setSaveSimulationDataMode(
				(SaveSimulationDataMode) saveSimulationDataCombo.getSelectedItem()));
		this.add(saveSimulationDataCombo, "wrap");

		//// Save RASAero Format warning dialog
		final JCheckBox rasaeroWarningDialogBox = new JCheckBox(trans.get("pref.dlg.lbl.RASAeroWarning"));
		rasaeroWarningDialogBox.setSelected(preferences.getShowRASAeroFormatWarning());
		rasaeroWarningDialogBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setShowRASAeroFormatWarning(rasaeroWarningDialogBox.isSelected());
			}
		});
		this.add(rasaeroWarningDialogBox,"spanx, wrap");
		
		//// Save RockSim Format warning dialog
		final JCheckBox rocksimWarningDialogBox = new JCheckBox(trans.get("pref.dlg.lbl.RockSimWarning"));
		rocksimWarningDialogBox.setSelected(preferences.getShowRockSimFormatWarning());
		rocksimWarningDialogBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setShowRockSimFormatWarning(rocksimWarningDialogBox.isSelected());
			}
		});
		this.add(rocksimWarningDialogBox,"spanx, wrap");

		//// Show confirmation dialog when discarding preferences
		final JCheckBox prefsDiscardBox = new JCheckBox(trans.get("pref.dlg.checkbox.ShowDiscardPreferencesConfirmation"));
		prefsDiscardBox.setSelected(preferences.isShowDiscardPreferencesConfirmation());
		prefsDiscardBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				preferences.setShowDiscardPreferencesConfirmation(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		this.add(prefsDiscardBox,"spanx, wrap");

        //// Auto-open parts library
        final JCheckBox partsLibraryAutoOpenBox = new JCheckBox(trans.get("pref.dlg.checkbox.AutoOpenPartsLibrary"));
        partsLibraryAutoOpenBox.setToolTipText(trans.get("pref.dlg.checkbox.AutoOpenPartsLibrary.ttip"));
		partsLibraryAutoOpenBox.setSelected(preferences.isAutoOpenPartsLibrary());
        partsLibraryAutoOpenBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                preferences.setAutoOpenPartsLibrary(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        this.add(partsLibraryAutoOpenBox, "spanx, wrap");

		// Preference buttons
		JPanel buttonPanel = new JPanel(new MigLayout("fillx, ins 0"));

		//// Import preferences
		final JButton importPreferences = new JButton(trans.get("pref.dlg.but.importPreferences"));
		importPreferences.setToolTipText(trans.get("pref.dlg.but.importPreferences.ttip"));
		importPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean imported = PreferencesImporter.importPreferences(parent);
				if (imported) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(parent,
									trans.get("generalprefs.ImportWarning.msg"),
									trans.get("generalprefs.ImportWarning.title"),
									JOptionPane.WARNING_MESSAGE);

							// Need to execute after delay, otherwise the dialog will not be disposed
							GUIUtil.executeAfterDelay(100, () -> {
								PreferencesDialog.showPreferences(parent.getParentFrame());		// Refresh the preferences dialog
							});
						}
					});
				}
			}
		});
		buttonPanel.add(importPreferences);

		//// Export preferences
		final JButton exportPreferences = new JButton(trans.get("pref.dlg.but.exportPreferences"));
		exportPreferences.setToolTipText(trans.get("pref.dlg.but.exportPreferences.ttip"));
		exportPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PreferencesExporter.exportPreferences(parent, preferences.getPreferences());
			}
		});
		buttonPanel.add(exportPreferences);

		//// Reset all preferences
		final JButton resetAllPreferences = new JButton(trans.get("pref.dlg.but.resetAllPreferences"));
		resetAllPreferences.setToolTipText(trans.get("pref.dlg.but.resetAllPreferences.ttip"));
		resetAllPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int resultYesNo = JOptionPane.showConfirmDialog(parent, trans.get("pref.dlg.clearCachedPreferences.message"),
						trans.get("pref.dlg.clearCachedPreferences.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (resultYesNo == JOptionPane.YES_OPTION) {
					preferences.clearPreferences();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(parent,
									trans.get("generalprefs.ImportWarning.msg"),
									trans.get("generalprefs.ImportWarning.title"),
									JOptionPane.WARNING_MESSAGE);
							PreferencesDialog.showPreferences(parent.getParentFrame());        // Refresh the preferences dialog
						}
					});
				}
			}
		});
		buttonPanel.add(resetAllPreferences, "pushx, right, gaptop 20lp, wrap");

		this.add(buttonPanel, "spanx, growx, pushy, bottom, wrap");
	}

	/**
	 * Create an "Add" button that lets the user select files or a folder, and appends the selected paths to
	 * the semicolon-separated list in the text field.
	 *
	 * @param field the text field containing the list of paths
	 * @param logMessage the log message prefix for each added path
	 * @param filters the file filters for selecting files; the first one is selected by default
	 * @return the button
	 */
	private JButton createAddPathsButton(JTextField field, String logMessage, FileFilter... filters) {
		DropdownButton button = new DropdownButton(trans.get("pref.dlg.but.add"));

		//// Files...
		button.addMenuItem(trans.get("pref.dlg.but.addFiles"), e -> {
			NativeFileChooser chooser = new NativeFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			for (FileFilter filter : filters) {
				chooser.addChoosableFileFilter(filter);
			}
			chooser.setFileFilter(filters[0]);
			chooser.setMultiSelectionEnabled(true);
			addSelectedPaths(chooser, field, logMessage);
		});

		//// Folder...
		button.addMenuItem(trans.get("pref.dlg.but.addFolder"), e -> {
			NativeFileChooser chooser = new NativeFileChooser();
			chooser.setFileSelectionMode(NativeFileChooser.DIRECTORIES_ONLY);
			addSelectedPaths(chooser, field, logMessage);
		});

		return button;
	}

	private void addSelectedPaths(NativeFileChooser chooser, JTextField field, String logMessage) {
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());

		//// Add
		int returnVal = chooser.showDialog(this, trans.get("pref.dlg.Add"));
		if (returnVal != NativeFileChooser.APPROVE_OPTION) {
			return;
		}

		StringBuilder text = new StringBuilder(field.getText().trim());
		for (File file : chooser.getSelectedFiles()) {
			log.info(Markers.USER_MARKER, logMessage + file);
			if (text.length() > 0) {
				text.append(';');
			}
			text.append(file.getAbsolutePath());
		}
		field.setText(text.toString());
		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());
	}
}
