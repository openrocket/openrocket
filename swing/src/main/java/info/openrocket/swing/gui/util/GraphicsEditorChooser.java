package info.openrocket.swing.gui.util;

import java.awt.Component;
import java.io.File;
import java.util.Locale;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileView;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.core.startup.Application;

/**
 * Shared helper for selecting an external graphics editor executable.
 */
public final class GraphicsEditorChooser {

	private GraphicsEditorChooser() {
	}

	public static Optional<String> chooseEditor(Component parentComponent) {
		JFileChooser chooser = new JFileChooser();
		if (SystemInfo.getPlatform() == Platform.MAC_OS) {
			chooser.setFileView(createMacApplicationFileView());
		}
		File initialDirectory = determineInitialDirectory();
		if (initialDirectory != null) {
			chooser.setCurrentDirectory(initialDirectory);
		}

		int action = chooser.showOpenDialog(parentComponent);
		if (action == JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			return Optional.ofNullable(selected)
					.map(File::getAbsolutePath)
					.map(GraphicsEditorChooser::quoteIfNeeded);
		}

		return Optional.empty();
	}

	/**
	 * Create a file view that exposes macOS application bundles as selectable files.
	 * Swing otherwise treats these directories as folders and opens them in the chooser.
	 */
	static FileView createMacApplicationFileView() {
		return new FileView() {
			@Override
			public Boolean isTraversable(File file) {
				return isMacApplicationBundle(file) ? Boolean.FALSE : null;
			}
		};
	}

	private static boolean isMacApplicationBundle(File file) {
		return file != null && file.isDirectory()
				&& file.getName().toLowerCase(Locale.ROOT).endsWith(".app");
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
