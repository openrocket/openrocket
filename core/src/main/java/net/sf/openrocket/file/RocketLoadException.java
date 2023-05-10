package net.sf.openrocket.file;

public class RocketLoadException extends Exception {

	public RocketLoadException() {
	}

	public RocketLoadException(String message) {
		super(message);
	}

	public RocketLoadException(Throwable cause) {
		super(cause);
	}

	public RocketLoadException(String message, Throwable cause) {
		super(message, cause);
	}

}
