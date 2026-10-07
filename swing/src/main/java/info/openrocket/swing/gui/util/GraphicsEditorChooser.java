package info.openrocket.swing.gui.util;

import java.awt.Component;
import java.io.File;
import java.util.Optional;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.widgets.NativeFileChooser;

/**
 * Shared helper for selecting an external graphics editor executable.
 */
public final class GraphicsEditorChooser {

	private GraphicsEditorChooser() {
	}

	public static Optional<String> chooseEditor(Component parentComponent) {
		// The native macOS file dialog lets application bundles be selected like files
		NativeFileChooser chooser = new NativeFileChooser();
		File initialDirectory = determineInitialDirectory();
		if (initialDirectory != null) {
			chooser.setCurrentDirectory(initialDirectory);
		}

		int action = chooser.showOpenDialog(parentComponent);
		if (action == NativeFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			return Optional.ofNullable(selected)
					.map(File::getAbsolutePath)
					.map(GraphicsEditorChooser::quoteIfNeeded);
		}

		return Optional.empty();
	}

	/**
	 * Determine the initial directory to show in the file chooser.
	 * Prefer the last directory chosen by the user, otherwise fall back to a sensible
	 * per-platform applications directory, and finally to the user's home directory.
	 */
	private static File determineInitialDirectory() {
		File preferred = Application.getPreferences().getDefaultDirectory();
		if (isUsableDirectory(preferred)) {
			return preferred;
		}

		File platformDefault = getPlatformDefaultDirectory();
		if (isUsableDirectory(platformDefault)) {
			return platformDefault;
		}

		File homeDirectory = new File(System.getProperty("user.home", "."));
		if (isUsableDirectory(homeDirectory)) {
			return homeDirectory;
		}

		return null;
	}

	private static File getPlatformDefaultDirectory() {
		return switch (SystemInfo.getPlatform()) {
			case MAC_OS -> new File("/Applications");
			case WINDOWS -> {
				String programFiles = System.getenv("ProgramFiles");
				if (programFiles != null && !programFiles.isBlank()) {
					yield new File(programFiles);
				} else {
					yield new File("C:\\Program Files");
				}
			}
			case UNIX -> {
				File usrBin = new File("/usr/bin");
				if (usrBin.exists()) {
					yield usrBin;
				} else {
					yield new File("/usr/local/bin");
				}
			}
		};
	}

	private static boolean isUsableDirectory(File directory) {
		return directory != null && directory.exists() && directory.isDirectory();
	}

	private static String quoteIfNeeded(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		if (value.indexOf(' ') < 0 && value.indexOf('\t') < 0) {
			return value;
		}
		String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
		return "\"" + escaped + "\"";
	}
}
