package net.sf.openrocket.gui.main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.openrocket.gui.dialogs.BugReportDialog;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwingExceptionHandler implements Thread.UncaughtExceptionHandler, ExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(SwingExceptionHandler.class);

	private static final int MEMORY_RESERVE = 512 * 1024;

	/**
	 * A memory reserve of 0.5 MB of memory, that can be freed when showing the dialog.
	 * <p>
	 * This field is package-private so that the JRE cannot optimize its use away.
	 */
	volatile byte[] memoryReserve = null;

	private volatile boolean handling = false;




	@Override
	public void uncaughtException(final Thread thread, final Throwable throwable) {

		// Free memory reserve if out of memory
		if (isOutOfMemoryError(throwable)) {
			memoryReserve = null;
			handling = false;
			log.error("Out of memory error detected", throwable);
		}

		if (isNonFatalJREBug(throwable)) {
			log.warn("Ignoring non-fatal JRE bug", throwable);
			return;
		}

		log.error("Handling uncaught exception on thread=" + thread, throwable);
		throwable.printStackTrace();

		if (handling) {
			log.warn("Exception is currently being handled, ignoring");
			return;
		}

		try {
			handling = true;

			// Show on the EDT
			if (SwingUtilities.isEventDispatchThread()) {
				log.info("Exception handler running on EDT, showing dialog");
				showDialog(thread, throwable);
			} else {
				log.info("Exception handler not on EDT, invoking dialog on EDT");
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						showDialog(thread, throwable);
					}
				});
			}

		} catch (Throwable ex) {

			// Make sure the handler does not throw any exceptions
			try {
				log.error("Caught exception while handling exception", ex);
				System.err.println("Exception in exception handler, dumping exception:");
				ex.printStackTrace();
			} catch (Exception ignore) {
			}

		} finally {
			// Mark handling as completed
			handling = false;
		}

	}


	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * <p>
	 * This method is guaranteed never to throw an exception, and can thus be safely
	 * used in finally blocks.
	 * 
	 * @param message	the error message.
	 */
	@Override
	public void handleErrorCondition(String message) {
		log.error(message, new Throwable());
		handleErrorCondition(new InternalException(message));
	}


	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * <p>
	 * This method is guaranteed never to throw an exception, and can thus be safely
	 * used in finally blocks.
	 * 
	 * @param message	the error message.
	 * @param exception	the exception that occurred.
	 */
	@Override
	public void handleErrorCondition(String message, Throwable exception) {
		log.error(message, exception);
		handleErrorCondition(new InternalException(message, exception));
	}


	/**
	 * Handle an error condition programmatically without throwing an exception.
	 * This can be used in cases where recovery of the error is desirable.
	 * <p>
	 * This method is guaranteed never to throw an exception, and can thus be safely
	 * used in finally blocks.
	 * 
	 * @param exception		the exception that occurred.
	 */
	@Override
	public void handleErrorCondition(final Throwable exception) {
		try {
			if (!(exception instanceof InternalException)) {
				log.error("Error occurred", exception);
			}
			final Thread thread = Thread.currentThread();

			if (SwingUtilities.isEventDispatchThread()) {
				log.info("Running in EDT, showing dialog");
				this.showDialog(thread, exception);
			} else {
				log.info("Not in EDT, invoking dialog later");
				final SwingExceptionHandler instance = this;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						instance.showDialog(thread, exception);
					}
				});
			}
		} catch (Exception e) {
			log.error("Exception occurred in error handler", e);
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
		if (isOutOfMemoryError(e)) {
			log.info("Showing out-of-memory dialog");
			JOptionPane.showMessageDialog(null,
					new Object[] {
					"OpenRocket is out of available memory!",
					"You should immediately close unnecessary design windows,",
					"save any unsaved designs and restart OpenRocket!"
			}, "Out of memory", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Create the message
		String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
		if (msg.length() > 90) {
			msg = msg.substring(0, 80) + "...";
		}

		// Unknown Error
		if (!(e instanceof Exception) && !(e instanceof LinkageError)) {
			log.info("Showing Error dialog");
			JOptionPane.showMessageDialog(null,
					new Object[] {
					"An unknown Java error occurred:",
					msg,
					"<html>You should immediately close unnecessary design windows,<br>" +
							"save any unsaved designs and restart OpenRocket!"
			}, "Unknown Java error", JOptionPane.ERROR_MESSAGE);
			return;
		}


		// Normal exception, show question dialog		
		log.info("Showing Exception dialog");
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
			log.info(Markers.USER_MARKER, "User chose not to fill bug report");
			return;
		}

		// Show bug report dialog
		log.info(Markers.USER_MARKER, "User requested sending bug report");
		BugReportDialog.showExceptionDialog(null, t, e);
	}



	/**
	 * Registers the uncaught exception handler.  This should be used to ensure that
	 * all necessary registrations are performed.
	 */
	public void registerExceptionHandler() {

		Thread.setDefaultUncaughtExceptionHandler(this);

		// Handler for modal dialogs of Sun's Java implementation
		// See bug ID 4499199.
		System.setProperty("sun.awt.exception.handler", AwtHandler.class.getName());

		reserveMemory();

	}


	/**
	 * Reserve the buffer memory that is freed in case an OutOfMemoryError occurs.
	 */
	private void reserveMemory() {
		memoryReserve = new byte[MEMORY_RESERVE];
		for (int i = 0; i < MEMORY_RESERVE; i++) {
			memoryReserve[i] = (byte) i;
		}
	}



	/**
	 * Return whether this throwable was caused by an OutOfMemoryError
	 * condition.  An exception is deemed to be caused by OutOfMemoryError
	 * if the throwable or any of its causes is of the type OutOfMemoryError.
	 * <p>
	 * This method is required because Apple's JRE implementation sometimes
	 * masks OutOfMemoryErrors within RuntimeExceptions.  Idiots.
	 * 
	 * @param t		the throwable to examine.
	 * @return		whether this is an out-of-memory condition.
	 */
	private boolean isOutOfMemoryError(Throwable t) {
		while (t != null) {
			if (t instanceof OutOfMemoryError)
				return true;
			t = t.getCause();
		}
		return false;
	}



	/**
	 * Handler used in modal dialogs by Sun Java implementation.
	 */
	public static class AwtHandler {
		public void handle(Throwable t) {
			Application.getExceptionHandler().uncaughtException(Thread.currentThread(), t);
		}
	}


	/**
	 * Detect various non-fatal Sun JRE bugs.
	 * 
	 * @param t		the throwable
	 * @return		whether this exception should be ignored
	 */
	private boolean isNonFatalJREBug(Throwable t) {

		// NOTE:  Calling method logs the entire throwable, so log only message here


		/*
		 * Detect and ignore bug 6826104 in Sun JRE.
		 */
		if (t instanceof NullPointerException) {
			StackTraceElement[] trace = t.getStackTrace();

			if (trace.length > 3 &&
					trace[0].getClassName().equals("sun.awt.X11.XWindowPeer") &&
					trace[0].getMethodName().equals("restoreTransientFor") &&

					trace[1].getClassName().equals("sun.awt.X11.XWindowPeer") &&
					trace[1].getMethodName().equals("removeFromTransientFors") &&

					trace[2].getClassName().equals("sun.awt.X11.XWindowPeer") &&
					trace[2].getMethodName().equals("setModalBlocked")) {
				log.warn("Ignoring Sun JRE bug (6826104): http://bugs.sun.com/view_bug.do?bug_id=6826104" + t);
				return true;
			}

		}


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
				log.warn("Ignoring Sun JRE bug 6828938:  " +
						"(see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6828938): " + t);
				return true;
			}
		}

		/*
		 * Detect and ignore bug 6561072 in Sun JRE 1.6.0_?
		 */
		if (t instanceof NullPointerException) {
			StackTraceElement[] trace = t.getStackTrace();

			if (trace.length > 3 &&
					trace[0].getClassName().equals("javax.swing.JComponent") &&
					trace[0].getMethodName().equals("repaint") &&

					trace[1].getClassName().equals("sun.swing.FilePane$2") &&
					trace[1].getMethodName().equals("repaintListSelection") &&

					trace[2].getClassName().equals("sun.swing.FilePane$2") &&
					trace[2].getMethodName().equals("repaintSelection")) {
				log.warn("Ignoring Sun JRE bug 6561072 " +
						"(see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6561072): " + t);
				return true;
			}
		}


		/*
		 * Detect and ignore bug 6933331 in Sun JRE 1.6.0_18 and others
		 */
		if (t instanceof IllegalStateException) {
			StackTraceElement[] trace = t.getStackTrace();

			if (trace.length > 1 &&
					trace[0].getClassName().equals("sun.awt.windows.WComponentPeer") &&
					trace[0].getMethodName().equals("getBackBuffer")) {
				log.warn("Ignoring Sun JRE bug 6933331 " +
						"(see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6933331): " + t);
				return true;
			}
		}

		/*
		 * Detect and ignore bug in Sun JRE 1.6.0_19
		 */
		if (t instanceof NullPointerException) {
			StackTraceElement[] trace = t.getStackTrace();

			if (trace.length > 3 &&
					trace[0].getClassName().equals("sun.awt.shell.Win32ShellFolder2") &&
					trace[0].getMethodName().equals("pidlsEqual") &&

					trace[1].getClassName().equals("sun.awt.shell.Win32ShellFolder2") &&
					trace[1].getMethodName().equals("equals") &&

					trace[2].getClassName().equals("sun.awt.shell.Win32ShellFolderManager2") &&
					trace[2].getMethodName().equals("isFileSystemRoot")) {
				log.warn("Ignoring Sun JRE bug " +
						"(see http://forums.sun.com/thread.jspa?threadID=5435324): " + t);
				return true;
			}
		}

		/*
		 * Detect Sun JRE bug in D3D
		 */
		if (t instanceof ClassCastException) {
			if (t.getMessage().equals("sun.awt.Win32GraphicsConfig cannot be cast to sun.java2d.d3d.D3DGraphicsConfig")) {
				log.warn("Ignoring Sun JRE bug " +
						"(see http://forums.sun.com/thread.jspa?threadID=5440525): " + t);
				return true;
			}
		}

		/*
		 * Detect and ignore DnD bug in component tree - related to 6560955 in Sun JRE.
		 */
		if (t instanceof NullPointerException) {
			StackTraceElement[] trace = t.getStackTrace();

			if (trace.length > 2 &&
					trace[0].getClassName().equals("javax.swing.tree.TreePath") &&
					trace[0].getMethodName().equals("pathByAddingChild") &&

					trace[1].getClassName().equals("javax.swing.plaf.basic.BasicTreeUI") &&
					trace[1].getMethodName().equals("getDropLineRect")) {

				log.warn("Ignoring Sun JRE bug updating drop location " +
						"(see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6560955): " + t);
				return true;
			}
		}
		return false;
	}


	@SuppressWarnings("unused")
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
