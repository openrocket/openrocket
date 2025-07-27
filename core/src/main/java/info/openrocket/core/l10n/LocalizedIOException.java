package info.openrocket.core.l10n;

import java.io.IOException;

import info.openrocket.core.startup.Application;

public class LocalizedIOException extends IOException {

	private static final Translator trans = Application.getTranslator();

	private final String key;

	public LocalizedIOException(String key) {
		super(key);
		this.key = key;
	}

	public LocalizedIOException(String key, Throwable cause) {
		super(key, cause);
		this.key = key;
	}

	@Override
	public String getLocalizedMessage() {
		return trans.get(key);
	}
}
