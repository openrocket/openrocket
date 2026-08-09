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
	private File preferencesSnapshotFile = null;
	private SimulationPreferencesPanel simulationPanel = null;

	public final static int TAB_GENERAL = 0;
	public final static int TAB_UI = 1;
	public final static int TAB_DESIGN = 2;
	public final static int TAB_SIMULATION = 3;
	public final static int TAB_LAUNCH = 4;
	public final static int TAB_UNITS = 5;
	public final static int TAB_MATERIALS = 6;
	public final static int TAB_GRAPHICS = 7;

	private PreferencesDialog(BasicFrame parent, int selectedTab) {
		// // Preferences
		super(parent, trans.get("pref.dlg.title.Preferences"),
				Dialog.ModalityType.APPLICATION_MODAL);

		this.parentFrame = parent;

		// Keep a rollback point for changes made after the dialog opens or is applied.
		preferencesSnapshotFile = storePreferencesSnapshot();

		JPanel panel = new JPanel(new MigLayout("fill, gap unrel", "[grow]",
				"[grow][]"));

		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "grow, wrap");

		// General options
		tabbedPane.insertTab(trans.get("pref.dlg.tab.General"), null,
				new GeneralPreferencesPanel(this),
				trans.get("pref.dlg.tab.General.ttip"), TAB_GENERAL);
		// UI options
		tabbedPane.insertTab(trans.get("pref.dlg.tab.UI"), null,
				new UIPreferencesPanel(this),
				trans.get("pref.dlg.tab.UI.ttip"), TAB_UI);
		// Designer options
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Design"), null,
				new DesignPreferencesPanel(), trans.get("pref.dlg.tab.Design"), TAB_DESIGN);
		// Simulation options
		this.simulationPanel = new SimulationPreferencesPanel();
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Simulation"), null,
				this.simulationPanel,
				trans.get("pref.dlg.tab.Simulation"), TAB_SIMULATION);
		// Launch options
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Launch"), null,
				new LaunchPreferencesPanel(), trans.get("pref.dlg.tab.Launch"), TAB_LAUNCH);
		// Units and Default units
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Units"), null,
				new UnitsPreferencesPanel(this),
				trans.get("pref.dlg.tab.Defaultunits"), TAB_UNITS);
		// Materials and Custom materials
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Materials"), null,
				new MaterialEditPanel(parent.getRocketPanel().getDocument()),
				trans.get("pref.dlg.tab.Custommaterials"), TAB_MATERIALS);
		// Graphics selection
		tabbedPane.insertTab(trans.get("pref.dlg.tab.Graphics"), null,
				new GraphicsPreferencesPanel(this),
				trans.get("pref.dlg.tab.Graphics"), TAB_GRAPHICS);

		// Default Colors Preferences
		// tabbedPane.addTab(trans.get("pref.dlg.tab.Colors"),
		// new DisplayPreferencesPanel());

		tabbedPane.setSelectedIndex(selectedTab);


		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
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
		panel.add(cancelButton, "span, split 3, right, tag cancel");

		//// Apply button
		JButton applyButton = new JButton(trans.get("dlg.but.apply"));
		applyButton.setToolTipText(trans.get("PreferencesDialog.btn.Apply.ttip"));
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyChanges();
			}
		});
		panel.add(applyButton, "tag apply");

		//// Ok button
		JButton okButton = new JButton(trans.get("dlg.but.ok"));
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
					if (simulationPanel != null) {
						simulationPanel.prepareForSave();
					}
					storeAndApplyPreferences(preferences, parentFrame);
				} else {
					loadPreferencesSnapshot();
					refreshParentFrame(parentFrame);
				}

				// Store the preference for showing the confirmation dialog
				preferences.setShowDiscardPreferencesConfirmation(isShowDiscardConfirmation);

				deletePreferencesSnapshot();
			}
		});

		GUIUtil.setDisposableDialogOptions(this, okButton);
	}

	private PreferencesDialog(BasicFrame parent) {
		this(parent, 0);
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
	 * Applies the current preferences while leaving the dialog open. The updated
	 * snapshot ensures that Cancel only rolls back edits made after this action.
	 */
	private void applyChanges() {
		storeAndApplyPreferences(preferences, parentFrame);
		replacePreferencesSnapshot();
	}

	/**
	 * Stores the current preferences in a temporary file.
	 *
	 * @return the file containing the preferences, or {@code null} if they could not be stored
	 */
	private File storePreferencesSnapshot() {
		try {
			File outputFile = Files.createTempFile("ORInitPrefs_" + System.currentTimeMillis(), ".xml").toFile();
			try (FileOutputStream outputFos = new FileOutputStream(outputFile)) {
				PreferencesExporter.exportPreferencesToFile(preferences.getPreferences(), outputFos, false);
				log.debug("Preferences snapshot stored in temporary file: {}", outputFile.getAbsolutePath());
			} catch (BackingStoreException e) {
				log.error("Could not store preferences snapshot", e);
				return null;
			}
			return outputFile;
		} catch (IOException e) {
			log.error("Could not create temporary preferences snapshot", e);
			return null;
		}
	}

	/**
	 * Replaces the rollback point after preferences have been applied.
	 */
	private void replacePreferencesSnapshot() {
		File updatedSnapshotFile = storePreferencesSnapshot();
		if (updatedSnapshotFile == null) {
			return;
		}

		deletePreferencesSnapshot();
		preferencesSnapshotFile = updatedSnapshotFile;
	}

	/**
	 * Restores the preferences from the most recent rollback point.
	 */
	private void loadPreferencesSnapshot() {
		if (preferencesSnapshotFile == null) {
			return;
		}
		PreferencesImporter.importPreferences(preferencesSnapshotFile);
	}

	/**
	 * Deletes the temporary rollback point when it is replaced or no longer needed.
	 */
	private void deletePreferencesSnapshot() {
		if (preferencesSnapshotFile == null) {
			return;
		}
		if (preferencesSnapshotFile.exists() && !preferencesSnapshotFile.delete()) {
			log.warn("Could not delete preferences snapshot: {}", preferencesSnapshotFile.getAbsolutePath());
		}
		preferencesSnapshotFile = null;
	}

	/**
	 * Stores preferences that are held outside the backing preferences tree and
	 * refreshes the visible rocket panel.
	 *
	 * @param preferences preferences to store
	 * @param parent parent frame to refresh, or {@code null} when none is open
	 */
	static void storeAndApplyPreferences(SwingPreferences preferences, BasicFrame parent) {
		preferences.storeDefaultUnits();
		refreshParentFrame(parent);
	}

	/**
	 * Refreshes elements whose presentation depends on application preferences.
	 *
	 * @param parent parent frame to refresh, or {@code null} when none is open
	 */
	private static void refreshParentFrame(BasicFrame parent) {
		if (parent == null) {
			return;
		}
		parent.getRocketPanel().updateExtras();
		parent.getRocketPanel().updateFigures();
		parent.getRocketPanel().updateRulers();
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

	public static void showPreferences(BasicFrame parent, int selectedTab) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog(parent, selectedTab);
		dialog.setVisible(true);

	}

	public static void showPreferences(BasicFrame parent) {
		showPreferences(parent, TAB_GENERAL);
	}

}
