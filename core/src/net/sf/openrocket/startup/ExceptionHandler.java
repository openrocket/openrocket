package net.sf.openrocket.startup;

public interface ExceptionHandler {

	public void handleErrorCondition(String message);
	public void handleErrorCondition(String message, Throwable exception);
	public void handleErrorCondition(final Throwable exception);

	
	public void uncaughtException(final Thread thread, final Throwable throwable);
	
}
