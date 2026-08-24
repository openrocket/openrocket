package info.openrocket.swing.logging;

import info.openrocket.core.arch.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Collects the crash reports the JVM writes when it dies in native code, and files them
 * under the OpenRocket application directory.
 *
 * <p>A fatal native error (a GPU driver fault, for instance) never reaches Java: HotSpot
 * writes an {@code hs_err_pid<pid>.log} and the process is gone. That file is the only
 * record of what happened, and by default it lands in the launcher's working directory —
 * for an installed build that is the installation directory, which an ordinary user
 * cannot write to, so HotSpot silently falls back to the temporary directory instead.
 * Neither location is somewhere a user would think to look, which is why crash reports
 * come in without one attached.</p>
 *
 * <p>Ideally the JVM would be told where to put it, but {@code -XX:ErrorFile} is
 * read-only once the VM is up and the packaged launcher shares one set of VM parameters
 * across every platform, so it cannot name a per-platform directory. The launcher
 * therefore anchors the file somewhere always writable, and this class moves whatever it
 * finds into the application directory on the next start, where the bug report dialog and
 * the user can both find it.</p>
 */
public final class JvmCrashLog {

	private static final Logger log = LoggerFactory.getLogger(JvmCrashLog.class);

	/** Sub-directory of the application directory that collected reports are moved into. */
	public static final String CRASH_LOG_DIRECTORY = "crash-reports";

	/** Prefixes HotSpot uses for the files it writes on a fatal error. */
	private static final String[] CRASH_LOG_PREFIXES = {"hs_err_pid", "replay_pid", "OpenRocket-crash"};

	private JvmCrashLog() {
	}

	/**
	 * The directory collected crash reports are moved into.
	 *
	 * @return the crash report directory, which may not exist yet
	 */
	public static File getCrashLogDirectory() {
		return new File(SystemInfo.getUserApplicationDirectory(), CRASH_LOG_DIRECTORY);
	}

	/**
	 * Moves any JVM crash reports found in the places HotSpot writes them into the
	 * application directory, and logs where they went.
	 *
	 * <p>Never throws: this runs during startup, and failing to tidy up a previous crash
	 * must not stop OpenRocket from starting.</p>
	 *
	 * @return the reports now sitting in the crash report directory, newest first
	 */
	public static List<File> collectCrashLogs() {
		List<File> collected = new ArrayList<>();
		try {
			File destination = getCrashLogDirectory();
			for (File source : findCrashLogs()) {
				File moved = moveIntoDirectory(source, destination);
				if (moved != null) {
					collected.add(moved);
				}
			}
			if (!collected.isEmpty()) {
				log.warn("OpenRocket appears to have crashed previously. {} JVM crash report(s) moved to {} — "
								+ "please attach them when reporting the problem.",
						collected.size(), destination.getAbsolutePath());
			}
		} catch (Exception | LinkageError e) {
			log.warn("Could not collect JVM crash reports", e);
		}
		return collected;
	}

	/**
	 * The directories HotSpot may have written a crash report to: the working directory it
	 * was launched with, and the temporary directory it falls back to when that one is not
	 * writable. The user's home is included because the packaged launcher points
	 * {@code -XX:ErrorFile} there, being the one location writable on every platform.
	 */
	private static Set<File> searchDirectories() {
		Set<File> directories = new LinkedHashSet<>();
		addIfUsable(directories, System.getProperty("user.dir"));
		addIfUsable(directories, System.getProperty("java.io.tmpdir"));
		addIfUsable(directories, System.getProperty("user.home"));
		return directories;
	}

	private static void addIfUsable(Set<File> directories, String path) {
		if (path == null || path.isBlank()) {
			return;
		}
		File directory = new File(path);
		if (directory.isDirectory()) {
			directories.add(directory);
		}
	}

	private static List<File> findCrashLogs() {
		List<File> found = new ArrayList<>();
		File crashLogDirectory = getCrashLogDirectory();
		for (File directory : searchDirectories()) {
			if (directory.equals(crashLogDirectory)) {
				continue;
			}
			File[] candidates = directory.listFiles(JvmCrashLog::isCrashLog);
			if (candidates != null) {
				found.addAll(List.of(candidates));
			}
		}
		found.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
		return found;
	}

	private static boolean isCrashLog(File file) {
		if (!file.isFile()) {
			return false;
		}
		String name = file.getName().toLowerCase(Locale.ROOT);
		if (!name.endsWith(".log")) {
			return false;
		}
		for (String prefix : CRASH_LOG_PREFIXES) {
			if (name.startsWith(prefix.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

	private static File moveIntoDirectory(File source, File destinationDirectory) {
		try {
			if (!destinationDirectory.isDirectory() && !destinationDirectory.mkdirs()) {
				log.warn("Could not create crash report directory {}", destinationDirectory.getAbsolutePath());
				return null;
			}

			Path target = destinationDirectory.toPath().resolve(source.getName());
			try {
				Files.move(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException moveFailed) {
				// A move across filesystems, or from a directory we may read but not write,
				// can fail where a copy still succeeds. Keeping the original is fine.
				Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			}
			return target.toFile();
		} catch (Exception e) {
			log.warn("Could not move crash report {}: {}", source.getAbsolutePath(), e.getMessage());
			return null;
		}
	}
}
