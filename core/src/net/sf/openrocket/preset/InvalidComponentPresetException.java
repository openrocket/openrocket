package net.sf.openrocket.preset;

import java.util.ArrayList;
import java.util.List;

public class InvalidComponentPresetException extends Exception {

	private List<String> errors = new ArrayList<String>();
	private List<TypedKey<?>> invalidParameters = new ArrayList<TypedKey<?>>();
	
	public InvalidComponentPresetException() {
		super();
	}

	public InvalidComponentPresetException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidComponentPresetException(String message) {
		super(message);
	}

	public InvalidComponentPresetException(Throwable cause) {
		super(cause);
	}
	
	void addInvalidParameter(TypedKey<?> key ) {
		invalidParameters.add(key);
	}
	
	void addInvalidParameter(TypedKey<?> key, String message ) {
		invalidParameters.add(key);
		errors.add(message);
	}
	
	void addMessage( String message ) {
		errors.add(message);
	}

	boolean hasProblems() {
		return (invalidParameters.size() + errors.size()) > 0;
	}
	
	public int problemCount() {
		return Math.max( invalidParameters.size(), errors.size() );
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public List<TypedKey<?>> getInvalidParameters() {
		return invalidParameters;
	}
	
}
