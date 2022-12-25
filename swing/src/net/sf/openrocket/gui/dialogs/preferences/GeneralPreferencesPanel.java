package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.ReleaseInfo;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.communication.UpdateInfoRetriever.ReleaseStatus;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Named;
import net.sf.openrocket.util.Utils;
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class GeneralPreferencesPanel extends PreferencesPanel {

	public GeneralPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("fillx, ins 30lp n n n"));
		
		
		//// Language selector
		Locale userLocale = null;
		{
			String locale = preferences.getString("locale", null);
			userLocale = L10N.toLocale(locale);
		}
		List<Named<Locale>> locales = new ArrayList<Named<Locale>>();
		for (Locale l : SwingPreferences.getSupportedLocales()) {
			locales.add(new Named<Locale>(l, l.getDisplayLanguage(l) + "/" + l.getDisplayLanguage()));
		}
		Collections.sort(locales);
		locales.add(0, new Named<Locale>(null, trans.get("generalprefs.languages.default")));
		
		final JComboBox<?> languageCombo = new JComboBox<Object>(locales.toArray());
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
				Locale l = selection.get();
				preferences.putString(Preferences.USER_LOCAL, l == null ? null : l.toString());
			}
		});
		this.add(new JLabel(trans.get("generalprefs.lbl.language")), "gapright para");
		this.add(languageCombo, "wrap rel, growx, sg combos");
		
		this.add(new StyledLabel(trans.get("generalprefs.lbl.languageEffect"), -3, Style.ITALIC), "span, wrap para*2");
		
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
				List<File> list = new ArrayList<File>();
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
		JButton button = new SelectColorButton(trans.get("pref.dlg.but.add"));
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
		button = new SelectColorButton(trans.get("pref.dlg.but.reset"));
		
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
		DescriptionArea desc = new DescriptionArea(trans.get("pref.dlg.DescriptionArea.Adddirectories"), 3, -3, false);
		desc.setBackground(getBackground());
		this.add(desc, "spanx, growx, wrap 40lp");
		
		
		

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
		button = new SelectColorButton(trans.get("pref.dlg.but.checknow"));
		//// Check for software updates now
		button.setToolTipText(trans.get("pref.dlg.ttip.Checkupdatesnow"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkForUpdates();
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

		//// Clear cached preferences
		final JButton clearCachedPreferences = new SelectColorButton(trans.get("pref.dlg.but.clearCachedPreferences"));
		clearCachedPreferences.setToolTipText(trans.get("pref.dlg.but.clearCachedPreferences.ttip"));
		clearCachedPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int resultYesNo = JOptionPane.showConfirmDialog(parent, trans.get("pref.dlg.clearCachedPreferences.message"),
						trans.get("pref.dlg.clearCachedPreferences.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (resultYesNo == JOptionPane.YES_OPTION) {
					preferences.clearPreferences();
				}
			}
		});
		this.add(clearCachedPreferences, "spanx, pushy, bottom, wrap");

	}


	private void checkForUpdates() {
		final UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();
		
		
		// Progress dialog
		final JDialog dialog1 = new JDialog(this.parentDialog, ModalityType.APPLICATION_MODAL);
		JPanel panel = new JPanel(new MigLayout());
		
		//// Checking for updates...
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Checkingupdates")), "wrap");
		
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		panel.add(bar, "growx, wrap para");
		
		//// Cancel button
		JButton cancel = new SelectColorButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog1.dispose();
			}
		});
		panel.add(cancel, "right");
		dialog1.add(panel);
		
		GUIUtil.setDisposableDialogOptions(dialog1, cancel);
		
		
		// Timer to monitor progress
		final Timer timer = new Timer(100, null);
		final long startTime = System.currentTimeMillis();
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!retriever.isRunning() || startTime + 10000 < System.currentTimeMillis()) {
					timer.stop();
					dialog1.dispose();
				}
			}
		};
		timer.addActionListener(listener);
		timer.start();
		
		
		// Wait for action
		dialog1.setVisible(true);
		
		
		// Check result
		UpdateInfo info = retriever.getUpdateInfo();

		// Something went wrong
		if (info == null) {
			JOptionPane.showMessageDialog(this,
					//// An error occurred while communicating with the server.
					trans.get("update.dlg.error"),
					//// Unable to retrieve update information
					trans.get("update.dlg.error.title"), JOptionPane.WARNING_MESSAGE, null);
			return;
		}

		// Something went wrong, but we know what went wrong
		if (info.getException() != null) {
			JOptionPane.showMessageDialog(this,
					info.getException().getMessage(),
					trans.get("update.dlg.exception.title"), JOptionPane.WARNING_MESSAGE, null);
			return;
		}

		// Nothing went wrong (yay!)
		ReleaseStatus status = info.getReleaseStatus();
		ReleaseInfo release = info.getLatestRelease();

		// Do nothing if the release is part of the ignore versions
		if (preferences.getIgnoreUpdateVersions().contains(release.getReleaseName())) {
			return;
		}

		// Display software updater dialog, based on the current build version status
		switch (status) {
			case LATEST:
				JOptionPane.showMessageDialog(this,
						//// You are running the latest version of OpenRocket.
						String.format(trans.get("update.dlg.latestVersion"), BuildProperties.getVersion()),
						//// No updates available
						trans.get("update.dlg.latestVersion.title"), JOptionPane.INFORMATION_MESSAGE, null);
				break;
			case NEWER:
				JOptionPane.showMessageDialog(this,
						//// You are running a newer version than the latest official release
						String.format("<html><body><p style='width: %dpx'>%s", 400, String.format(trans.get("update.dlg.newerVersion"),
								BuildProperties.getVersion(), release.getReleaseName())),
						//// Newer version detected
						trans.get("update.dlg.newerVersion.title"), JOptionPane.INFORMATION_MESSAGE, null);
				break;
			case OLDER:
				UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
				infoDialog.setVisible(true);
		}
	}
}
