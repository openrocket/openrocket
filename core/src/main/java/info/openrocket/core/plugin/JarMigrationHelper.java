package info.openrocket.core.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Helper class for migrating JAR files from the old OpenRocket package structure (net.sf.openrocket) to the new
 * structure (info.openrocket.core and info.openrocket.swing).
 * <p>
 * The migration process involves remapping the package names in the JAR file using ASM.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class JarMigrationHelper {
	private static final String OLD_PACKAGE_PATH_PREFIX = "net/sf/openrocket/";
	public static final String MIGRATION_SUFFIX = "-migrated";
	// Legacy suffix. Older versions saved the migrated plugin with this extra suffix and renamed it later during
	// startup. That rename could silently fail (e.g. on Windows, where the JAR is locked once it is on the classpath),
	// so such files may still be present and must be recognized as already migrated.
	public static final String NEW_MIGRATION_SUFFIX = "-new";
	// System property under which the JAR files that were migrated during this startup are stored
	// (absolute paths, separated by File.pathSeparator), so the UI can inform the user about the migration.
	public static final String MIGRATED_JARS_PROPERTY = "openrocket.plugins.migrated";

	/**
	 * Describes how a plugin JAR should be handled while building the plugin classpath.
	 */
	public enum MigrationStatus {
		COMPATIBLE,
		NEEDS_MIGRATION,
		SUPERSEDED
	}

	/**
	 * Exception thrown when an error occurs while migrating a JAR file.
	 */
	public static class JarMigrationException extends Exception {
		public JarMigrationException(String message) {
			super(message);
		}

		public JarMigrationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Check if the JAR file is still using the old OpenRocket package structure (net.sf.openrocket), and that it has
	 * not been migrated before.
	 * @param jarFile The JAR file to check.
	 * @param allJars All JAR files in the plugin directory.
	 * @return True if the JAR file should be migrated.
	 * @throws JarMigrationException If an error occurs while checking the JAR file.
	 */
	public static boolean shouldMigrate(File jarFile, List<File> allJars) throws JarMigrationException {
		return getMigrationStatus(jarFile, allJars) == MigrationStatus.NEEDS_MIGRATION;
	}

	/**
	 * Determine how a plugin JAR should be handled. An original JAR is superseded only when a readable migrated
	 * companion exists; a corrupt or incomplete companion therefore does not suppress a new migration attempt.
	 *
	 * @param jarFile The JAR file to check.
	 * @param allJars All JAR files in the plugin directory.
	 * @return The migration status of the JAR file.
	 * @throws JarMigrationException If the JAR itself cannot be read.
	 */
	public static MigrationStatus getMigrationStatus(File jarFile, List<File> allJars)
			throws JarMigrationException {
		if (isMigratedJar(jarFile)) {
			if (containsOldPackage(jarFile)) {
				throw new JarMigrationException("Migrated JAR still contains old OpenRocket packages: "
						+ jarFile.getName());
			}
			return MigrationStatus.COMPATIBLE;
		}

		if (!containsOldPackage(jarFile)) {
			return MigrationStatus.COMPATIBLE;
		}

		String migratedName = getMigratedName(jarFile, false);
		String legacyMigratedName = getMigratedName(jarFile, true);
		for (File f : allJars) {
			if ((f.getName().equals(migratedName) || f.getName().equals(legacyMigratedName))
					&& isReadableMigratedJar(f)) {
				return MigrationStatus.SUPERSEDED;
			}
		}

		return MigrationStatus.NEEDS_MIGRATION;
	}

	/**
	 * Check whether a JAR uses either the current or legacy migrated filename.
	 *
	 * @param jarFile The JAR file to check.
	 * @return True when the filename identifies a migrated plugin JAR.
	 */
	public static boolean isMigratedJar(File jarFile) {
		String lowerName = jarFile.getName().toLowerCase(Locale.ROOT);
		return lowerName.endsWith(MIGRATION_SUFFIX + ".jar") || isLegacyMigratedJar(jarFile);
	}

	/**
	 * Check whether a JAR uses the legacy temporary migrated filename.
	 *
	 * @param jarFile The JAR file to check.
	 * @return True when the filename ends in {@code -migrated-new.jar}.
	 */
	public static boolean isLegacyMigratedJar(File jarFile) {
		String lowerName = jarFile.getName().toLowerCase(Locale.ROOT);
		return lowerName.endsWith(MIGRATION_SUFFIX + NEW_MIGRATION_SUFFIX + ".jar");
	}

	/**
	 * Rename a legacy migrated JAR to its final name before it is added to the classpath. If a readable final JAR
	 * already exists, that file is preferred and the legacy file is left untouched.
	 *
	 * @param legacyJar A JAR named with the legacy {@code -migrated-new.jar} suffix.
	 * @return The final JAR, or the original file when it does not use the legacy name.
	 * @throws JarMigrationException If the legacy file cannot be normalized.
	 */
	public static File normalizeLegacyMigratedJar(File legacyJar) throws JarMigrationException {
		if (!isLegacyMigratedJar(legacyJar)) {
			return legacyJar;
		}

		File finalJar = new File(legacyJar.getParentFile(), getFinalNameForLegacyJar(legacyJar));
		if (finalJar.isFile() && isReadableMigratedJar(finalJar)) {
			return finalJar;
		}
		if (containsOldPackage(legacyJar)) {
			throw new JarMigrationException("Legacy migrated JAR still contains old OpenRocket packages: "
					+ legacyJar.getName());
		}

		try {
			moveReplacing(legacyJar.toPath(), finalJar.toPath());
			return finalJar;
		} catch (IOException e) {
			String message = String.format("Error renaming legacy migrated JAR %s: %s",
					legacyJar.getName(), e.getMessage());
			throw new JarMigrationException(message, e);
		}
	}

	/**
	 * Get the name of the migrated JAR file.
	 * @param jarFile The original JAR file.
	 * @return The name of the migrated JAR file.
	 */
	private static String getMigratedName(File jarFile, boolean includeLegacySuffix) {
		String name = jarFile.getName();
		String baseName = name.toLowerCase(Locale.ROOT).endsWith(".jar")
				? name.substring(0, name.length() - ".jar".length()) : name;
		return baseName + MIGRATION_SUFFIX + (includeLegacySuffix ? NEW_MIGRATION_SUFFIX : "") + ".jar";
	}

	private static String getFinalNameForLegacyJar(File legacyJar) {
		String name = legacyJar.getName();
		int legacySuffixLength = (MIGRATION_SUFFIX + NEW_MIGRATION_SUFFIX + ".jar").length();
		return name.substring(0, name.length() - legacySuffixLength) + MIGRATION_SUFFIX + ".jar";
	}

	private static boolean isReadableMigratedJar(File jarFile) {
		try {
			return jarFile.isFile() && !containsOldPackage(jarFile);
		} catch (JarMigrationException e) {
			return false;
		}
	}

	/**
	 * Check if the JAR file still contains classes in the old OpenRocket package structure (net.sf.openrocket).
	 * @param jarFile The JAR file to check.
	 * @return True if the JAR file contains classes in the old package structure.
	 * @throws JarMigrationException If an error occurs while checking the JAR file.
	 */
	private static boolean containsOldPackage(File jarFile) throws JarMigrationException {
		try (JarFile jar = new JarFile(jarFile)) {
			return jar.stream().anyMatch(entry -> entry.getName().startsWith(OLD_PACKAGE_PATH_PREFIX));
		} catch (IOException e) {
			String msg = String.format("Error checking JAR file %s: %s", jarFile.getName(), e.getMessage());
			throw new JarMigrationException(msg, e);
		}
	}

	/**
	 * Migrate the JAR file to the new OpenRocket package structure (info.openrocket.core and info.openrocket.swing).
	 * @param inputJar The JAR file to migrate.
	 * @return The migrated JAR file.
	 * @throws JarMigrationException If an error occurs while migrating the JAR file.
	 */
	public static File migrateJarFile(File inputJar) throws JarMigrationException {
		final String migratedName = getMigratedName(inputJar, false);
		final File migratedJar = new File(inputJar.getParent(), migratedName);
		Path temporaryJar = null;

		CustomRemapper remapper = new CustomRemapper();

		try {
			temporaryJar = Files.createTempFile(inputJar.getParentFile().toPath(), migratedName + "-", ".tmp");
			try (JarFile jar = new JarFile(inputJar);
				 JarOutputStream jos = new JarOutputStream(new FileOutputStream(temporaryJar.toFile()))) {

				jar.stream().forEach(entry -> {
					try (InputStream is = jar.getInputStream(entry)) {
						String name = entry.getName();
						String newName = name;
						// Only remap entries in net/sf/openrocket
						if (name.startsWith(OLD_PACKAGE_PATH_PREFIX)) {
							newName = remapper.map(name);
						}
						// Skip leftover net/sf entries.
						if (newName.startsWith("net/sf/") || newName.startsWith("net")) {
							return;
						}
						JarEntry newEntry = new JarEntry(newName);
						jos.putNextEntry(newEntry);
						if (!entry.isDirectory()) {
							byte[] data;
							if (name.endsWith(".class")) {
								// Remap class files using ASM.
								ClassReader reader = new ClassReader(is);
								ClassWriter writer = new ClassWriter(reader, 0);
								ClassRemapper classRemapper = new ClassRemapper(writer, remapper);
								reader.accept(classRemapper, 0);
								data = writer.toByteArray();
							} else {
								// Copy other resources as is.
								data = is.readAllBytes();
							}
							jos.write(data);
						}
						jos.closeEntry();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}

			if (containsOldPackage(temporaryJar.toFile())) {
				throw new JarMigrationException("Migrated JAR still contains old OpenRocket packages: "
						+ migratedJar.getName());
			}
			moveReplacing(temporaryJar, migratedJar.toPath());
		} catch (IOException | RuntimeException e) {
			String msg = String.format("Error migrating JAR file %s: %s", migratedJar.getName(), e.getMessage());
			throw new JarMigrationException(msg, e);
		} finally {
			if (temporaryJar != null) {
				try {
					Files.deleteIfExists(temporaryJar);
				} catch (IOException ignored) {
					// The incomplete temporary file is not used for migration detection or classpath construction.
				}
			}
		}

		return migratedJar;
	}

	private static void moveReplacing(Path source, Path destination) throws IOException {
		try {
			Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Custom remapper that remaps classes in net/sf/openrocket to info/openrocket/core or info/openrocket/swing.
	 * If the class belongs to a common package, it checks if the class exists in core. If it does, it remaps to core,
	 * otherwise it remaps to swing.
	 * If the class belongs to a swing exclusive package, it remaps to swing.
	 * All other classes default to core.
	 */
	public static class CustomRemapper extends Remapper {
		private static final Set<String> commonPackages;
		private static final Set<String> swingExclusivePackages;

		static {
			commonPackages = Set.of("communication", "file", "logging", "simulation/extension", "startup", "utils");
			swingExclusivePackages = Set.of("gui");
		}

		@Override
		public String map(String internalName) {
			// Only remap classes in net/sf/openrocket
			if (internalName.startsWith("net/sf/openrocket/")) {
				String rest = internalName.substring("net/sf/openrocket/".length());

				// 1. If it belongs to a swing exclusive package, map to swing.
				for (String swingPkg : swingExclusivePackages) {
					if (rest.startsWith(swingPkg + "/")) {
						return "info/openrocket/swing/" + rest;
					}
				}

				// 2. If it belongs to a common package, check if it exists in core.
				for (String commonPkg : commonPackages) {
					if (rest.startsWith(commonPkg + "/")) {
						String coreCandidate = "info/openrocket/core/" + rest;
						if (resourceExists(coreCandidate)) {
							return coreCandidate;
						} else {
							return "info/openrocket/swing/" + rest;
						}
					}
				}

				// 3. All other classes default to core.
				return "info/openrocket/core/" + rest;
			}
			return internalName;
		}

		private boolean resourceExists(String internalName) {
			// Look for the .class resource in the classpath
			String resourcePath = internalName + ".class";
			return getClass().getClassLoader().getResource(resourcePath) != null;
		}
	}
}
