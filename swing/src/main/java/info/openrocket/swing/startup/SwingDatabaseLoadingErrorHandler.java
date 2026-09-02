package info.openrocket.swing.startup;

import java.awt.Dialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import info.openrocket.core.database.DatabaseLoadingErrorHandler;

/**
 * Swing implementation of {@link DatabaseLoadingErrorHandler}: presents database
 * loading failures to the user as a non-modal warning dialog.
 * <p>
 * This is where the {@code java.desktop} dependency for these warnings lives; the
 * {@code core} loaders merely report the localized text.  Reports may arrive on the
 * background loading thread, so display is marshalled onto the Event Dispatch Thread.
 */
public class SwingDatabaseLoadingErrorHandler implements DatabaseLoadingErrorHandler {

	@Override
	public void handleError(String title, String message) {
		// The localized messages already contain HTML markup (e.g. a bold file path and
		// <br> breaks), so the text is treated as HTML rather than escaped.  Newline
		// separators added by the caller are converted to <br> for rendering.
		final String html = "<html><body><p style='width: 400px;'>"
				+ message.replace("\n", "<br>")
				+ "</p></body></html>";
		SwingUtilities.invokeLater(() -> {
			JOptionPane pane = new JOptionPane(html, JOptionPane.WARNING_MESSAGE);
			JDialog dialog = pane.createDialog(null, title);
			dialog.setModalityType(Dialog.ModalityType.MODELESS);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		});
	}
}
