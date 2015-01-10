package net.sf.openrocket.simulation.extension.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;

import com.google.inject.Inject;

/**
 * Utility class used by the scripting extension and its configurator.
 */
public class ScriptingUtil {
	
	static final String NODE_ID = ScriptingExtension.class.getCanonicalName();
	
	private static final List<String> DEFAULT_TRUSTED_HASHES = Arrays.asList(
			// Roll control script in roll control example file:
			"SHA-256:9bf364ce4d4a75f09b29178bf9d6872b232084f73dae20dc7b5b073e54e95a42"
			);
	
	/** The name to be chosen from a list of alternatives.  If not found, will use the default name. */
	private static final List<String> PREFERRED_LANGUAGE_NAMES = Arrays.asList("JavaScript");
	
	@Inject
	Preferences prefs;
	
	
	
	
	/**
	 * Return the preferred internal language name based on a script language name.
	 * 
	 * @return	the preferred language name, or null if the language is not supported.
	 */
	public String getLanguage(String language) {
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
	
	
	public List<String> getLanguages() {
		List<String> langs = new ArrayList<String>();
		ScriptEngineManager manager = new ScriptEngineManager();
		for (ScriptEngineFactory factory : manager.getEngineFactories()) {
			langs.add(getLanguage(factory));
		}
		return langs;
	}
	
	
	private String getLanguage(ScriptEngineFactory factory) {
		for (String name : factory.getNames()) {
			if (PREFERRED_LANGUAGE_NAMES.contains(name)) {
				return name;
			}
		}
		
		return factory.getLanguageName();
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
			digest.update(language.getBytes("UTF-8"));
			digest.update((byte) '|');
			byte[] hash = digest.digest(script.getBytes("UTF-8"));
			BigInteger bigInt = new BigInteger(1, hash);
			output = bigInt.toString(16);
			while (output.length() < 64) {
				output = "0" + output;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new BugException("JRE does not support SHA-256 hash algorithm", e);
		} catch (UnsupportedEncodingException e) {
			throw new BugException(e);
		}
		
		return digest.getAlgorithm() + ":" + output;
	}
	
}
