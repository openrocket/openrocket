package info.openrocket.swing.logging;

import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmCrashLogTest extends BaseTestCase {

	private String originalUserDir;

	@AfterEach
	void restoreUserDir() {
		if (originalUserDir != null) {
			System.setProperty("user.dir", originalUserDir);
			originalUserDir = null;
		}
		File crashDirectory = JvmCrashLog.getCrashLogDirectory();
		File[] files = crashDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().startsWith("hs_err_pid_jvmcrashlogtest")) {
					file.delete();
				}
			}
		}
	}

	@Test
	void movesCrashReportsIntoTheApplicationDirectory(@TempDir Path workingDirectory) throws Exception {
		originalUserDir = System.getProperty("user.dir");
		System.setProperty("user.dir", workingDirectory.toString());

		Path crashReport = workingDirectory.resolve("hs_err_pid_jvmcrashlogtest1.log");
		Files.writeString(crashReport, "# SIGSEGV in native code");
		// A file that merely lives alongside it must be left alone.
		Path unrelated = workingDirectory.resolve("openrocket.log");
		Files.writeString(unrelated, "not a crash report");

		List<File> collected = JvmCrashLog.collectCrashLogs();

		assertEquals(1, collected.size(), "The crash report should have been collected");
		File moved = collected.get(0);
		assertEquals(JvmCrashLog.getCrashLogDirectory(), moved.getParentFile(),
				"Crash reports belong in the application directory, where a user can find them");
		assertTrue(moved.isFile(), "The collected report should exist at its new location");
		assertEquals("# SIGSEGV in native code", Files.readString(moved.toPath()));
		assertFalse(Files.exists(crashReport), "The original should have been moved, not copied");
		assertTrue(Files.exists(unrelated), "Unrelated files must be left where they are");
	}

	/**
	 * Scoped to the working directory rather than asserting nothing at all is collected: the
	 * collector deliberately also sweeps the temporary directory and the user's home, so a
	 * developer machine that has genuinely crashed before will have reports there.
	 */
	@Test
	void leavesFilesThatAreNotCrashReports(@TempDir Path workingDirectory) throws Exception {
		originalUserDir = System.getProperty("user.dir");
		System.setProperty("user.dir", workingDirectory.toString());

		Path notACrash = workingDirectory.resolve("hs_err_pid_jvmcrashlogtest2.txt");
		Files.writeString(notACrash, "wrong extension");
		Path alsoNotACrash = workingDirectory.resolve("openrocket-2026.log");
		Files.writeString(alsoNotACrash, "the ordinary application log");

		List<File> collected = JvmCrashLog.collectCrashLogs();

		assertTrue(collected.stream().noneMatch(file -> file.getName().contains("jvmcrashlogtest2")),
				"Only JVM crash reports should be collected");
		assertTrue(Files.exists(notACrash), "A file with the wrong extension should be left alone");
		assertTrue(Files.exists(alsoNotACrash), "The application's own log should be left alone");
	}
}
