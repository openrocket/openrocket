package net.sf.openrocket.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class L10nPropertyReport {
	
	private static String[] supportedLocales = new String[] { "en", "de", "es", "fr", "it", "ru", "cs", "pl", "ja", "pt", "tr", "zh_CN", "uk_UA" };
	
	public static void main(String[] args) throws Exception {
		
		Properties english;
		
		Map<String, Properties> langs = new HashMap<String, Properties>();
		
		System.out.println("Loading All Resource files");
		
		english = new Properties();
		InputStream in = L10nPropertyReport.class.getResourceAsStream("/l10n/messages.properties");
		english.load(in);
		
		System.out.println("en contains " + english.keySet().size());
		
		
		for (String localename : supportedLocales) {
			
			if ("en".equals(localename)) {
				continue;
			}
			
			Properties p = new Properties();
			
			in = L10nPropertyReport.class.getResourceAsStream("/l10n/messages_" + localename + ".properties");
			
			p.load(in);
			
			System.out.println(localename + " contains " + p.keySet().size());
			
			langs.put(localename, p);
			
		}
		
		// check for languages missing an en key:
		
		List<String> sortedKeys = getSortedKeys(english);
		
		for (String key : sortedKeys) {
			
			List<String> missing = new ArrayList<String>(10);
			
			for (Map.Entry<String, Properties> en : langs.entrySet()) {
				if (en.getValue().getProperty(key) != null) {
					continue;
				}
				missing.add(en.getKey());
			}
			
			if (missing.size() > 0) {
				System.out.println(key + " missing from " + missing);
			}
			
		}
		
		// Check each locale for extra keys:
		for (Map.Entry<String, Properties> lang : langs.entrySet()) {
			System.out.println("Extra keys in " + lang.getKey());
			
			sortedKeys = getSortedKeys(lang.getValue());
			for (String key : sortedKeys) {
				if (english.getProperty(key) == null) {
					System.out.println("\t" + key);
				}
			}
		}
	}
	
	private static List<String> getSortedKeys(Properties p) {
		List<String> sortedKeys = new ArrayList<String>(p.keySet().size());
		for (Object obj : p.keySet()) {
			sortedKeys.add((String) obj);
		}
		Collections.sort(sortedKeys);
		return sortedKeys;
		
	}
}
