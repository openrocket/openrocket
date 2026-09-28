package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;

import info.openrocket.core.document.SaveSimulationDataMode;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.DropdownButton;
import info.openrocket.swing.gui.widgets.NativeFileChooser;
import net.miginfocom.swing.MigLayout;

/**
 * Preferences for opening and saving design files, and for the user-defined thrust curve and
 * component preset libraries.
 */
@SuppressWarnings("serial")
public class FilesPreferencesPanel extends PreferencesPanel {

	public FilesPreferencesPanel(PreferencesDialog parent) {
		super(parent, new MigLayout("fillx, ins 30lp n n n"));

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
