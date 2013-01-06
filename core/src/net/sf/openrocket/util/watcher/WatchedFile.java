package net.sf.openrocket.util.watcher;

import java.io.File;

/**
 * Models a file on the filesystem that is being watched for state changes (other than creation).
 */
class WatchedFile {

    /**
     * The last timestamp of the file.
     */
    private long timeStamp;

    /**
     * The file to watch.
     */
    private final File target;

    /**
     * Constructor.
     *
     * @param aFile the file to watch
     *
     * @throws IllegalArgumentException thrown if aFile is null
     */
    WatchedFile(File aFile) throws IllegalArgumentException {
        if (aFile == null) {
            throw new IllegalArgumentException("The file may not be null.");
        }
        target = aFile;
        timeStamp = target.lastModified();
    }

    /**
     * Create a 'create' event.
     *
     * @return a watch event indicating the file was created
     */
    WatchEvent<File> createEvent() {
        return new FileWatchEvent(WatchEventKind.ENTRY_CREATE);
    }

    /**
     * Get the watched target.
     *
     * @return the file being monitored
     */
    File getTarget() {
        return target;
    }

    /**
     * Detects if any changes have been made to the file.  This is a 'destructive' read in the sense that it is not
     * idempotent.  The act of checking for changes resets the internal state and subsequent checks will indicate
     * no changes until the next physical change.
     *
     * @return a WatchEvent instance or null if no event
     */
    public final WatchEvent check() {

        if (!target.exists()) {
            return new FileWatchEvent(WatchEventKind.ENTRY_DELETE);
        }

        long latest = target.lastModified();

        if (timeStamp != latest) {
            timeStamp = latest;
            return new FileWatchEvent(WatchEventKind.ENTRY_MODIFY);
        }
        return WatchEvent.NO_EVENT;
    }

    /**
     * Delegates existence check to the target.
     *
     * @return true if exists
     */
    public boolean exists() {
        return target.exists();
    }

    /**
     * Determine equivalence to a given object.
     *
     * @param o another watched file
     *
     * @return true if the underlying file is the same
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatchedFile)) {
            return false;
        }

        final WatchedFile that = (WatchedFile) o;

        if (!target.equals(that.target)) {
            return false;
        }

        return true;
    }

    /**
     * Compute hash code.
     *
     * @return a hash value
     */
    @Override
    public int hashCode() {
        return target.hashCode();
    }

    /**
     * A class that depicts events that occur upon a file.
     */
    protected class FileWatchEvent implements WatchEvent<File> {

        /**
         * The kind of event.
         */
        private Kind<File> type;

        /**
         * Constructor.
         *
         * @param theType the
         */
        FileWatchEvent(Kind<File> theType) {
            type = theType;
        }

        @Override
        public File context() {
            return target;
        }

        @Override
        public Kind<File> kind() {
            return type;
        }
    }
}