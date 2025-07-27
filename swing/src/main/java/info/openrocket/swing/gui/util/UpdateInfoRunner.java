package info.openrocket.swing.gui.util;

import info.openrocket.core.communication.ReleaseInfo;
import info.openrocket.core.communication.UpdateInfo;
import info.openrocket.core.communication.UpdateInfoRetriever;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BuildProperties;
import info.openrocket.swing.gui.dialogs.UpdateInfoDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.Dialog;
import java.awt.Window;

/**
 * Helper class for checking for updates.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class UpdateInfoRunner {
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();

	public static void checkForUpdates(Window parent) {
		final UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();

		final JDialog dialog1 = new JDialog(parent, Dialog.ModalityType.MODELESS); // Make non-modal
		JPanel panel = new JPanel(new MigLayout());

		panel.add(new JLabel(trans.get("pref.dlg.lbl.Checkingupdates")), "wrap");

		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		panel.add(bar, "growx, wrap para");

		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(e -> {
			retriever.cancel(); // Add way to cancel retriever
			dialog1.dispose();
		});
		panel.add(cancel, "right");
		dialog1.add(panel);

		GUIUtil.setDisposableDialogOptions(dialog1, cancel);

		SwingWorker<UpdateInfo, Void> worker = new SwingWorker<>() {
			@Override
			protected UpdateInfo doInBackground() {
				long startTime = System.currentTimeMillis();
				while (retriever.isRunning() && System.currentTimeMillis() - startTime < 10000) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}
				return retriever.getUpdateInfo();
			}

			@Override
			protected void done() {
				dialog1.dispose();
				try {
					handleUpdateResult(parent, get(), retriever);
				} catch (Exception e) {
					handleError(parent, e);
				}
			}
		};

		worker.execute();
		dialog1.setVisible(true);
	}

	private static void handleUpdateResult(Window parent, UpdateInfo info, UpdateInfoRetriever retriever) {
		if (info == null) {
			if (!retriever.isCancelled()) {
				JOptionPane.showMessageDialog(parent,
						trans.get("update.dlg.error"),
						trans.get("update.dlg.error.title"),
						JOptionPane.WARNING_MESSAGE);
			}
			return;
		}

		if (info.getException() != null) {
			JOptionPane.showMessageDialog(parent,
					info.getException().getMessage(),
					trans.get("update.dlg.exception.title"),
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		ReleaseInfo release = info.getLatestRelease();
		// Skip if version is in ignore list
		boolean checkAllUpdates = System.getProperty("openrocket.debug.checkAllVersionUpdates") != null;
		if (!checkAllUpdates && preferences.getIgnoreUpdateVersions().contains(release.getReleaseName())) {
			return;
		}

		switch (info.getReleaseStatus()) {
			case LATEST:
				JOptionPane.showMessageDialog(parent,
						String.format(trans.get("update.dlg.latestVersion"),
								BuildProperties.getVersion()),
						trans.get("update.dlg.latestVersion.title"),
						JOptionPane.INFORMATION_MESSAGE);
				break;
			case NEWER:
				JOptionPane.showMessageDialog(parent,
						String.format("<html><body><p style='width: %dpx'>%s", 400,
								String.format(trans.get("update.dlg.newerVersion"),
										BuildProperties.getVersion(), release.getReleaseName())),
						trans.get("update.dlg.newerVersion.title"),
						JOptionPane.INFORMATION_MESSAGE);
				break;
			case OLDER:
				new UpdateInfoDialog(info).setVisible(true);
				break;
		}
	}

	private static void handleError(Window parent, Exception e) {
		JOptionPane.showMessageDialog(parent,
				trans.get("update.dlg.error"),
				trans.get("update.dlg.error.title"),
				JOptionPane.WARNING_MESSAGE);
	}
}
