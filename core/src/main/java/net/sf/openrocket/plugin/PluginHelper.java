package net.sf.openrocket.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.arch.SystemInfo;

public class PluginHelper {
	
	private static final String PLUGIN_DIRECTORY = "Plugins";
	private static final String PLUGIN_EXTENSION = ".jar";
	
	public static List<File> getPluginJars() {
		File userDir = SystemInfo.getUserApplicationDirectory();
		File pluginDir = new File(userDir, PLUGIN_DIRECTORY);
		if (!pluginDir.exists()) {
			pluginDir.mkdirs();
		}
		
		File[] files = pluginDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(PLUGIN_EXTENSION);
			}
		});
		
		if (files == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(files);
		}
	}
	
}
