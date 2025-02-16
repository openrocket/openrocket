package info.openrocket.core.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
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
	private static final String OLD_PACKAGE_PREFIX = "net.sf.openrocket";
	public static final String MIGRATION_SUFFIX = "-migrated";
	// We first save the migrated plugin with this suffix so SwingStartup can check later that it has been migrated.
	// It is then renamed to the final name (without this suffix).
	public static final String NEW_MIGRATION_SUFFIX = "-new";

	/**
	 * Exception thrown when an error occurs while migrating a JAR file.
	 */
	public static class JarMigrationException extends Exception {
		public JarMigrationException(String message) {
			super(message);
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
		return containsOldPackage(jarFile) && !isMigrated(jarFile, allJars);
	}

	/**
	 * Check if the JAR file has already been migrated.
	 * @param jarFile The JAR file to check.
	 * @param allJars All JAR files in the plugin directory.
	 * @return True if the JAR file has already been migrated.
	 */
	private static boolean isMigrated(File jarFile, List<File> allJars) {
		if (jarFile.getName().contains(MIGRATION_SUFFIX)) {
			return true;
		}

		// Check if another JAR in the list is the migrated version of this JAR
		String migratedName = getMigratedName(jarFile, false);

		for (File f : allJars) {
			if (f.getName().equals(migratedName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the name of the migrated JAR file.
	 * @param jarFile The original JAR file.
	 * @return The name of the migrated JAR file.
	 */
	private static String getMigratedName(File jarFile, boolean includeNewSuffix) {
		return jarFile.getName().replaceFirst("\\.jar$", MIGRATION_SUFFIX + (includeNewSuffix ? NEW_MIGRATION_SUFFIX : "") + ".jar");
	}

	private static String getMigratedName(File jarFile) {
		return getMigratedName(jarFile, true);
	}

	/**
	 * Check if the JAR file still contains classes in the old OpenRocket package structure (net.sf.openrocket).
	 * @param jarFile The JAR file to check.
	 * @return True if the JAR file contains classes in the old package structure.
	 * @throws JarMigrationException If an error occurs while checking the JAR file.
	 */
	private static boolean containsOldPackage(File jarFile) throws JarMigrationException {
		try (JarInputStream jin = new JarInputStream(new FileInputStream(jarFile))) {
			JarEntry entry;
			while ((entry = jin.getNextJarEntry()) != null) {
				String name = entry.getName();
				if (name.replace('/', '.').startsWith(OLD_PACKAGE_PREFIX)) {
					return true;
				}
			}
		} catch (IOException e) {
			String msg = String.format("Error checking JAR file %s: %s", jarFile.getName(), e.getMessage());
			throw new JarMigrationException(msg);
		}
		return false;
	}

	/**
	 * Migrate the JAR file to the new OpenRocket package structure (info.openrocket.core and info.openrocket.swing).
	 * @param inputJar The JAR file to migrate.
	 * @return The migrated JAR file.
	 * @throws JarMigrationException If an error occurs while migrating the JAR file.
	 */
	public static File migrateJarFile(File inputJar) throws JarMigrationException {
		final String migratedName = getMigratedName(inputJar);
		final File migratedJar = new File(inputJar.getParent(), migratedName);

		CustomRemapper remapper = new CustomRemapper();

		try (JarFile jar = new JarFile(inputJar);
			 JarOutputStream jos = new JarOutputStream(new FileOutputStream(migratedJar))) {

			jar.stream().forEach(entry -> {
				try (InputStream is = jar.getInputStream(entry)) {
					String name = entry.getName();
					String newName = name;
					// Only remap entries in net/sf/openrocket
					if (name.startsWith("net/sf/openrocket/")) {
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
					throw new RuntimeException(e.getMessage());
				}
			});
		} catch (IOException | RuntimeException e) {
			if (migratedJar.exists()) {
				migratedJar.delete();
			}
			String msg = String.format("Error migrating JAR file %s: %s", migratedJar.getName(), e.getMessage());
			throw new JarMigrationException(e.getMessage());
		}

		return migratedJar;
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