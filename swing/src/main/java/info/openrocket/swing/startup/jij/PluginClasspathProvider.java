package info.openrocket.swing.startup.jij;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
	private static List<File> getCompatibleFiles(List<File> files) {
		List<File> migratedFiles = new ArrayList<>();
		for (File f : files) {
			try {
				if (JarMigrationHelper.shouldMigrate(f, files)) {
					File migratedJar = JarMigrationHelper.migrateJarFile(f);
					migratedFiles.add(migratedJar);
				} else {
					migratedFiles.add(f);
				}
			} catch (JarMigrationHelper.JarMigrationException e) {
				// Ignore the exception, the JAR file is not migrated
				System.err.println(e.getMessage());
			}
		}
		return migratedFiles;
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
