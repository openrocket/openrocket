package net.sf.openrocket.l10n;

/**
 * A translator implementation that returns the logical key in brackets instead
 * of an actual translation.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DebugTranslator implements Translator {
	
	@Override
	public String get(String key) {
		return "[" + key + "]";
	}
	
}
