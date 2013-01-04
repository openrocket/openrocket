package net.sf.openrocket.util.watcher;

/**
 * This interface abstracts the public API for a directory change reactor.  In order to use the watcher subsystem, clients may use the default
 * change reactor (that implements this interface), or use the WatchService directly.  This interface is more of a convenience abstraction.
 * <p/>
 * This only monitors directories.  If you want to monitor an individual file, it is recommended that you monitor the directory that the file resides
 * within, then filter the WatchEvents accordingly.
 */
public interface DirectoryChangeReactor {

    /**
     * Register an event handler with the reactor.  The event handler will be called when either a creation, modification, or deletion event is detected
     * in the watched directory.  Or a modification or a deletion event detected upon a watched file.
     *
     * @param theEventHandler the handler to be called when an event is detected
     */
    void registerHandler(WatchedEventHandler<Directory> theEventHandler);

    /**
     * Unregister an event handler with the reactor.
     *
     * @param theEventHandler the handler
     */
    void unregisterHandler(WatchedEventHandler<Directory> theEventHandler);
}
