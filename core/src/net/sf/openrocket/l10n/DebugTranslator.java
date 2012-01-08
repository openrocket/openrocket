package net.sf.openrocket.l10n;

/**
 * A translator implementation that returns the logical key in brackets instead
 * of an actual translation.  The class optionally verifies that the translation
 * is actually obtainable from some other translator.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DebugTranslator implements Translator {
	
	private final Translator translator;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param translator	the translator to verify the translation exists, or <code>null</code> not to verify.
	 */
	public DebugTranslator(Translator translator) {
		this.translator = translator;
	}
	
	

	@Override
	public String get(String key) {
		if (translator != null) {
			translator.get(key);
		}
		return "[" + key + "]";
	}
	
}
