package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.PreferencesExporter;
import info.openrocket.swing.gui.util.PreferencesImporter;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesDialog extends JDialog {
	private static final Logger log = LoggerFactory
			.getLogger(PreferencesDialog.class);

	private static final Translator trans = Application.getTranslator();

	private final SwingPreferences preferences = (SwingPreferences) Application
			.getPreferences();

	private BasicFrame parentFrame;

	private boolean storePreferences = true;
	private File initPrefsFile = null;

	private PreferencesDialog(BasicFrame parent) {
		// // Preferences
		super(parent, trans.get("pref.dlg.title.Preferences"),
				Dialog.ModalityType.APPLICATION_MODAL);

		this.parentFrame = parent;

		// First store the initial preferences
		initPrefsFile = storeInitPreferences();

		JPanel panel = new JPanel(new MigLayout("fill, gap unrel", "[grow]",
				"[grow][]"));

		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "grow, wrap");

		// Options and Miscellaneous options
		tabbedPane.addTab(trans.get("pref.dlg.tab.Options"), null,
				new GeneralPreferencesPanel(this),
				trans.get("pref.dlg.tab.Miscellaneousoptions"));
		// Designer options
		tabbedPane.addTab(trans.get("pref.dlg.tab.Design"), null,
				new DesignPreferencesPanel(), trans.get("pref.dlg.tab.Design"));
		// Simulation options
		tabbedPane.addTab(trans.get("pref.dlg.tab.Simulation"), null,
				new SimulationPreferencesPanel(),
				trans.get("pref.dlg.tab.Design"));
		// Launch options
		tabbedPane.addTab(trans.get("pref.dlg.tab.Launch"), null,
				new LaunchPreferencesPanel(), trans.get("pref.dlg.tab.Launch"));
		// Units and Default units
		tabbedPane.addTab(trans.get("pref.dlg.tab.Units"), null,
				new UnitsPreferencesPanel(this),
				trans.get("pref.dlg.tab.Defaultunits"));
		// Materials and Custom materials
		tabbedPane.addTab(trans.get("pref.dlg.tab.Materials"), null,
				new MaterialEditPanel(),
				trans.get("pref.dlg.tab.Custommaterials"));
		// Decal Editor selection
		tabbedPane.addTab(trans.get("pref.dlg.tab.Graphics"),
				new GraphicsPreferencesPanel(this));

		// Default Colors Preferences
		// tabbedPane.addTab(trans.get("pref.dlg.tab.Colors"),
		// new DisplayPreferencesPanel());


		//// Cancel button
		JButton cancelButton = new SelectColorButton(trans.get("dlg.but.cancel"));
		cancelButton.setToolTipText(trans.get("SimulationConfigDialog.btn.Cancel.ttip"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Apply the cancel operation if set to auto discard in preferences
				if (!preferences.isShowDiscardPreferencesConfirmation()) {
					closeDialog(false);
					return;
				}

				// Yes/No dialog: Are you sure you want to discard your changes?
				JPanel msg = createCancelOperationContent();
				int resultYesNo = JOptionPane.showConfirmDialog(PreferencesDialog.this, msg,
						trans.get("PreferencesDialog.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (resultYesNo == JOptionPane.YES_OPTION) {
					closeDialog(false);
				}
			}
		});
		panel.add(cancelButton, "span, split 2, right, tag cancel");

		//// Ok button
		JButton okButton = new SelectColorButton(trans.get("dlg.but.ok"));
		okButton.setToolTipText(trans.get("SimulationConfigDialog.btn.OK.ttip"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog(true);
			}
		});
		panel.add(okButton, "tag ok");



		this.setContentPane(panel);
		pack();
		this.setLocationRelativeTo(null);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// We don't want to lose the preference for the confirmation dialog
				boolean isShowDiscardConfirmation = preferences.isShowDiscardPreferencesConfirmation();

				// Either store changed preferences (if OK) or reload initial preferences (if Cancel)
				if (storePreferences) {
					preferences.storeDefaultUnits();
				} else {
					loadInitPreferences();
				}

				// Store the preference for showing the confirmation dialog
				preferences.setShowDiscardPreferencesConfirmation(isShowDiscardConfirmation);

				// Delete the init prefs
				if (initPrefsFile != null) {
					initPrefsFile.delete();
				}

				// Make sure unit change applies to the rocket figure
				if (parent != null) {
					parent.getRocketPanel().updateExtras();
					parent.getRocketPanel().updateFigures();
					parent.getRocketPanel().updateRulers();
				}
			}
		});

		GUIUtil.setDisposableDialogOptions(this, okButton);
	}

	public BasicFrame getParentFrame() {
		return parentFrame;
	}

	private void closeDialog(boolean storeChanges) {
		storePreferences = storeChanges;
		PreferencesDialog.this.setVisible(false);
		PreferencesDialog.this.dispose();
	}

	/**
	 * Store the intial preferences in a temporary file, and return that file.
	 * @return the file containing the initial preferences, or null if something went wrong
	 */
	private File storeInitPreferences() {
		try {
			File outputFile = Files.createTempFile("ORInitPrefs_" + System.currentTimeMillis(), ".xml").toFile();
			try (FileOutputStream outputFos = new FileOutputStream(outputFile)) {
				PreferencesExporter.exportPreferencesToFile(preferences.getPreferences(), outputFos, false);
				log.debug("Initial preferences stored in temporary file: " + outputFile.getAbsolutePath());
			} catch (BackingStoreException e) {
				log.error("Could not store initial preferences", e);
				return null;
			}
			return outputFile;
		} catch (IOException e) {
			log.error("Could not create temporary preferences file", e);
			return null;
		}
	}

	/**
	 * Loads the initial stored preferences back (restores preferences).
	 */
	private void loadInitPreferences() {
		if (initPrefsFile == null) {
			return;
		}
		PreferencesImporter.importPreferences(initPrefsFile);
	}

	private JPanel createCancelOperationContent() {
		JPanel panel = new JPanel(new MigLayout());
		String msg = trans.get("PreferencesDialog.CancelOperation.msg.discardChanges");
		JLabel msgLabel = new JLabel(msg);
		JCheckBox dontAskAgain = new JCheckBox(trans.get("SimulationConfigDialog.CancelOperation.checkbox.dontAskAgain"));
		dontAskAgain.setSelected(false);
		dontAskAgain.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					preferences.setShowDiscardPreferencesConfirmation(false);
				}
				// Unselected state should be not be possible and thus not be handled
			}
		});

		panel.add(msgLabel, "left, wrap");
		panel.add(dontAskAgain, "left, gaptop para");

		return panel;
	}

	// ////// Singleton implementation ////////

	private static PreferencesDialog dialog = null;

	public static void showPreferences(BasicFrame parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog(parent);
		dialog.setVisible(true);
	}

}
