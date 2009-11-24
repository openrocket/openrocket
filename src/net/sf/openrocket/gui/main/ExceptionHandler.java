package net.sf.openrocket.gui.main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.openrocket.gui.dialogs.BugReportDialog;


public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final int MEMORY_RESERVE = 512*1024;
	
	/**
	 * A memory reserve of 0.5 MB of memory, that can be freed when showing the dialog.
	 */
	private static volatile byte[] memoryReserve = null;
	
	private static ExceptionHandler instance = null;
	
	
	private volatile boolean handling = false;
	
	
	
	
	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		
		// Free memory reserve if out of memory
		if (e instanceof OutOfMemoryError) {
			memoryReserve = null;
			handling = false;
		}

		e.printStackTrace();
		
		try {
			
			if (handling) {
				System.err.println("Exception is currently being handled, ignoring:");
				e.printStackTrace();
				return;
			}
			
			handling = true;
			
			// Show on the EDT
			if (SwingUtilities.isEventDispatchThread()) {
				showDialog(t, e);
			} else {
	            SwingUtilities.invokeAndWait(new Runnable() {
	                public void run() {
	                    showDialog(t, e);
	                }
	            });
			}
			
		} catch (Throwable ex) {
			
			// Make sure the handler does not throw any exceptions
			try {
				System.err.println("Exception in exception handler, dumping exception:");
				ex.printStackTrace();
			} catch (Throwable ignore) { }
			
		} finally {
			// Mark handling as completed
			handling = false;
		}
		
	}

	
	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * 
	 * @param message	the error message.
	 */
	public static void handleErrorCondition(String message) {
		handleErrorCondition(new InternalException(message));
	}
	

	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * 
	 * @param message	the error message.
	 * @param exception	the exception that occurred.
	 */
	public static void handleErrorCondition(String message, Exception exception) {
		handleErrorCondition(new InternalException(message, exception));
	}
	
	
	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * 
	 * @param exception		the exception that occurred.
	 */
	public static void handleErrorCondition(final Exception exception) {
		final ExceptionHandler handler;

		try {

			if (instance == null) {
				handler = new ExceptionHandler();
			} else {
				handler = instance;
			}

			final Thread thread = Thread.currentThread();

			if (SwingUtilities.isEventDispatchThread()) {
				handler.showDialog(thread, exception);
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						handler.showDialog(thread, exception);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * The actual handling routine.
	 * 
	 * @param t		the thread that caused the exception, or <code>null</code>.
	 * @param e		the exception.
	 */
	private void showDialog(Thread t, Throwable e) {
		
		// Out of memory
		if (e instanceof OutOfMemoryError) {
			JOptionPane.showMessageDialog(null, 
					new Object[] { 
						"Out of memory!",
						"<html>You should immediately close unnecessary design windows,<br>" +
						"save any unsaved designs and restart OpenRocket!"
					}, "Out of memory", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Unknown Error
		if (!(e instanceof Exception)) {
			JOptionPane.showMessageDialog(null, 
					new Object[] { 
						"An unknown Java error occurred:",
						e.getMessage(),
						"<html>You should immediately close unnecessary design windows,<br>" +
						"save any unsaved designs and restart OpenRocket!"
					}, "Unknown Java error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		// Normal exception, show question dialog
		String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
		if (msg.length() > 90) {
			msg = msg.substring(0, 80) + "...";
		}
		
		
		int selection = JOptionPane.showOptionDialog(null, new Object[] {
				"OpenRocket encountered an uncaught exception.  This typically signifies " +
				"a bug in the software.", 
				"<html><em>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + msg + "</em>",
				" ",
				"Please take a moment to report this bug to the developers.",
				"This can be done automatically if you have an Internet connection."
				}, "Uncaught exception", JOptionPane.DEFAULT_OPTION, 
				JOptionPane.ERROR_MESSAGE, null, 
				new Object[] { "View bug report", "Close" }, "View bug report");
		
		if (selection != 0) {
			// User cancelled
			return;
		}
		
		// Show bug report dialog
		BugReportDialog.showExceptionDialog(null, t, e);

	}
	
	
	
	/**
	 * Registers the uncaught exception handler.  This should be used to ensure that
	 * all necessary registrations are performed.
	 */
	public static void registerExceptionHandler() {
		
		if (instance == null) {
			instance = new ExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(instance);
			
			// Handler for modal dialogs of Sun's Java implementation
			// See bug ID 4499199.
			System.setProperty("sun.awt.exception.handler", AwtHandler.class.getName());
			
			reserveMemory();
		}
		
	}
	
	
	private static void reserveMemory() {
		memoryReserve = new byte[MEMORY_RESERVE];
		for (int i=0; i<MEMORY_RESERVE; i++) {
			memoryReserve[i] = (byte)i;
		}
	}

	
	/**
	 * Handler used in modal dialogs by Sun Java implementation.
	 */
	public static class AwtHandler {
		public void handle(Throwable t) {
			
			/*
			 * Detect and ignore bug 6828938 in Sun JRE 1.6.0_14 - 1.6.0_16.
			 */
			if (t instanceof ArrayIndexOutOfBoundsException) {
				final String buggyClass = "sun.font.FontDesignMetrics";
				StackTraceElement[] elements = t.getStackTrace();
				if (elements.length >= 3 &&
						(buggyClass.equals(elements[0].getClassName()) ||
						 buggyClass.equals(elements[1].getClassName()) ||
						 buggyClass.equals(elements[2].getClassName()))) {
					System.err.println("Ignoring Sun JRE bug 6828938:  " + t);
					return;
				}
			}
			
			
			if (instance != null) {
				instance.uncaughtException(Thread.currentThread(), t);
			}
		}
	}
	
	
	private static class InternalException extends Exception {
		public InternalException() {
			super();
		}

		public InternalException(String message, Throwable cause) {
			super(message, cause);
		}

		public InternalException(String message) {
			super(message);
		}

		public InternalException(Throwable cause) {
			super(cause);
		}
	}
}
