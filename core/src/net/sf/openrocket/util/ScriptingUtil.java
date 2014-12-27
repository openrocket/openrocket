package net.sf.openrocket.util;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public class ScriptingUtil {
	
	/** The name to be chosen from a list of alternatives.  If not found, will use the default name. */
	private static final List<String> PREFERRED_LANGUAGE_NAMES = Arrays.asList("JavaScript");
	
	/**
	 * Return the preferred internal language name based on a script language name.
	 * 
	 * @return	the preferred language name, or null if the language is not supported.
	 */
	public static String getLanguage(String language) {
		if (language == null) {
			return null;
		}
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(language);
		if (engine == null) {
			return null;
		}
		return getLanguage(engine.getFactory());
	}
	
	
	public static List<String> getLanguages() {
		List<String> langs = new ArrayList<String>();
		ScriptEngineManager manager = new ScriptEngineManager();
		for (ScriptEngineFactory factory : manager.getEngineFactories()) {
			langs.add(getLanguage(factory));
		}
		return langs;
	}
	
	
	private static String getLanguage(ScriptEngineFactory factory) {
		for (String name : factory.getNames()) {
			if (PREFERRED_LANGUAGE_NAMES.contains(name)) {
				return name;
			}
		}
		
		return factory.getLanguageName();
	}
}
