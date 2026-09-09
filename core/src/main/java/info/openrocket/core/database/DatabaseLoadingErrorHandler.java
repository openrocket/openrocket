package info.openrocket.core.database;

/**
 * Callback used by the asynchronous database loaders to report a user-facing
 * loading failure (e.g. a malformed user-supplied motor or component preset file).
 * <p>
 * The headless {@code core} module must not present any UI itself, so instead of
 * showing a dialog it hands the localized title and message to a handler.  A GUI
 * front-end (Swing) registers an implementation that displays the message; when no
 * handler is registered — as in headless or command-line use — the failure is only
 * logged and loading of the remaining files continues.
 */
public interface DatabaseLoadingErrorHandler {

	/**
	 * Report a loading failure to the user.
	 *
	 * @param title   the localized dialog title
	 * @param message the localized, human-readable message (plain text; line breaks
	 *                are expressed as {@code \n} and may be rendered as the front-end sees fit)
	 */
	void handleError(String title, String message);
}
