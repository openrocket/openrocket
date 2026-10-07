package info.openrocket.swing.gui.widgets;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.formdev.flatlaf.util.SystemFileChooser;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * File chooser that shows the operating system's file dialog (see {@link SystemFileChooser}).
 * <p>
 * Native file dialogs cannot host custom components, so options that used to be shown as a
 * {@code JFileChooser} accessory are set with {@link #setOptionsPanel(JComponent)}. The panel is then shown
 * in a separate OK/Cancel dialog before the file dialog opens.
 */
public class NativeFileChooser extends SystemFileChooser {
	private static final Translator trans = Application.getTranslator();

	/** Maximum time to wait for the parent window to become active again after the options dialog closed. */
	private static final int OWNER_ACTIVATION_TIMEOUT_MS = 500;

	private JComponent optionsPanel;

	public NativeFileChooser() {
		super();
	}

	public NativeFileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * Sets the panel shown in an OK/Cancel dialog before the file dialog opens,
	 * or {@code null} to open the file dialog directly.
	 *
	 * @param optionsPanel the options panel
	 */
	public void setOptionsPanel(JComponent optionsPanel) {
		this.optionsPanel = optionsPanel;
	}

	public JComponent getOptionsPanel() {
		return optionsPanel;
	}

	@Override
	public int showOpenDialog(Component parent) {
		if (!prepareDialog(parent, OPEN_DIALOG)) {
			return CANCEL_OPTION;
		}
		return super.showOpenDialog(parent);
	}

	@Override
	public int showSaveDialog(Component parent) {
		if (!prepareDialog(parent, SAVE_DIALOG)) {
			return CANCEL_OPTION;
		}
		return super.showSaveDialog(parent);
	}

	@Override
	public int showDialog(Component parent, String approveButtonText) {
		if (!prepareDialog(parent, getDialogType())) {
			return CANCEL_OPTION;
		}
		return super.showDialog(parent, approveButtonText);
	}

	/**
	 * Shows the options dialog, if any, and prepares the chooser state for the native dialog.
	 *
	 * @return {@code false} if the user cancelled the options dialog
	 */
	boolean prepareDialog(Component parent, int dialogType) {
		if (optionsPanel != null) {
			if (!showOptionsDialog(parent)) {
				return false;
			}
			waitForOwnerActivation(parent);
		}

		// Native dialogs take the folder from the selected file, so resolve names without a folder
		// against the current directory instead of losing it.
		File selectedFile = getSelectedFile();
		if (selectedFile != null && !selectedFile.isAbsolute()) {
			setSelectedFile(new File(getCurrentDirectory(), selectedFile.getPath()));
		}

		// Let Windows append the extension of the chosen file type, so its overwrite prompt checks the final name
		if (dialogType == SAVE_DIALOG && getPlatformProperty(WINDOWS_DEFAULT_EXTENSION) == null) {
			String extension = getDefaultExtension(getFileFilter());
			if (extension != null) {
				putPlatformProperty(WINDOWS_DEFAULT_EXTENSION, extension);
			}
		}
		return true;
	}

	/**
	 * Shows the options panel in a modal OK/Cancel dialog.
	 *
	 * @return {@code true} if the user pressed OK
	 */
	protected boolean showOptionsDialog(Component parent) {
		String title = getDialogTitle() != null ? getDialogTitle() : trans.get("NativeFileChooser.options.title");
		int result = JOptionPane.showConfirmDialog(parent, optionsPanel, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		return result == JOptionPane.OK_OPTION;
	}

	/**
	 * Waits until the window of the parent component is active again after the options dialog closed.
	 * <p>
	 * On macOS, the native file dialog is closed immediately (as if cancelled) when it is shown while
	 * the focus is still moving from the closed options dialog back to its parent window.
	 */
	private static void waitForOwnerActivation(Component parent) {
		Window owner = parent instanceof Window window ? window
				: parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		if (owner == null || owner.isActive() || !owner.isShowing() || !EventQueue.isDispatchThread()) {
			return;
		}

		SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
		WindowAdapter activationListener = new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				loop.exit();
			}
		};
		// The owner does not become active if the user switched to another application
		Timer timeout = new Timer(OWNER_ACTIVATION_TIMEOUT_MS, e -> loop.exit());
		timeout.setRepeats(false);

		owner.addWindowListener(activationListener);
		timeout.start();
		try {
			loop.enter();
		} finally {
			timeout.stop();
			owner.removeWindowListener(activationListener);
		}
	}

	/**
	 * Returns the extension, without the dot, that files matching the filter should get by default.
	 *
	 * @param filter the file filter
	 * @return the first extension of the filter, or {@code null} if the filter does not define a single extension
	 */
	static String getDefaultExtension(FileFilter filter) {
		if (filter instanceof FileNameExtensionFilter extensionFilter) {
			return extensionFilter.getExtensions()[0];
		}
		if (filter instanceof PatternFilter patternFilter) {
			String pattern = patternFilter.getPatterns()[0];
			String extension = pattern.startsWith("*.") ? pattern.substring(2) : "";
			if (!extension.isEmpty() && extension.indexOf('*') < 0 && extension.indexOf('?') < 0) {
				return extension;
			}
		}
		return null;
	}
}
