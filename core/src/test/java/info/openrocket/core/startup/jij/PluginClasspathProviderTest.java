package info.openrocket.core.startup.jij;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import info.openrocket.core.plugin.JarMigrationHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests migration filtering performed before plugin JARs are added to the application classpath.
 */
public class PluginClasspathProviderTest {
	@TempDir
	private Path tempDir;

	@AfterEach
	public void clearMigrationProperty() {
		System.clearProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY);
	}

	@Test
	public void testOriginalJarIsExcludedWhenMigratedCompanionExists() throws Exception {
		File original = createJar("MyPlugin.jar", "net/sf/openrocket/plugin.txt");
		File migrated = createJar("MyPlugin-migrated.jar", "info/openrocket/core/plugin.txt");

		List<File> compatibleFiles = PluginClasspathProvider.getCompatibleFiles(List.of(original, migrated));

		assertEquals(List.of(migrated), compatibleFiles);
		assertNull(System.getProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY));
	}

	@Test
	public void testLegacyMigratedJarIsNormalizedBeforeClasspathUse() throws Exception {
		File original = createJar("MyPlugin.jar", "net/sf/openrocket/plugin.txt");
		File legacyMigrated = createJar("MyPlugin-migrated-new.jar", "info/openrocket/core/plugin.txt");
		File finalMigrated = tempDir.resolve("MyPlugin-migrated.jar").toFile();

		List<File> compatibleFiles = PluginClasspathProvider.getCompatibleFiles(List.of(original, legacyMigrated));

		assertEquals(List.of(finalMigrated), compatibleFiles);
		assertTrue(finalMigrated.isFile());
		assertFalse(legacyMigrated.exists());
		assertNull(System.getProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY));
	}

	@Test
	public void testExistingFinalMigratedJarIsPreferredOverLegacyJar() throws Exception {
		File legacyMigrated = createJar("MyPlugin-migrated-new.jar", "info/openrocket/core/legacy.txt");
		File finalMigrated = createJar("MyPlugin-migrated.jar", "info/openrocket/core/final.txt");

		List<File> compatibleFiles = PluginClasspathProvider.getCompatibleFiles(
				List.of(legacyMigrated, finalMigrated));

		assertEquals(List.of(finalMigrated), compatibleFiles);
		assertTrue(legacyMigrated.isFile());
		assertTrue(finalMigrated.isFile());
	}

	@Test
	public void testCorruptMigratedCompanionIsReplaced() throws Exception {
		File original = createJar("MyPlugin.jar", "net/sf/openrocket/plugin.txt");
		File corruptMigrated = tempDir.resolve("MyPlugin-migrated.jar").toFile();
		Files.writeString(corruptMigrated.toPath(), "not a JAR");

		List<File> compatibleFiles = PluginClasspathProvider.getCompatibleFiles(List.of(original, corruptMigrated));

		assertEquals(List.of(corruptMigrated), compatibleFiles);
		assertTrue(isJarReadable(corruptMigrated));
		assertEquals(corruptMigrated.getAbsolutePath(),
				System.getProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY));
	}

	@Test
	public void testCompletedMigrationIsNotReportedAgain() throws Exception {
		File original = createJar("MyPlugin.jar", "net/sf/openrocket/plugin.txt");

		List<File> firstRun = PluginClasspathProvider.getCompatibleFiles(List.of(original));
		File migrated = tempDir.resolve("MyPlugin-migrated.jar").toFile();

		assertEquals(List.of(migrated), firstRun);
		assertEquals(migrated.getAbsolutePath(),
				System.getProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY));

		List<File> secondRun = PluginClasspathProvider.getCompatibleFiles(List.of(original, migrated));

		assertEquals(List.of(migrated), secondRun);
		assertNull(System.getProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY));
	}

	private File createJar(String jarName, String entryName) throws IOException {
		File jar = tempDir.resolve(jarName).toFile();
		try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar))) {
			output.putNextEntry(new JarEntry(entryName));
			output.write(new byte[] { 1 });
			output.closeEntry();
		}
		return jar;
	}

	private boolean isJarReadable(File jarFile) {
		try (JarFile ignored = new JarFile(jarFile)) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
