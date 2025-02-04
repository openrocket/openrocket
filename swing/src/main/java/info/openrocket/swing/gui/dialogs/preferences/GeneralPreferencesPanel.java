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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.swing.gui.util.UpdateInfoRunner;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.gui.util.SimpleFileFilter;
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
		JButton button = new JButton(trans.get("pref.dlg.but.add"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				SimpleFileFilter filter =
						new SimpleFileFilter(
								//// All thrust curve files (*.eng; *.rse; *.zip; directories)
								trans.get("pref.dlg.Allthrustcurvefiles"),
								true, "eng", "rse", "zip");
				chooser.addChoosableFileFilter(filter);
				//// RASP motor files (*.eng)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.RASPfiles"),
						true, "eng"));
				//// RockSim engine files (*.rse)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.RockSimfiles"),
						true, "rse"));
				//// ZIP archives (*.zip)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.ZIParchives"),
						true, "zip"));
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (defaultDirectory != null) {
					chooser.setCurrentDirectory(defaultDirectory);
				}
				
				//// Add
				int returnVal = chooser.showDialog(GeneralPreferencesPanel.this, trans.get("pref.dlg.Add"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					log.info(Markers.USER_MARKER, "Adding user thrust curve: " + chooser.getSelectedFile());
					defaultDirectory = chooser.getCurrentDirectory();
					String text = field.getText().trim();
					if (text.length() > 0) {
						text += ";";
					}
					text += chooser.getSelectedFile().getAbsolutePath();
					field.setText(text);
				}
			}
		});
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
		desc.setBackground(GUIUtil.getUITheme().getBackgroundColor());
		desc.setForeground(GUIUtil.getUITheme().getTextColor());
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
		button = new JButton(trans.get("pref.dlg.but.add"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				SimpleFileFilter filter =
						new SimpleFileFilter(
								trans.get("pref.dlg.AllComponentPresetfiles"),
								true, "orc");
				chooser.addChoosableFileFilter(filter);
				//// OpenRocket component files (*.orc)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.ORCfiles"),
						true, "orc"));
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (defaultDirectory != null) {
					chooser.setCurrentDirectory(defaultDirectory);
				}

				//// Add
				int returnVal = chooser.showDialog(GeneralPreferencesPanel.this, trans.get("pref.dlg.Add"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					log.info(Markers.USER_MARKER, "Adding component preset file: " + chooser.getSelectedFile());
					defaultDirectory = chooser.getCurrentDirectory();
					String text = fieldCompPres.getText().trim();
					if (text.length() > 0) {
						text += ";";
					}
					text += chooser.getSelectedFile().getAbsolutePath();
					fieldCompPres.setText(text);
				}
			}
		});
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
}
