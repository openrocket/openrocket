package net.sf.openrocket.simulation.extension.impl;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import net.sf.openrocket.scripting.ScriptEngineManagerRedux;
import net.sf.openrocket.scripting.GraalJSScriptEngineFactory;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;

import com.google.inject.Inject;

/**
 * Utility class used by the scripting extension and its configurator.
 */
public class ScriptingUtil {
	static final String NODE_ID = ScriptingExtension.class.getCanonicalName();
	
	private static final List<String> DEFAULT_TRUSTED_HASHES = List.of(
			// Roll control script in roll control example file:
			"SHA-256:9bf364ce4d4a75f09b29178bf9d6872b232084f73dae20dc7b5b073e54e95a42"
	);
	
	/** The name to be chosen from a list of alternatives.  If not found, will use the default name. */
	private static final List<String> PREFERRED_LANGUAGE_NAMES = List.of("JavaScript");

	private static ScriptEngineManagerRedux manager;
	
	@Inject
	Preferences prefs;

	public ScriptingUtil() {
		if (manager == null) {
			// using the ScriptEngineManger from javax.script package pulls in the sun.misc.ServiceConfigurationError 
			// which is removed in Java 9+ which causes a ClassNotFoundException to be thrown.
			manager = new ScriptEngineManagerRedux();

			manager.registerEngineName("Javascript", new GraalJSScriptEngineFactory());
		}
	}

	public ScriptEngine getEngineByName(String shortName) {
		return manager.getEngineByName(shortName);
	}
	
	/**
	 * Return the preferred internal language name based on a script language name.
	 * 
	 * @return	the preferred language name, or null if the language is not supported.
	 */
	public String getLanguage(String language) {
		if (language == null) {
			return null;
		}

		ScriptEngine engine = manager.getEngineByName(language);
		if (engine == null) {
			return null;
		}

		return getLanguageByFactory(engine.getFactory());
	}

	public List<String> getLanguages() {
		List<String> languages = new ArrayList<>();
		for (ScriptEngineFactory factory : manager.getEngineFactories()) {
			languages.add(getLanguageByFactory(factory));
		}
		return languages;
	}
	
	/**
	 * Test whether the user has indicated this script to be trusted,
	 * or if it is an internally trusted script.
	 */
	public boolean isTrustedScript(String language, String script) {
		if (language == null || script == null) {
			return false;
		}
		script = normalize(script);
		if (script.length() == 0) {
			return true;
		}
		String hash = hash(language, script);
		if (DEFAULT_TRUSTED_HASHES.contains(hash)) {
			return true;
		}
		return prefs.getNode(NODE_ID).getBoolean(hash, false);
	}
	
	/**
	 * Mark a script as trusted.
	 */
	public void setTrustedScript(String language, String script, boolean trusted) {
		script = normalize(script);
		String hash = hash(language, script);
		if (trusted) {
			prefs.getNode(NODE_ID).putBoolean(hash, true);
		} else {
			prefs.getNode(NODE_ID).remove(hash);
		}
	}
	
	/**
	 * Clear all trusted scripts.
	 */
	public void clearTrustedScripts() {
		try {
			prefs.getNode(NODE_ID).clear();
		} catch (BackingStoreException e) {
			throw new BugException(e);
		}
	}

	private String getLanguageByFactory(ScriptEngineFactory factory) {
		for (String name : factory.getNames()) {
			if (PREFERRED_LANGUAGE_NAMES.contains(name)) {
				return name;
			}
		}

		return factory.getLanguageName();
	}
	
	static String normalize(String script) {
		return script.replaceAll("\r", "").trim();
	}
	
	static String hash(String language, String script) {
		/*
		 * NOTE:  Hash length must be max 80 chars, the max length of a key in a Properties object.
		 */
		String output;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.update(language.getBytes(StandardCharsets.UTF_8));
			digest.update((byte) '|');
			byte[] hash = digest.digest(script.getBytes(StandardCharsets.UTF_8));
			BigInteger bigInt = new BigInteger(1, hash);
			output = bigInt.toString(16);
			while (output.length() < 64) {
				output = "0" + output;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new BugException("JRE does not support SHA-256 hash algorithm", e);
		}

		return digest.getAlgorithm() + ":" + output;
	}
}
