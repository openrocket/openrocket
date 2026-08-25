package info.openrocket.core.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests migration detection and validation for original, current, and legacy plugin JARs.
 */
public class JarMigrationHelperTest {
	@TempDir
	private Path tempDir;

	@Test
	public void testShouldMigrateOldPackageJar() throws Exception {
		File plugin = createJar("MyPlugin.jar", "net/sf/openrocket/util/MyClass.class");

		assertTrue(JarMigrationHelper.shouldMigrate(plugin, List.of(plugin)));
	}

	@Test
	public void testShouldNotMigrateNewPackageJar() throws Exception {
		File plugin = createJar("MyPlugin.jar", "info/openrocket/core/util/MyClass.class");

		assertFalse(JarMigrationHelper.shouldMigrate(plugin, List.of(plugin)));
	}

	@Test
	public void testShouldNotMigrateWhenMigratedVersionExists() throws Exception {
		File plugin = createJar("MyPlugin.jar", "net/sf/openrocket/util/MyClass.class");
		File migrated = createJar("MyPlugin" + JarMigrationHelper.MIGRATION_SUFFIX + ".jar",
				"info/openrocket/core/util/MyClass.class");

		assertFalse(JarMigrationHelper.shouldMigrate(plugin, List.of(plugin, migrated)));
		assertEquals(JarMigrationHelper.MigrationStatus.SUPERSEDED,
				JarMigrationHelper.getMigrationStatus(plugin, List.of(plugin, migrated)));
	}

	@Test
	public void testShouldNotMigrateWhenLegacyMigratedVersionExists() throws Exception {
		// Older versions left the migrated JAR with an extra "-new" suffix when the rename during startup
		// failed; such a file must still count as an already performed migration.
		File plugin = createJar("MyPlugin.jar", "net/sf/openrocket/util/MyClass.class");
		File legacyMigrated = createJar(
				"MyPlugin" + JarMigrationHelper.MIGRATION_SUFFIX + JarMigrationHelper.NEW_MIGRATION_SUFFIX + ".jar",
				"info/openrocket/core/util/MyClass.class");

		assertFalse(JarMigrationHelper.shouldMigrate(plugin, List.of(plugin, legacyMigrated)));
	}

	@Test
	public void testShouldNotMigrateMigratedJarItself() throws Exception {
		File migrated = createJar("MyPlugin" + JarMigrationHelper.MIGRATION_SUFFIX + ".jar",
				"info/openrocket/core/util/MyClass.class");

		assertFalse(JarMigrationHelper.shouldMigrate(migrated, List.of(migrated)));
	}

	@Test
	public void testCorruptMigratedVersionDoesNotSuppressMigration() throws Exception {
		File plugin = createJar("MyPlugin.jar", "net/sf/openrocket/util/MyClass.class");
		File migrated = tempDir.resolve("MyPlugin" + JarMigrationHelper.MIGRATION_SUFFIX + ".jar").toFile();
		Files.writeString(migrated.toPath(), "not a JAR");

		assertTrue(JarMigrationHelper.shouldMigrate(plugin, List.of(plugin, migrated)));
	}

	@Test
	public void testUnreadableMigratedJarIsRejected() throws Exception {
		File migrated = tempDir.resolve("MyPlugin" + JarMigrationHelper.MIGRATION_SUFFIX + ".jar").toFile();
		Files.writeString(migrated.toPath(), "not a JAR");

		assertThrows(JarMigrationHelper.JarMigrationException.class,
				() -> JarMigrationHelper.getMigrationStatus(migrated, List.of(migrated)));
	}

	/**
	 * Create a JAR file containing a single (empty) entry with the given name.
	 * Only the entry name matters for the migration checks.
	 */
	private File createJar(String jarName, String entryName) throws IOException {
		File jar = tempDir.resolve(jarName).toFile();
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
			jos.putNextEntry(new JarEntry(entryName));
			jos.closeEntry();
		}
		return jar;
	}
}
