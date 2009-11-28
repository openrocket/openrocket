package net.sf.openrocket.util;

/**
 * An exception to be thrown when a fatal problem with the environment
 * is encountered (for example some file cannot be found).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ConfigurationException extends FatalException {

	public ConfigurationException() {
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
