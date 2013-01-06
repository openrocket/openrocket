package net.sf.openrocket.util.watcher;

import java.util.List;

/**
 * The public contract that must be implemented by clients wanting to register an interest in, and receive notification of,
 * changes to a directory or file.
 */
public interface WatchedEventHandler<W extends WatchedFile> {

    /**
     * Get the target being watched.
     *
     * @return a instance of a watched file
     */
    W watchTarget();

    /**
     * If the target is a directory, then answer if subdirectories should also be watched for state changes.  The watched target is a file, this has no
     * meaning.
     *
     * @return true if directories are to be watched recursively (watch all subdirectories et. al.)
     */
    boolean watchRecursively();

    /**
     * Callback method.  This is invoked by the reactor whenever events are detected upon the target.
     *
     * @param theEvents a list of detected events; it's a list because if the target is a directory, potentially many files within the directory were
     *                  affected
     */
    void handleEvents(List<WatchEvent<?>> theEvents);
}
