package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesDialog extends JDialog {
	private static final Logger log = LoggerFactory
			.getLogger(PreferencesDialog.class);

	private File defaultDirectory = null;
	private static final Translator trans = Application.getTranslator();

	private final SwingPreferences preferences = (SwingPreferences) Application
			.getPreferences();

	private PreferencesDialog(Window parent) {
		// // Preferences
		super(parent, trans.get("pref.dlg.title.Preferences"),
				Dialog.ModalityType.APPLICATION_MODAL);

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

		// Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PreferencesDialog.this.setVisible(false);
				PreferencesDialog.this.dispose();
			}
		});
		panel.add(close, "span, right, tag close");

		this.setContentPane(panel);
		pack();
		this.setLocationRelativeTo(null);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				preferences.storeDefaultUnits();
			}
		});

		GUIUtil.setDisposableDialogOptions(this, close);
	}

	// ////// Singleton implementation ////////

	private static PreferencesDialog dialog = null;

	public static void showPreferences(Window parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog(parent);
		dialog.setVisible(true);
	}

}
