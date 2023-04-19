package net.sf.openrocket.startup.jij;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.plugin.PluginHelper;
import net.sf.openrocket.util.BugException;

public class PluginClasspathProvider implements ClasspathProvider {
	
	private static final String CUSTOM_PLUGIN_PROPERTY = "openrocket.plugins";
	
	@Override
	public List<URL> getUrls() {
		List<URL> urls = new ArrayList<URL>();
		
		findPluginDirectoryUrls(urls);
		findCustomPlugins(urls);
		
		return urls;
	}
	
	private void findPluginDirectoryUrls(List<URL> urls) {
		List<File> files = PluginHelper.getPluginJars();
		for (File f : files) {
			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new BugException(e);
			}
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
			if (s.length() > 0) {
				try {
					urls.add(new File(s).toURI().toURL());
				} catch (MalformedURLException e) {
					throw new BugException(e);
				}
			}
		}
	}
	
}
