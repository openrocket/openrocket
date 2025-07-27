package info.openrocket.swing.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngineFactory;

import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import info.openrocket.core.scripting.ScriptEngineManagerRedux;

public class Scripting {
	
	public static void main(String[] args) {
		System.out.println("Scripting APIs:");
		
		ScriptEngineManagerRedux manager = new ScriptEngineManagerRedux();
		for (ScriptEngineFactory factory : manager.getEngineFactories()) {
			System.out.println("  engineName=" + factory.getEngineName() +
					" engineVersion=" + factory.getEngineVersion() +
					" languageName=" + factory.getLanguageName() +
					" languageVersion=" + factory.getLanguageVersion() +
					" names=" + factory.getNames() +
					" mimeTypes=" + factory.getMimeTypes() +
					" extensions=" + factory.getExtensions());
		}
		System.out.println();
		
		System.out.println("RSyntaxTextArea supported syntax languages:");
		TokenMakerFactory f = TokenMakerFactory.getDefaultInstance();
		List<String> list = new ArrayList<>(f.keySet());
		Collections.sort(list);
		for (String type : list) {
			System.out.println("  " + type);
		}
		System.out.println();
	}
}
