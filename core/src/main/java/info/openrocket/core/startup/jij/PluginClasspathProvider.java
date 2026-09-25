package info.openrocket.core.startup.jij;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.openrocket.core.plugin.JarMigrationHelper;
import info.openrocket.core.plugin.PluginHelper;
import info.openrocket.core.util.BugException;


public class PluginClasspathProvider implements ClasspathProvider {
	
	private static final String CUSTOM_PLUGIN_PROPERTY = "openrocket.plugins";
	
	@Override
	public List<URL> getUrls() {
		List<URL> urls = new ArrayList<>();
		
		findPluginDirectoryUrls(urls);
		findCustomPlugins(urls);
		
		return urls;
	}
	
	private void findPluginDirectoryUrls(List<URL> urls) {
		List<File> files = PluginHelper.getPluginJars();

		// Migrate files that still use the old package structure (net.sf.openrocket instead of info.openrocket.core/swing)
		List<File> migratedFiles = getCompatibleFiles(files);

		for (File f : migratedFiles) {
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new BugException(e);
			}
		}
	}

	/**
	 * Return a list of both JAR files that are already compatible with the new package structure and JAR files that
	 * have been migrated to the new package structure.
	 * @param files List of JAR files to check.
	 * @return List of JAR files that are compatible with the new package structure.
	 */
	static List<File> getCompatibleFiles(List<File> files) {
		List<File> normalizedFiles = normalizeLegacyMigratedFiles(files);
		List<File> compatibleFiles = new ArrayList<>();
		Set<Path> compatiblePaths = new HashSet<>();
		List<String> migratedThisRun = new ArrayList<>();
		System.clearProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY);

		for (File f : normalizedFiles) {
			try {
				JarMigrationHelper.MigrationStatus status =
						JarMigrationHelper.getMigrationStatus(f, normalizedFiles);
				switch (status) {
					case COMPATIBLE -> addCompatibleFile(compatibleFiles, compatiblePaths, f);
					case NEEDS_MIGRATION -> {
						File migratedJar = JarMigrationHelper.migrateJarFile(f);
						addCompatibleFile(compatibleFiles, compatiblePaths, migratedJar);
						migratedThisRun.add(migratedJar.getAbsolutePath());
					}
					case SUPERSEDED -> {
						// The migrated companion is processed separately; do not put the old source JAR on the classpath.
					}
				}
			} catch (JarMigrationHelper.JarMigrationException e) {
				// Exclude unreadable or unsuccessfully migrated JARs from the plugin classpath.
				System.err.println(e.getMessage());
			}
		}

		// Record the migrations so the UI can later inform the user (only for migrations performed during
		// this startup, to avoid repeating the message on every startup)
		if (!migratedThisRun.isEmpty()) {
			System.setProperty(JarMigrationHelper.MIGRATED_JARS_PROPERTY,
					String.join(File.pathSeparator, migratedThisRun));
		}

		return compatibleFiles;
	}

	/**
	 * Rename legacy migrated JARs before the plugin classloader is created. The returned list is de-duplicated because
	 * both the legacy and final name can be present in the directory during recovery from older migrations.
	 */
	private static List<File> normalizeLegacyMigratedFiles(List<File> files) {
		List<File> normalizedFiles = new ArrayList<>();
		Set<Path> normalizedPaths = new HashSet<>();
		for (File file : files) {
			File normalizedFile = file;
			if (JarMigrationHelper.isLegacyMigratedJar(file)) {
				try {
					normalizedFile = JarMigrationHelper.normalizeLegacyMigratedJar(file);
				} catch (JarMigrationHelper.JarMigrationException e) {
					// Keep the legacy path as a fallback; its validity is checked before it is added to the classpath.
					System.err.println(e.getMessage());
				}
			}
			addCompatibleFile(normalizedFiles, normalizedPaths, normalizedFile);
		}
		return normalizedFiles;
	}

	private static void addCompatibleFile(List<File> files, Set<Path> paths, File file) {
		Path path = file.toPath().toAbsolutePath().normalize();
		if (paths.add(path)) {
			files.add(file);
		}
	}

	private void findCustomPlugins(List<URL> urls) {
		String prop = System.getProperty(CUSTOM_PLUGIN_PROPERTY);
		if (prop == null) {
			return;
		}
		
		String[] array = prop.split(File.pathSeparator);
		for (String s : array) {
			s = s.trim();
			if (!s.isEmpty()) {
				try {
					urls.add(new File(s).toURI().toURL());
				} catch (MalformedURLException e) {
					throw new BugException(e);
				}
			}
		}
	}
	
}
